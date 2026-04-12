import { Component, inject, input } from '@angular/core';
import { FieldTree, FormField } from '@angular/forms/signals';
import { FoosballApiService } from '../../../services/foosball-api.service';

@Component({
  selector: 'select-player',
  imports: [FormField],
  template: `
    <select [formField]="formField()"
            class="flex block px-3 py-2 w-50 border border-gray-300 rounded-md bg-white text-gray-700 text-sm cursor-pointer focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500">
      @for (player of apiService.players(); track player.playerId) {
        <option class="text-xl" [value]="player.playerId">{{ player.name }}</option>
      }
    </select>
  `
})
export class PlayerEntryComponent {
  apiService = inject(FoosballApiService);
  formField = input.required<FieldTree<string, string>>();
}
