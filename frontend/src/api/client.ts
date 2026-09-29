import axios, { AxiosError, AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import { useAuthStore, readRefresh, writeRefresh, clearRefresh } from '@/auth/store';
import { refreshSession, LoginResponse } from './auth';

export const api: AxiosInstance = axios.create({
  baseURL: '',
  headers: { 'Content-Type': 'application/json' },
});

// ---- request: attach Authorization ----
api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`);
  }
  return config;
});

// ---- response: 401 → refresh + retry ----
let refreshInFlight: Promise<LoginResponse | null> | null = null;

async function performRefresh(): Promise<LoginResponse | null> {
  const refresh = readRefresh();
  if (!refresh) return null;

  try {
    const res = await refreshSession(refresh);
    writeRefresh(res.refreshToken);
    useAuthStore.getState().setAccessToken(res.accessToken);
    useAuthStore.getState().setSession({
      accessToken: res.accessToken,
      user: { id: res.userId, email: res.email, role: res.role },
    });
    return res;
  } catch (e) {
    return null;
  }
}

export async function attemptRefresh(): Promise<boolean> {
  if (!refreshInFlight) {
    refreshInFlight = performRefresh().finally(() => {
      refreshInFlight = null;
    });
  }
  const res = await refreshInFlight;
  return res !== null;
}

export function logoutAndRedirect(): void {
  clearRefresh();
  useAuthStore.getState().clear();
  // гард: если уже на /login — не петлим
  if (!window.location.pathname.startsWith('/login')) {
    window.location.assign('/login');
  }
}

api.interceptors.response.use(
  (r) => r,
  async (error: AxiosError) => {
    const original = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined;
    if (error.response?.status !== 401 || !original || original._retry) {
      return Promise.reject(error);
    }
    original._retry = true;

    const ok = await attemptRefresh();
    if (!ok) {
      logoutAndRedirect();
      return Promise.reject(error);
    }

    // обновить Authorization на повторе
    const newToken = useAuthStore.getState().accessToken;
    if (newToken) {
      original.headers.set('Authorization', `Bearer ${newToken}`);
    }
    return api(original);
  },
);
