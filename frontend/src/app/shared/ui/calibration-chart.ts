import { Component, ElementRef, OnDestroy, afterNextRender, effect, input, viewChild } from '@angular/core';
import { CalibrationBucket } from '../../core/api/models';
import { Chart, Plugin } from './charts';
import { pct } from './format';
import { chartTheme, onThemeChange } from './theme';

@Component({
  selector: 'app-calibration-chart',
  template: `<div class="chart-box"><canvas #canvas aria-label="График калибровки"></canvas></div>`,
})
export class CalibrationChart implements OnDestroy {
  readonly buckets = input.required<CalibrationBucket[]>();

  private readonly canvas = viewChild.required<ElementRef<HTMLCanvasElement>>('canvas');
  private chart?: Chart<'scatter'>;
  private unsubscribeTheme?: () => void;

  constructor() {
    afterNextRender(() => {
      this.build();
      this.unsubscribeTheme = onThemeChange(() => this.build());
    });
    effect(() => {
      this.buckets();
      if (this.chart) this.build();
    });
  }

  private build(): void {
    this.chart?.destroy();
    const t = chartTheme();
    const filled = this.buckets().filter((b) => b.sampleSize > 0 && b.meanConfidence !== undefined && b.hitRate !== undefined);

    const whiskers: Plugin<'scatter'> = {
      id: 'whiskers',
      afterDatasetsDraw: (chart) => {
        const { ctx, scales } = chart;
        ctx.save();
        ctx.strokeStyle = t.series1;
        ctx.lineWidth = 2;
        for (const b of filled) {
          const x = scales['x'].getPixelForValue(b.meanConfidence!);
          const y1 = scales['y'].getPixelForValue(b.wilsonLow);
          const y2 = scales['y'].getPixelForValue(b.wilsonHigh);
          ctx.beginPath();
          ctx.moveTo(x, y1);
          ctx.lineTo(x, y2);
          ctx.moveTo(x - 4, y1);
          ctx.lineTo(x + 4, y1);
          ctx.moveTo(x - 4, y2);
          ctx.lineTo(x + 4, y2);
          ctx.stroke();
        }
        ctx.restore();
      },
    };

    this.chart = new Chart(this.canvas().nativeElement, {
      type: 'scatter',
      data: {
        datasets: [
          {
            label: 'Корзина',
            data: filled.map((b) => ({ x: b.meanConfidence!, y: b.hitRate! })),
            backgroundColor: t.series1,
            borderColor: t.surface,
            borderWidth: 2,
            pointRadius: 6,
            pointHoverRadius: 8,
          },
          {
            type: 'line',
            label: 'Идеальная калибровка',
            data: [
              { x: 0.5, y: 0.5 },
              { x: 1, y: 1 },
            ],
            borderColor: t.muted,
            borderDash: [4, 4],
            borderWidth: 1,
            pointRadius: 0,
          } as never,
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        animation: false,
        plugins: {
          tooltip: {
            filter: (item) => item.datasetIndex === 0,
            callbacks: {
              label: (item) => {
                const b = filled[item.dataIndex];
                return ` уверенность ${pct(b.meanConfidence)} → hit rate ${pct(b.hitRate)} [${pct(b.wilsonLow)}–${pct(b.wilsonHigh)}], n=${b.sampleSize}`;
              },
            },
          },
        },
        scales: {
          x: {
            min: 0.5,
            max: 1,
            title: { display: true, text: 'Заявленная уверенность', color: t.text2 },
            ticks: { color: t.text2, maxRotation: 0, stepSize: 0.1, callback: (v) => pct(Number(v)) },
            grid: { color: t.grid },
          },
          y: {
            min: 0,
            max: 1,
            title: { display: true, text: 'Фактический hit rate', color: t.text2 },
            ticks: { color: t.text2, callback: (v) => pct(Number(v)) },
            grid: { color: t.grid },
          },
        },
      },
      plugins: [whiskers],
    });
  }

  ngOnDestroy(): void {
    this.unsubscribeTheme?.();
    this.chart?.destroy();
  }
}
