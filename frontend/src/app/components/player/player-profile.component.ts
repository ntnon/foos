import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FoosballApiService } from '../../services/foosball-api.service';
import { PlayerStats, Match, MatchTeam } from '../../models/foosball.models';
import { TeamSummaryCardComponent } from '../team/team-summary-card.component';
import { PlayerNameComponent } from './player-name.component';

@Component({
  selector: 'player-detail',
  standalone: true,
  imports: [CommonModule, TeamSummaryCardComponent, PlayerNameComponent],
  template: `
    <div class="px-6 py-8 max-w-5xl mx-auto">
      <button (click)="back()" class="mb-6 text-sm text-pitch-500 hover:underline">← Back to Players</button>

      @if (loading()) {
        <div class="text-gray-500 text-center py-16">Loading...</div>
      } @else if (error()) {
        <div class="text-red-500 text-center py-16">{{ error() }}</div>
      } @else if (stats()) {

        <!-- Title -->
        <h2 class="text-3xl font-black text-gray-900 mb-1">
          <player-name [playerId]="stats()!.playerId" [name]="stats()!.playerName" [pill]="false" />
        </h2>

        <!-- Stats table -->
        <div class="overflow-x-auto rounded-xl shadow mb-10">
          <table class="w-full text-sm text-left bg-white">
            <thead class="bg-pitch-100 text-pitch-500 uppercase text-xs">
              <tr>
                <th class="px-4 py-3">Stat</th>
                <th class="px-4 py-3">Value</th>
              </tr>
            </thead>
            <tbody>
              <tr class="border-t border-gray-100">
                <td class="px-4 py-3 text-pitch-500">ELO Rating</td>
                <td class="px-4 py-3 font-mono font-bold text-pitch-900">{{ stats()!.eloRating | number:'1.0-0' }}</td>
              </tr>
              <tr class="border-t border-gray-100">
                <td class="px-4 py-3 text-pitch-500">Win Rate</td>
                <td class="px-4 py-3 font-mono font-bold text-pitch-900">{{ (stats()!.winRate * 100) | number:'1.0-1' }}%</td>
              </tr>
              <tr class="border-t border-gray-100">
                <td class="px-4 py-3 text-pitch-500">Record</td>
                <td class="px-4 py-3 font-mono font-bold text-pitch-900">{{ stats()!.wins }}W – {{ stats()!.losses }}L</td>
              </tr>
              <tr class="border-t border-gray-100">
                <td class="px-4 py-3 text-pitch-500">Current Streak</td>
                <td class="px-4 py-3 font-mono font-bold text-pitch-900">
                  {{ stats()!.currentWinStreak }}
                  @if (stats()!.hotStreak) { <span class="ml-1">🔥</span> }
                </td>
              </tr>
              <tr class="border-t border-gray-100">
                <td class="px-4 py-3 text-pitch-500">Best Streak</td>
                <td class="px-4 py-3 font-mono font-bold text-pitch-900">{{ stats()!.longestWinStreak }}</td>
              </tr>
              <tr class="border-t border-gray-100">
                <td class="px-4 py-3 text-pitch-500">As Offense</td>
                <td class="px-4 py-3 font-mono font-bold text-pitch-900">
                  {{ stats()!.winsAsOffense }}W – {{ stats()!.matchesAsOffense - stats()!.winsAsOffense }}L
                  <span class="text-pitch-400 font-normal ml-2">({{ (stats()!.positionWinRateOffense * 100) | number:'1.0-1' }}%)</span>
                </td>
              </tr>
              <tr class="border-t border-gray-100">
                <td class="px-4 py-3 text-pitch-500">As Defense</td>
                <td class="px-4 py-3 font-mono font-bold text-pitch-900">
                  {{ stats()!.winsAsDefense }}W – {{ stats()!.matchesAsDefense - stats()!.winsAsDefense }}L
                  <span class="text-pitch-400 font-normal ml-2">({{ (stats()!.positionWinRateDefense * 100) | number:'1.0-1' }}%)</span>
                </td>
              </tr>
              <tr class="border-t border-gray-100">
                <td class="px-4 py-3 text-pitch-500">As Red</td>
                <td class="px-4 py-3 font-mono font-bold text-pitch-900">
                  {{ stats()!.winsAsRed }}W – {{ stats()!.matchesAsRed - stats()!.winsAsRed }}L
                  <span class="text-pitch-400 font-normal ml-2">({{ (stats()!.colorWinRateRed * 100) | number:'1.0-1' }}%)</span>
                </td>
              </tr>
              <tr class="border-t border-gray-100">
                <td class="px-4 py-3 text-pitch-500">As Blue</td>
                <td class="px-4 py-3 font-mono font-bold text-pitch-900">
                  {{ stats()!.winsAsBlue }}W – {{ stats()!.matchesAsBlue - stats()!.winsAsBlue }}L
                  <span class="text-pitch-400 font-normal ml-2">({{ (stats()!.colorWinRateBlue * 100) | number:'1.0-1' }}%)</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Social table -->
        @if (stats()!.bestPartners.length || stats()!.worstEnemies.length) {
          <h3 class="text-lg font-bold text-pitch-900 mb-3">Relationships</h3>
          <div class="overflow-x-auto rounded-xl shadow mb-10">
            <table class="w-full text-sm text-left bg-white">
              <thead class="bg-pitch-100 text-pitch-500 uppercase text-xs">
                <tr>
                  <th class="px-4 py-3">Stat</th>
                  <th class="px-4 py-3">Value</th>
                </tr>
              </thead>
              <tbody>
                @if (stats()!.bestPartners.length) {
                  <tr class="border-t border-gray-100">
                    <td class="px-4 py-3 text-pitch-500">Best Teammates</td>
                    <td class="px-4 py-3">
                      <div class="flex flex-wrap gap-2">
                        @for (p of stats()!.bestPartners.slice(0, 3); track p.partnerId) {
                          <team-summary-card
                            [player1Id]="stats()!.playerId"
                            [player1Name]="stats()!.playerName"
                            [player2Id]="p.partnerId"
                            [player2Name]="p.partnerName"
                          />
                        }
                      </div>
                    </td>
                  </tr>
                }
                @if (stats()!.worstEnemies.length) {
                  <tr class="border-t border-gray-100">
                    <td class="px-4 py-3 text-pitch-500">Toughest Rival</td>
                    <td class="px-4 py-3">
                      <player-name
                        [playerId]="stats()!.worstEnemies[0].rivalId"
                        [name]="stats()!.worstEnemies[0].rivalName"
                        [pill]="false"
                      />
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }

        <!-- Match history -->
        <h3 class="text-lg font-bold text-pitch-900 mb-3">Match History</h3>
        <div class="overflow-x-auto rounded-xl shadow">
          <table class="w-full text-sm text-left bg-white">
            <thead class="bg-pitch-100 text-pitch-500 uppercase text-xs">
              <tr>
                <th class="px-4 py-3">Date</th>
                <th class="px-4 py-3">Result</th>
                <th class="px-4 py-3 text-center">Score</th>
                <th class="px-4 py-3">Partner</th>
                <th class="px-4 py-3">Opponent</th>
              </tr>
            </thead>
            <tbody>
              @for (match of matches(); track match.matchId) {
                @let sides = perspective(match);
                @let us = sides[0];
                @let them = sides[1];
                @let won = us.gameScore > them.gameScore;
                <tr class="border-t border-gray-100 hover:bg-team-blue-50 transition-colors">
                  <td class="px-4 py-3 text-pitch-400 whitespace-nowrap">{{ formatDate(match.matchDate) }}</td>
                  <td class="px-4 py-3 font-bold" [class]="won ? 'text-field-600' : 'text-team-red-500'">
                    {{ won ? 'Win' : 'Loss' }}
                  </td>
                  <td class="px-4 py-3 text-center font-black whitespace-nowrap">
                    <span [class]="won ? 'text-pitch-900' : 'text-pitch-400'">{{ us.gameScore }}</span>
                    <span class="text-pitch-200 mx-1">–</span>
                    <span [class]="!won ? 'text-pitch-900' : 'text-pitch-400'">{{ them.gameScore }}</span>
                  </td>
                  <td class="px-4 py-3 font-medium text-pitch-900">{{ partner(us) }}</td>
                  <td class="px-4 py-3 cursor-pointer hover:underline font-medium text-pitch-900"
                      (click)="goToTeam(them)">
                    {{ them.offense.playerName }} &amp; {{ them.defense.playerName }}
                  </td>
                </tr>
              }
              @empty {
                <tr><td colspan="5" class="text-center py-10 text-pitch-300">No matches yet</td></tr>
              }
            </tbody>
          </table>
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
          error: () => { this.error.set('Player not found'); this.loading.set(false); },
        });
        this.api.getMatchesByPlayer(this.playerId).subscribe({
          next: (ms) => this.matches.set(ms),
          error: () => {},
        });
      } else {
        this.router.navigate(['/stats/players']);
      }
    });
  }

  /** Returns [our team, opponent team] from this player's perspective */
  perspective(match: Match): [MatchTeam, MatchTeam] {
    const pid = this.playerId;
    const onTeam1 = match.team1.offense.playerId === pid || match.team1.defense.playerId === pid;
    return onTeam1 ? [match.team1, match.team2] : [match.team2, match.team1];
  }

  /** Returns the name of the teammate (the other player on our team) */
  partner(team: MatchTeam): string {
    const pid = this.playerId;
    return team.offense.playerId === pid ? team.defense.playerName : team.offense.playerName;
  }

  goToTeam(team: MatchTeam) {
    this.api.getTeamStats(team.offense.playerId, team.defense.playerId).subscribe({
      next: (s) => this.router.navigate(['/stats/teams', s.teamStatsId]),
      error: () => {},
    });
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('en-US', {
      month: 'short', day: 'numeric', year: 'numeric',
    });
  }

  back() { this.router.navigate(['/stats/players']); }
}
