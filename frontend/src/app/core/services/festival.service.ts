import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Festival, FestivalRequest } from '../models/festival.model';

@Injectable({ providedIn: 'root' })
export class FestivalService {
  private readonly http = inject(HttpClient);
  private readonly API = '/api/admin/festivals';

  getAll(): Observable<Festival[]> {
    return this.http.get<Festival[]>(this.API);
  }

  create(request: FestivalRequest): Observable<Festival> {
    return this.http.post<Festival>(this.API, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${id}`);
  }
}
