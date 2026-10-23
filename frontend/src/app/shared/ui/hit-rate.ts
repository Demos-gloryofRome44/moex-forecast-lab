import { Component, input } from '@angular/core';
import { HitRateStats } from '../../core/api/models';
import { IntPipe, PctPipe } from './format.pipe';

@Component({
  selector: 'app-hit-rate',
  imports: [PctPipe, IntPipe],
  styleUrl: './hit-rate.scss',
  template: `
    <div class="hit" [class.muted-stats]="stats().insufficientData">
      <div class="value">
        <span class="rate">{{ stats().hitRate | pct }}</span>
        <span class="n">n = {{ stats().sampleSize | int }}</span>
        @if (stats().beatsCoinFlip) {
          <span class="fact accent" title="Нижняя граница интервала выше 50%">лучше монетки</span>
        }
      </div>
      @if (stats().sampleSize > 0) {
        <div class="track" [attr.aria-label]="'95% интервал: ' + (stats().wilsonLow | pct) + ' – ' + (stats().wilsonHigh | pct)">
          <span class="coin"></span>
          <span class="range" [style.left.%]="pos(stats().wilsonLow)" [style.width.%]="pos(stats().wilsonHigh) - pos(stats().wilsonLow)"></span>
          @if (stats().hitRate !== undefined) {
            <span class="point" [style.left.%]="pos(stats().hitRate!)"></span>
          }
        </div>
        <div class="interval">{{ stats().wilsonLow | pct }} – {{ stats().wilsonHigh | pct }}</div>
      }
      @if (stats().insufficientData) {
        <div class="hint">Мало данных · нужно ≥ {{ minSample() }}</div>
      }
    </div>
  `,
})
export class HitRate {
  readonly stats = input.required<HitRateStats>();
  readonly minSample = input(30);

  protected pos(value: number): number {
    return Math.min(100, Math.max(0, ((value - 0.3) / 0.4) * 100));
  }
}
