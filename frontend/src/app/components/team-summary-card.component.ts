import { Component, input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FoosballApiService } from '../services/foosball-api.service';
import { PlayerNameComponent } from './player-name.component';

/**
 * A compact card for displaying a pair of players (team) with navigation to team stats.
 * Navigates via /api/stats/teams/{player1Id}/{player2Id} lookup.
 * This is distinct from TeamCardComponent which is used inside match results.
 */
@Component({
  selector: 'team-summary-card',
  standalone: true,
  imports: [CommonModule, PlayerNameComponent],
  host: { class: 'contents' },
  template: `
    <div
      class="flex flex-col items-stretch bg-white/80 border border-gray-200 rounded-xl p-4 shadow-sm hover:shadow-md hover:border-brand-300 transition-all cursor-pointer w-52"
      (click)="goToTeam($event)"
    >
      <!-- Players stacked -->
      <div class="flex flex-col gap-1.5">
        <player-name [playerId]="player1Id()" [name]="player1Name()" [pill]="true" extraClass="text-gray-800 text-sm w-full" />
        <div class="flex items-center gap-1">
          <div class="flex-1 h-px bg-gray-200"></div>
          <span class="text-gray-300 text-xs">&amp;</span>
          <div class="flex-1 h-px bg-gray-200"></div>
        </div>
        <player-name [playerId]="player2Id()" [name]="player2Name()" [pill]="true" extraClass="text-gray-800 text-sm w-full" />
      </div>

      <!-- Label -->
      @if (label()) {
        <span class="text-xs text-gray-400 text-center mt-3 leading-tight">{{ label() }}</span>
      }
    </div>
  `
})
export class TeamSummaryCardComponent {
  player1Id = input.required<number>();
  player1Name = input.required<string>();
  player2Id = input.required<number>();
  player2Name = input.required<string>();
  /** Optional right-hand label e.g. "12 matches · 60% loss" */
  label = input<string>('');

  private router = inject(Router);
  private api = inject(FoosballApiService);

  goToTeam(event: Event) {
    event.stopPropagation();
    this.api.getTeamStats(this.player1Id(), this.player2Id()).subscribe({
      next: (stats) => this.router.navigate(['/stats/teams', stats.teamStatsId]),
      error: () => this.router.navigate(['/stats/teams'])
    });
  }
}
