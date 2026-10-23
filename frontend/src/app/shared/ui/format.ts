const pctFmt = new Intl.NumberFormat('ru-RU', { style: 'percent', minimumFractionDigits: 1, maximumFractionDigits: 1 });
const pctFine = new Intl.NumberFormat('ru-RU', { style: 'percent', minimumFractionDigits: 2, maximumFractionDigits: 2 });
const num1 = new Intl.NumberFormat('ru-RU', { minimumFractionDigits: 1, maximumFractionDigits: 1 });
const num2 = new Intl.NumberFormat('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const num3 = new Intl.NumberFormat('ru-RU', { minimumFractionDigits: 3, maximumFractionDigits: 3 });
const intFmt = new Intl.NumberFormat('ru-RU');

export const DASH = '—';

const clean = (value: number): number => (Object.is(value, -0) || Math.abs(value) < 5e-7 ? 0 : value);
const MINUS = '−';
const withMinus = (s: string): string => s.replace('-', MINUS);

export function pct(value: number | null | undefined): string {
  if (value === null || value === undefined || Number.isNaN(value)) return DASH;
  const v = clean(value);
  return withMinus(v !== 0 && Math.abs(v) < 0.01 ? pctFine.format(v) : pctFmt.format(v));
}

export function signedPct(value: number | null | undefined): string {
  if (value === null || value === undefined) return DASH;
  const v = clean(value);
  const s = withMinus(pctFmt.format(v));
  return v > 0 ? `+${s}` : s;
}

export function num(value: number | null | undefined, digits: 2 | 3 = 2): string {
  if (value === null || value === undefined || Number.isNaN(value)) return DASH;
  return withMinus((digits === 3 ? num3 : num2).format(clean(value)));
}

export function int(value: number | null | undefined): string {
  return value === null || value === undefined ? DASH : intFmt.format(value);
}

export function pp(value: number | null | undefined): string {
  if (value === null || value === undefined) return DASH;
  const v = clean(value);
  return `${v > 0 ? '+' : ''}${withMinus(num1.format(v))} п.п.`;
}

export function shortDate(iso: string | null | undefined): string {
  if (!iso) return DASH;
  const [y, m, d] = iso.slice(0, 10).split('-');
  return `${d}.${m}.${y}`;
}
