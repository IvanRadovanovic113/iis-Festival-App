import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  EventReservationRequest,
  EventReservationScheduleRequest,
  EventReservationStatus,
  TimetableSlot
} from '../models/event-organization.model';

@Injectable({ providedIn: 'root' })
export class EventReservationService {
  private readonly http = inject(HttpClient);
  private readonly API = '/api/event-organization';

  getReservationRequests(status?: EventReservationStatus): Observable<EventReservationRequest[]> {
    const params = status ? new HttpParams().set('status', status) : undefined;
    return this.http.get<EventReservationRequest[]>(`${this.API}/requests`, { params });
  }

  approveReservationRequest(requestId: number): Observable<EventReservationRequest> {
    return this.http.put<EventReservationRequest>(`${this.API}/requests/${requestId}/approve`, null);
  }

  scheduleReservationRequest(requestId: number, request: EventReservationScheduleRequest): Observable<EventReservationRequest> {
    return this.http.put<EventReservationRequest>(`${this.API}/requests/${requestId}/schedule`, request);
  }

  rejectReservationRequest(requestId: number): Observable<EventReservationRequest> {
    return this.http.put<EventReservationRequest>(`${this.API}/requests/${requestId}/reject`, null);
  }

  getTimetable(stageId: number, date: string): Observable<TimetableSlot[]> {
    const params = new HttpParams().set('date', date);
    return this.http.get<TimetableSlot[]>(`${this.API}/stages/${stageId}/timetable`, { params });
  }
}
