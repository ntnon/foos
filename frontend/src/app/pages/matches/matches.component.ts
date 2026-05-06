import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { DataLoaderService } from '../../services/data-loader.service';
import { FoosballApiService } from '../../services/foosball-api.service';
import { SearchBarComponent } from '../../components/search-bar.component';
import { Match, MatchTeam } from '../../models/foosball.models';

@Component({
  selector: 'matches',
  standalone: true,
  imports: [CommonModule, SearchBarComponent],
  template: `
    <div class="px-6 py-8 max-w-5xl mx-auto">
      <h2 class="text-3xl font-black text-gray-900 mb-6">Kamper</h2>

      <div class="mb-6">
        <search-bar class="flex-1" [(value)]="searchTerm" placeholder="Søk etter spillernavn…" />
      </div>

      <div class="overflow-x-auto rounded-xl shadow">
        <table class="w-full text-sm text-left bg-white">
          <thead class="bg-pitch-100 text-pitch-500 uppercase text-xs">
            <tr>
              <th class="px-4 py-3">Dato</th>
              <th class="px-4 py-3">Vinner</th>
              <th class="px-4 py-3 text-center">Poeng</th>
              <th class="px-4 py-3 text-center">Totalt</th>
              <th class="px-4 py-3">Taper</th>
            </tr>
          </thead>
          <tbody>
            @for (match of filteredMatches(); track (match.matchId + '-' + i); let i = $index) {
              @let ordered = winnerFirst(match);
              @let w = ordered[0];
              @let l = ordered[1];
              <tr class="border-t border-gray-100 hover:bg-team-blue-50 transition-colors">

                <!-- Date -->
                <td class="px-4 py-3 text-pitch-400 whitespace-nowrap">
                  {{ formatDate(match.matchDate) }}
                </td>

                <!-- Winner -->
                <td class="px-4 py-3 font-medium text-pitch-900 cursor-pointer hover:underline"
                    (click)="goToTeam(w)">
                  {{ w.offense.playerName }} &amp; {{ w.defense.playerName }}
                </td>

                <!-- Score -->
                <td class="px-4 py-3 text-center font-black text-base whitespace-nowrap">
                  <span class="text-pitch-900">{{ w.gameScore }}</span>
                  <span class="text-pitch-200 mx-1">–</span>
                  <span class="text-pitch-400">{{ l.gameScore }}</span>
                </td>

                <!-- All-time head-to-head -->
                <td class="px-4 py-3 text-center font-mono text-pitch-500 whitespace-nowrap">
                  {{ w.pairWins }} – {{ l.pairWins }}
                </td>

                <!-- Loser -->
                <td class="px-4 py-3 font-medium text-pitch-400 cursor-pointer hover:underline"
                    (click)="goToTeam(l)">
                  {{ l.offense.playerName }} &amp; {{ l.defense.playerName }}
                </td>

              </tr>
            }
            @empty {
              <tr>
                <td colspan="5" class="text-center py-10 text-pitch-300">Ingen kamper funnet</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  `,
})
export class MatchesComponent {
  private dataLoaderService = inject(DataLoaderService);
  private api = inject(FoosballApiService);
  private router = inject(Router);

  matches = this.dataLoaderService.matches;
  searchTerm = signal('');

  filteredMatches = computed(() => {
    const q = this.searchTerm().trim().toLowerCase();
    if (!q) return this.matches();
    return this.matches().filter(m => {
      const names = [
        m.team1.offense.playerName,
        m.team1.defense.playerName,
        m.team2.offense.playerName,
        m.team2.defense.playerName,
      ];
      return names.some(n => n.toLowerCase().includes(q));
    });
  });

  winnerFirst(match: Match): [MatchTeam, MatchTeam] {
    return match.winnerColor === match.team1.teamColor
      ? [match.team1, match.team2]
      : [match.team2, match.team1];
  }

  goToTeam(team: MatchTeam) {
    this.api.getTeamStats(team.offense.playerId, team.defense.playerId).subscribe({
      next: (stats) => this.router.navigate(['/stats/teams', stats.teamStatsId]),
      error: () => {},
    });
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('nb-NO', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    });
  }
}
