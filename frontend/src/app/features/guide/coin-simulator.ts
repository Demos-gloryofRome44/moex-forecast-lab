import { Component, ElementRef, OnDestroy, afterNextRender, computed, signal, viewChild } from '@angular/core';
import { HitRateStats } from '../../core/api/models';
import { HitRate } from '../../shared/ui/hit-rate';

const Z = 1.96;
const MIN_SAMPLE = 30;
const AUTO_FLIPS = 1000;
const AUTO_MS = 3200;
const FLIP_MS = 700;
const RACE_COINS = 20;
const RACE_FLIPS = 200;
const RACE_MS = 3000;
const STRIP = 60;
const HEIGHT = 240;
const PAD = { l: 44, r: 16, t: 12, b: 28 };

interface Point {
  n: number;
  rate: number;
  low: number;
  high: number;
}

function wilson(hits: number, n: number): [number, number] {
  if (n === 0) return [0, 1];
  const p = hits / n;
  const z2 = Z * Z;
  const d = 1 + z2 / n;
  const center = (p + z2 / (2 * n)) / d;
  const margin = (Z * Math.sqrt((p * (1 - p)) / n + z2 / (4 * n * n))) / d;
  return [Math.max(0, center - margin), Math.min(1, center + margin)];
}

function statsOf(hits: number, n: number): HitRateStats {
  const [low, high] = wilson(hits, n);
  return {
    evaluated: n,
    sampleSize: n,
    hits,
    misses: n - hits,
    neutrals: 0,
    hitRate: n ? hits / n : undefined,
    wilsonLow: low,
    wilsonHigh: high,
    insufficientData: n < MIN_SAMPLE,
    beatsCoinFlip: n >= MIN_SAMPLE && low > 0.5,
  };
}

function cumulative(flips: boolean[]): Point[] {
  let hits = 0;
  return flips.map((hit, i) => {
    if (hit) hits++;
    const n = i + 1;
    const [low, high] = wilson(hits, n);
    return { n, rate: hits / n, low, high };
  });
}

const ease = (t: number): number => 1 - Math.pow(1 - t, 2);
const coin = (): boolean => Math.random() < 0.5;

@Component({
  selector: 'app-coin-simulator',
  imports: [HitRate],
  templateUrl: './coin-simulator.html',
  styleUrl: './coin-simulator.scss',
})
export class CoinSimulator implements OnDestroy {
  private readonly plot = viewChild.required<ElementRef<HTMLElement>>('plot');
  private resizeObserver?: ResizeObserver;
  private frame?: number;
  private flipTimer?: ReturnType<typeof setTimeout>;

  protected readonly height = HEIGHT;
  protected readonly pad = PAD;
  protected readonly width = signal(640);
  protected readonly raceCoins = RACE_COINS;
  protected readonly raceFlips = RACE_FLIPS;

  protected readonly flips = signal<boolean[]>([]);
  protected readonly domain = signal(MIN_SAMPLE);
  protected readonly busy = signal(false);
  protected readonly spinning = signal(false);
  protected readonly coinAngle = signal(0);
  protected readonly hover = signal<number | null>(null);

  protected readonly points = computed(() => cumulative(this.flips()));
  protected readonly stats = computed(() => {
    const f = this.flips();
    return statsOf(f.filter(Boolean).length, f.length);
  });
  protected readonly strip = computed(() => this.flips().slice(-STRIP));
  protected readonly lastHit = computed(() => this.flips().at(-1));

  protected readonly n = computed(() => Math.max(this.domain(), this.flips().length));
  protected readonly yTicks = [0, 0.25, 0.5, 0.75, 1];
  protected readonly xTicks = computed(() => {
    const n = this.n();
    return [1, Math.round(n / 2), n];
  });

  protected readonly linePath = computed(() => this.path(this.points().map((p) => [p.n, p.rate]), this.n()));
  protected readonly bandPath = computed(() => {
    const pts = this.points();
    if (pts.length < 2) return '';
    const n = this.n();
    const upper = pts.map((p) => `${this.x(p.n, n)},${this.y(p.high)}`);
    const lower = pts.map((p) => `${this.x(p.n, n)},${this.y(p.low)}`).reverse();
    return `M${upper.join('L')}L${lower.join('L')}Z`;
  });
  protected readonly hovered = computed(() => {
    const i = this.hover();
    return i === null ? null : this.points()[i] ?? null;
  });

  protected readonly verdict = computed(() => {
    const s = this.stats();
    if (s.sampleSize === 0) return 'Подбросьте монетку: каждый бросок — один прогноз, орёл — попадание, решка — промах.';
    if (s.sampleSize < MIN_SAMPLE) {
      return `Всего ${s.sampleSize} бросков — полоса интервала огромная. На такой выборке монетка легко выглядит и гением, и неудачником.`;
    }
    if (s.wilsonLow > 0.5) return 'Редкий случай: даже честная монетка иногда выходит за 50% всей полосой. Именно поэтому победителей проверяют на новых данных.';
    return `Полоса захватывает 50% — результат не отличить от везения. Чем больше бросков, тем уже полоса и тем ближе линия к честным 50%.`;
  });

