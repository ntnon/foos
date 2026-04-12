import { Component, input, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatchComponent } from './match.component';
import { Match } from '../models/foosball.models';

@Component({
  selector: 'match-list',
  standalone: true,
  imports: [CommonModule, MatchComponent],
  template: `
    @if (sortedMatches().length === 0) {
      <div class="text-center py-10">
        <p class="text-gray-400">No matches found.</p>
      </div>
    } @else {
      <div class="space-y-8">
        @for (group of groupedMatches(); track group.date) {
          <div>
            <!-- Date header -->
            <div class="flex items-center gap-3 mb-3">
              <div class="flex items-center gap-1.5 bg-gray-800 text-white text-xs font-bold uppercase tracking-widest px-3 py-1 rounded-full shadow-sm">
                <span class="text-gray-400">{{ formatDayOfWeek(group.date) }}</span>
                <span class="text-gray-600">·</span>
                <span>{{ formatDate(group.date) }}</span>
              </div>
              <div class="flex-1 h-px bg-gray-200"></div>
              <span class="text-xs text-gray-400 font-semibold">
                {{ group.matches.length }} {{ group.matches.length === 1 ? 'match' : 'matches' }}
              </span>
            </div>
            <!-- Matches for this day -->
            <div class="space-y-2 pl-2 flex flex-col border-l-2 border-gray-100">
              @for (match of group.matches; track match.matchId) {
                <match [match]="match"
                       [perspectivePlayerId]="perspectivePlayerId()"
                       [perspectiveTeamPlayerIds]="perspectiveTeamPlayerIds()" />
              }
            </div>
          </div>
        }
      </div>
    }
  `,
})
export class MatchListComponent {
  matches = input.required<Match[]>();
  /** When set, this player's team always appears on the left */
  perspectivePlayerId = input<number | null>(null);
  /** When set, this team (by player1Id+player2Id) always appears on the left */
  perspectiveTeamPlayerIds = input<[number, number] | null>(null);

  sortedMatches = computed(() =>
    [...this.matches()].sort((a, b) =>
      new Date(b.matchDate).getTime() - new Date(a.matchDate).getTime()
    )
  );

  groupedMatches = computed(() => {
    const groups = new Map<string, Match[]>();
    for (const match of this.sortedMatches()) {
      const key = match.matchDate;
      if (!groups.has(key)) groups.set(key, []);
      groups.get(key)!.push(match);
    }
    return Array.from(groups.entries()).map(([date, matches]) => ({
      date,
      matches: [...matches].reverse(), // oldest first within a day
    }));
  });

  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  }

  formatDayOfWeek(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', { weekday: 'short' });
  }
}
