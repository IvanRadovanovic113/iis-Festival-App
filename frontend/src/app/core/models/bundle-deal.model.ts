export type BundleDealStatus = 'ACTIVE' | 'PAUSED' | 'EXPIRED' | 'EXHAUSTED';

export interface BundleDeal {
  akcijaId: number;
  kupiKarata: number;
  dobijaKarata: number;
  vaziOd: string;
  vaziDo: string;
  dostupnoAkcija: number;
  usedCount: number;
  active: boolean;
  ticketTypeId: number;
  ticketTypeName: string;
  festivalId: number;
  status: BundleDealStatus;
}

export interface BundleDealRequest {
  ticketTypeId: number;
  kupiKarata: number;
  dobijaKarata: number;
  vaziOd: string;
  vaziDo: string;
  dostupnoAkcija: number;
}
