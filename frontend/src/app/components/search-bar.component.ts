import { Component, input, model } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'search-bar',
  standalone: true,
  imports: [FormsModule],
  host: { class: 'block' },
  template: `
    <div class="relative">
      <span class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-sm pointer-events-none">🔍</span>
      <input
        type="text"
        [ngModel]="value()"
        (ngModelChange)="value.set($event)"
        [placeholder]="placeholder()"
        class="w-full pl-9 pr-9 py-2.5 rounded-xl border border-gray-300 bg-white text-sm font-medium text-gray-700 shadow-sm
               focus:outline-none focus:ring-2 focus:ring-brand-400 focus:border-brand-400 transition"
      />
      @if (value().length > 0) {
        <button
          type="button"
          (click)="value.set('')"
          class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 text-lg leading-none"
          aria-label="Clear search"
        >✕</button>
      }
    </div>
  `
})
export class SearchBarComponent {
  /** Two-way bound search string */
  value = model('');
  placeholder = input<string>('Search…');
}

