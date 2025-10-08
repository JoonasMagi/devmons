import { api } from '../lib/api';
import type { LoginRequest, RegisterRequest, AuthResponse } from '../types/auth';

export const authService = {
  async login(data: LoginRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/auth/login', data);
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
    }
    return response.data;
  },

  async register(data: RegisterRequest): Promise<{ message: string }> {
    const response = await api.post('/auth/register', data);
    return response.data;
  },

  logout() {
    localStorage.removeItem('token');
    window.location.href = '/login';
  },

  getToken(): string | null {
    return localStorage.getItem('token');
  },

  isAuthenticated(): boolean {
    return !!this.getToken();
  },

  async requestPasswordReset(email: string): Promise<{ message: string }> {
    const response = await api.post('/auth/password-reset/request', { email });
    return response.data;
  },

  async confirmPasswordReset(token: string, newPassword: string): Promise<{ message: string }> {
    const response = await api.post('/auth/password-reset/confirm', { token, newPassword });
    return response.data;
  },
};

