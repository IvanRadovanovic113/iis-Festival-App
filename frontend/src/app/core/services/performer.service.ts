import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse, PerformerRequest, PerformerResponse } from '../models/performer.model';

@Injectable({
  providedIn: 'root'
})
export class PerformerService {
  private baseUrl = '/api/negotiation-manager/performers';

  constructor(private http: HttpClient) {}

  getPerformers(filters: any, page: number = 0, size: number = 10): Observable<PageResponse<PerformerResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    Object.keys(filters).forEach(key => {
      if (filters[key] !== null && filters[key] !== undefined && filters[key] !== '') {
        params = params.set(key, filters[key]);
      }
    });

    return this.http.get<PageResponse<PerformerResponse>>(this.baseUrl, { params });
  }

  getPerformerById(id: number): Observable<PerformerResponse> {
    return this.http.get<PerformerResponse>(`${this.baseUrl}/${id}`);
  }

  createPerformer(request: PerformerRequest): Observable<PerformerResponse> {
    return this.http.post<PerformerResponse>(this.baseUrl, request);
  }

  updatePerformer(id: number, request: PerformerRequest): Observable<PerformerResponse> {
    return this.http.put<PerformerResponse>(`${this.baseUrl}/${id}`, request);
  }

  archivePerformer(id: number): Observable<PerformerResponse> {
    return this.http.patch<PerformerResponse>(`${this.baseUrl}/${id}/archive`, {});
  }
}