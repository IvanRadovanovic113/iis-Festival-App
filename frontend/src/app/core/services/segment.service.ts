import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Segment, BinaSegment } from '../models/segment.model';

@Injectable({ providedIn: 'root' })
export class SegmentService {
  private readonly http = inject(HttpClient);

  getFestivalSegments(festivalId: number): Observable<Segment[]> {
    return this.http.get<Segment[]>(`/api/festivals/${festivalId}/segments`);
  }

  createSegment(festivalId: number, naziv: string): Observable<Segment> {
    return this.http.post<Segment>(`/api/festivals/${festivalId}/segments`, { naziv });
  }

  deleteSegment(festivalId: number, segmentId: number): Observable<void> {
    return this.http.delete<void>(`/api/festivals/${festivalId}/segments/${segmentId}`);
  }

  getStageSegments(stageId: number): Observable<BinaSegment[]> {
    return this.http.get<BinaSegment[]>(`/api/stages/${stageId}/segments`);
  }

  assignSegment(stageId: number, segmentId: number, kapacitet: number): Observable<BinaSegment> {
    return this.http.post<BinaSegment>(`/api/stages/${stageId}/segments`, { segmentId, kapacitet });
  }

  updateAssignment(stageId: number, segmentId: number, kapacitet: number): Observable<BinaSegment> {
    return this.http.put<BinaSegment>(`/api/stages/${stageId}/segments/${segmentId}`, { kapacitet });
  }

  removeFromStage(stageId: number, segmentId: number): Observable<void> {
    return this.http.delete<void>(`/api/stages/${stageId}/segments/${segmentId}`);
  }
}
