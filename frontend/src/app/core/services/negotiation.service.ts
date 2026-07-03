import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { NegotiationResponse, NegotiationDetailsResponse, PerformerStatsDto, ConditionValueDto, StatePerformance, NegotiationEfficiency, AnalyticsTrend, OfferOutcome, CriticalNegotiationDto } from '../models/negotiation.model';

@Injectable({ providedIn: 'root' })
export class NegotiationService {
  private apiUrl = '/api/negotiation-manager/negotiations';
  private reportUrl = '/api/negotiation-manager/reports';

  constructor(private http: HttpClient) {}

  getAllNegotiations(): Observable<NegotiationResponse[]> {
    return this.http.get<NegotiationResponse[]>(this.apiUrl);
  }

  getNegotiationDetails(id: number): Observable<NegotiationDetailsResponse> {
    return this.http.get<NegotiationDetailsResponse>(`${this.apiUrl}/${id}`);
  }

  saveNegotiationConditions(negotiationId: number, conditions: ConditionValueDto[]): Observable<void> {
    return this.http.post<void>(`/api/negotiation-manager/negotiations/${negotiationId}/conditions`, conditions);
  }

  performTransition(negotiationId: number, transitionId: number): Observable<void> {
    return this.http.post<void>(`/api/negotiation-manager/negotiations/${negotiationId}/transition/${transitionId}`, {});
  }

  startNegotiation(offerId: number, performerId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${offerId}/start/${performerId}`, {});
  }

  completeNegotiation(id: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${id}/complete`, {});
  }

  failNegotiation(id: number, reason: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${id}/fail`, { reason });
  }

  getPerformerStats(genre?: string, type?: string): Observable<PerformerStatsDto[]> {
    return this.http.get<PerformerStatsDto[]>(`${this.reportUrl}/performer-performance`, {
      params: { ...(genre && { genre }), ...(type && { type }) }
    });
  }

  getBottleneckReport(templateId?: number): Observable<StatePerformance[]> {
    let params = new HttpParams();
    if (templateId !== undefined && templateId !== null) {
      params = params.set('templateId', templateId.toString());
    }
    return this.http.get<StatePerformance[]>(`${this.reportUrl}/bottlenecks`, { params });
  }

  getNegotiationEfficiency(startDate: string, endDate: string): Observable<NegotiationEfficiency> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<NegotiationEfficiency>(`${this.reportUrl}/efficiency`, { params });
  }

  getNegotiationDurationTrend(startDate: string, endDate: string, interval: string = 'YYYY-MM'): Observable<AnalyticsTrend[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate)
      .set('interval', interval);
    return this.http.get<AnalyticsTrend[]>(`${this.reportUrl}/negotiation-duration-trend`, { params });
  }

  getOfferOutcomes(startDate: string, endDate: string): Observable<OfferOutcome[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<OfferOutcome[]>(`${this.reportUrl}/offer-outcomes`, { params });
  }

  getOfferDurationTrend(startDate: string, endDate: string, interval: string = 'YYYY-MM'): Observable<AnalyticsTrend[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate)
      .set('interval', interval);
    return this.http.get<AnalyticsTrend[]>(`${this.reportUrl}/offer-duration-trend`, { params });
  }

  getCriticalAlerts(): Observable<CriticalNegotiationDto[]> {
    return this.http.get<CriticalNegotiationDto[]>(`${this.reportUrl}/critical-alerts`);
  }
}