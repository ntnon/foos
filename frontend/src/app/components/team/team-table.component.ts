import { Component, signal, inject, computed, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FoosballApiService } from '../../services/foosball-api.service';
import { SearchBarComponent } from '../../components/search-bar.component';
import { TeamStats } from '../../models/foosball.models';

type SortField = 'name' | 'elo' | 'winRate';

@Component({
  selector: 'teams-stats-list',
  standalone: true,
  imports: [CommonModule, SearchBarComponent],
  template: `
    <div class="px-6 py-8 max-w-5xl mx-auto">
      <h2 class="text-3xl font-black text-gray-900 mb-6">Lag</h2>

      <!-- Search + Sort -->
      <div class="mb-6 flex gap-3">
        <search-bar class="flex-1" [(value)]="searchTerm" placeholder="Search by player name…" />
        <div class="flex rounded-lg border border-gray-300 overflow-hidden text-sm">
          @for (opt of sortOptions; track opt.field) {
            <button
              class="px-3 py-2 transition-colors"
              [class.bg-field-600]="sortField() === opt.field"
              [class.text-white]="sortField() === opt.field"
              [class.bg-white]="sortField() !== opt.field"
              [class.text-gray-600]="sortField() !== opt.field"
              [class.hover:bg-pitch-100]="sortField() !== opt.field"
              (click)="setSort(opt.field)"
            >{{ opt.label }}</button>
          }
        </div>
      </div>

      @if (loading()) {
        <div class="text-gray-500 text-center py-12">Laster lag...</div>
      } @else if (error()) {
        <div class="text-red-500 text-center py-12">{{ error() }}</div>
      } @else {
        <div class="overflow-x-auto rounded-xl shadow">
          <table class="w-full text-sm text-left bg-white">
            <thead class="bg-pitch-100 text-pitch-500 uppercase text-xs">
              <tr>
                <th class="px-4 py-3 cursor-pointer hover:text-pitch-900" (click)="setSort('name')">
                  Lag {{ sortField() === 'name' ? (sortAsc() ? '↑' : '↓') : '' }}
                </th>
                <th class="px-4 py-3 cursor-pointer hover:text-pitch-900" (click)="setSort('elo')">
                  ELO {{ sortField() === 'elo' ? (sortAsc() ? '↑' : '↓') : '' }}
                </th>
                <th class="px-4 py-3 text-center min-w-32 cursor-pointer hover:text-pitch-900" (click)="setSort('winRate')">
                  W/L {{ sortField() === 'winRate' ? (sortAsc() ? '↑' : '↓') : '' }}
                </th>
              </tr>
            </thead>
            <tbody>
              @for (t of sortedTeams(); track t.teamStatsId) {
                <tr
                  class="border-t border-gray-100 hover:bg-team-blue-50 cursor-pointer transition-colors"
                  (click)="goToTeam(t.teamStatsId)"
                >
                  <td class="px-4 py-3 font-medium text-pitch-900">{{ t.player1Name }} & {{ t.player2Name }}
                    @if (t.currentWinStreak>=3) { 🔥 {{ t.currentWinStreak }} }
                  </td>
                  <td class="px-4 py-3 font-mono font-bold">{{ t.eloRating | number:'1.0-0' }}</td>
                  <td class="px-4 py-3">
                    <div class="flex items-center gap-2">
                      <span class="text-xs text-field-600 w-4 text-right">{{ t.wins }}</span>
                      <div class="flex-1 flex h-2 rounded-full overflow-hidden bg-team-red-200">
                        <div class="bg-field-500 h-full" [style.width.%]="t.totalMatches > 0 ? (t.wins / t.totalMatches) * 100 : 0"></div>
                      </div>
                      <span class="text-xs text-team-red-500 w-4">{{ t.losses }}</span>
                    </div>
                  </td>
                </tr>
              }
              @empty {
                <tr><td colspan="3" class="text-center py-10 text-gray-400">Ingen lag funnet</td></tr>
              }
            </tbody>
          </table>
        </div>

        @if (totalPages() > 1) {
          <div class="flex items-center justify-between mt-6">
            <button
              class="px-4 py-2 rounded-lg bg-pitch-100 hover:bg-pitch-300 disabled:opacity-40 disabled:cursor-not-allowed"
              [disabled]="currentPage() === 0"
              (click)="goToPage(currentPage() - 1)"
            >← Forrige</button>
            <span class="text-sm text-pitch-500">Side {{ currentPage() + 1 }} av {{ totalPages() }}</span>
            <button
              class="px-4 py-2 rounded-lg bg-pitch-100 hover:bg-pitch-300 disabled:opacity-40 disabled:cursor-not-allowed"
              [disabled]="currentPage() === totalPages() - 1"
              (click)="goToPage(currentPage() + 1)"
            >Neste →</button>
          </div>
        }
      }
    </div>
  `,
})
export class TeamListComponent {
  private api = inject(FoosballApiService);
  private router = inject(Router);

  teams = signal<TeamStats[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);
  currentPage = signal(0);
  totalPages = signal(0);
  searchTerm = signal('');
  readonly pageSize = 20;

  private searchTimeout: any;

  constructor() {
    this.load();
    let first = true;
    effect(() => {
      this.searchTerm(); // track
      if (first) { first = false; return; }
      this.onSearch();
    });
  }

  load() {
    this.loading.set(true);
    this.error.set(null);
    this.api.getTeamStatsPage(this.searchTerm() || undefined, this.currentPage(), this.pageSize).subscribe({
      next: (page) => {
        this.teams.set(page.content);
        this.totalPages.set(page.totalPages);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Kunne ikke laste lag');
        this.loading.set(false);
      }
    });
  }

  onSearch() {
    clearTimeout(this.searchTimeout);
    this.searchTimeout = setTimeout(() => {
      this.currentPage.set(0);
      this.load();
    }, 300);
  }

  goToPage(page: number) {
    this.currentPage.set(page);
    this.load();
  }

  goToTeam(id: number) {
    this.router.navigate(['/stats/teams', id]);
  }

  sortField = signal<SortField>('elo');
  sortAsc = signal(false);

  readonly sortOptions: { field: SortField; label: string }[] = [
    { field: 'name', label: 'Navn' },
    { field: 'elo', label: 'ELO' },
    { field: 'winRate', label: 'Seier %' },
  ];

  sortedTeams = computed(() => {
    const teams = [...this.teams()];
    const field = this.sortField();
    const asc = this.sortAsc();
    teams.sort((a, b) => {
      let val: number;
      if (field === 'name') val = `${a.player1Name} ${a.player2Name}`.localeCompare(`${b.player1Name} ${b.player2Name}`);
      else if (field === 'elo') val = a.eloRating - b.eloRating;
      else val = a.winRate - b.winRate;
      return asc ? val : -val;
    });
    return teams;
  });

  setSort(field: SortField) {
    if (this.sortField() === field) this.sortAsc.update(v => !v);
    else { this.sortField.set(field); this.sortAsc.set(false); }
  }
}
