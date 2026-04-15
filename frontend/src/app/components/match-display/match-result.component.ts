import { Component, input, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { TeamCardComponent } from '../team/team-card.component';
import { FoosballApiService } from '../../services/foosball-api.service';
import { Match, MatchTeam } from '../../models/foosball.models';

@Component({
  selector: 'match',
  imports: [CommonModule, TeamCardComponent],
  template: `
    <div class="grid grid-cols-5 gap-4 items-center p-3 rounded-lg shadow-md bg-white">
      <!-- Left team -->
      <div class="col-span-2 cursor-pointer" (click)="goToTeam(orderedTeams()[0])">
        <team-card [team]="orderedTeams()[0]" [matchScore]="orderedTeams()[0].gameScore" [headToHead]="getHeadToHead()" />
      </div>

      <!-- VS / Score -->
      <div class="col-span-1 flex flex-col items-center justify-center gap-1">
        <div class="text-lg font-black text-gray-400 tracking-widest">VS</div>
        <div class="flex flex-row gap-2 justify-center items-center">
          <p class="font-black text-3xl" [class]="orderedTeams()[0].gameScore >= orderedTeams()[1].gameScore ? 'text-gray-900' : 'text-gray-400'">{{ orderedTeams()[0].gameScore }}</p>
          <p class="font-semibold text-3xl text-gray-300">-</p>
          <p class="font-black text-3xl" [class]="orderedTeams()[1].gameScore > orderedTeams()[0].gameScore ? 'text-gray-900' : 'text-gray-400'">{{ orderedTeams()[1].gameScore }}</p>
        </div>
        <div class="flex flex-row gap-2 justify-center items-center">
          (<p class="font-bold text-base text-gray-500">{{ orderedTeams()[0].pairWins }}</p>
          <p class="font-bold text-base text-gray-400">-</p>
          <p class="font-bold text-base text-gray-500">{{ orderedTeams()[1].pairWins }}</p>)
        </div>
        <span class="text-xs text-gray-400 uppercase tracking-wide">all time</span>
      </div>

      <!-- Right team -->
      <div class="col-span-2 cursor-pointer" (click)="goToTeam(orderedTeams()[1])">
        <team-card [team]="orderedTeams()[1]" [matchScore]="orderedTeams()[1].gameScore" [headToHead]="getHeadToHead()" />
      </div>
    </div>
  `,
})
export class MatchComponent {
  match = input.required<Match>();
  perspectivePlayerId = input<number | null>(null);
  perspectiveTeamPlayerIds = input<[number, number] | null>(null);

  private router = inject(Router);
  private api = inject(FoosballApiService);

  /** Returns [leftTeam, rightTeam] based on perspective, falling back to winner-left */
  orderedTeams = computed((): [MatchTeam, MatchTeam] => {
    const m = this.match();
    const pid = this.perspectivePlayerId();
    const tids = this.perspectiveTeamPlayerIds();

    // Perspective: specific team by player IDs
    if (tids) {
      const [p1, p2] = tids;
      const team1HasBoth = this.teamHasPlayers(m.team1, p1, p2);
      return team1HasBoth ? [m.team1, m.team2] : [m.team2, m.team1];
    }

    // Perspective: specific player
    if (pid != null) {
      const team1HasPlayer =
        m.team1.offense.playerId === pid || m.team1.defense.playerId === pid;
      return team1HasPlayer ? [m.team1, m.team2] : [m.team2, m.team1];
    }

    // Default: winner on left
    return m.winner === 'Team 1'
      ? [m.team1, m.team2]
      : [m.team2, m.team1];
  });

  private teamHasPlayers(team: MatchTeam, p1: number, p2: number): boolean {
    const ids = [team.offense.playerId, team.defense.playerId];
    return ids.includes(p1) && ids.includes(p2);
  }

  getHeadToHead() {
    return { team1PairWins: this.match().team1.pairWins, team2PairWins: this.match().team2.pairWins };
  }

  goToTeam(team: MatchTeam) {
    this.api.getTeamStats(team.offense.playerId, team.defense.playerId).subscribe({
      next: (stats) => this.router.navigate(['/stats/teams', stats.teamStatsId]),
      error: () => {}
    });
  }
}
