import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  RequestResource,
  RequestResourceRequest
} from '../models/event-organization.model';

@Injectable({ providedIn: 'root' })
export class RequestResourceService {
  private readonly http = inject(HttpClient);
  private readonly API = '/api/event-organization/requests';

  getRequestResources(requestId: number): Observable<RequestResource[]> {
    return this.http.get<RequestResource[]>(`${this.API}/${requestId}/resources`);
  }

  addResourceToRequest(requestId: number, request: RequestResourceRequest): Observable<RequestResource> {
    return this.http.post<RequestResource>(`${this.API}/${requestId}/resources`, request);
  }

  updateRequestResource(requestId: number, requestResourceId: number, request: RequestResourceRequest): Observable<RequestResource> {
    return this.http.put<RequestResource>(`${this.API}/${requestId}/resources/items/${requestResourceId}`, request);
  }

  confirmRequestResource(requestId: number, requestResourceId: number): Observable<RequestResource> {
    return this.http.put<RequestResource>(`${this.API}/${requestId}/resources/items/${requestResourceId}/confirm`, {});
  }

  removeResourceFromRequest(requestId: number, requestResourceId: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${requestId}/resources/items/${requestResourceId}`);
  }
}
