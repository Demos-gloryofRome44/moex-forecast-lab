import { httpResource } from '@angular/common/http';
import { Component, effect, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Leaderboard, Origin } from '../../core/api/models';
import { StrategiesApi } from '../../core/api/strategies.api';
import { EventStreamService } from '../../core/sse/event-stream.service';
import { kindLabel } from '../../shared/ui/badges';
import { NumPipe, PctPipe, PpPipe, SignedPctPipe } from '../../shared/ui/format.pipe';
import { HitRate } from '../../shared/ui/hit-rate';
import { HORIZON_OPTIONS, ORIGIN_OPTIONS, Segmented } from '../../shared/ui/segmented';
import { StateView } from '../../shared/ui/state-view';

@Component({
  selector: 'app-leaderboard-page',
  imports: [RouterLink, StateView, Segmented, HitRate, NumPipe, PctPipe, SignedPctPipe, PpPipe],
  templateUrl: './leaderboard.page.html',
  styleUrl: './leaderboard.page.scss',
})
export class LeaderboardPage {
  private readonly api = inject(StrategiesApi);
  private readonly stream = inject(EventStreamService);

  protected readonly kindLabel = kindLabel;
  protected readonly horizonOptions = HORIZON_OPTIONS;
  protected readonly originOptions = ORIGIN_OPTIONS;
  protected readonly horizon = signal(1);
  protected readonly origin = signal<Origin>('LIVE');

  protected readonly board = httpResource<Leaderboard>(() => this.api.leaderboardUrl(this.horizon(), this.origin()));

  constructor() {
    effect(() => {
      if (this.stream.revision() > 0) this.board.reload();
    });
  }
}
