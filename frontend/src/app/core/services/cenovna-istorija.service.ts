import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CenovnaIstorija, PageResponse } from '../models/cenovna-istorija.model';

@Injectable({ providedIn: 'root' })
export class CenovnaIstorijaService {
  private readonly http = inject(HttpClient);

  getHistory(ticketTypeId: number, page = 0, size = 20): Observable<PageResponse<CenovnaIstorija>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<CenovnaIstorija>>(
      `/api/ticket-types/${ticketTypeId}/price-history`, { params }
    );
  }
}
