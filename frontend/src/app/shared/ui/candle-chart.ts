import { Component, ElementRef, OnDestroy, afterNextRender, effect, input, viewChild } from '@angular/core';
import {
  CandlestickSeries,
  IChartApi,
  ISeriesApi,
  ISeriesMarkersPluginApi,
  SeriesMarker,
  Time,
  createChart,
  createSeriesMarkers,
} from 'lightweight-charts';
import { Candle, Forecast } from '../../core/api/models';
import { chartTheme, onThemeChange } from './theme';

@Component({
  selector: 'app-candle-chart',
  styles: `.chart { height: 460px; width: 100%; }`,
  template: `<div #host class="chart"></div>`,
})
export class CandleChart implements OnDestroy {
  readonly candles = input.required<Candle[]>();
  readonly forecasts = input<Forecast[]>([]);

  private readonly host = viewChild.required<ElementRef<HTMLDivElement>>('host');
  private chart?: IChartApi;
  private series?: ISeriesApi<'Candlestick'>;
  private markers?: ISeriesMarkersPluginApi<Time>;
  private unsubscribeTheme?: () => void;
  private resizeObserver?: ResizeObserver;
  private fittedWidth = 0;

  constructor() {
    afterNextRender(() => {
      this.build();
      this.resizeObserver = new ResizeObserver(([entry]) => {
        const width = Math.round(entry.contentRect.width);
        if (width > 0 && width !== this.fittedWidth) {
          this.fittedWidth = width;
          this.chart?.timeScale().fitContent();
        }
      });
      this.resizeObserver.observe(this.host().nativeElement);
      this.unsubscribeTheme = onThemeChange(() => {
        this.chart?.remove();
        this.build();
      });
    });
    effect(() => {
      const candles = this.candles();
      const forecasts = this.forecasts();
      if (this.series) this.render(candles, forecasts);
    });
  }

  private build(): void {
    const t = chartTheme();
    this.chart = createChart(this.host().nativeElement, {
      autoSize: true,
      layout: { background: { color: t.surface }, textColor: t.text2, attributionLogo: false },
      grid: { vertLines: { color: t.grid }, horzLines: { color: t.grid } },
      rightPriceScale: { borderColor: t.border },
      timeScale: { borderColor: t.border },
      localization: { locale: 'ru-RU' },
    });
    this.series = this.chart.addSeries(CandlestickSeries, {
      upColor: t.candleUp,
      downColor: t.candleDown,
      wickUpColor: t.candleUp,
      wickDownColor: t.candleDown,
      borderVisible: false,
    });
    this.markers = createSeriesMarkers(this.series, []);
    this.render(this.candles(), this.forecasts());
  }

  private render(candles: Candle[], forecasts: Forecast[]): void {
    const t = chartTheme();
    this.series!.setData(
      candles.map((c) => ({ time: c.tradeDate as Time, open: c.open, high: c.high, low: c.low, close: c.close })),
    );
    const days = new Set(candles.map((c) => c.tradeDate));
    const markers: SeriesMarker<Time>[] = forecasts
      .filter((f) => days.has(f.tradeDate) && f.direction !== 'FLAT')
      .sort((a, b) => a.tradeDate.localeCompare(b.tradeDate))
      .map((f) => {
        const outcome = f.evaluation?.outcome;
        const color = outcome === 'HIT' ? t.good : outcome === 'MISS' ? t.critical : t.pending;
        const up = f.direction === 'UP';
        return {
          time: f.tradeDate as Time,
          position: up ? 'belowBar' : 'aboveBar',
          shape: up ? 'arrowUp' : 'arrowDown',
          color,
          id: f.id,
        } satisfies SeriesMarker<Time>;
      });
    this.markers!.setMarkers(markers);
    requestAnimationFrame(() => this.chart?.timeScale().fitContent());
  }

  ngOnDestroy(): void {
    this.resizeObserver?.disconnect();
    this.unsubscribeTheme?.();
    this.chart?.remove();
  }
}
