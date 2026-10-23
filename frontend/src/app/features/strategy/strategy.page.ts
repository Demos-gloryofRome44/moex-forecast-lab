import { httpResource } from '@angular/common/http';
import { Component, computed, effect, inject, input, linkedSignal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Calibration, Equity, ForecastPage, Origin, Performance, Strategy } from '../../core/api/models';
import { ForecastsApi } from '../../core/api/forecasts.api';
import { StrategiesApi } from '../../core/api/strategies.api';
import { EventStreamService } from '../../core/sse/event-stream.service';
import { DirectionBadge, OriginBadge, OutcomeBadge, kindLabel } from '../../shared/ui/badges';
import { CalibrationChart } from '../../shared/ui/calibration-chart';
import { EquityChart } from '../../shared/ui/equity-chart';
import { IntPipe, NumPipe, PctPipe, RuDatePipe, SignedPctPipe } from '../../shared/ui/format.pipe';
import { HitRate } from '../../shared/ui/hit-rate';
import { HORIZON_OPTIONS, ORIGIN_OPTIONS, Segmented } from '../../shared/ui/segmented';
import { StateView } from '../../shared/ui/state-view';

@Component({
  selector: 'app-strategy-page',
  imports: [
    RouterLink, StateView, Segmented, HitRate, EquityChart, CalibrationChart,
    DirectionBadge, OutcomeBadge, OriginBadge, PctPipe, SignedPctPipe, NumPipe, IntPipe, RuDatePipe,
  ],
  templateUrl: './strategy.page.html',
  styleUrl: './strategy.page.scss',
})
export class StrategyPage {
  private readonly strategies = inject(StrategiesApi);
  private readonly forecastsApi = inject(ForecastsApi);
  private readonly stream = inject(EventStreamService);

  readonly id = input.required<string>();
  readonly horizonParam = input<string | undefined>(undefined, { alias: 'horizon' });
  readonly originParam = input<string | undefined>(undefined, { alias: 'origin' });

  protected readonly kindLabel = kindLabel;
  protected readonly horizonOptions = HORIZON_OPTIONS;
  protected readonly originOptions = ORIGIN_OPTIONS;
  protected readonly horizon = linkedSignal(() => Number(this.horizonParam() ?? 1) || 1);
  protected readonly origin = linkedSignal<Origin>(() => (this.originParam() === 'BACKTEST' ? 'BACKTEST' : 'LIVE'));

  protected readonly strategy = httpResource<Strategy>(() => this.strategies.strategyUrl(this.id()));
  protected readonly performance = httpResource<Performance>(() =>
    this.strategies.performanceUrl(this.id(), this.horizon(), this.origin()),
  );
  protected readonly equity = httpResource<Equity>(() => this.strategies.equityUrl(this.id(), this.horizon(), this.origin()));
  protected readonly calibration = httpResource<Calibration>(() =>
    this.strategies.calibrationUrl(this.id(), this.horizon(), this.origin()),
  );
  protected readonly recent = httpResource<ForecastPage>(() =>
    this.forecastsApi.searchUrl({ strategyId: this.id(), horizon: this.horizon(), origin: this.origin(), page: 0, size: 20 }),
  );

  protected readonly paramsText = computed(() =>
    Object.entries(this.strategy.value()?.params ?? {})
      .map(([k, v]) => `${k}=${v}`)
      .join(', '),
  );

  constructor() {
    effect(() => {
      if (this.stream.revision() > 0) {
        this.performance.reload();
        this.equity.reload();
        this.calibration.reload();
        this.recent.reload();
      }
    });
  }
}
