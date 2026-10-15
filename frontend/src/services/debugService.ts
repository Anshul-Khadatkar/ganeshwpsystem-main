import { api } from './api';

export interface DebugClearResult {
  ok: boolean;
  message: string;
  deletedRows: Record<string, number>;
  clearedBy: string;
}

export const debugService = {
  async clearData(password: string): Promise<DebugClearResult> {
    const { data } = await api.post<DebugClearResult>('/debug-controller-4719/clear-data', { password });
    return data;
  },
};
