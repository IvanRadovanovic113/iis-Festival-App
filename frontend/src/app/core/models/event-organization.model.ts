export interface EventResource {
  id: number;
  name: string;
  type: string;
  description: string | null;
  totalQuantity: number;
  shareable: boolean;
  festivalId: number;
}

export interface EventResourceRequest {
  name: string;
  type: string;
  description: string | null;
  totalQuantity: number;
  shareable: boolean;
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
  createdAt: string;
  reviewedAt: string | null;
}

export interface EventReservationScheduleRequest {
  startTime: string;
}

export type RequestResourceStatus = 'REQUESTED' | 'CONFIRMED' | 'UNAVAILABLE';

export interface RequestResource {
  id: number;
  reservationRequestId: number;
  resourceId: number | null;
  resourceName: string;
  resourceType: string;
  quantity: number;
  totalQuantity: number | null;
  status: RequestResourceStatus;
}

export interface RequestResourceRequest {
  resourceId: number | null;
  requestedName?: string | null;
  requestedType?: string | null;
  quantity: number;
}

export interface TimetableSlot {
  date: string;
  startTime: string;
  endTime: string;
  status: string;
  performerName: string | null;
}

export type EventOrganizationTaskStatus = 'OPEN' | 'RESOLVED' | 'REJECTED';
export type EventOrganizationTaskType = 'PROCUREMENT' | 'NON_EXISTING';

export interface EventOrganizationTask {
  id: number;
  reservationRequestId: number;
  requestResourceId: number;
  resourceId: number | null;
  title: string;
  performerName: string;
  stageName: string;
  deadline: string;
  type: EventOrganizationTaskType;
  status: EventOrganizationTaskStatus;
  resolutionNote: string | null;
  rejectionReason: string | null;
  actor: string | null;
  changedAt: string | null;
}

export interface ResolveTaskRequest {
  note: string | null;
}

export interface RejectTaskRequest {
  reason: string;
}
