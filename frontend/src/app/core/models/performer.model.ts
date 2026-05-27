export enum PerformerType {
  SOLO = 'SOLO',
  BAND = 'BAND'
}

export enum PerformerStatus {
  ACTIVE = 'ACTIVE',
  ARCHIVED = 'ARCHIVED'
}

export interface PerformerRequest {
  stageName: string;
  firstName?: string;
  lastName?: string;
  genre: string;
  popularity: number;
  averageDurationMinutes: number;
  countryOfOrigin: string;
  performerType: PerformerType;
  numberOfMembers: number;
  bio?: string;
}

export interface PerformerResponse {
  performerId: number;
  stageName: string;
  firstName?: string;
  lastName?: string;
  genre: string;
  popularity: number;
  averageDurationMinutes: number;
  countryOfOrigin: string;
  performerType: PerformerType;
  numberOfMembers: number;
  status: PerformerStatus;
  bio?: string;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}