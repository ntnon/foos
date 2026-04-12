import { Component, signal, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FoosballApiService } from '../../services/foosball-api.service';
import { TeamStats, Match } from '../../models/foosball.models';
import { PieChartComponent } from '../../components/pie-chart.component';
import { MatchListComponent } from '../../components/match-list.component';
import { PlayerNameComponent } from '../../components/player-name.component';
import { TeamSummaryCardComponent } from '../../components/team-summary-card.component';

@Component({
  selector: 'team-detail',
  standalone: true,
  imports: [CommonModule, PieChartComponent, MatchListComponent, PlayerNameComponent, TeamSummaryCardComponent],
  template: `
    <div class="px-6 py-8">
      <div class="max-w-3xl mx-auto">
        <button (click)="back()" class="mb-6 text-sm text-blue-500 hover:underline">← Back to Teams</button>
      </div>

      @if (loading()) {
        <div class="text-gray-500 text-center py-16">Loading...</div>
      } @else if (error()) {
        <div class="text-red-500 text-center py-16">{{ error() }}</div>
      } @else if (stats()) {

        <!-- Stats card — constrained width -->
        <div class="max-w-3xl mx-auto mb-8">
          <div class="bg-brand-50 border border-brand-200 rounded-2xl shadow p-8">

            <!-- Header -->
            <h2 class="text-3xl font-black text-gray-900 mb-1 flex flex-wrap items-center gap-2">
              <player-name [playerId]="stats()!.player1Id" [name]="stats()!.player1Name" [pill]="false" extraClass="hover:text-brand-700 transition-colors" />
              <span class="text-gray-400 font-normal">&amp;</span>
              <player-name [playerId]="stats()!.player2Id" [name]="stats()!.player2Name" [pill]="false" extraClass="hover:text-brand-700 transition-colors" />
            </h2>
            <p class="text-gray-400 text-sm mb-6">{{ stats()!.totalMatches }} matches played</p>

            <!-- Key stats grid -->
            <div class="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">

              <!-- ELO -->
              <div class="relative overflow-hidden bg-linear-to-br from-brand-50 to-brand-100 border border-brand-200 rounded-md p-4 flex flex-col items-center justify-center text-center shadow-sm">
                <div class="text-3xl mb-1">🏆</div>
                <div class="text-2xl font-black font-mono text-brand-700">{{ stats()!.eloRating | number:'1.0-0' }}</div>
                <div class="text-xs font-semibold text-brand-400 uppercase tracking-wide mt-1">ELO Rating</div>
              </div>

              <!-- Win Rate -->
              <div class="relative overflow-hidden bg-linear-to-br from-field-50 to-field-100 border border-field-200 rounded-md p-4 flex flex-col items-center justify-center text-center shadow-sm">
                <div class="text-3xl mb-1">📈</div>
                <div class="text-2xl font-black font-mono text-field-700">{{ (stats()!.winRate * 100) | number:'1.0-0' }}<span class="text-base font-bold">%</span></div>
                <div class="text-xs font-semibold text-field-600 uppercase tracking-wide mt-1">Win Rate</div>
                <div class="text-xs text-field-500 mt-0.5">{{ stats()!.wins }}W – {{ stats()!.losses }}L</div>
              </div>

              <!-- Current streak -->
              <div class="relative overflow-hidden bg-linear-to-br from-amber-50 to-amber-100 border border-amber-300 rounded-md p-4 flex flex-col items-center justify-center text-center shadow-sm">
                <div class="text-3xl mb-1">@if (stats()!.hotStreak) { 🔥 } @else { ⚡ }</div>
                <div class="text-2xl font-black font-mono text-amber-700">{{ stats()!.currentWinStreak }}</div>
                <div class="text-xs font-semibold text-amber-700 uppercase tracking-wide mt-1">Current Streak</div>
              </div>

              <!-- Best streak -->
              <div class="relative overflow-hidden bg-linear-to-br from-team-red-50 to-team-red-100 border border-team-red-200 rounded-md p-4 flex flex-col items-center justify-center text-center shadow-sm">
                <div class="text-3xl mb-1">🎯</div>
                <div class="text-2xl font-black font-mono text-team-red-700">{{ stats()!.longestWinStreak }}</div>
                <div class="text-xs font-semibold text-team-red-600 uppercase tracking-wide mt-1">Best Streak</div>
              </div>
            </div>


            <!-- Scoring -->


            <!-- Charts: W/L, Color & Position -->
            <div class="grid grid-cols-3 gap-6 mb-8 items-stretch">
              <div class="flex flex-col gap-4 ">
                <div class="bg-gray-50 rounded-xl p-4 flex-1 flex flex-col justify-center shadow-sm">
                  <div class="text-xs text-gray-500 uppercase tracking-wide mb-1">Avg scored / match</div>
                  <div class="text-2xl font-black font-mono text-gray-700">{{ stats()!.avgPointsScoredPerMatch | number:'1.1-1' }}</div>
                </div>
                <div class="bg-gray-50 rounded-xl p-4 flex-1 flex flex-col justify-center shadow-sm">
                  <div class="text-xs text-gray-500 uppercase tracking-wide mb-1">Avg allowed / match</div>
                  <div class="text-2xl font-black font-mono text-gray-700">{{ stats()!.avgPointsAllowedPerMatch | number:'1.1-1' }}</div>
                </div>
              </div>
              <div class="flex flex-col items-center">
                <pie-chart title="Wins / Losses" [data]="wlChartData()" [size]="130" />
              </div>
              <div class="flex flex-col items-center">
                <pie-chart title="Color" [data]="colorChartData()" [size]="130" />
              </div>
            </div>

            <!-- Toughest rivals -->
            @if (stats()!.rivalTeams.length) {
              <div>
                <h3 class="text-lg font-bold text-gray-800 mb-3">Toughest rivals</h3>
                <div class="flex flex-row justify-evenly flex-wrap gap-3">
                  @for (r of stats()!.rivalTeams; track r.rivalTeamStatsId) {
                    <team-summary-card
                      [player1Id]="r.rival1Id"
                      [player1Name]="r.rival1Name"
                      [player2Id]="r.rival2Id"
                      [player2Name]="r.rival2Name"
                      [label]="r.matchesAgainst + ' matches · ' + ((r.lossRate * 100) | number:'1.0-1') + '% loss'"
                    />
                  }
                </div>
              </div>
            }

          </div>
        </div>

        <!-- Match history — full width -->
        <div class="mt-6 px-2">
          <h3 class="text-lg font-bold text-gray-800 mb-3 max-w-3xl mx-auto">Match History</h3>
          <match-list [matches]="matches()" [perspectiveTeamPlayerIds]="[stats()!.player1Id, stats()!.player2Id]" />
        </div>
      }
    </div>
  `,
})
export class TeamComponent {
  private api = inject(FoosballApiService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  stats = signal<TeamStats | null>(null);
  matches = signal<Match[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  wlChartData = computed(() => {
    const s = this.stats();
    if (!s) return [];
    return [
      { label: 'Wins', value: s.wins, color: '#22c55e' },
      { label: 'Losses', value: s.losses, color: '#ef4444' },
    ];
  });

  colorChartData = computed(() => {
    const s = this.stats();
    if (!s) return [];
    return [
      { label: 'Red', value: s.matchesAsRed, color: '#ef4444' },
      { label: 'Blue', value: s.matchesAsBlue, color: '#3b82f6' },
    ];
  });


  constructor() {
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id'));
      if (id) {
        this.loading.set(true);
        this.error.set(null);
        this.stats.set(null);
        this.matches.set([]);
        this.api.getTeamStatsById(id).subscribe({
          next: (s) => {
            this.stats.set(s);
            this.loading.set(false);
            this.api.getMatchesByTeam(s.player1Id, s.player2Id).subscribe({
              next: (ms) => this.matches.set(ms),
              error: () => {}
            });
          },
          error: () => { this.error.set('Team not found'); this.loading.set(false); }
        });
      } else {
        this.router.navigate(['/stats/teams']);
      }
    });
  }

  back() { this.router.navigate(['/stats/teams']); }
}
