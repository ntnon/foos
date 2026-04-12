import { Component, input, inject } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'player-name',
  standalone: true,
  host: { class: 'contents' },
  template: `
    @if (pill()) {
      <button
        type="button"
        class="bg-white/70 rounded-lg px-3 py-2 shadow-sm text-center w-full hover:bg-white hover:shadow-md transition-all font-bold text-sm cursor-pointer border-none block"
        [class]="extraClass()"
        (click)="goToPlayer($event)"
      >{{ name() }}</button>
    } @else {
      <button
        type="button"
        class="font-semibold hover:underline hover:text-brand-700 transition-colors cursor-pointer bg-transparent border-none p-0"
        [class]="extraClass()"
        (click)="goToPlayer($event)"
      >{{ name() }}</button>
    }
  `
})
export class PlayerNameComponent {
  playerId = input.required<number>();
  name = input.required<string>();
  extraClass = input<string>('');
  /** When true (default), renders with the white pill background */
  pill = input<boolean>(true);

  private router = inject(Router);

  goToPlayer(event: Event) {
    event.stopPropagation();
    this.router.navigate(['/stats/players', this.playerId()]);
  }
}

