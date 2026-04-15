import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatchTeam } from '../../models/foosball.models';
import { EloComponent } from '../stats/elo.component';
import { PlayerNameComponent } from '../player/player-name.component';

@Component({
  selector: 'team-card',
  imports: [CommonModule, EloComponent, PlayerNameComponent],
  template: `
    <div
      class="rounded-lg p-6 shadow-lg border-3 flex flex-col justify-between min-h-40 relative overflow-hidden"
      [class]="team().teamColor === 'RED'
        ? 'bg-gradient-to-br from-red-50 to-red-100 border-red-400'
        : 'bg-gradient-to-br from-blue-50 to-blue-100 border-blue-400'"
    >
      <!-- Top accent bar -->
      <div
        class="absolute top-0 left-0 right-0 h-6 rounded-t-1xl"
        [class]="team().teamColor === 'RED' ? 'bg-red-400' : 'bg-brand-400'"
      ></div>

      <!-- Players -->
      <div class="flex flex-row items-stretch gap-2 mt-2 w-full">
        <!-- Offense -->
        <div class="flex flex-col items-center gap-1 flex-1 min-w-0">
          <span class="text-xs font-semibold uppercase tracking-wide opacity-50">Offense</span>
          <player-name
            [playerId]="team().offense.playerId"
            [name]="team().offense.playerName"
            [pill]="true"
            extraClass="text-gray-700 w-full"
          />
          <elo [player]="team().offense"></elo>
        </div>

        <!-- Separator -->
        <div class="flex items-center px-1 shrink-0">
          <span class="text-lg font-bold text-black/20">&</span>
        </div>

        <!-- Defense -->
        <div class="flex flex-col items-center gap-1 flex-1 min-w-0">
          <span class="text-xs font-semibold uppercase tracking-wide opacity-50">Defense</span>
          <player-name
            [playerId]="team().defense.playerId"
            [name]="team().defense.playerName"
            [pill]="true"
            extraClass="text-gray-700 w-full"
          />
          <elo [player]="team().defense"></elo>
        </div>
      </div>
      <div></div>
    </div>
  `
})
export class TeamCardComponent {
  team = input.required<MatchTeam>();
  matchScore = input<number | undefined>(undefined);
  headToHead = input<any>(undefined);
}
