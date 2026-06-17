import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { NegotiationResponse, NegotiationDetailsResponse, TransitionRequest, ConditionValueDto } from '../models/negotiation.model';

@Injectable({ providedIn: 'root' })
export class NegotiationService {
  private apiUrl = '/api/negotiation-manager/negotiations';

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
}