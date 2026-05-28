export enum OfferStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED',
  FROZEN = 'FROZEN',
  ACCEPTED = 'ACCEPTED',
  ARCHIVED = 'ARCHIVED'
}

export interface PerformerShortInfo {
  performerId: number;
  stageName: string;
  genre: string;
  countryOfOrigin: string;
  performerType: string;
}

export interface OfferRequest {
  price: number;
  performanceDate: string;
  location: string;
  durationMinutes: number;
  additionalRequirements?: string;
  workflowTemplateId: number;
}

export interface OfferResponse {
  offerId: number;
  price: number;
  performanceDate: string;
  location: string;
  status: OfferStatus;
  workflowTemplateId: number;
}

export interface OfferDetailResponse extends OfferResponse {
  durationMinutes: number;
  additionalRequirements: string;
  createdAt: string;
  publishedAt?: string;
  frozenAt?: string;
  acceptedAt?: string;
  archivedAt?: string;
  createdByFullName: string;
  interestedPerformers: PerformerShortInfo[];
  negotiationCount: number;
  successfulContractsCount: number;
  negotiations: string[];
}