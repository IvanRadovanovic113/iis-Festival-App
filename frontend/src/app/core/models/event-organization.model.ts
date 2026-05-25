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

export interface TimetableSlot {
  date: string;
  startTime: string;
  endTime: string;
  status: string;
  performerName: string | null;
}
