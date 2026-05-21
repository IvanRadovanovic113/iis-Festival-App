import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  EventResource,
  EventResourceRequest,
  StageResource,
  StageResourceRequest,
  TimetableSlot
} from '../models/event-organization.model';

@Injectable({ providedIn: 'root' })
export class EventOrganizationService {
  private readonly http = inject(HttpClient);
  private readonly API = '/api/event-organization';

  getResources(): Observable<EventResource[]> {
    return this.http.get<EventResource[]>(`${this.API}/resources`);
  }

  createResource(request: EventResourceRequest): Observable<EventResource> {
    return this.http.post<EventResource>(`${this.API}/resources`, request);
  }

  updateResource(resourceId: number, request: EventResourceRequest): Observable<EventResource> {
    return this.http.put<EventResource>(`${this.API}/resources/${resourceId}`, request);
  }

  deleteResource(resourceId: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/resources/${resourceId}`);
  }

  getStageResources(stageId: number): Observable<StageResource[]> {
    return this.http.get<StageResource[]>(`${this.API}/stages/${stageId}/resources`);
  }

  assignResourceToStage(stageId: number, request: StageResourceRequest): Observable<StageResource> {
    return this.http.post<StageResource>(`${this.API}/stages/${stageId}/resources`, request);
  }

  updateStageResource(stageId: number, resourceId: number, request: StageResourceRequest): Observable<StageResource> {
    return this.http.put<StageResource>(`${this.API}/stages/${stageId}/resources/${resourceId}`, request);
  }

  removeResourceFromStage(stageId: number, resourceId: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/stages/${stageId}/resources/${resourceId}`);
  }

  getTimetable(stageId: number, date: string): Observable<TimetableSlot[]> {
    const params = new HttpParams().set('date', date);
    return this.http.get<TimetableSlot[]>(`${this.API}/stages/${stageId}/timetable`, { params });
  }
}
