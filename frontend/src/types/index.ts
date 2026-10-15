export interface Building {
  id: number;
  name: string;
  description: string | null;
  active: boolean;
  requiresFloor: boolean;
  requiresFlat: boolean;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
}

export interface BuildingRequest {
  name: string;
  description?: string | null;
  active: boolean;
  requiresFloor: boolean;
  requiresFlat: boolean;
  sortOrder: number;
}

export interface Contributor {
  id: number;
  name: string;
  mobileNumber: string;
  buildingId: number;
  buildingName: string;
  floorNumber: string | null;
  flatNumber: string | null;
  whatsappOptIn: boolean;
}

export type WhatsAppStatus = 'NOT_SENT' | 'PENDING' | 'SENT' | 'FAILED';

export interface Donation {
  id: number;
  contributor: Contributor;
  amount: number;
  donationDate: string; // yyyy-MM-dd
  description: string | null;
  whatsappStatus: WhatsAppStatus;
  whatsappMessageId: string | null;
  whatsappSentAt: string | null;
  whatsappErrorMessage: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface DonationCreateRequest {
  name: string;
  mobileNumber: string;
  buildingId: number;
  floorNumber?: string | null;
  flatNumber?: string | null;
  amount: number;
  donationDate: string;
  description?: string | null;
  sendWhatsAppThankYou: boolean;
  /** Active DONATION_THANK_YOU template used when sendWhatsAppThankYou is true. */
  thankYouTemplateId?: number | null;
}

export interface DonationUpdateRequest {
  amount: number;
  donationDate: string;
  description?: string | null;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface DonationFilters {
  buildingId?: number;
  minAmount?: number;
  maxAmount?: number;
  floor?: string;
  flat?: string;
  dateFrom?: string;
  dateTo?: string;
  search?: string;
  whatsappStatus?: WhatsAppStatus;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
}

export interface ApiErrorResponse {
  success: false;
  message: string;
  timestamp: string;
  fieldErrors?: { field: string; message: string }[];
}

// ---- Auth ----

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  username: string;
}

// ---- WhatsApp: templates ----

export type MessageTemplateType = 'DONATION_THANK_YOU' | 'ANNOUNCEMENT' | 'CUSTOM';
export type MetaTemplateStatus = 'NOT_SUBMITTED' | 'PENDING' | 'APPROVED' | 'REJECTED';
export type MetaTemplateCategory = 'UTILITY' | 'MARKETING';

export interface MessageTemplate {
  id: number;
  name: string;
  message: string;
  imageUrl: string | null;
  type: MessageTemplateType;
  active: boolean;
  metaTemplateName: string | null;
  metaLanguageCode: string | null;
  metaParameterFormat: 'NAMED' | 'POSITIONAL' | null;
  metaStatus: MetaTemplateStatus | null;
  metaRejectionReason: string | null;
  metaCategory: MetaTemplateCategory | null;
  /** True when Meta template was approved with an IMAGE header. */
  metaHasImageHeader: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface MessageTemplateRequest {
  name: string;
  message: string;
  imageUrl?: string | null;
  type: MessageTemplateType;
  active: boolean;
  metaTemplateName?: string | null;
  metaLanguageCode?: string;
  metaParameterFormat?: 'NAMED' | 'POSITIONAL';
  metaCategory?: MetaTemplateCategory;
}

export interface MetaRemoteTemplate {
  name: string;
  language: string;
  status: MetaTemplateStatus;
  category: MetaTemplateCategory;
  bodyPreview: string;
  rejectionReason: string | null;
  linkedLocally: boolean;
}

export interface ImportMetaTemplateRequest {
  metaTemplateName: string;
  metaLanguageCode: string;
  name?: string;
  type?: MessageTemplateType;
}

// ---- WhatsApp: broadcasts ----

export type BroadcastStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'COMPLETED_WITH_ERRORS' | 'FAILED';
export type RecipientMode = 'ALL' | 'FILTERED' | 'EXPLICIT';

export interface BroadcastCreateRequest {
  templateId?: number | null;
  message: string;
  imageUrl?: string | null;
  recipientMode: RecipientMode;
  buildingId?: number | null;
  minDonationAmount?: number | null;
  donatedFrom?: string | null;
  donatedTo?: string | null;
  contributorIds?: number[] | null;
}

export interface BroadcastPreview {
  recipientCount: number;
  recipientCriteria: string;
  sampleNames: string[];
}

export interface Broadcast {
  id: number;
  message: string;
  imageUrl: string | null;
  recipientCriteria: string;
  createdBy: string | null;
  status: BroadcastStatus;
  totalRecipients: number;
  successfulCount: number;
  failedCount: number;
  createdAt: string;
}

export interface BroadcastRecipient {
  id: number;
  contributorId: number;
  contributorName: string;
  phoneNumber: string;
  status: WhatsAppStatus;
  providerMessageId: string | null;
  errorMessage: string | null;
  sentAt: string | null;
}

export interface BroadcastDetail {
  broadcast: Broadcast;
  recipients: PageResponse<BroadcastRecipient>;
}

// ---- Settings ----

export interface AppSettings {
  organizationName: string;
  defaultImageUrl: string | null;
}

export interface SummaryStats {
  totalDonation: number;
  totalDonors: number;
  totalDonations: number;
  averageDonation: number;
  highestDonation: number;
  lowestDonation: number;
}

export interface BuildingAnalytics {
  buildingId: number;
  buildingName: string;
  totalDonation: number;
  totalDonors: number;
  averageDonation: number;
  highestDonation: number;
  lowestDonation: number;
  requiresFloor: boolean;
  requiresFlat: boolean;
}

export interface FloorAnalytics {
  floorNumber: string;
  totalDonation: number;
  donorCount: number;
}

export interface FlatAnalytics {
  contributorId: number;
  contributorName: string;
  floorNumber: string | null;
  flatNumber: string | null;
  totalDonation: number;
  donationCount: number;
}

export interface TopDonor {
  contributorId: number;
  contributorName: string;
  buildingName: string;
  totalDonation: number;
  donationCount: number;
}

export interface ThresholdBucket {
  label: string;
  donationCount: number;
}

export interface WhatsAppStats {
  sent: number;
  pending: number;
  failed: number;
  notSent: number;
}
