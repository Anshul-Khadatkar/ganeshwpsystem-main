import { api } from './api';
import type { Broadcast, BroadcastCreateRequest, BroadcastDetail, BroadcastPreview, PageResponse } from '@/types';

export const broadcastService = {
  async preview(request: BroadcastCreateRequest): Promise<BroadcastPreview> {
    const { data } = await api.post<BroadcastPreview>('/whatsapp/broadcasts/preview', request);
    return data;
  },

  async create(request: BroadcastCreateRequest): Promise<Broadcast> {
    const { data } = await api.post<Broadcast>('/whatsapp/broadcasts', request);
    return data;
  },

  async history(page = 0, size = 20): Promise<PageResponse<Broadcast>> {
    const { data } = await api.get<PageResponse<Broadcast>>('/whatsapp/broadcasts', { params: { page, size } });
    return data;
  },

  async getDetail(id: number, page = 0, size = 50): Promise<BroadcastDetail> {
    const { data } = await api.get<BroadcastDetail>(`/whatsapp/broadcasts/${id}`, { params: { page, size } });
    return data;
  },

  async retryFailed(id: number): Promise<Broadcast> {
    const { data } = await api.post<Broadcast>(`/whatsapp/broadcasts/${id}/retry-failed`);
    return data;
  },
};
