export interface EventResource {
  id: number;
  name: string;
  type: string;
  description: string | null;
  totalQuantity: number;
  festivalId: number;
}

export interface EventResourceRequest {
  name: string;
  type: string;
  description: string | null;
  totalQuantity: number;
}

export interface StageResource {
  id: number;
  stageId: number;
  resourceId: number;
  resourceName: string;
  resourceType: string;
  quantity: number;
  totalQuantity: number;
}

export interface StageResourceRequest {
  resourceId: number;
  quantity: number;
}

export type EventReservationStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';

export interface EventReservationRequest {
  id: number;
  festivalId: number;
  performerName: string;
  stageId: number;
  stageName: string;
  performanceDate: string;
  startTime: string;
  endTime: string;
  status: EventReservationStatus;
  notes: string | null;
  reviewNote: string | null;
  createdAt: string;
  reviewedAt: string | null;
}

export interface EventReservationReviewRequest {
  reviewNote: string | null;
}

export interface EventReservationScheduleRequest {
  startTime: string;
  reviewNote: string | null;
}

export type RequestResourceStatus = 'REQUESTED' | 'CONFIRMED' | 'UNAVAILABLE';

export interface RequestResource {
  id: number;
  reservationRequestId: number;
  resourceId: number;
  resourceName: string;
  resourceType: string;
  quantity: number;
  totalQuantity: number;
  status: RequestResourceStatus;
}

export interface RequestResourceRequest {
  resourceId: number;
  quantity: number;
}

export interface TimetableSlot {
  date: string;
  startTime: string;
  endTime: string;
  status: string;
  performerName: string | null;
}
