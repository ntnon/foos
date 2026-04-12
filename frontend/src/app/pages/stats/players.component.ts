import { Component, signal, inject, computed, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FoosballApiService } from '../../services/foosball-api.service';
import { SearchBarComponent } from '../../components/search-bar.component';
import { PlayerStats } from '../../models/foosball.models';

type SortField = 'name' | 'elo' | 'winRate';

@Component({
  selector: 'players-stats-list',
  standalone: true,
  imports: [CommonModule, SearchBarComponent],
  template: `
    <div class="px-6 py-8 max-w-5xl mx-auto">
      <h2 class="text-3xl font-black text-gray-900 mb-6">Players</h2>

      <!-- Search + Sort -->
      <div class="mb-6 flex gap-3">
        <search-bar class="flex-1" [(value)]="searchTerm" placeholder="Search by name…" />
        <div class="flex rounded-lg border border-gray-300 overflow-hidden text-sm">
          @for (opt of sortOptions; track opt.field) {
            <button
              class="px-3 py-2 transition-colors"
              [class.bg-field-600]="sortField() === opt.field"
              [class.text-white]="sortField() === opt.field"
              [class.bg-white]="sortField() !== opt.field"
              [class.text-gray-600]="sortField() !== opt.field"
              (click)="setSort(opt.field)"
            >{{ opt.label }}</button>
          }
        </div>
      </div>

      @if (loading()) {
        <div class="text-gray-500 text-center py-12">Loading players...</div>
      } @else if (error()) {
        <div class="text-red-500 text-center py-12">{{ error() }}</div>
      } @else {
        <div class="overflow-x-auto rounded-xl shadow">
          <table class="w-full text-sm text-left bg-white">
            <thead class="bg-pitch-100 text-pitch-500 uppercase text-xs">
              <tr>
                <th class="px-4 py-3 cursor-pointer hover:text-pitch-900" (click)="setSort('name')">
                  Player {{ sortField() === 'name' ? (sortAsc() ? '↑' : '↓') : '' }}
                </th>
                <th class="px-4 py-3 text-center cursor-pointer hover:text-pitch-900" (click)="setSort('elo')">
                  ELO {{ sortField() === 'elo' ? (sortAsc() ? '↑' : '↓') : '' }}
                </th>
                <th class="px-4 py-3 text-center min-w-32 cursor-pointer hover:text-pitch-900" (click)="setSort('winRate')">
                  W/L {{ sortField() === 'winRate' ? (sortAsc() ? '↑' : '↓') : '' }}
                </th>
              </tr>
            </thead>
            <tbody>
              @for (p of sortedPlayers(); track p.playerId; let i = $index) {
                <tr
                  class="border-t border-gray-100 hover:bg-team-blue-50 cursor-pointer transition-colors"
                  (click)="goToPlayer(p.playerId)"
                >
                  <td class="px-4 py-3 font-medium text-pitch-900">

                    {{ p.playerName }}
                    @if (i === 0 && sortField() === 'elo') { <span class="mr-1 text-base" title="1st place">🥇</span> }
                    @else if (i === 1 && sortField() === 'elo') { <span class="mr-1 text-base" title="2nd place">🥈</span> }
                    @else if (i === 2 && sortField() === 'elo') { <span class="mr-1 text-base" title="3rd place">🥉</span> }
                    @if (p.currentWinStreak >= 3) { <span class="ml-1 text-orange-500 font-bold text-xs">🔥 {{ p.currentWinStreak }}</span> }
                  </td>
                  <td class="px-4 py-3 text-center font-mono font-bold">{{ p.eloRating | number:'1.0-0'  }}</td>
                  <td class="px-4 py-3">
                    <div class="flex items-center gap-2">
                      <span class="text-xs text-field-600 w-4 text-right">{{ p.wins }}</span>
                      <div class="flex-1 flex h-2 rounded-full overflow-hidden bg-team-red-200">
                        <div class="bg-field-500 h-full" [style.width.%]="p.totalMatches > 0 ? (p.wins / p.totalMatches) * 100 : 0"></div>
                      </div>
                      <span class="text-xs text-team-red-500 w-4">{{ p.losses }}</span>
                    </div>
                  </td>
                </tr>
              }
              @empty {
                <tr><td colspan="7" class="text-center py-10 text-pitch-300">No players found</td></tr>
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
            >← Prev</button>
            <span class="text-sm text-pitch-500">Page {{ currentPage() + 1 }} of {{ totalPages() }}</span>
            <button
              class="px-4 py-2 rounded-lg bg-pitch-100 hover:bg-pitch-300 disabled:opacity-40 disabled:cursor-not-allowed"
              [disabled]="currentPage() === totalPages() - 1"
              (click)="goToPage(currentPage() + 1)"
            >Next →</button>
          </div>
        }
      }
    </div>
  `,
})
export class PlayerListComponent {
  private api = inject(FoosballApiService);
  private router = inject(Router);

  players = signal<PlayerStats[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);
  currentPage = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);
  searchTerm = signal('');
  readonly pageSize = 20;
  private searchTimeout: any;

  sortField = signal<SortField>('elo');
  sortAsc = signal(false);

  readonly sortOptions: { field: SortField; label: string }[] = [
    { field: 'name', label: 'Name' },
    { field: 'elo', label: 'ELO' },
    { field: 'winRate', label: 'Win %' },
  ];

  sortedPlayers = computed(() => {
    const list = [...this.players()];
    const field = this.sortField();
    const asc = this.sortAsc();
    list.sort((a, b) => {
      let val: number;
      if (field === 'name') val = a.playerName.localeCompare(b.playerName);
      else if (field === 'elo') val = a.eloRating - b.eloRating;
      else val = a.winRate - b.winRate;
      return asc ? val : -val;
    });
    return list;
  });

  setSort(field: SortField) {
    if (this.sortField() === field) this.sortAsc.update(v => !v);
    else { this.sortField.set(field); this.sortAsc.set(false); }
  }

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
    this.api.getPlayerStatsPage(this.searchTerm() || undefined, this.currentPage(), this.pageSize).subscribe({
      next: (page) => {
        this.players.set(page.content);
        this.totalPages.set(page.totalPages);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: (_err) => {
        this.error.set('Failed to load players');
        this.loading.set(false);
      }
    });
  }

  onSearch() {
    clearTimeout(this.searchTimeout);
    this.searchTimeout = setTimeout(() => { this.currentPage.set(0); this.load(); }, 300);
  }

  goToPage(page: number) { this.currentPage.set(page); this.load(); }
  goToPlayer(id: number) { this.router.navigate(['/stats/players', id]); }

  private recentDelta(trend: number[]): number {
    if (!trend?.length) return 0;
    return Math.round(trend.slice(-5).reduce((a, b) => a + b, 0));
  }
  trendIcon(trend: number[]): string {
    const d = this.recentDelta(trend);
    return d > 5 ? '▲' : d < -5 ? '▼' : '●';
  }
  trendClass(trend: number[]): string {
    const d = this.recentDelta(trend);
    return d > 5 ? 'text-field-600 font-semibold text-xs' : d < -5 ? 'text-team-red-500 font-semibold text-xs' : 'text-pitch-300 text-xs';
  }
  trendDelta(trend: number[]): string {
    const d = this.recentDelta(trend);
    return d === 0 ? '' : (d > 0 ? '+' : '') + d;
  }
  trendLabel(trend: number[]): string {
    const d = this.recentDelta(trend);
    return `ELO change over last 5 games: ${d > 0 ? '+' : ''}${d}`;
  }
}
