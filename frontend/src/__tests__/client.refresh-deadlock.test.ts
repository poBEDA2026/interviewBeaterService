import { describe, it, expect, vi, beforeEach } from 'vitest';

// Отличие от client.interceptor.test.ts: @/api/auth НЕ мокаем —
// refresh-запрос должен пройти через реальный api-инстанс и его интерсептор,
// как это происходит в браузере, когда бэкенд отвергает протухший refresh-токен.

vi.mock('@/auth/store', () => ({
  useAuthStore: {
    getState: vi.fn(),
  },
  REFRESH_KEY: 'auth_refresh',
  readRefresh: vi.fn(),
  writeRefresh: vi.fn(),
  clearRefresh: vi.fn(),
}));

vi.mock('@/auth/refreshScheduler', () => ({
  scheduleRefresh: vi.fn(() => () => undefined),
}));

import { api } from '@/api/client';
import { refreshSession } from '@/api/auth';
import { useAuthStore, readRefresh } from '@/auth/store';

const mockedGetState = vi.mocked(useAuthStore.getState as unknown as ReturnType<typeof vi.fn>);
const mockedReadRefresh = vi.mocked(readRefresh);

// Axios v1 оборачивает ошибки адаптера без isAxiosError в новый AxiosError,
// затирая вручную выставленный .response. Метка сохраняет ошибку как есть.
// Кроме того, error.config должен указывать на конфиг исходного запроса —
// интерсептор читает именно его.
const makeAxiosError = (status: number, config: any) => {
  const err = new Error(`${status}`) as Error & {
    isAxiosError: true;
    config: typeof config;
    response: { status: number; data: unknown; headers: Record<string, unknown>; config: typeof config };
  };
  err.isAxiosError = true;
  err.config = config;
  err.response = { status, data: null, headers: {}, config };
  return err;
};

beforeEach(() => {
  vi.clearAllMocks();
  mockedGetState.mockReturnValue({
    accessToken: null,
    user: null,
    setSession: vi.fn(),
    setAccessToken: vi.fn(),
    clear: vi.fn(),
  });
});

describe('refresh при невалидном refresh-токене', () => {
  it('refreshSession не должен зависать, когда /auth/refresh отвечает 401', async () => {
    // Сценарий из браузера: в localStorage лежит старый refresh-токен,
    // который бэкенд больше не признаёт (например, после перехода на Spring Security).
    mockedReadRefresh.mockReturnValue('STALE_REFRESH');

    const adapter = vi.fn().mockImplementation((config) =>
      Promise.reject(makeAxiosError(401, config)),
    );
    (api.defaults.adapter as unknown) = adapter;

    // С дедлоком промис никогда не завершится — ловим это таймаутом.
    const HANG = Symbol('hang');
    const outcome = await Promise.race([
      refreshSession('STALE_REFRESH').then(
        () => 'RESOLVED' as const,
        () => 'REJECTED' as const,
      ),
      new Promise<typeof HANG>((resolve) => setTimeout(() => resolve(HANG), 1000)),
    ]);

    expect(outcome).toBe('REJECTED');
  });
});
