import { useEffect, useState, useCallback } from 'react';
import { buildingService } from '@/services/buildingService';
import type { Building } from '@/types';

export function useBuildings(activeOnly = false) {
  const [buildings, setBuildings] = useState<Building[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const reload = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await buildingService.list(activeOnly);
      setBuildings(data);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load buildings');
    } finally {
      setLoading(false);
    }
  }, [activeOnly]);

  useEffect(() => {
    reload();
  }, [reload]);

  return { buildings, loading, error, reload };
}
