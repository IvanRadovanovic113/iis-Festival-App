export enum ConditionDataType {
  NUMBER = 'NUMBER',
  DATE = 'DATE',
  TIME = 'TIME',
  TEXT = 'TEXT',
  BOOLEAN = 'BOOLEAN'
}

// Globalni katalog uslova (Šabloni pravila)
export interface TransitionConditionRequest {
  conditionKey: string;
  label: string;
  dataType: ConditionDataType;
  necessary: boolean;
}

export interface TransitionConditionResponse {
  id: number;
  conditionKey: string;
  label: string;
  dataType: ConditionDataType;
  necessary: boolean;
}

// Stanja (Čvorovi grafa)
export interface WorkflowStateRequest {
  name: string;
  initial: boolean;
  finalState: boolean;
  defaultDeadlineDays: number;
}

export interface WorkflowStateResponse {
  id: number;
  name: string;
  initial: boolean;
  finalState: boolean;
  defaultDeadlineDays: number;
  currentNegotiationsCount: number;
}

// Tranzicije (Grane grafa)
export interface WorkflowTransitionRequest {
  label: string;
  sourceStateName: string;
  targetStateName: string;
  conditionIds: number[];
}

export interface WorkflowTransitionResponse {
  id: number;
  label: string;
  sourceStateId: number;
  targetStateId: number;
  conditions: TransitionConditionResponse[];
}

// Šabloni Radnog Toka
export interface WorkflowTemplateRequest {
  name: string;
  states: WorkflowStateRequest[];
  transitions: WorkflowTransitionRequest[];
}

export interface WorkflowTemplateResponse {
  id: number;
  name: string;
  archived: boolean;
  createdAt: string;
  copiedFromId?: number;
  statesCount: number;
  activeNegotiationsCount: number;
}

export interface WorkflowTemplateDetailResponse extends WorkflowTemplateResponse {
  states: WorkflowStateResponse[];
  transitions: WorkflowTransitionResponse[];
}