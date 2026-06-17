export interface NegotiationResponse {
  id: number;
  performerName: string;
  offerTitle: string;
  currentStateName: string;
  status: 'ACTIVE' | 'CONCLUDED' | 'FAILED';
  startedAt: Date;
}

export interface NegotiationDetailsResponse {
  id: number;
  performerName: string;
  status: string;
  currentState: string;
  offerTitle: string;
  lastUpdated: Date | string;
  history: any[];
  enteredConditions: ConditionValueDto[];
  offerId: number;
  availableTransitions: TransitionDto[];
}

export interface TransitionDto {
    id: number;
    label: string;
    requiredConditions: TransitionConditionResponse[];
}

export interface TransitionConditionResponse {
    id: number;
    label: string;
    dataType: string;
    necessary: boolean;
}

export interface ConditionValueDto {
  conditionId: number;
  value: string;
  conditionLabel?: string;
}

export interface TransitionRequest {
  transitionId: number;
  conditionValues: ConditionValueDto[];
}

export interface NegotiationStateHistoryDto {
  stateName: string;
  entryTime: Date;
  exitTime?: Date;
  duration?: string;
}