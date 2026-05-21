import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Bina, BinaRequest } from '../models/bina.model';

@Injectable({ providedIn: 'root' })
export class BinaService {
  private readonly http = inject(HttpClient);
  private readonly API = '/api/stages';

  getAll(): Observable<Bina[]> {
    return this.http.get<Bina[]>(this.API);
  }

  create(request: BinaRequest): Observable<Bina> {
    return this.http.post<Bina>(this.API, request);
  }

  update(id: number, request: BinaRequest): Observable<Bina> {
    return this.http.put<Bina>(`${this.API}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${id}`);
  }
}
