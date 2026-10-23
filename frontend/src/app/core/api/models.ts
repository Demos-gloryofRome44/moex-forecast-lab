
export type Direction = 'UP' | 'DOWN' | 'FLAT';
export type Outcome = 'HIT' | 'MISS' | 'NEUTRAL';
export type ForecastStatus = 'PENDING' | 'EVALUATED';
export type Origin = 'LIVE' | 'BACKTEST';

export interface ErrorResponse {
  error: string;
  details?: string[];
}

export interface Instrument {
  secid: string;
  board: string;
  name: string;
  isActive: boolean;
  lastTradeDate?: string;
}

export interface Candle {
  tradeDate: string;
  beginAt: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
}

export interface Strategy {
  id: string;
  code: string;
  version: string;
  kind: 'INDICATOR' | 'BASELINE' | 'ENSEMBLE' | 'ML';
  params: Record<string, string>;
  isActive: boolean;
  isBaseline: boolean;
}

export interface Evaluation {
  evaluatedAt: string;
  targetTradeDate: string;
  actualPrice: number;
  actualReturn: number;
  outcome: Outcome;
  brier?: number;
}

export interface Forecast {
  id: string;
  strategyId: string;
  strategyCode?: string;
  strategyVersion?: string;
  secid: string;
  horizon: number;
  issuedAt: string;
  tradeDate: string;
  basePrice: number;
  direction: Direction;
  confidence: number;
  expectedTargetDate: string;
  status: ForecastStatus;
  origin: Origin;
  evaluation?: Evaluation;
}

export interface ForecastPage {
  items: Forecast[];
  page: number;
  size: number;
  total: number;
}

export interface HitRateStats {
  evaluated: number;
  sampleSize: number;
  hits: number;
  misses: number;
  neutrals: number;
  hitRate?: number;
  wilsonLow: number;
  wilsonHigh: number;
  meanBrier?: number;
  insufficientData: boolean;
  beatsCoinFlip: boolean;
}

export interface PaperTradingStats {
  trades: number;
  totalReturn: number;
  sharpe?: number;
  maxDrawdown: number;
  commissionPerSide: number;
  slippagePerSide: number;
}

export interface Performance {
  strategyId: string;
  strategyCode: string;
  strategyVersion: string;
  kind: string;
  secid?: string;
  horizon?: number;
  origin: Origin;
  stats: HitRateStats;
  paper: PaperTradingStats;
}

export interface CalibrationBucket {
  lower: number;
  upper: number;
  sampleSize: number;
  meanConfidence?: number;
  hitRate?: number;
  wilsonLow: number;
  wilsonHigh: number;
}

export interface Calibration {
  strategyId: string;
  horizon?: number;
  origin: Origin;
  sampleSize: number;
  buckets: CalibrationBucket[];
}

export interface EquityPoint {
  date: string;
  equity: number;
  periodReturn: number;
  trades: number;
}

export interface Equity {
  strategyId: string;
  secid?: string;
  horizon: number;
  origin: Origin;
  trades: number;
  points: EquityPoint[];
}

export interface LeaderboardRow {
  rank: number;
  strategyId: string;
  strategyCode: string;
  strategyVersion: string;
  kind: string;
  isBaseline: boolean;
  stats: HitRateStats;
  paper: PaperTradingStats;
  hitRateVsBestBaseline?: number;
}

export interface Leaderboard {
  horizon: number;
  origin: Origin;
  from?: string;
  till?: string;
  minSampleSize: number;
  rows: LeaderboardRow[];
  warnings: string[];
}

export const HORIZONS = [1, 5, 20] as const;
