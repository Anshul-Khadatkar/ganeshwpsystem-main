import { api } from './api';
import type { AppSettings } from '@/types';

export const settingsService = {
  async get(): Promise<AppSettings> {
    const { data } = await api.get<AppSettings>('/settings');
    return data;
  },

  async update(settings: AppSettings): Promise<AppSettings> {
    const { data } = await api.put<AppSettings>('/settings', settings);
    return data;
  },
};
