export type PromoCodeStatus = 'ACTIVE' | 'INACTIVE' | 'EXPIRED' | 'EXHAUSTED';

export interface PromoCode {
  promoCodeId: number;
  code: string;
  discountPercent: number;
  validFrom: string;
  validTo: string;
  maxUses: number | null;
  usedCount: number;
  active: boolean;
  festivalId: number;
  status: PromoCodeStatus;
}

export interface PromoCodeRequest {
  code: string;
  discountPercent: number;
  validFrom: string;
  validTo: string;
  maxUses: number | null;
}
