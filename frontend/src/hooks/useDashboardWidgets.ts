import { useCallback, useEffect, useState } from 'react';

export const WIDGET_DEFINITIONS = [
  { key: 'totalDonation', label: 'Total donation', defaultOn: true },
  { key: 'totalContributors', label: 'Total contributors', defaultOn: true },
  { key: 'averageDonation', label: 'Average donation', defaultOn: true },
  { key: 'highestDonation', label: 'Highest donation', defaultOn: true },
  { key: 'lowestDonation', label: 'Lowest donation', defaultOn: false },
  { key: 'buildingCollection', label: 'Building-wise collection', defaultOn: true },
  { key: 'floorCollection', label: 'Floor-wise collection', defaultOn: true },
  { key: 'flatCollection', label: 'Flat-wise collection', defaultOn: false },
  { key: 'topDonors', label: 'Top donors', defaultOn: true },
  { key: 'recentDonations', label: 'Recent donations', defaultOn: true },
  { key: 'donationThresholds', label: 'Donation thresholds', defaultOn: false },
  { key: 'whatsappStatistics', label: 'WhatsApp statistics', defaultOn: false },
] as const;

export type WidgetKey = (typeof WIDGET_DEFINITIONS)[number]['key'];

const STORAGE_KEY = 'ganpati.dashboard.widgets';

function loadStoredPrefs(): Partial<Record<WidgetKey, boolean>> {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : {};
  } catch {
    return {};
  }
}

export function useDashboardWidgets() {
  const [visibility, setVisibility] = useState<Record<WidgetKey, boolean>>(() => {
    const stored = loadStoredPrefs();
    return Object.fromEntries(
      WIDGET_DEFINITIONS.map((w) => [w.key, stored[w.key] ?? w.defaultOn])
    ) as Record<WidgetKey, boolean>;
  });

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(visibility));
  }, [visibility]);

  const toggle = useCallback((key: WidgetKey) => {
    setVisibility((prev) => ({ ...prev, [key]: !prev[key] }));
  }, []);

  const isVisible = useCallback((key: WidgetKey) => visibility[key], [visibility]);

  return { visibility, toggle, isVisible };
}
