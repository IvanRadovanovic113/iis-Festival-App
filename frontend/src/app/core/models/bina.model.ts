export interface Bina {
  binaId: number;
  naziv: string;
  kapacitet: number;
  lokacija: string;
  festivalId: number;
  festivalNaziv: string;
}

export interface BinaRequest {
  naziv: string;
  kapacitet: number;
  lokacija: string;
}
