import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TicketType, TicketTypeRequest } from '../models/ticket-type.model';

@Injectable({ providedIn: 'root' })
export class TicketTypeService {
  private readonly http = inject(HttpClient);

  getAll(festivalId: number): Observable<TicketType[]> {
    return this.http.get<TicketType[]>(`/api/festivals/${festivalId}/ticket-types`);
  }

  getById(id: number): Observable<TicketType> {
    return this.http.get<TicketType>(`/api/ticket-types/${id}`);
  }

  create(festivalId: number, request: TicketTypeRequest): Observable<TicketType> {
    return this.http.post<TicketType>(`/api/festivals/${festivalId}/ticket-types`, request);
  }

  update(id: number, request: TicketTypeRequest): Observable<TicketType> {
    return this.http.put<TicketType>(`/api/ticket-types/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`/api/ticket-types/${id}`);
  }

  toggleDynamicPricing(id: number, active: boolean): Observable<TicketType> {
    return this.http.patch<TicketType>(`/api/ticket-types/${id}/dynamic-pricing`, { active });
  }
}
