import { httpResource } from '@angular/common/http';
import { Component, computed, effect, inject, input, linkedSignal, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { toErrorResponse } from '../../core/api/api-error';
import { ForecastsApi } from '../../core/api/forecasts.api';
import { InstrumentsApi } from '../../core/api/instruments.api';
import { Candle, ErrorResponse, ForecastPage, Instrument, Origin, Strategy } from '../../core/api/models';
import { StrategiesApi } from '../../core/api/strategies.api';
import { EventStreamService } from '../../core/sse/event-stream.service';
import { CandleChart } from '../../shared/ui/candle-chart';
import { RuDatePipe } from '../../shared/ui/format.pipe';
import { HORIZON_OPTIONS, ORIGIN_OPTIONS, Segmented } from '../../shared/ui/segmented';
import { StateView } from '../../shared/ui/state-view';

@Component({
  selector: 'app-instrument-page',
  imports: [FormsModule, RouterLink, StateView, Segmented, CandleChart, RuDatePipe],
  templateUrl: './instrument.page.html',
  styleUrl: './instrument.page.scss',
})
export class InstrumentPage {
  private readonly instrumentsApi = inject(InstrumentsApi);
  private readonly strategiesApi = inject(StrategiesApi);
  private readonly forecastsApi = inject(ForecastsApi);
  private readonly stream = inject(EventStreamService);
  private readonly router = inject(Router);

  readonly secid = input.required<string>();

  protected readonly horizonOptions = HORIZON_OPTIONS;
  protected readonly originOptions = ORIGIN_OPTIONS;
  protected readonly horizon = signal(1);
  protected readonly origin = signal<Origin>('LIVE');

  protected readonly instruments = httpResource<Instrument[]>(() => this.instrumentsApi.listUrl(), { defaultValue: [] });
  protected readonly strategies = httpResource<Strategy[]>(() => this.strategiesApi.listUrl(), { defaultValue: [] });
  protected readonly activeStrategies = computed(() => this.strategies.value().filter((s) => s.isActive));

  protected readonly strategyId = linkedSignal(() => {
    const active = this.activeStrategies();
    return (active.find((s) => s.code === 'sma-crossover') ?? active.find((s) => s.kind === 'INDICATOR') ?? active[0])?.id ?? '';
  });

  private readonly from = computed(() => {
    const d = new Date();
    d.setFullYear(d.getFullYear() - 1);
    return d.toISOString().slice(0, 10);
  });

  protected readonly candles = httpResource<Candle[]>(() => this.instrumentsApi.candlesUrl(this.secid(), this.from()), {
    defaultValue: [],
  });
  protected readonly forecasts = httpResource<ForecastPage>(() =>
    this.strategyId()
      ? this.forecastsApi.searchUrl({
          secid: this.secid(),
          strategyId: this.strategyId(),
          horizon: this.horizon(),
          origin: this.origin(),
          from: this.from(),
          page: 0,
          size: 200,
        })
      : undefined,
  );

  protected readonly current = computed(() => this.instruments.value().find((i) => i.secid === this.secid()));
  protected readonly counts = computed(() => {
    const items = this.forecasts.value()?.items ?? [];
    const by = (o: string) => items.filter((f) => (f.evaluation?.outcome ?? 'PENDING') === o).length;
    const flat = items.filter((f) => f.direction === 'FLAT').length;
    return { hit: by('HIT'), miss: by('MISS'), neutral: by('NEUTRAL'), pending: by('PENDING'), flat, total: items.length };
  });

  protected newSecid = '';
  protected readonly adding = signal(false);
  protected readonly addError = signal<ErrorResponse | null>(null);

  constructor() {
    effect(() => {
      const event = this.stream.lastEvent();
      if (this.stream.revision() === 0 || !event) return;
      this.candles.reload();
      this.forecasts.reload();
      this.instruments.reload();
    });
  }

  protected add(): void {
    const secid = this.newSecid.trim();
    if (!secid) return;
    this.adding.set(true);
    this.addError.set(null);
    this.instrumentsApi.add(secid).subscribe({
      next: (instrument) => {
        this.adding.set(false);
        this.newSecid = '';
        this.instruments.reload();
        void this.router.navigate(['/instruments', instrument.secid]);
      },
      error: (e: unknown) => {
        this.adding.set(false);
        this.addError.set(toErrorResponse(e));
      },
    });
  }
}
