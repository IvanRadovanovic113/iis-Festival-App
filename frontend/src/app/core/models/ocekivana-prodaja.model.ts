export interface OcekivanaProdaja {
  ocekivanaProdajaId: number;
  pricingPeriodId: number;
  brojKarata: number;
  brojSati: number;
  agresivnost: number;
  intervalMinuti: number;
  scarcityPragNizak: number;
  scarcityPragVisok: number;
  scarcityMultiplikatorNizak: number;
  scarcityMultiplikatorVisok: number;
}

export interface OcekivanaProdajaRequest {
  brojKarata: number;
  brojSati: number;
  agresivnost: number;
  intervalMinuti: number;
  scarcityPragNizak: number;
  scarcityPragVisok: number;
  scarcityMultiplikatorNizak: number;
  scarcityMultiplikatorVisok: number;
}
