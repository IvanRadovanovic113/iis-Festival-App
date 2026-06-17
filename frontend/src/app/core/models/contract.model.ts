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