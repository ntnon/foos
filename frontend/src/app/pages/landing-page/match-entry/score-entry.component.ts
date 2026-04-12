import { Component, input, computed, output } from '@angular/core';
import { TeamColor } from '../../../models/foosball.models';
import { FieldTree } from '@angular/forms/signals';

@Component({
  selector: 'score-tracker',
  imports: [],
  template: `
    <div class="score-slider-container w-full gap-2 flex flex-col items-center" [class]="teamColor() === 'RED' ? 'flex-col' : 'flex-col-reverse'">
      <div class="text-4xl font-extrabold" [class]="teamColor() === 'RED' ? 'text-red-800' : 'text-brand-800'">{{score()}}</div>
      <input
        [value]="score()"
        type="range"
        min="0"
        step="1"
        [max]="targetScore()"
        class="w-full h-3 rounded-lg appearance-none cursor-pointer"
        [style.background]="trackBackground()"
        (input)="updateScore($event)"
      />
    </div>
  `
})
export class ScoreEntryComponent {
  teamColor = input.required<TeamColor>();
  targetScore = input<number>(10);
  formField = input.required<FieldTree<number, string>>();

  score = computed(() => {
    const fieldState = this.formField()();
    return fieldState.value() ?? 0;
  });

  updateScore(event: Event) {
    const value = (event.target as HTMLInputElement).valueAsNumber;
    this.formField()().value.set(value);
  }

  trackBackground = computed(() => {
    const pct = (this.score() / this.targetScore()) * 100;
    const color = this.teamColor() === 'RED' ? '#ef4444' : '#3b82f6';
    return `linear-gradient(to right, ${color} ${pct}%, white ${pct}%)`;
  });
}
