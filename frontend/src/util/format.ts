/**
 * Formats a possibly-fractional number for display, capping precision at `maxDecimals` (default 2)
 * and stripping trailing zeros so whole values stay clean ("30" not "30.00", "62.33" not
 * "62.333333"). Returns an em dash for null/undefined/NaN.
 */
export function fmtNum(value: number | null | undefined, maxDecimals = 2): string {
  if (value == null || Number.isNaN(value)) return '—';
  return Number(value.toFixed(maxDecimals)).toString();
}
