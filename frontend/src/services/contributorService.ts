import { api } from './api';
import type { Contributor, PageResponse } from '@/types';

export const contributorService = {
  async search(params: {
    search?: string;
    buildingId?: number;
    whatsappOptInOnly?: boolean;
    page?: number;
    size?: number;
  }): Promise<PageResponse<Contributor>> {
    const { data } = await api.get<PageResponse<Contributor>>('/contributors', { params });
    return data;
  },
};
