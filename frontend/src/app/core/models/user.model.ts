export interface AssignmentDto {
  festivalId: number;
  festivalName: string;
  festivalRole: string;
}

export interface User {
  id: number;
  username: string;
  email: string;
  role: string | null;
  assignment: AssignmentDto | null;
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
}
