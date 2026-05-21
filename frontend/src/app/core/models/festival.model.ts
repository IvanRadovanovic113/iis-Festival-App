export type FestivalStatus = 'AKTIVAN' | 'NEAKTIVAN' | 'NADOLAZECI' | 'ZAVRSEN' | 'OTKAZAN';

export const FESTIVAL_STATUS_LABELS: Record<FestivalStatus, string> = {
  AKTIVAN: 'Aktivan',
  NEAKTIVAN: 'Neaktivan',
  NADOLAZECI: 'Nadolazeći',
  ZAVRSEN: 'Završen',
  OTKAZAN: 'Otkazan'
};

export interface Festival {
  festivalId: number;
  naziv: string;
  lokacija: string;
  status: FestivalStatus;
  datumPocetka: string;
  datumZavrsetka: string;
}

export interface FestivalRequest {
  naziv: string;
  lokacija: string;
  status: FestivalStatus;
  datumPocetka: string;
  datumZavrsetka: string;
}
