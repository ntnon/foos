import { input, Component } from '@angular/core';
import { MatchPlayer } from '../../models/foosball.models';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'elo',
  imports: [CommonModule],
  template: `
  <span class="text-xs font-mono w-full text-center mt-2">
    {{ this.player().initialRating | number:'1.0-0' }}
     →
      <span class="font-bold">
    {{ player().newRating | number:'1.0-0' }} </span>
    <span [class]="this.player().ratingChange >= 0 ? 'text-green-600 font-bold' : 'text-red-600 font-bold'">
      ({{ this.player().ratingChange >= 0 ? '+' : '' }}{{ this.player().ratingChange | number:'1.1-1' }})
    </span>
  </span>
  `
})
export class EloComponent {
  readonly player = input.required<MatchPlayer>();

}
