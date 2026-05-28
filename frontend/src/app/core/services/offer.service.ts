import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OfferDetailResponse, OfferRequest, OfferResponse, OfferStatus } from '../models/offer.model';

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class OfferService {
  private readonly apiUrl = '/api/negotiation-manager/offers';

  constructor(private http: HttpClient) {}

  createOffer(request: OfferRequest): Observable<OfferDetailResponse> {
    return this.http.post<OfferDetailResponse>(this.apiUrl, request);
  }

  updateOffer(offerId: number, request: OfferRequest): Observable<OfferDetailResponse> {
    return this.http.put<OfferDetailResponse>(`${this.apiUrl}/${offerId}`, request);
  }

  publishOffer(offerId: number): Observable<OfferDetailResponse> {
    return this.http.patch<OfferDetailResponse>(`${this.apiUrl}/${offerId}/publish`, {});
  }

  getOffers(status?: OfferStatus, page: number = 0, size: number = 10, searchTerm?: string): Observable<PageResponse<OfferResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (status) {
      params = params.set('status', status);
    }

    if (searchTerm && searchTerm.trim()) {
      params = params.set('searchTerm', searchTerm.trim());
    }

    return this.http.get<PageResponse<OfferResponse>>(this.apiUrl, { params });
  }

  getOfferById(offerId: number): Observable<OfferDetailResponse> {
    return this.http.get<OfferDetailResponse>(`${this.apiUrl}/${offerId}`);
  }

  archiveOffer(offerId: number): Observable<OfferDetailResponse> {
    return this.http.patch<OfferDetailResponse>(`${this.apiUrl}/${offerId}/archive`, {});
  }

  addInterestedPerformer(offerId: number, performerId: number): Observable<void> {
    return this.http.post<void>(`/api/negotiation-manager/offers/${offerId}/interested-performers/${performerId}`, {});
  }

  removeInterestedPerformer(offerId: number, performerId: number): Observable<void> {
    return this.http.delete<void>(`/api/negotiation-manager/offers/${offerId}/interested-performers/${performerId}`);
  }
}