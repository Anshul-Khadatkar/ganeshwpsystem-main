import { api } from './api';
import type {
  ImportMetaTemplateRequest,
  MessageTemplate,
  MessageTemplateRequest,
  MessageTemplateType,
  MetaRemoteTemplate,
} from '@/types';

export const templateService = {
  async list(type?: MessageTemplateType): Promise<MessageTemplate[]> {
    const { data } = await api.get<MessageTemplate[]>('/whatsapp/templates', { params: { type } });
    return data;
  },

  async create(request: MessageTemplateRequest): Promise<MessageTemplate> {
    const { data } = await api.post<MessageTemplate>('/whatsapp/templates', request);
    return data;
  },

  async update(id: number, request: MessageTemplateRequest): Promise<MessageTemplate> {
    const { data } = await api.put<MessageTemplate>(`/whatsapp/templates/${id}`, request);
    return data;
  },

  async setActive(id: number, active: boolean): Promise<MessageTemplate> {
    const { data } = await api.patch<MessageTemplate>(`/whatsapp/templates/${id}/status`, null, { params: { active } });
    return data;
  },

  async submitToMeta(id: number): Promise<MessageTemplate> {
    const { data } = await api.post<MessageTemplate>(`/whatsapp/templates/${id}/submit-to-meta`);
    return data;
  },

  async syncMetaStatus(id: number): Promise<MessageTemplate> {
    const { data } = await api.post<MessageTemplate>(`/whatsapp/templates/${id}/sync-meta-status`);
    return data;
  },

  async listApprovedFromMeta(): Promise<MetaRemoteTemplate[]> {
    const { data } = await api.get<MetaRemoteTemplate[]>('/whatsapp/templates/meta/approved');
    return data;
  },

  async importFromMeta(request: ImportMetaTemplateRequest): Promise<MessageTemplate> {
    const { data } = await api.post<MessageTemplate>('/whatsapp/templates/import-from-meta', request);
    return data;
  },

  async delete(id: number): Promise<void> {
    await api.delete(`/whatsapp/templates/${id}`);
  },
};
