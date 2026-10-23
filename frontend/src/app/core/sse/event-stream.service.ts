import { Injectable, OnDestroy, signal } from '@angular/core';
import { Forecast } from '../api/models';

export type StreamEvent =
  | { type: 'forecast.created'; forecast: Forecast }
  | { type: 'forecast.evaluated'; forecast: Forecast }
  | { type: 'candle.synced'; secid: string; candles: number; lastTradeDate?: string };

@Injectable({ providedIn: 'root' })
export class EventStreamService implements OnDestroy {
  private source?: EventSource;
  private pendingBump?: ReturnType<typeof setTimeout>;

  readonly connected = signal(false);
  readonly lastEvent = signal<StreamEvent | null>(null);
  readonly revision = signal(0);

  connect(): void {
    if (this.source || typeof EventSource === 'undefined') return;
    const source = new EventSource('/api/stream');
    this.source = source;
    source.onopen = () => this.connected.set(true);
    source.onerror = () => this.connected.set(false);
    const forecastHandler = (type: 'forecast.created' | 'forecast.evaluated') => (e: MessageEvent<string>) =>
      this.emit({ type, forecast: JSON.parse(e.data) as Forecast });
    source.addEventListener('forecast.created', forecastHandler('forecast.created'));
    source.addEventListener('forecast.evaluated', forecastHandler('forecast.evaluated'));
    source.addEventListener('candle.synced', (e: MessageEvent<string>) =>
      this.emit({ type: 'candle.synced', ...(JSON.parse(e.data) as { secid: string; candles: number; lastTradeDate?: string }) }),
    );
  }

  private emit(event: StreamEvent): void {
    this.lastEvent.set(event);
    this.pendingBump ??= setTimeout(() => {
      this.pendingBump = undefined;
      this.revision.update((r) => r + 1);
    }, COALESCE_MS);
  }

  ngOnDestroy(): void {
    clearTimeout(this.pendingBump);
    this.source?.close();
  }
}

const COALESCE_MS = 800;
