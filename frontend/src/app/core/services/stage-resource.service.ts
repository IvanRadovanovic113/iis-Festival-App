import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  StageResource,
  StageResourceRequest
} from '../models/event-organization.model';

@Injectable({ providedIn: 'root' })
export class StageResourceService {
  private readonly http = inject(HttpClient);
  private readonly API = '/api/event-organization/stages';

  getStageResources(stageId: number): Observable<StageResource[]> {
    return this.http.get<StageResource[]>(`${this.API}/${stageId}/resources`);
  }

  assignResourceToStage(stageId: number, request: StageResourceRequest): Observable<StageResource> {
    return this.http.post<StageResource>(`${this.API}/${stageId}/resources`, request);
  }

  updateStageResource(stageId: number, resourceId: number, request: StageResourceRequest): Observable<StageResource> {
    return this.http.put<StageResource>(`${this.API}/${stageId}/resources/${resourceId}`, request);
  }

  removeResourceFromStage(stageId: number, resourceId: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${stageId}/resources/${resourceId}`);
  }
}
