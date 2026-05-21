export interface Segment {
  segmentId: number;
  naziv: string;
  festivalId: number;
  festivalNaziv: string;
}

export interface BinaSegment {
  id: number;
  segmentId: number;
  segmentNaziv: string;
  kapacitet: number;
}
