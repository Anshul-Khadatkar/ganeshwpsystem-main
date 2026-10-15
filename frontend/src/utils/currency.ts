const formatter = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  maximumFractionDigits: 0,
});

export function formatINR(amount: number | string | null | undefined): string {
  if (amount === null || amount === undefined) return '—';
  const value = typeof amount === 'string' ? parseFloat(amount) : amount;
  if (Number.isNaN(value)) return '—';
  return formatter.format(value);
}

/** Renders a possibly-null field as an em-dash instead of "null"/"undefined"/empty. */
export function orDash(value: string | null | undefined): string {
  return value && value.trim().length > 0 ? value : '—';
}
