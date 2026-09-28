import { create } from 'zustand';

export interface AuthUser {
  id: number;
  email: string;
  role: string;
}

interface AuthState {
  accessToken: string | null;
  user: AuthUser | null;
  setSession: (input: { accessToken: string; user: AuthUser }) => void;
  setAccessToken: (token: string) => void;
  clear: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  user: null,
  setSession: ({ accessToken, user }) => set({ accessToken, user }),
  setAccessToken: (accessToken) => set({ accessToken }),
  clear: () => set({ accessToken: null, user: null }),
}));

export const REFRESH_KEY = 'auth_refresh';

export function readRefresh(): string | null {
  return localStorage.getItem(REFRESH_KEY);
}

export function writeRefresh(token: string): void {
  localStorage.setItem(REFRESH_KEY, token);
}

export function clearRefresh(): void {
  localStorage.removeItem(REFRESH_KEY);
}
