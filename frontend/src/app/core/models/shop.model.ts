export interface ShopBundleInfo {
  kupiKarata: number;
  dobijaKarata: number;
}

export interface ShopTicketType {
  ticketTypeId: number;
  name: string;
  festivalId: number;
  festivalName: string;
  totalQuantity: number;
  soldCount: number;
  available: number;
  currentPrice: number | null;
  activeBundles: ShopBundleInfo[];
}

export interface CheckoutPreview {
  ticketTypeId: number;
  ticketTypeName: string;
  pricePerTicket: number;
  quantityPaid: number;
  baseTotal: number;
  promoCodeApplied: string | null;
  promoDiscountPercent: number;
  tierName: string | null;
  tierDiscountPercent: number;
  totalDiscountPercent: number;
  freeTickets: number;
  bundleDealDescription: string | null;
  totalTickets: number;
  finalPrice: number;
  availableCount: number;
}

export interface KartaDto {
  kartaId: number;
  qrKod: string;
  status: string;
}

export interface PurchaseResult {
  kupovinaId: number;
  datum: string;
  festivalName: string;
  ticketTypeName: string;
  totalTickets: number;
  finalPrice: number;
  karte: KartaDto[];
}
