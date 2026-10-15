import { api } from './api';
import type { Building, BuildingRequest } from '@/types';

export const buildingService = {
  async list(activeOnly = false): Promise<Building[]> {
    const { data } = await api.get<Building[]>('/buildings', { params: { activeOnly } });
    return data;
  },

  async getById(id: number): Promise<Building> {
    const { data } = await api.get<Building>(`/buildings/${id}`);
    return data;
  },

  async create(request: BuildingRequest): Promise<Building> {
    const { data } = await api.post<Building>('/buildings', request);
    return data;
  },

  async update(id: number, request: BuildingRequest): Promise<Building> {
    const { data } = await api.put<Building>(`/buildings/${id}`, request);
    return data;
  },

  async setActive(id: number, active: boolean): Promise<Building> {
    const { data } = await api.patch<Building>(`/buildings/${id}/status`, null, { params: { active } });
    return data;
  },
};
