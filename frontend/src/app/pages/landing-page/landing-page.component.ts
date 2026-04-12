import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatchEntryComponent } from './match-entry/match-entry.component';
import { MatchListComponent } from '../../components/match-list.component';
import { DataLoaderService } from '../../services/data-loader.service';
import { inject } from '@angular/core';

@Component({
  selector: 'landing-page',
  imports: [CommonModule, MatchEntryComponent, MatchListComponent],
  template: `
    <match-entry />

    <div class="max-w-6xl mx-auto px-6 mt-10 pb-12">
      <div class="flex items-center gap-3 mb-4">
        <h2 class="text-2xl font-black text-gray-900">Today's Matches</h2>
        @if (todaysMatches().length > 0) {
          <span class="bg-field-500 text-white text-xs font-bold px-2.5 py-1 rounded-full">
            {{ todaysMatches().length }}
          </span>
        }
      </div>

      @if (todaysMatches().length === 0) {
        <div class="text-center py-10 text-gray-400 bg-white/60 rounded-xl border border-gray-200">
          <div class="text-3xl mb-2">⚽</div>
          <p class="font-medium">No matches today yet — be the first!</p>
        </div>
      } @else {
        <match-list [matches]="todaysMatches()" />
      }
    </div>
  `,
})
export class LandingPageComponent {
  private dataLoader = inject(DataLoaderService);
  todaysMatches = this.dataLoader.todaysMatches;
}
