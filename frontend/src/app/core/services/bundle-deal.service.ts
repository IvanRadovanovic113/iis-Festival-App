import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BundleDeal, BundleDealRequest } from '../models/bundle-deal.model';

@Injectable({ providedIn: 'root' })
export class BundleDealService {
  private readonly http = inject(HttpClient);

  getAll(festivalId: number): Observable<BundleDeal[]> {
    return this.http.get<BundleDeal[]>(`/api/festivals/${festivalId}/bundle-deals`);
  }

  create(festivalId: number, request: BundleDealRequest): Observable<BundleDeal> {
    return this.http.post<BundleDeal>(`/api/festivals/${festivalId}/bundle-deals`, request);
  }

  update(id: number, request: BundleDealRequest): Observable<BundleDeal> {
    return this.http.put<BundleDeal>(`/api/bundle-deals/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`/api/bundle-deals/${id}`);
  }

  toggleActive(id: number): Observable<BundleDeal> {
    return this.http.patch<BundleDeal>(`/api/bundle-deals/${id}/toggle-active`, {});
  }
}
