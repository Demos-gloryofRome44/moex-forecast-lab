import { Component, ElementRef, OnDestroy, afterNextRender, effect, input, viewChild } from '@angular/core';
import { EquityPoint } from '../../core/api/models';
import { Chart } from './charts';
import { shortDate, signedPct } from './format';

const monthFmt = new Intl.DateTimeFormat('ru-RU', { month: 'short', year: '2-digit' });
const monthLabel = (iso: string): string => monthFmt.format(new Date(`${iso}T00:00:00`)).replace(' г.', '');
import { chartTheme, onThemeChange } from './theme';

@Component({
  selector: 'app-equity-chart',
  template: `<div class="chart-box"><canvas #canvas aria-label="Кривая капитала"></canvas></div>`,
})
export class EquityChart implements OnDestroy {
  readonly points = input.required<EquityPoint[]>();

  private readonly canvas = viewChild.required<ElementRef<HTMLCanvasElement>>('canvas');
  private chart?: Chart;
  private unsubscribeTheme?: () => void;

  constructor() {
    afterNextRender(() => {
      this.build();
      this.unsubscribeTheme = onThemeChange(() => this.build());
    });
    effect(() => {
      this.points();
      if (this.chart) this.build();
    });
  }

  private build(): void {
    this.chart?.destroy();
    const t = chartTheme();
    const points = this.points();
    this.chart = new Chart(this.canvas().nativeElement, {
      type: 'line',
      data: {
        labels: points.map((p) => monthLabel(p.date)),
        datasets: [
          {
            label: 'Капитал',
            data: points.map((p) => p.equity),
            borderColor: t.series1,
            borderWidth: 2,
            pointRadius: 0,
            pointHoverRadius: 4,
            tension: 0,
          },
          {
            label: 'Старт',
            data: points.map(() => 1),
            borderColor: t.muted,
            borderWidth: 1,
            borderDash: [4, 4],
            pointRadius: 0,
            pointHoverRadius: 0,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        animation: false,
        interaction: { mode: 'index', intersect: false },
        plugins: {
          tooltip: {
            filter: (item) => item.datasetIndex === 0,
            callbacks: {
              title: (items) => shortDate(points[items[0].dataIndex].date),
              label: (item) => {
                const p = points[item.dataIndex];
                return ` капитал ${signedPct(p.equity - 1)} · день ${signedPct(p.periodReturn)} · сделок ${p.trades}`;
              },
            },
          },
        },
        scales: {
          x: { ticks: { color: t.text2, maxTicksLimit: 7, maxRotation: 0, autoSkipPadding: 16 }, grid: { display: false }, border: { color: t.border } },
          y: { ticks: { color: t.text2, callback: (v) => signedPct(Number(v) - 1) }, grid: { color: t.grid }, border: { display: false } },
        },
      },
    });
  }

  ngOnDestroy(): void {
    this.unsubscribeTheme?.();
    this.chart?.destroy();
  }
}
