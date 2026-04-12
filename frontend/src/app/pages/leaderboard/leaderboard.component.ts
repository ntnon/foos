import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'leaderboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="px-6 py-8 max-w-7xl mx-auto">
      <h2 class="text-4xl font-black text-gray-900 -tracking-widest">Leaderboard</h2>
      <p class="text-gray-600 mt-4">Coming soon: A detailed leaderboard showcasing player rankings and stats.</p>
    </div>
  `,
})
export class LeaderboardComponent {}
