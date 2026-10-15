import { api } from './api';
import type { Donation, DonationCreateRequest, DonationUpdateRequest, DonationFilters, PageResponse } from '@/types';

export const donationService = {
  async create(request: DonationCreateRequest): Promise<Donation> {
    const { data } = await api.post<Donation>('/donations', request);
    return data;
  },

  async getById(id: number): Promise<Donation> {
    const { data } = await api.get<Donation>(`/donations/${id}`);
    return data;
  },

  async update(id: number, request: DonationUpdateRequest): Promise<Donation> {
    const { data } = await api.put<Donation>(`/donations/${id}`, request);
    return data;
  },

  async remove(id: number): Promise<void> {
    await api.delete(`/donations/${id}`);
  },

  async search(filters: DonationFilters): Promise<PageResponse<Donation>> {
    const { data } = await api.get<PageResponse<Donation>>('/donations', { params: filters });
    return data;
  },

  async retryWhatsApp(id: number): Promise<Donation> {
    const { data } = await api.post<Donation>(`/whatsapp/donations/${id}/retry`);
    return data;
  },
};