  private readonly raceRuns = signal<number[][]>([]);
  protected readonly raceStep = signal(0);
  protected readonly raceDone = computed(() => this.raceRuns().length > 0 && this.raceStep() >= RACE_FLIPS);
  protected readonly raceLeader = computed(() => {
    const step = this.raceStep();
    const runs = this.raceRuns();
    if (!runs.length || step === 0) return -1;
    let best = 0;
    runs.forEach((r, i) => {
      if (r[step - 1] > runs[best][step - 1]) best = i;
    });
    return best;
  });
  protected readonly racePaths = computed(() => {
    const step = this.raceStep();
    return this.raceRuns().map((r) => this.path(r.slice(0, step).map((rate, i) => [i + 1, rate]), RACE_FLIPS));
  });
  protected readonly funnelPath = computed(() => {
    const ns = Array.from({ length: RACE_FLIPS }, (_, i) => i + 1).filter((n) => n >= 4);
    const half = (n: number) => Z * Math.sqrt(0.25 / n);
    const upper = ns.map((n) => `${this.x(n, RACE_FLIPS)},${this.y(Math.min(1, 0.5 + half(n)))}`);
    const lower = ns.map((n) => `${this.x(n, RACE_FLIPS)},${this.y(Math.max(0, 0.5 - half(n)))}`).reverse();
    return `M${upper.join('L')}L${lower.join('L')}Z`;
  });
  protected readonly leaderLabel = computed(() => {
    const i = this.raceLeader();
    const step = this.raceStep();
    if (i < 0) return null;
    const rate = this.raceRuns()[i][step - 1];
    return { x: this.x(step, RACE_FLIPS), y: this.y(rate), rate };
  });
  protected readonly raceSummary = computed(() => {
    const runs = this.raceRuns();
    if (!this.raceDone()) return null;
    const finals = runs.map((r) => r[RACE_FLIPS - 1]);
    const best = Math.max(...finals);
    const above55 = finals.filter((v) => v >= 0.55).length;
    const recognised = finals.filter((v) => wilson(Math.round(v * RACE_FLIPS), RACE_FLIPS)[0] > 0.5).length;
    return { best, above55, recognised };
  });

  constructor() {
    afterNextRender(() => {
      this.resizeObserver = new ResizeObserver(([entry]) => this.width.set(Math.max(320, Math.round(entry.contentRect.width))));
      this.resizeObserver.observe(this.plot().nativeElement);
    });
  }

  protected flipOnce(): void {
    if (this.busy()) return;
    const hit = coin();
    this.busy.set(true);
    this.turnCoin(hit);
    this.flipTimer = setTimeout(() => {
      this.flips.update((f) => [...f, hit]);
      this.busy.set(false);
    }, FLIP_MS);
  }

  protected flipMany(count: number): void {
    if (this.busy()) return;
    const batch = Array.from({ length: count }, coin);
    this.turnCoin(batch[batch.length - 1]);
    this.flips.update((f) => [...f, ...batch]);
  }

  protected autoRun(): void {
    if (this.busy()) return;
    const start = this.flips();
    const extra = Array.from({ length: AUTO_FLIPS }, coin);
    this.domain.set(start.length + AUTO_FLIPS);
    this.busy.set(true);
    this.spinning.set(true);
    const t0 = performance.now();
    const tick = (now: number) => {
      const shown = Math.round(AUTO_FLIPS * ease(Math.min(1, (now - t0) / AUTO_MS)));
      this.flips.set([...start, ...extra.slice(0, shown)]);
      if (shown < AUTO_FLIPS) {
        this.frame = requestAnimationFrame(tick);
      } else {
        this.spinning.set(false);
        this.turnCoin(extra[AUTO_FLIPS - 1]);
        this.busy.set(false);
      }
    };
    this.frame = requestAnimationFrame(tick);
  }

  protected reset(): void {
    this.stopAnimation();
    this.flips.set([]);
    this.domain.set(MIN_SAMPLE);
    this.hover.set(null);
  }

  protected race(): void {
    if (this.frame) cancelAnimationFrame(this.frame);
    const runs = Array.from({ length: RACE_COINS }, () => {
      let hits = 0;
      return Array.from({ length: RACE_FLIPS }, (_, i) => {
        if (coin()) hits++;
        return hits / (i + 1);
      });
    });
    this.raceRuns.set(runs);
    this.raceStep.set(0);
    const t0 = performance.now();
    const tick = (now: number) => {
      const step = Math.max(1, Math.round(RACE_FLIPS * ease(Math.min(1, (now - t0) / RACE_MS))));
      this.raceStep.set(step);
      if (step < RACE_FLIPS) this.frame = requestAnimationFrame(tick);
    };
    this.frame = requestAnimationFrame(tick);
  }

  protected onMove(event: MouseEvent): void {
    const pts = this.points();
    if (!pts.length) return;
    const rect = (event.currentTarget as SVGElement).getBoundingClientRect();
    const inner = this.width() - PAD.l - PAD.r;
    const n = Math.round(((event.clientX - rect.left - PAD.l) / inner) * (this.n() - 1)) + 1;
    this.hover.set(Math.min(pts.length, Math.max(1, n)) - 1);
  }

  protected x(n: number, domain: number): number {
    return PAD.l + ((n - 1) / Math.max(1, domain - 1)) * (this.width() - PAD.l - PAD.r);
  }

  protected y(v: number): number {
    return PAD.t + (1 - v) * (HEIGHT - PAD.t - PAD.b);
  }

  protected pct(v: number): string {
    return `${Math.round(v * 100)}%`;
  }

  private path(points: [number, number][], domain: number): string {
    return points.map(([n, v], i) => `${i ? 'L' : 'M'}${this.x(n, domain).toFixed(1)},${this.y(v).toFixed(1)}`).join('');
  }

  private turnCoin(hit: boolean): void {
    const angle = this.coinAngle();
    this.coinAngle.set(angle - (angle % 360) + 1440 + (hit ? 0 : 180));
  }

  private stopAnimation(): void {
    if (this.frame) cancelAnimationFrame(this.frame);
    clearTimeout(this.flipTimer);
    this.busy.set(false);
    this.spinning.set(false);
  }

  ngOnDestroy(): void {
    this.stopAnimation();
    this.resizeObserver?.disconnect();
  }
}
