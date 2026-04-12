import { Component, signal, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FoosballApiService } from '../../services/foosball-api.service';
import { PlayerStats, Match } from '../../models/foosball.models';
import { PieChartComponent } from '../../components/pie-chart.component';
import { MatchListComponent } from '../../components/match-list.component';
import { PlayerNameComponent } from '../../components/player-name.component';
import { TeamSummaryCardComponent } from '../../components/team-summary-card.component';

@Component({
  selector: 'player-detail',
  standalone: true,
  imports: [CommonModule, PieChartComponent, MatchListComponent, PlayerNameComponent, TeamSummaryCardComponent],
  template: `
    <div class="px-6 py-8">
      <div class="max-w-3xl mx-auto">
        <button (click)="back()" class="mb-6 text-sm text-blue-500 hover:underline">← Back to Players</button>
      </div>

      @if (loading()) {
        <div class="text-gray-500 text-center py-16">Loading...</div>
      } @else if (error()) {
        <div class="text-red-500 text-center py-16">{{ error() }}</div>
      } @else if (stats()) {

        <!-- Stats card — constrained width -->
        <div class="max-w-3xl mx-auto mb-8">
          <div class="bg-brand-50 border border-brand-200 rounded-2xl shadow p-8">
            <h2 class="text-3xl font-black text-gray-900 mb-1">{{ stats()!.playerName }}</h2>
            <p class="text-gray-400 text-sm mb-6">Player #{{ stats()!.playerId }}</p>

            <!-- Key stats grid -->
            <div class="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">

              <!-- ELO -->
              <div class="relative overflow-hidden bg-linear-to-br from-brand-50 to-brand-100 border border-brand-200 rounded-md p-4 flex flex-col items-center justify-center text-center shadow-sm">
                <div class="text-3xl mb-1">🏆</div>
                <div class="text-2xl font-black font-mono text-brand-700">{{ stats()!.eloRating | number:'1.0-0' }}</div>
                <div class="text-xs font-semibold text-brand-400 uppercase tracking-wide mt-1">ELO Rating</div>
              </div>

              <!-- No.1 Rival -->
              @if (stats()!.worstEnemies.length) {
                <div class="relative overflow-hidden bg-linear-to-br from-team-red-50 to-team-red-100 border border-team-red-200 rounded-md p-4 flex flex-col items-center justify-center text-center shadow-sm">
                  <div class="text-3xl mb-1">⚔️</div>
                  <player-name
                    [playerId]="stats()!.worstEnemies[0].rivalId"
                    [name]="stats()!.worstEnemies[0].rivalName"
                    [pill]="true"
                    extraClass="text-team-red-700 font-black w-full"
                  />
                  <div class="text-xs font-semibold text-team-red-400 uppercase tracking-wide mt-1">No.1 Rival</div>
                </div>
              } @else {
                <div class="relative overflow-hidden bg-linear-to-br from-team-red-50 to-team-red-100 border border-team-red-200 rounded-md p-4 flex flex-col items-center justify-center text-center shadow-sm">
                  <div class="text-3xl mb-1">⚔️</div>
                  <div class="text-lg font-black text-team-red-200">—</div>
                  <div class="text-xs font-semibold text-team-red-400 uppercase tracking-wide mt-1">No.1 Rival</div>
                </div>
              }

              <!-- Current streak -->
              <div class="relative overflow-hidden bg-linear-to-br from-amber-50 to-amber-100 border border-amber-300 rounded-md p-4 flex flex-col items-center justify-center text-center shadow-sm">
                <div class="text-3xl mb-1">@if (stats()!.hotStreak) { 🔥 } @else { ⚡ }</div>
                <div class="text-2xl font-black font-mono text-amber-700">{{ stats()!.currentWinStreak }}</div>
                <div class="text-xs font-semibold text-amber-700 uppercase tracking-wide mt-1">Current Streak</div>
              </div>

              <!-- Longest streak -->
              <div class="relative overflow-hidden bg-linear-to-br from-field-50 to-field-100 border border-field-200 rounded-md p-4 flex flex-col items-center justify-center text-center shadow-sm">
                <div class="text-3xl mb-1">🎯</div>
                <div class="text-2xl font-black font-mono text-field-700">{{ stats()!.longestWinStreak }}</div>
                <div class="text-xs font-semibold text-field-600 uppercase tracking-wide mt-1">Best Streak</div>
              </div>

            </div>

            <!-- Charts: W/L, Position & Color -->
            <div class="grid grid-cols-3 gap-6 mb-8">
              <div class="flex flex-col items-center">
                <pie-chart title="Wins / Losses" [data]="wlChartData()" [size]="130" />
              </div>
              <div class="flex flex-col items-center">
                <pie-chart title="Position" [data]="positionChartData()" [size]="130" />
              </div>
              <div class="flex flex-col items-center">
                <pie-chart title="Color" [data]="colorChartData()" [size]="130" />
              </div>
            </div>

            <!-- Best partners -->
            @if (stats()!.bestPartners.length) {
              <div>
                <h3 class="text-lg font-bold text-gray-800 mb-3">Best teams</h3>
                <div class="flex flex-row justify-evenly flex-wrap gap-3">
                  @for (p of stats()!.bestPartners; track p.partnerId) {
                    <team-summary-card
                      [player1Id]="stats()!.playerId"
                      [player1Name]="stats()!.playerName"
                      [player2Id]="p.partnerId"
                      [player2Name]="p.partnerName"
                      [label]="p.matchesTogether + ' matches · ' + ((p.winRate * 100) | number:'1.0-1') + '% win'"
                    />
                  }
                </div>
              </div>
            }

          </div>
        </div>

        <!-- Match history — full width, outside the stats card -->
        <div class="mt-6 px-2">
          <h3 class="text-lg font-bold text-gray-800 mb-3 max-w-3xl mx-auto">Match History</h3>
          <match-list [matches]="matches()" [perspectivePlayerId]="stats()!.playerId" />
        </div>

      }
    </div>
  `,
})
export class PlayerComponent {
  private api = inject(FoosballApiService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  private playerId = 0;
  stats = signal<PlayerStats | null>(null);
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

  positionChartData = computed(() => {
    const s = this.stats();
    if (!s) return [];
    return [
      { label: '⚔️ Offense', value: s.matchesAsOffense, color: '#f59e0b' },
      { label: '🛡️ Defense', value: s.matchesAsDefense, color: '#6366f1' },
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
      this.playerId = Number(params.get('id'));
      if (this.playerId) {
        this.loading.set(true);
        this.error.set(null);
        this.stats.set(null);
        this.matches.set([]);
        this.api.getPlayerStats(this.playerId).subscribe({
          next: (s) => { this.stats.set(s); this.loading.set(false); },
          error: () => { this.error.set('Player not found'); this.loading.set(false); }
        });
        this.api.getMatchesByPlayer(this.playerId).subscribe({
          next: (ms) => this.matches.set(ms),
          error: () => {}
        });
      } else {
        this.router.navigate(['/stats/players']);
      }
    });
  }


  back() { this.router.navigate(['/stats/players']); }
}
