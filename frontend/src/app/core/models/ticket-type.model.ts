export interface SegmentInfo {
  segmentId: number;
  name: string;
}

export interface TicketType {
  ticketTypeId: number;
  name: string;
  totalQuantity: number;
  soldCount: number;
  dynamicPricingActive: boolean;
  segments: SegmentInfo[];
  festivalId: number;
  festivalName: string;
  currentPrice?: number | null;
}

export interface TicketTypeRequest {
  name: string;
  totalQuantity: number;
  segmentIds: number[];
}
