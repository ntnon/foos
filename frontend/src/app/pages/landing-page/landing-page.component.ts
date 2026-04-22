import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatchEntryComponent } from '../../components/data-entry/match-entry.component';
import { MatchListComponent } from '../../components/match-display/match-list.component';
import { DataLoaderService } from '../../services/data-loader.service';
import { inject } from '@angular/core';

@Component({
  selector: 'landing-page',
  imports: [CommonModule, MatchEntryComponent, MatchListComponent],
  template: `
    <match-entry />

    <div class="max-w-6xl mx-auto px-6 mt-10 pb-12">
      <div class="flex items-center gap-3 mb-4">
        <h2 class="text-2xl font-black text-gray-900">Siste kamper</h2>
        @if (recentMatches().length > 0) {
          <span class="bg-field-500 text-white text-xs font-bold px-2.5 py-1 rounded-full">
            {{ recentMatches().length }}
          </span>
        }
      </div>

      @if (recentMatches().length === 0) {
        <div class="text-center py-10 text-gray-400 bg-white/60 rounded-xl border border-gray-200">
          <div class="text-3xl mb-2">⚽</div>
          <p class="font-medium">Ingen kamper i dag ennå — bli den første!</p>
        </div>
      } @else {
        <match-list [matches]="recentMatches()" />
      }
    </div>
  `,
})
export class LandingPageComponent {
  private dataLoader = inject(DataLoaderService);
  recentMatches = this.dataLoader.recentMatches;
}
