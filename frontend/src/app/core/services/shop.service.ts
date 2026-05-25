import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CheckoutPreview, PurchaseResult, ShopTicketType } from '../models/shop.model';

@Injectable({ providedIn: 'root' })
export class ShopService {
  private readonly http = inject(HttpClient);

  getAvailableTicketTypes(): Observable<ShopTicketType[]> {
    return this.http.get<ShopTicketType[]>('/api/shop/ticket-types');
  }

  preview(ticketTypeId: number, quantity: number, promoCode: string | null): Observable<CheckoutPreview> {
    return this.http.post<CheckoutPreview>('/api/shop/preview', {
      ticketTypeId,
      quantity,
      promoCode: promoCode || null
    });
  }

  purchase(ticketTypeId: number, quantity: number, promoCode: string | null): Observable<PurchaseResult> {
    return this.http.post<PurchaseResult>('/api/shop/purchase', {
      ticketTypeId,
      quantity,
      promoCode: promoCode || null
    });
  }

  getMyPurchases(): Observable<PurchaseResult[]> {
    return this.http.get<PurchaseResult[]>('/api/shop/my-purchases');
  }
}
