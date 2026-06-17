export interface Contract {
  id: number;
  negotiationId: number;
  offerTitle: string;
  performerName: string;
  signedByUserName: string;
  signedAt: Date;
  conditionSnapshotJson: string;
  snapshotData?: any;
}

export interface PerformerSchedulingItem {
  contractId: number;
  negotiationId: number;
  performerName: string;
  stageId: number | null;
  stageName: string | null;
  schedulingStatus: 'NOT_ASSIGNED' | 'STAGE_ASSIGNED' | 'TIME_ASSIGNED';
}

export interface StageAssignmentRequest {
  stageId: number;
}