import {FormField, FieldTree} from '@angular/forms/signals';
import {CommonModule} from '@angular/common';
import { Component, input,  ChangeDetectionStrategy} from '@angular/core';
import {TeamColor} from '../../models/foosball.models';
import {PlayerEntryComponent} from './player-entry.component';

@Component({
  selector: 'team-entry',
  imports: [CommonModule, FormField, PlayerEntryComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="flex justify-between gap-5 w-full"
         [class.flex-row-reverse]="teamColor() === 'RED'"
         [class.flex-row]="teamColor() !== 'RED'">
      <div class="flex items-center" [class.flex-col]="teamColor() !== 'RED'" [class.flex-col-reverse]="teamColor() === 'RED'">
        <p class="font-bold text-l text-gray-700">⚔️ Offense</p>
        <select-player [formField]="teamForm().offense"/>
      </div>
      <button type="button" class="text-3xl flex items-center" (click)="swapPlayers()">
        ⇄
      </button>
      <div class="flex items-center" [class.flex-col]="teamColor() !== 'RED'" [class.flex-col-reverse]="teamColor() === 'RED'">
        <p class="font-bold text-l text-gray-700">🛡️Defense</p>
        <select-player [formField]="teamForm().defense"/>
      </div>
    </div>
    @if (teamForm().offense().touched() && teamForm().defense().touched()) {
      @for (error of teamForm()().errors(); track error) {
        <span class="text-red-700 text-sm font-medium">✖︎ {{ error.message }}</span>
      }
    }
  `
})
export class TeamEntryComponent {
  readonly teamColor = input.required<TeamColor>();

  readonly teamForm = input.required<FieldTree<{ offense: string; defense: string }, any>>();

  swapPlayers() {
    const tree = this.teamForm();
    const p1 = tree.offense().value();
    const p2 = tree.defense().value();
    tree.offense().value.set(p2);
    tree.defense().value.set(p1);
  }
}
