import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TransitionConditionRequest, TransitionConditionResponse } from '../models/workflow.model';
import { PageResponse } from './offer.service';

@Injectable({
  providedIn: 'root'
})
export class ConditionCatalogService {
  private readonly apiUrl = '/api/negotiation-manager/workflow-conditions';

  constructor(private http: HttpClient) {}

  createCatalogCondition(request: TransitionConditionRequest): Observable<TransitionConditionResponse> {
    return this.http.post<TransitionConditionResponse>(this.apiUrl, request);
  }

  getCatalogConditions(searchTerm?: string, page: number = 0, size: number = 10): Observable<PageResponse<TransitionConditionResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (searchTerm && searchTerm.trim()) {
      params = params.set('searchTerm', searchTerm.trim());
    }

    return this.http.get<PageResponse<TransitionConditionResponse>>(this.apiUrl, { params });
  }
}