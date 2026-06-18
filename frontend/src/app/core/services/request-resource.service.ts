import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RequestResource } from '../models/event-organization.model';

@Injectable({ providedIn: 'root' })
export class RequestResourceService {
  private readonly http = inject(HttpClient);
  private readonly API = '/api/event-organization/requests';

  getRequestResources(requestId: number): Observable<RequestResource[]> {
    return this.http.get<RequestResource[]>(`${this.API}/${requestId}/resources`);
  }
}
