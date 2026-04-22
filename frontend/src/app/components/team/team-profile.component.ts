import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FoosballApiService } from '../../services/foosball-api.service';
import { TeamStats, Match, MatchTeam } from '../../models/foosball.models';
import { TeamSummaryCardComponent } from './team-summary-card.component';
import { PlayerNameComponent } from '../player/player-name.component';

@Component({
  selector: 'team-profile',
  standalone: true,
  imports: [CommonModule, TeamSummaryCardComponent, PlayerNameComponent],
  template: `
    <div class="px-6 py-8 max-w-5xl mx-auto">
      <button (click)="back()" class="mb-6 text-sm text-pitch-500 hover:underline">← Back to Teams</button>

      @if (loading()) {
        <div class="text-gray-500 text-center py-16">Loading...</div>
      } @else if (error()) {
        <div class="text-red-500 text-center py-16">{{ error() }}</div>
      } @else if (stats()) {

        <!-- Title -->
        <h2 class="text-3xl font-black text-gray-900 mb-1 flex items-center gap-2">
          <player-name [playerId]="stats()!.player1Id" [name]="stats()!.player1Name" [pill]="false" />
          <span class="text-pitch-300 font-normal">&amp;</span>
          <player-name [playerId]="stats()!.player2Id" [name]="stats()!.player2Name" [pill]="false" />
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
              <!-- Highlighted: most interesting stats -->
              <tr class="border-t border-gray-100 bg-pitch-50">
                <td class="px-4 py-3 font-semibold text-pitch-600">ELO Rating</td>
                <td class="px-4 py-3 font-mono font-black text-lg text-pitch-900">{{ stats()!.eloRating | number:'1.0-0' }}</td>
              </tr>
              <tr class="border-t border-gray-100 bg-pitch-50">
                <td class="px-4 py-3 font-semibold text-pitch-600">Win Rate</td>
                <td class="px-4 py-3 font-mono font-black text-lg text-pitch-900">{{ (stats()!.winRate * 100) | number:'1.0-1' }}%</td>
              </tr>
              <tr class="border-t border-gray-100 bg-pitch-50">
                <td class="px-4 py-3 font-semibold text-pitch-600">Record</td>
                <td class="px-4 py-3 font-mono font-black text-lg text-pitch-900">{{ stats()!.wins }}W – {{ stats()!.losses }}L</td>
              </tr>
              <tr class="border-t border-gray-100 bg-pitch-50">
                <td class="px-4 py-3 font-semibold text-pitch-600">Current Streak</td>
                <td class="px-4 py-3 font-mono font-black text-lg text-pitch-900">
                  {{ stats()!.currentWinStreak }}
                  @if (stats()!.hotStreak) { <span class="ml-1">🔥</span> }
                </td>
              </tr>

              <!-- Secondary stats -->
              @if (stats()!.rivalTeams.length) {
                <tr class="border-t border-gray-100">
                  <td class="px-4 py-3 text-pitch-500">Rival</td>
                  <td class="px-4 py-3 font-mono font-bold text-pitch-900">
                    <team-summary-card
                      [player1Id]="stats()!.rivalTeams[0].rival1Id"
                      [player1Name]="stats()!.rivalTeams[0].rival1Name"
                      [player2Id]="stats()!.rivalTeams[0].rival2Id"
                      [player2Name]="stats()!.rivalTeams[0].rival2Name"
                    />
                  </td>
                </tr>
              }
              <tr class="border-t border-gray-100">
                <td class="px-4 py-3 text-pitch-500">Best Streak</td>
                <td class="px-4 py-3 font-mono font-bold text-pitch-900">{{ stats()!.longestWinStreak }}</td>
              </tr>
              <tr class="border-t border-gray-100">
                <td class="px-4 py-3 text-pitch-500">Best Color</td>
                <td class="px-4 py-3 font-mono font-bold text-pitch-900">
                  {{ stats()!.colorWinRateRed >= stats()!.colorWinRateBlue ? '🔴 Red' : '🔵 Blue' }}
                  <span class="text-pitch-400 font-normal ml-1">
                    ({{ ((stats()!.colorWinRateRed >= stats()!.colorWinRateBlue ? stats()!.colorWinRateRed : stats()!.colorWinRateBlue) * 100) | number:'1.0-1' }}% win rate)
                  </span>
                </td>
              </tr>
              <tr class="border-t border-gray-100">
                <td class="px-4 py-3 text-pitch-500">Avg Score Diff</td>
                <td class="px-4 py-3 font-mono font-bold"
                    [class]="stats()!.averageScoreDifference >= 0 ? 'text-field-600' : 'text-team-red-500'">
                  {{ stats()!.averageScoreDifference >= 0 ? '+' : '' }}{{ stats()!.averageScoreDifference | number:'1.1-1' }}
                </td>
              </tr>
              <tr class="border-t border-gray-100">
                <td class="px-4 py-3 text-pitch-500">Avg Scored / Match</td>
                <td class="px-4 py-3 font-mono font-bold text-pitch-900">{{ stats()!.avgPointsScoredPerMatch | number:'1.1-1' }}</td>
              </tr>
              <tr class="border-t border-gray-100">
                <td class="px-4 py-3 text-pitch-500">Avg Allowed / Match</td>
                <td class="px-4 py-3 font-mono font-bold text-pitch-900">{{ stats()!.avgPointsAllowedPerMatch | number:'1.1-1' }}</td>
              </tr>
              <tr class="border-t border-gray-100">
                <td class="px-4 py-3 text-pitch-500">Last Played</td>
                <td class="px-4 py-3 font-mono font-bold text-pitch-900">{{ formatDate(stats()!.lastPlayed) }}</td>
              </tr>
              <tr class="border-t border-gray-100">
                <td class="px-4 py-3 text-pitch-500">Avg Matches / Week</td>
                <td class="px-4 py-3 font-mono font-bold text-pitch-900">{{ stats()!.avgMatchesPerWeek | number:'1.1-1' }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Match history -->
        <h3 class="text-lg font-bold text-pitch-900 mb-3">Match History</h3>
        <div class="overflow-x-auto rounded-xl shadow">
          <table class="w-full text-sm text-left bg-white">
            <thead class="bg-pitch-100 text-pitch-500 uppercase text-xs">
              <tr>
                <th class="px-4 py-3">Date</th>
                <th class="px-4 py-3">Result</th>
                <th class="px-4 py-3 text-center">Score</th>
                <th class="px-4 py-3 text-center">All Time</th>
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
                  <td class="px-4 py-3 text-center font-mono text-pitch-500 whitespace-nowrap">
                    {{ us.pairWins }} – {{ them.pairWins }}
                  </td>
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
export class TeamProfileComponent {
  private api = inject(FoosballApiService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  stats = signal<TeamStats | null>(null);
  matches = signal<Match[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

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
              error: () => {},
            });
          },
          error: () => { this.error.set('Team not found'); this.loading.set(false); },
        });
      } else {
        this.router.navigate(['/stats/teams']);
      }
    });
  }

  /** Returns [ourTeam, opponentTeam] from the perspective of the current team */
  perspective(match: Match): [MatchTeam, MatchTeam] {
    const s = this.stats()!;
    const ourIds = [s.player1Id, s.player2Id];
    const t1Ids = [match.team1.offense.playerId, match.team1.defense.playerId];
    const isTeam1 = ourIds.every(id => t1Ids.includes(id));
    return isTeam1 ? [match.team1, match.team2] : [match.team2, match.team1];
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

  back() { this.router.navigate(['/stats/teams']); }
}
