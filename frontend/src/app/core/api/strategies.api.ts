import { Injectable } from '@angular/core';
import { Origin } from './models';
import { query } from './query';

@Injectable({ providedIn: 'root' })
export class StrategiesApi {
  listUrl(): string {
    return '/api/strategies';
  }

  strategyUrl(id: string): string {
    return `/api/strategies/${id}`;
  }

  performanceUrl(id: string, horizon: number, origin: Origin, secid?: string): string {
    return `/api/strategies/${id}/performance${query({ horizon, origin, secid })}`;
  }

  calibrationUrl(id: string, horizon: number, origin: Origin): string {
    return `/api/strategies/${id}/calibration${query({ horizon, origin })}`;
  }

  equityUrl(id: string, horizon: number, origin: Origin, secid?: string): string {
    return `/api/strategies/${id}/equity${query({ horizon, origin, secid })}`;
  }

  leaderboardUrl(horizon: number, origin: Origin, from?: string, till?: string): string {
    return `/api/leaderboard${query({ horizon, origin, from, till })}`;
  }
}
