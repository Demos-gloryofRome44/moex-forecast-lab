export function cssVar(name: string): string {
  return getComputedStyle(document.documentElement).getPropertyValue(name).trim();
}

export function chartTheme() {
  return {
    surface: cssVar('--surface'),
    text: cssVar('--text'),
    text2: cssVar('--text-2'),
    muted: cssVar('--text-3'),
    grid: cssVar('--grid'),
    border: cssVar('--border'),
    series1: cssVar('--series-1'),
    good: cssVar('--good'),
    critical: cssVar('--critical'),
    pending: cssVar('--pending'),
    candleUp: cssVar('--candle-up'),
    candleDown: cssVar('--candle-down'),
  };
}

export function onThemeChange(callback: () => void): () => void {
  const observer = new MutationObserver(() => callback());
  observer.observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] });
  return () => observer.disconnect();
}
