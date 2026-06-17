export interface CenovnaIstorija {
  cenovnaIstorijaId: number;
  ticketTypeId: number;
  pricingPeriodId: number;
  datum: string;
  staraCena: number;
  novaCena: number;
  razlog: string;
  jeRucnaPromena: boolean;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
