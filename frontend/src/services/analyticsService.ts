import { api } from './api';
import type {
  SummaryStats,
  BuildingAnalytics,
  FloorAnalytics,
  FlatAnalytics,
  TopDonor,
  Donation,
  ThresholdBucket,
  WhatsAppStats,
} from '@/types';

export const analyticsService = {
  async summary(buildingId?: number): Promise<SummaryStats> {
    const { data } = await api.get<SummaryStats>('/analytics/summary', { params: { buildingId } });
    return data;
  },

  async buildingWise(): Promise<BuildingAnalytics[]> {
    const { data } = await api.get<BuildingAnalytics[]>('/analytics/buildings');
    return data;
  },

  async buildingDetail(buildingId: number): Promise<BuildingAnalytics> {
    const { data } = await api.get<BuildingAnalytics>(`/analytics/buildings/${buildingId}`);
    return data;
  },

  async floorWise(buildingId: number): Promise<FloorAnalytics[]> {
    const { data } = await api.get<FloorAnalytics[]>(`/analytics/buildings/${buildingId}/floors`);
    return data;
  },

  async flatWise(buildingId: number): Promise<FlatAnalytics[]> {
    const { data } = await api.get<FlatAnalytics[]>(`/analytics/buildings/${buildingId}/flats`);
    return data;
  },

  async topDonors(buildingId?: number, limit = 10): Promise<TopDonor[]> {
    const { data } = await api.get<TopDonor[]>('/analytics/top-donors', { params: { buildingId, limit } });
    return data;
  },

  async recentDonations(buildingId?: number, limit = 10): Promise<Donation[]> {
    const { data } = await api.get<Donation[]>('/analytics/recent-donations', { params: { buildingId, limit } });
    return data;
  },

  async thresholds(buildingId?: number): Promise<ThresholdBucket[]> {
    const { data } = await api.get<ThresholdBucket[]>('/analytics/thresholds', { params: { buildingId } });
    return data;
  },

  async whatsappStats(buildingId?: number): Promise<WhatsAppStats> {
    const { data } = await api.get<WhatsAppStats>('/analytics/whatsapp-stats', { params: { buildingId } });
    return data;
  },
};
