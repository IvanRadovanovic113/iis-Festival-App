import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TierConfig } from '../models/tier-config.model';

@Injectable({ providedIn: 'root' })
export class TierConfigService {
  private http = inject(HttpClient);

  getAll(): Observable<TierConfig[]> {
    return this.http.get<TierConfig[]>('/api/admin/tier-config');
  }

  update(tier: string, minTickets: number, discountPercent: number): Observable<TierConfig> {
    return this.http.put<TierConfig>(`/api/admin/tier-config/${tier}`, {
      minTickets,
      discountPercent
    });
  }
}
