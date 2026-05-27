import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  EventReservationRequest,
  EventReservationReviewRequest,
  EventReservationScheduleRequest,
  EventReservationStatus,
  EventResource,
  EventResourceRequest,
  RequestResource,
  RequestResourceRequest,
  StageResource,
  StageResourceRequest,
  TimetableSlot
} from '../models/event-organization.model';

@Injectable({ providedIn: 'root' })
export class EventOrganizationService {
  private readonly http = inject(HttpClient);
  private readonly API = '/api/event-organization';

  getReservationRequests(status?: EventReservationStatus): Observable<EventReservationRequest[]> {
    const params = status ? new HttpParams().set('status', status) : undefined;
    return this.http.get<EventReservationRequest[]>(`${this.API}/requests`, { params });
  }

  approveReservationRequest(requestId: number, request: EventReservationReviewRequest): Observable<EventReservationRequest> {
    return this.http.put<EventReservationRequest>(`${this.API}/requests/${requestId}/approve`, request);
  }

  scheduleReservationRequest(requestId: number, request: EventReservationScheduleRequest): Observable<EventReservationRequest> {
    return this.http.put<EventReservationRequest>(`${this.API}/requests/${requestId}/schedule`, request);
  }

  rejectReservationRequest(requestId: number, request: EventReservationReviewRequest): Observable<EventReservationRequest> {
    return this.http.put<EventReservationRequest>(`${this.API}/requests/${requestId}/reject`, request);
  }

  getRequestResources(requestId: number): Observable<RequestResource[]> {
    return this.http.get<RequestResource[]>(`${this.API}/requests/${requestId}/resources`);
  }

  addResourceToRequest(requestId: number, request: RequestResourceRequest): Observable<RequestResource> {
    return this.http.post<RequestResource>(`${this.API}/requests/${requestId}/resources`, request);
  }

  updateRequestResource(requestId: number, resourceId: number, request: RequestResourceRequest): Observable<RequestResource> {
    return this.http.put<RequestResource>(`${this.API}/requests/${requestId}/resources/${resourceId}`, request);
  }

  confirmRequestResource(requestId: number, resourceId: number): Observable<RequestResource> {
    return this.http.put<RequestResource>(`${this.API}/requests/${requestId}/resources/${resourceId}/confirm`, {});
  }

  removeResourceFromRequest(requestId: number, resourceId: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/requests/${requestId}/resources/${resourceId}`);
  }

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
