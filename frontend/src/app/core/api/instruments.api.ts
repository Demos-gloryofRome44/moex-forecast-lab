import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Candle, Instrument } from './models';
import { query } from './query';

@Injectable({ providedIn: 'root' })
export class InstrumentsApi {
  private readonly http = inject(HttpClient);

  listUrl(): string {
    return '/api/instruments';
  }

  candlesUrl(secid: string, from?: string, till?: string): string {
    return `/api/instruments/${encodeURIComponent(secid)}/candles${query({ from, till, interval: 24 })}`;
  }

  add(secid: string): Observable<Instrument> {
    return this.http.post<Instrument>('/api/instruments', { secid });
  }

  candles(secid: string, from?: string, till?: string): Observable<Candle[]> {
    return this.http.get<Candle[]>(this.candlesUrl(secid, from, till));
  }
}
