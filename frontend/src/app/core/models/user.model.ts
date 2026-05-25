export interface AssignmentDto {
  festivalId: number;
  festivalName: string;
  festivalRole: string;
}

export interface BuyerDto {
  kupacId: number;
  ime: string;
  tier: 'STANDARD' | 'BRONZE' | 'SILVER' | 'GOLD';
  ukupnoKupovina: number;
}

export interface User {
  id: number;
  username: string;
  email: string;
  role: string | null;
  assignment: AssignmentDto | null;
  buyer: BuyerDto | null;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: User;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  customer?: boolean;
  fullName?: string;
}
