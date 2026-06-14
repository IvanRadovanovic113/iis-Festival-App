import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OcekivanaProdaja, OcekivanaProdajaRequest } from '../models/ocekivana-prodaja.model';

@Injectable({ providedIn: 'root' })
export class OcekivanaProdajaService {
  private readonly http = inject(HttpClient);

  get(periodId: number): Observable<OcekivanaProdaja> {
    return this.http.get<OcekivanaProdaja>(`/api/price-periods/${periodId}/expected-sales`);
  }

  upsert(periodId: number, request: OcekivanaProdajaRequest): Observable<OcekivanaProdaja> {
    return this.http.post<OcekivanaProdaja>(`/api/price-periods/${periodId}/expected-sales`, request);
  }
}
