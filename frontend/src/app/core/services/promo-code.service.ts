import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PromoCode, PromoCodeRequest } from '../models/promo-code.model';

@Injectable({ providedIn: 'root' })
export class PromoCodeService {
  private readonly http = inject(HttpClient);

  getAll(festivalId: number): Observable<PromoCode[]> {
    return this.http.get<PromoCode[]>(`/api/festivals/${festivalId}/promo-codes`);
  }

  create(festivalId: number, request: PromoCodeRequest): Observable<PromoCode> {
    return this.http.post<PromoCode>(`/api/festivals/${festivalId}/promo-codes`, request);
  }

  update(id: number, request: PromoCodeRequest): Observable<PromoCode> {
    return this.http.put<PromoCode>(`/api/promo-codes/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`/api/promo-codes/${id}`);
  }

  toggleActive(id: number): Observable<PromoCode> {
    return this.http.patch<PromoCode>(`/api/promo-codes/${id}/toggle-active`, {});
  }
}
