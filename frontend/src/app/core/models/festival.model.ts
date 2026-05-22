export type FestivalStatus = 'ACTIVE' | 'INACTIVE' | 'UPCOMING' | 'COMPLETED' | 'CANCELLED';

export const FESTIVAL_STATUS_LABELS: Record<FestivalStatus, string> = {
  ACTIVE: 'Active',
  INACTIVE: 'Inactive',
  UPCOMING: 'Upcoming',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled'
};

export interface Festival {
  festivalId: number;
  name: string;
  location: string;
  status: FestivalStatus;
  startDate: string;
  endDate: string;
}

export interface FestivalRequest {
  name: string;
  location: string;
  status: FestivalStatus;
  startDate: string;
  endDate: string;
}
