import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ResourceAnalytics } from '../models/event-organization.model';

@Injectable({ providedIn: 'root' })
export class ResourceAnalyticsService {
  private readonly http = inject(HttpClient);
  private readonly API = '/api/event-organization/analytics';

  getAnalytics(year: number | null, month: number | null, stageId: number | null): Observable<ResourceAnalytics> {
    let params = new HttpParams();
    if (year !== null) params = params.set('year', year);
    if (month !== null) params = params.set('month', month);
    if (stageId !== null) params = params.set('stageId', stageId);
    return this.http.get<ResourceAnalytics>(this.API, { params });
  }

  downloadPdf(year: number | null, month: number | null, stageId: number | null, stageName: string | null): Observable<Blob> {
    let params = new HttpParams();
    if (year !== null) params = params.set('year', year);
    if (month !== null) params = params.set('month', month);
    if (stageId !== null) params = params.set('stageId', stageId);
    if (stageName !== null) params = params.set('stageName', stageName);
    return this.http.get(this.API + '/pdf', { params, responseType: 'blob' });
  }
}
