import { api } from './client';

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  userId: number;
  email: string;
  fullName: string | null;
  role: string;
}

export interface RegisterResponse {
  id: number;
}

export async function login(email: string, password: string): Promise<LoginResponse> {
  const { data } = await api.post<LoginResponse>('/auth/login', { email, password });
  return data;
}

export async function signup(email: string, password: string, fullName?: string): Promise<RegisterResponse> {
  const { data } = await api.post<RegisterResponse>('/auth/signup', {
    email,
    password,
    fullName: fullName?.trim() || undefined,
  });
  return data;
}

export async function refreshSession(refreshToken: string): Promise<LoginResponse> {
  const { data } = await api.post<LoginResponse>('/auth/refresh', { refreshToken });
  return data;
}
