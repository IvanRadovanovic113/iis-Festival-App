import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  RevenueDataPoint, SalesByTicketType, SalesOverview,
  TicketTypePriceHistoryItem, TicketTypeSalesByPeriodItem, TicketTypeSalesDataPoint,
  PromoCodeStats, ActionsAnalytics, DynamicPricingAnalytics
} from '../models/analytics.model';

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private readonly http = inject(HttpClient);

  getOverview(festivalId: number): Observable<SalesOverview> {
    return this.http.get<SalesOverview>(`/api/festivals/${festivalId}/analytics/overview`);
  }

  getRevenueOverTime(festivalId: number): Observable<RevenueDataPoint[]> {
    return this.http.get<RevenueDataPoint[]>(`/api/festivals/${festivalId}/analytics/revenue-over-time`);
  }

  getSalesByTicketType(festivalId: number): Observable<SalesByTicketType[]> {
    return this.http.get<SalesByTicketType[]>(`/api/festivals/${festivalId}/analytics/sales-by-ticket-type`);
  }

  getSalesOverTime(ticketTypeId: number, from: string, to: string): Observable<TicketTypeSalesDataPoint[]> {
    return this.http.get<TicketTypeSalesDataPoint[]>(
      `/api/ticket-types/${ticketTypeId}/analytics/sales-over-time`,
      { params: { from, to } }
    );
  }

  getPriceHistory(ticketTypeId: number): Observable<TicketTypePriceHistoryItem[]> {
    return this.http.get<TicketTypePriceHistoryItem[]>(
      `/api/ticket-types/${ticketTypeId}/analytics/price-history`
    );
  }

  getSalesByPeriod(ticketTypeId: number): Observable<TicketTypeSalesByPeriodItem[]> {
    return this.http.get<TicketTypeSalesByPeriodItem[]>(
      `/api/ticket-types/${ticketTypeId}/analytics/sales-by-period`
    );
  }

  getPromoCodeStats(festivalId: number): Observable<PromoCodeStats[]> {
    return this.http.get<PromoCodeStats[]>(`/api/festivals/${festivalId}/analytics/promo-codes`);
  }

  getActionsAnalytics(festivalId: number): Observable<ActionsAnalytics> {
    return this.http.get<ActionsAnalytics>(`/api/festivals/${festivalId}/analytics/actions`);
  }

  getDynamicPricingAnalytics(festivalId: number): Observable<DynamicPricingAnalytics> {
    return this.http.get<DynamicPricingAnalytics>(`/api/festivals/${festivalId}/analytics/dynamic-pricing`);
  }
}
