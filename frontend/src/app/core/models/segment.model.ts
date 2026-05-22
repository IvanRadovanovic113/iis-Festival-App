export interface Segment {
  segmentId: number;
  name: string;
  festivalId: number;
  festivalName: string;
}

export interface StageSegment {
  id: number;
  segmentId: number;
  segmentName: string;
  capacity: number;
}

// Backwards compatibility alias
export type BinaSegment = StageSegment;
