import { OcekivanaProdaja } from './ocekivana-prodaja.model';

export interface PricingPeriod {
  pricingPeriodId: number;
  startDate: string;
  endDate: string;
  basePrice: number;
  minPrice: number;
  currentPrice: number;
  dynamicPricingActive: boolean;
  ticketTypeId: number;
  ocekivanaProdaja?: OcekivanaProdaja | null;
}

export interface PricingPeriodRequest {
  startDate: string;
  endDate: string;
  basePrice: number;
  minPrice: number;
  dynamicPricingActive: boolean;
}
