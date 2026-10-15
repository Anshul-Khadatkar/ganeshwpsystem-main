import { format, startOfWeek, startOfMonth, endOfDay } from 'date-fns';

export function formatDate(dateStr: string | null | undefined): string {
  if (!dateStr) return '—';
  try {
    return format(new Date(dateStr), 'd MMM yyyy');
  } catch {
    return dateStr;
  }
}

export function formatDateTime(dateStr: string | null | undefined): string {
  if (!dateStr) return '—';
  try {
    return format(new Date(dateStr), 'd MMM yyyy, h:mm a');
  } catch {
    return dateStr;
  }
}

export type DatePreset = 'today' | 'thisWeek' | 'thisMonth' | 'custom';

/** Translates a preset into an explicit ISO date range for the backend - the API only ever sees dateFrom/dateTo. */
export function resolveDatePreset(preset: DatePreset): { dateFrom?: string; dateTo?: string } {
  const today = new Date();
  const iso = (d: Date) => format(d, 'yyyy-MM-dd');

  switch (preset) {
    case 'today':
      return { dateFrom: iso(today), dateTo: iso(today) };
    case 'thisWeek':
      return { dateFrom: iso(startOfWeek(today, { weekStartsOn: 1 })), dateTo: iso(endOfDay(today)) };
    case 'thisMonth':
      return { dateFrom: iso(startOfMonth(today)), dateTo: iso(endOfDay(today)) };
    default:
      return {};
  }
}
