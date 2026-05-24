export interface PricingPeriod {
  pricingPeriodId: number;
  startDate: string;
  endDate: string;
  basePrice: number;
  minPrice: number;
  dynamicPricingActive: boolean;
  ticketTypeId: number;
}

export interface PricingPeriodRequest {
  startDate: string;
  endDate: string;
  basePrice: number;
  minPrice: number;
  dynamicPricingActive: boolean;
}
