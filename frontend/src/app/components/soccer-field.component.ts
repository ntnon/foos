import { Component } from '@angular/core';

@Component({
  selector: 'soccer-field',
  standalone: true,
  template: `
    <div class="soccer-field" aria-hidden="true">
      <div class="grass-stripes"></div>
      <div class="boundary"></div>
      <div class="halfway-line"></div>
      <div class="centre-circle"></div>
      <div class="centre-dot"></div>
      <div class="penalty-area penalty-area-left"></div>
      <div class="goal-area goal-area-left"></div>
      <div class="penalty-area penalty-area-right"></div>
      <div class="goal-area goal-area-right"></div>
      <div class="ball"></div>
    </div>
  `,
  styles: [`
    .soccer-field {
      position: absolute;
      inset: 0;
      background-color: var(--color-field-700);
      overflow: hidden;
    }

    .grass-stripes {
      position: absolute;
      inset: 0;
      background: repeating-linear-gradient(
        to right,
        color-mix(in srgb, var(--color-field-500) 35%, transparent) 0px,
        color-mix(in srgb, var(--color-field-500) 35%, transparent) 60px,
        color-mix(in srgb, var(--color-field-600) 35%, transparent) 60px,
        color-mix(in srgb, var(--color-field-600) 35%, transparent) 120px
      );
    }

    .boundary {
      position: absolute;
      inset: 14px;
      border: 10px solid rgba(255,255,255,0.85);
      border-radius: 12px;
    }

    .halfway-line {
      position: absolute;
      top: 14px; bottom: 14px;
      left: 50%;
      width: 10px;
      background: rgba(255,255,255,0.85);
      transform: translateX(-50%);
    }

    .centre-circle {
      position: absolute;
      top: 50%; left: 50%;
      width: 160px; height: 160px;
      border: 10px solid rgba(255,255,255,0.85);
      border-radius: 50%;
      transform: translate(-50%, -50%);
    }

    .centre-dot {
      position: absolute;
      top: 50%; left: 50%;
      width: 20px; height: 20px;
      background: rgba(255,255,255,0.85);
      border-radius: 50%;
      transform: translate(-50%, -50%);
    }

    .penalty-area {
      position: absolute;
      top: 50%;
      width: 160px; height: 300px;
      border: 10px solid rgba(255,255,255,0.85);
      border-radius: 8px;
      transform: translateY(-50%);
    }
    .penalty-area-left  { left: 14px;  border-left: none;  border-top-left-radius: 0; border-bottom-left-radius: 0; }
    .penalty-area-right { right: 14px; border-right: none; border-top-right-radius: 0; border-bottom-right-radius: 0; }

    .goal-area {
      position: absolute;
      top: 50%;
      width: 80px; height: 180px;
      border: 10px solid rgba(255,255,255,0.85);
      border-radius: 10px;
      transform: translateY(-50%);
    }
    .goal-area-left  { left: 14px;  border-left: none;  border-top-left-radius: 0; border-bottom-left-radius: 0; }
    .goal-area-right { right: 14px; border-right: none; border-top-right-radius: 0; border-bottom-right-radius: 0; }

    /* ── Bouncing ball ── */
    .ball {
      position: absolute;
      width: 22px;
      height: 22px;
      background: radial-gradient(circle at 35% 35%, #fff176, #f9a825 60%, #e65100);
      border-radius: 50%;
      box-shadow: 0 0 8px 2px rgba(255, 200, 0, 0.7), 0 2px 4px rgba(0,0,0,0.4);
      animation: ballBounce 6s linear infinite;
      top: 20%;
      left: 20%;
    }

    @keyframes ballBounce {
      0%   { top: 15%;  left: 15%;  }
      12%  { top: 70%;  left: 55%;  }
      25%  { top: 20%;  left: 75%;  }
      37%  { top: 65%;  left: 30%;  }
      50%  { top: 10%;  left: 60%;  }
      62%  { top: 75%;  left: 80%;  }
      75%  { top: 30%;  left: 40%;  }
      87%  { top: 80%;  left: 15%;  }
      100% { top: 15%;  left: 15%;  }
    }
  `]
})
export class SoccerFieldComponent {}
