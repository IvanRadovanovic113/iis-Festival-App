export interface TierConfig {
  tier: 'BRONZE' | 'SILVER' | 'GOLD';
  minTickets: number;
  discountPercent: number;
}
