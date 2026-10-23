import { httpResource } from '@angular/common/http';
import { Component, computed, effect, inject, input, linkedSignal, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ForecastsApi } from '../../core/api/forecasts.api';
import { InstrumentsApi } from '../../core/api/instruments.api';
import { ForecastPage, ForecastStatus, Instrument, Origin, Strategy } from '../../core/api/models';
import { StrategiesApi } from '../../core/api/strategies.api';
import { EventStreamService } from '../../core/sse/event-stream.service';
import { DirectionBadge, OriginBadge, OutcomeBadge } from '../../shared/ui/badges';
import { IntPipe, NumPipe, PctPipe, RuDatePipe, SignedPctPipe } from '../../shared/ui/format.pipe';
import { StateView } from '../../shared/ui/state-view';

const PAGE_SIZE = 50;

@Component({
  selector: 'app-forecasts-page',
  imports: [RouterLink, StateView, DirectionBadge, OutcomeBadge, OriginBadge, PctPipe, SignedPctPipe, NumPipe, IntPipe, RuDatePipe],
  templateUrl: './forecasts.page.html',
  styleUrl: './forecasts.page.scss',
})
export class ForecastsPage {
  private readonly api = inject(ForecastsApi);
  private readonly instrumentsApi = inject(InstrumentsApi);
  private readonly strategiesApi = inject(StrategiesApi);
  private readonly stream = inject(EventStreamService);

  readonly strategyIdParam = input<string | undefined>(undefined, { alias: 'strategyId' });

  protected readonly secid = signal('');
  protected readonly strategyId = linkedSignal(() => this.strategyIdParam() ?? '');
  protected readonly status = signal<ForecastStatus | ''>('');
  protected readonly horizon = signal<number | null>(null);
  protected readonly origin = signal<Origin | ''>('LIVE');
  protected readonly page = linkedSignal(() => {
    this.secid();
    this.strategyId();
    this.status();
    this.horizon();
    this.origin();
    return 0;
  });

  protected readonly instruments = httpResource<Instrument[]>(() => this.instrumentsApi.listUrl(), { defaultValue: [] });
  protected readonly strategies = httpResource<Strategy[]>(() => this.strategiesApi.listUrl(), { defaultValue: [] });
  protected readonly journal = httpResource<ForecastPage>(() =>
    this.api.searchUrl({
      secid: this.secid(),
      strategyId: this.strategyId(),
      status: this.status(),
      horizon: this.horizon(),
      origin: this.origin(),
      page: this.page(),
      size: PAGE_SIZE,
    }),
  );

  protected readonly pages = computed(() => Math.max(1, Math.ceil((this.journal.value()?.total ?? 0) / PAGE_SIZE)));

  constructor() {
    effect(() => {
      if (this.stream.revision() > 0) this.journal.reload();
    });
  }

  protected value(event: Event): string {
    return (event.target as HTMLSelectElement).value;
  }
}
