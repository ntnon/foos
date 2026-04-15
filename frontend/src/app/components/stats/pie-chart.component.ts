import { Component, input, computed } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface PieSlice {
  label: string;
  value: number;
  color: string;
}

@Component({
  selector: 'pie-chart',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="flex flex-col items-center gap-3 bg-gray-50 rounded-xl p-4 shadow-sm">
      @if (title()) {
        <h3 class="text-sm font-bold text-gray-700">{{ title() }}</h3>
      }
      <svg [attr.width]="size()" [attr.height]="size()" [attr.viewBox]="'0 0 ' + size() + ' ' + size()">
        @for (slice of slices(); track slice.label) {
          <path
            [attr.d]="slice.path"
            [attr.fill]="slice.color"
            [attr.stroke]="'white'"
            stroke-width="2"
          >
            <title>{{ slice.label }}: {{ slice.pct | number:'1.0-1' }}%</title>
          </path>
        }
        @if (total() === 0) {
          <circle [attr.cx]="size()/2" [attr.cy]="size()/2" [attr.r]="size()/2 - 2" fill="#e5e7eb" />
          <text [attr.x]="size()/2" [attr.y]="size()/2 + 5" text-anchor="middle" class="text-xs" fill="#9ca3af">No data</text>
        }
      </svg>
      <!-- Legend -->
      <div class="flex flex-wrap justify-center gap-x-4 gap-y-1">
        @for (s of slices(); track s.label) {
          <div class="flex items-center gap-1 text-xs text-gray-600">
            <span class="inline-block w-2.5 h-2.5 rounded-full flex-shrink-0" [style.background]="s.color"></span>
            {{ s.label }} — {{ s.value }} ({{ s.pct | number:'1.0-1' }}%)
          </div>
        }
      </div>
    </div>
  `,
})
export class PieChartComponent {
  data = input.required<PieSlice[]>();
  size = input<number>(120);
  title = input<string>('');

  rawSlices = this.data;

  total = computed(() => this.data().reduce((sum, s) => sum + s.value, 0));

  slices = computed(() => {
    const t = this.total();
    if (t === 0) return [];
    const cx = this.size() / 2;
    const cy = this.size() / 2;
    const r = this.size() / 2 - 2;

    let startAngle = -Math.PI / 2;
    return this.data().map(s => {
      const pct = s.value / t;
      const angle = pct * 2 * Math.PI;
      const endAngle = startAngle + angle;

      const x1 = cx + r * Math.cos(startAngle);
      const y1 = cy + r * Math.sin(startAngle);
      const x2 = cx + r * Math.cos(endAngle);
      const y2 = cy + r * Math.sin(endAngle);
      const largeArc = angle > Math.PI ? 1 : 0;

      const path = pct >= 1
        // Full circle
        ? `M ${cx} ${cy} m -${r} 0 a ${r} ${r} 0 1 1 ${r * 2} 0 a ${r} ${r} 0 1 1 -${r * 2} 0`
        : `M ${cx} ${cy} L ${x1} ${y1} A ${r} ${r} 0 ${largeArc} 1 ${x2} ${y2} Z`;

      startAngle = endAngle;
      return { ...s, path, pct: pct * 100 };
    });
  });
}

