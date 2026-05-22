export interface Stage {
  stageId: number;
  name: string;
  capacity: number;
  location: string;
  festivalId: number;
  festivalName: string;
}

export interface StageRequest {
  name: string;
  capacity: number;
  location: string;
}

// Backwards compatibility aliases
export type Bina = Stage;
export type BinaRequest = StageRequest;
