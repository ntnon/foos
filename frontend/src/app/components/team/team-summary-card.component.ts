import { Component, input, inject } from '@angular/core';
import { Router } from '@angular/router';
import { FoosballApiService } from '../../services/foosball-api.service';

@Component({
  selector: 'team-summary-card',
  standalone: true,
  imports: [],
  template: `
    <div
      class="inline-flex flex-col bg-white border border-gray-200 rounded-xl px-4 py-1 shadow-sm cursor-pointer hover:shadow-md hover:border-pitch-300 transition-all"
      (click)="goToTeam($event)"
    >
      <div class="flex items-center gap-1.5 font-medium text-pitch-900">
        <span>{{ player1Name() }}</span>
        <span class="text-pitch-300">&amp;</span>
        <span>{{ player2Name() }}</span>
      </div>
      @if (sublabel()) {
        <div class="text-xs text-pitch-400 mt-1">{{ sublabel() }}</div>
      }
    </div>
  `
})
export class TeamSummaryCardComponent {
  player1Id = input.required<number>();
  player1Name = input.required<string>();
  player2Id = input.required<number>();
  player2Name = input.required<string>();
  label = input<string>('');
  sublabel = input<string>('');

  private router = inject(Router);
  private api = inject(FoosballApiService);

  goToTeam(event: Event) {
    event.stopPropagation();
    this.api.getTeamStats(this.player1Id(), this.player2Id()).subscribe({
      next: (stats) => this.router.navigate(['/stats/teams', stats.teamStatsId]),
      error: () => this.router.navigate(['/stats/teams']),
    });
  }
}
