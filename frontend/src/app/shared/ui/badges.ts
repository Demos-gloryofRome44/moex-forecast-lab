import { Component, computed, input } from '@angular/core';
import { Direction, Origin, Outcome } from '../../core/api/models';

@Component({
  selector: 'app-outcome',
  template: `<span class="fact" [class.accent]="outcome() === 'HIT'" [class.danger]="outcome() === 'MISS'">{{ label() }}</span>`,
})
export class OutcomeBadge {
  readonly outcome = input<Outcome | undefined>();
  protected readonly label = computed(() => {
    switch (this.outcome()) {
      case 'HIT':
        return 'Попадание';
      case 'MISS':
        return 'Промах';
      case 'NEUTRAL':
        return 'Нейтрально';
      default:
        return 'Ждёт цели';
    }
  });
}

@Component({
  selector: 'app-direction',
  styles: `
    .dir { font-size: 13px; font-weight: 500; white-space: nowrap; }
    .flat { color: var(--text-3); }
  `,
  template: `<span class="dir" [class.flat]="direction() === 'FLAT'">{{ label() }}</span>`,
})
export class DirectionBadge {
  readonly direction = input.required<Direction>();
  protected readonly label = computed(() => ({ UP: '↑ Рост', DOWN: '↓ Падение', FLAT: '— Без ставки' })[this.direction()]);
}

@Component({
  selector: 'app-origin',
  template: `
    @if (origin() === 'BACKTEST') {
      <span class="fact violet" title="Историческая прогонка, не живой журнал">Бэктест</span>
    } @else {
      <span class="fact">Live</span>
    }
  `,
})
export class OriginBadge {
  readonly origin = input.required<Origin>();
}

export function kindLabel(kind: string): string {
  return ({ BASELINE: 'Бейзлайн', INDICATOR: 'Индикатор', ENSEMBLE: 'Ансамбль', ML: 'ML' } as Record<string, string>)[kind] ?? kind;
}

@Component({
  selector: 'app-kind',
  template: `<span class="fact" [class.warn]="kind() === 'BASELINE'">{{ label() }}</span>`,
})
export class KindBadge {
  readonly kind = input.required<string>();
  protected readonly label = computed(() => kindLabel(this.kind()));
}
