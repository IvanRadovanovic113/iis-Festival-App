import { EventReservationStatus, StageResource } from './event-organization.model';

export type PerformerSchedulingStatus = 'NOT_ASSIGNED' | 'STAGE_ASSIGNED' | 'TIME_ASSIGNED';

export interface PerformerContractReservation {
  contractId: number;
  negotiationId: number;
  performerName: string;
  stageId: number;
  stageName: string;
  performanceDate: string;
  durationMinutes: number;
  schedulingStatus: PerformerSchedulingStatus;
  reservationRequestId: number | null;
  reservationStatus: EventReservationStatus | null;
  stageResources: StageResource[];
}

export interface ContractReservationResourceRequest {
  resourceId: number;
  quantity: number;
}

export interface ContractReservationCustomResourceRequest {
  requestedName: string;
  requestedType: string;
  quantity: number;
}

export interface CreateContractReservationRequest {
  existingResources: ContractReservationResourceRequest[];
  customResources: ContractReservationCustomResourceRequest[];
  notes: string | null;
}
