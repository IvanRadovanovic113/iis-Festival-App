import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PricingPeriod, PricingPeriodRequest } from '../models/pricing-period.model';

@Injectable({ providedIn: 'root' })
export class PricingPeriodService {
  private readonly http = inject(HttpClient);

  getAll(ticketTypeId: number): Observable<PricingPeriod[]> {
    return this.http.get<PricingPeriod[]>(`/api/ticket-types/${ticketTypeId}/price-periods`);
  }

  create(ticketTypeId: number, request: PricingPeriodRequest): Observable<PricingPeriod> {
    return this.http.post<PricingPeriod>(`/api/ticket-types/${ticketTypeId}/price-periods`, request);
  }

  update(ticketTypeId: number, periodId: number, request: PricingPeriodRequest): Observable<PricingPeriod> {
    return this.http.put<PricingPeriod>(`/api/ticket-types/${ticketTypeId}/price-periods/${periodId}`, request);
  }

  delete(ticketTypeId: number, periodId: number): Observable<void> {
    return this.http.delete<void>(`/api/ticket-types/${ticketTypeId}/price-periods/${periodId}`);
  }
}
