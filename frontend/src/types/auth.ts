export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  fullName: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  email: string;
  fullName: string;
}

export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
}

