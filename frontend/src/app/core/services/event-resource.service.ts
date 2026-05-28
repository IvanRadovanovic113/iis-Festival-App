import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  EventResource,
  EventResourceRequest
} from '../models/event-organization.model';

@Injectable({ providedIn: 'root' })
export class EventResourceService {
  private readonly http = inject(HttpClient);
  private readonly API = '/api/event-organization/resources';

  getResources(): Observable<EventResource[]> {
    return this.http.get<EventResource[]>(this.API);
  }

  createResource(request: EventResourceRequest): Observable<EventResource> {
    return this.http.post<EventResource>(this.API, request);
  }

  updateResource(resourceId: number, request: EventResourceRequest): Observable<EventResource> {
    return this.http.put<EventResource>(`${this.API}/${resourceId}`, request);
  }

  deleteResource(resourceId: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${resourceId}`);
  }
}
