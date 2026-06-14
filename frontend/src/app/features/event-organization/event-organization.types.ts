import { EventResource } from '../../core/models/event-organization.model';

export type MainTab = 'requests' | 'timetable' | 'resources' | 'tasks' | 'analytics';
export type ResourceTab = 'manage' | 'inventory';
export type ResourceModalMode = 'add' | 'edit';
export type RequestFilter = 'All' | 'PENDING' | 'APPROVED' | 'PAST';
export type TimetableMode = 'static' | 'reservation';
export type TaskFilter = 'All' | 'OPEN' | 'RESOLVED' | 'REJECTED';

export interface InventoryRow {
  resource: EventResource;
  assignedQuantity: number;
  stageNames: string[];
  shared: boolean;
}

export interface TimetableDay {
  key: string;
  dayName: string;
  dateLabel: string;
}

export interface ResourceRequestPayload {
  name: string;
  type: string;
  description: string | null;
  totalQuantity: number;
  shareable: boolean;
}
