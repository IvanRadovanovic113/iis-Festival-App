import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  EventOrganizationTask,
  EventOrganizationTaskStatus,
  RejectTaskRequest,
  ResolveTaskRequest
} from '../models/event-organization.model';

@Injectable({ providedIn: 'root' })
export class EventOrganizationTaskService {
  private readonly http = inject(HttpClient);
  private readonly API = '/api/event-organization/tasks';

  getTasks(status?: EventOrganizationTaskStatus): Observable<EventOrganizationTask[]> {
    const params = status ? new HttpParams().set('status', status) : undefined;
    return this.http.get<EventOrganizationTask[]>(this.API, { params });
  }

  resolveTask(taskId: number, request: ResolveTaskRequest): Observable<EventOrganizationTask> {
    return this.http.put<EventOrganizationTask>(`${this.API}/${taskId}/resolve`, request);
  }

  rejectTask(taskId: number, request: RejectTaskRequest): Observable<EventOrganizationTask> {
    return this.http.put<EventOrganizationTask>(`${this.API}/${taskId}/reject`, request);
  }
}
