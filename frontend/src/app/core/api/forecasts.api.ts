import { Injectable } from '@angular/core';
import { ForecastStatus, Origin } from './models';
import { query } from './query';

export interface ForecastFilter {
  secid?: string;
  strategyId?: string;
  status?: ForecastStatus | '';
  horizon?: number | null;
  origin?: Origin | '';
  from?: string;
  till?: string;
  page: number;
  size: number;
}

@Injectable({ providedIn: 'root' })
export class ForecastsApi {
  searchUrl(f: ForecastFilter): string {
    return `/api/forecasts${query({
      secid: f.secid,
      strategyId: f.strategyId,
      status: f.status,
      horizon: f.horizon,
      origin: f.origin,
      from: f.from,
      till: f.till,
      page: f.page,
      size: f.size,
    })}`;
  }

  forecastUrl(id: string): string {
    return `/api/forecasts/${id}`;
  }
}
