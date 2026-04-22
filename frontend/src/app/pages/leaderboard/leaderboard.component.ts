import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'leaderboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="px-6 py-8 max-w-7xl mx-auto">
      <h2 class="text-4xl font-black text-gray-900 -tracking-widest">Toppliste</h2>
      <p class="text-gray-600 mt-4">Kommer snart: En detaljert toppliste med spillerrangeringer og statistikk.</p>
    </div>
  `,
})
export class LeaderboardComponent {}
