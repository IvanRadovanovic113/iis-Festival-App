import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PerformerSchedulingItem, StageAssignmentRequest } from '../models/contract.model';

@Injectable({ providedIn: 'root' })
export class PerformerSchedulingService {
  private readonly http = inject(HttpClient);
  private readonly API = '/api/performer-scheduling';

  getAll(): Observable<PerformerSchedulingItem[]> {
    return this.http.get<PerformerSchedulingItem[]>(this.API);
  }

  assignStage(contractId: number, stageId: number): Observable<PerformerSchedulingItem> {
    const body: StageAssignmentRequest = { stageId };
    return this.http.put<PerformerSchedulingItem>(`${this.API}/${contractId}/stage`, body);
  }

  removeStage(contractId: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${contractId}/stage`);
  }
}
