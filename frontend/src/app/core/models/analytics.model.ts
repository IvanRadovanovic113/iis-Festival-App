export interface TicketTypeSummary {
  ticketTypeId: number;
  name: string;
  soldCount: number;
  totalQuantity: number;
  currentPrice: number | null;
  revenue: number;
  performanceStatus: 'WARNING' | 'HIGHLIGHT' | 'NORMAL';
}

export interface SalesOverview {
  festivalName: string;
  totalRevenue: number;
  totalTicketsSold: number;
  totalTicketsAvailable: number;
  activeTicketTypes: number;
  ticketTypes: TicketTypeSummary[];
  salesWarning: boolean;
  warningMessages: string[];
  salesHighlight: boolean;
  highlightMessages: string[];
}

export interface RevenueDataPoint {
  date: string;
  revenue: number;
  ticketsSold: number;
}

export interface SalesByTicketType {
  ticketTypeId: number;
  name: string;
  soldCount: number;
  revenue: number;
  totalQuantity: number;
}

export interface TicketTypeSalesDataPoint {
  date: string;
  ticketsSold: number;
  revenue: number;
}

export interface TicketTypePriceHistoryItem {
  datum: string;
  oldPrice: number;
  newPrice: number;
  manual: boolean;
  reason: string;
}

export interface TicketTypeSalesByPeriodItem {
  periodId: number;
  startDate: string;
  endDate: string;
  basePrice: number;
  currentPrice: number | null;
  ticketsSold: number;
  revenue: number;
}

export interface PromoCodeStats {
  promoCodeId: number;
  code: string;
  discountPercent: number;
  active: boolean;
  validFrom: string;
  validTo: string;
  usedCount: number;
  maxUses: number | null;
  totalRevenue: number;
  estimatedValueLost: number;
}

export interface BundleDealStats {
  akcijaId: number;
  ticketTypeName: string;
  kupiKarata: number;
  dobijaKarata: number;
  vaziOd: string;
  vaziDo: string;
  active: boolean;
  applicationsCount: number;
  totalFreeTickets: number;
  totalRevenue: number;
  estimatedValueLost: number;
}

export interface DailyBundleImpact {
  date: string;
  purchasesCount: number;
  revenue: number;
}

export interface ActionsAnalytics {
  bundleDeals: BundleDealStats[];
  dailyImpact: DailyBundleImpact[];
}

export interface DailyPriceSales {
  date: string;
  ticketsSold: number;
  price: number;
}

export interface DynamicPricingTicketType {
  ticketTypeId: number;
  name: string;
  dynamicPricingActive: boolean;
  totalTicketsSold: number;
  baselineRevenue: number;
  revenuePremium: number;
  dailyData: DailyPriceSales[];
}

export interface DynamicPricingAnalytics {
  totalRevenuePremium: number;
  totalBaselineRevenue: number;
  ticketTypes: DynamicPricingTicketType[];
}
