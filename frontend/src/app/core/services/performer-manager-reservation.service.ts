import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EventReservationRequest } from '../models/event-organization.model';
import {
  CreateContractReservationRequest,
  PerformerContractReservation
} from '../models/performer-manager.model';

@Injectable({ providedIn: 'root' })
export class PerformerManagerReservationService {
  private readonly http = inject(HttpClient);
  private readonly API = '/api/performer-manager/reservations';

  getContracts(): Observable<PerformerContractReservation[]> {
    return this.http.get<PerformerContractReservation[]>(`${this.API}/contracts`);
  }

  createReservationRequest(contractId: number, request: CreateContractReservationRequest): Observable<EventReservationRequest> {
    return this.http.post<EventReservationRequest>(`${this.API}/contracts/${contractId}/request`, request);
  }
}
