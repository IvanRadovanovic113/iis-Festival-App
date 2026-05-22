import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Segment, StageSegment } from '../models/segment.model';

@Injectable({ providedIn: 'root' })
export class SegmentService {
  private readonly http = inject(HttpClient);

  getFestivalSegments(festivalId: number): Observable<Segment[]> {
    return this.http.get<Segment[]>(`/api/festivals/${festivalId}/segments`);
  }

  createSegment(festivalId: number, name: string): Observable<Segment> {
    return this.http.post<Segment>(`/api/festivals/${festivalId}/segments`, { name });
  }

  deleteSegment(festivalId: number, segmentId: number): Observable<void> {
    return this.http.delete<void>(`/api/festivals/${festivalId}/segments/${segmentId}`);
  }

  getStageSegments(stageId: number): Observable<StageSegment[]> {
    return this.http.get<StageSegment[]>(`/api/stages/${stageId}/segments`);
  }

  assignSegment(stageId: number, segmentId: number, capacity: number): Observable<StageSegment> {
    return this.http.post<StageSegment>(`/api/stages/${stageId}/segments`, { segmentId, capacity });
  }

  updateAssignment(stageId: number, segmentId: number, capacity: number): Observable<StageSegment> {
    return this.http.put<StageSegment>(`/api/stages/${stageId}/segments/${segmentId}`, { capacity });
  }

  removeFromStage(stageId: number, segmentId: number): Observable<void> {
    return this.http.delete<void>(`/api/stages/${stageId}/segments/${segmentId}`);
  }
}
