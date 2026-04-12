import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DataLoaderService } from '../../services/data-loader.service';
import { MatchListComponent } from '../../components/match-list.component';
import { SearchBarComponent } from '../../components/search-bar.component';
import { Match } from '../../models/foosball.models';

@Component({
  selector: 'matches',
  standalone: true,
  imports: [CommonModule, MatchListComponent, SearchBarComponent],
  template: `
    <div class="px-6 py-8 max-w-7xl mx-auto">
      <div class="flex items-center justify-between pb-4 mb-6 border-b-2 border-gray-200">
        <h2 class="text-4xl font-black text-gray-900 -tracking-widest">Recent Matches</h2>
        <span class="text-lg font-semibold text-gray-600 bg-gray-100 px-4 py-2 rounded-full">
          {{ filteredMatches().length }} matches
        </span>
      </div>

      <div class="mb-8 max-w-md">
        <search-bar [(value)]="searchQuery" placeholder="Search by player name…" />
      </div>

      <match-list [matches]="filteredMatches()" />
    </div>
  `
})
export class MatchesComponent {
  private dataLoaderService = inject(DataLoaderService);
  matches = this.dataLoaderService.matches;

  searchQuery = signal('');

  filteredMatches = computed((): Match[] => {
    const q = this.searchQuery().trim().toLowerCase();
    if (!q) return this.matches();
    return this.matches().filter(m => {
      const names = [
        m.team1.offense.playerName,
        m.team1.defense.playerName,
        m.team2.offense.playerName,
        m.team2.defense.playerName,
      ];
      return names.some(name => name.toLowerCase().includes(q));
    });
  });
}
