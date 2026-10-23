import { Component, input, model } from '@angular/core';

export interface SegmentOption<T> {
  value: T;
  label: string;
}

@Component({
  selector: 'app-segmented',
  template: `
    <div class="chips" role="radiogroup" [attr.aria-label]="label()">
      @for (o of options(); track o.value) {
        <button type="button" class="chip" role="radio" [attr.aria-checked]="o.value === value()" [class.active]="o.value === value()" (click)="value.set(o.value)">
          {{ o.label }}
        </button>
      }
    </div>
  `,
})
export class Segmented<T> {
  readonly options = input.required<SegmentOption<T>[]>();
  readonly label = input('');
  readonly value = model.required<T>();
}

export const HORIZON_OPTIONS: SegmentOption<number>[] = [
  { value: 1, label: '1 день' },
  { value: 5, label: '5 дней' },
  { value: 20, label: '20 дней' },
];

export const ORIGIN_OPTIONS: SegmentOption<'LIVE' | 'BACKTEST'>[] = [
  { value: 'LIVE', label: 'Живой журнал' },
  { value: 'BACKTEST', label: 'Бэктест' },
];
