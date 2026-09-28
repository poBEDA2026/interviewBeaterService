import { describe, it, expect, vi, beforeEach } from 'vitest';

// Mock store + scheduler + api/auth до импорта client
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

vi.mock('@/api/auth', () => ({
  refreshSession: vi.fn(),
  LoginResponse: class {},
}));

import { api } from '@/api/client';
import { useAuthStore, readRefresh, writeRefresh, clearRefresh } from '@/auth/store';
import { refreshSession } from '@/api/auth';

const mockedUseAuthStore = vi.mocked(useAuthStore);
const mockedGetState = mockedUseAuthStore.getState as unknown as ReturnType<typeof vi.fn>;

// Axios v1 wraps adapter-rejected errors that lack `isAxiosError` into a fresh AxiosError,
// stripping the manual `.response` we set in tests. Tagging the error keeps it intact.
// Also, real AxiosErrors carry `error.config = original request config` (the response also
// has .config, but our interceptor reads error.config — tests must mirror both).
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
const mockedReadRefresh = vi.mocked(readRefresh);
const mockedWriteRefresh = vi.mocked(writeRefresh);
const mockedClearRefresh = vi.mocked(clearRefresh);
const mockedRefreshSession = vi.mocked(refreshSession);

beforeEach(() => {
  vi.clearAllMocks();
  // дефолт: пустой access, нет refresh
  mockedGetState.mockReturnValue({ accessToken: null, user: null, setSession: vi.fn(), setAccessToken: vi.fn(), clear: vi.fn() });
  mockedReadRefresh.mockReturnValue(null);
});

describe('axios 401 interceptor', () => {
  it('attaches Authorization header when accessToken present', async () => {
    mockedGetState.mockReturnValue({
      accessToken: 'ACCESS',
      user: null,
      setSession: vi.fn(),
      setAccessToken: vi.fn(),
      clear: vi.fn(),
    });

    const adapter = vi.fn().mockResolvedValue({ status: 200, data: 'ok', headers: {}, config: { url: '/x' } });
    (api.defaults.adapter as unknown) = adapter;

    await api.get('/x');
    expect(adapter.mock.calls[0][0].headers.Authorization).toBe('Bearer ACCESS');
  });

  it('on 401 refreshes and retries original request', async () => {
    mockedReadRefresh.mockReturnValue('REFRESH');
    mockedRefreshSession.mockResolvedValue({
      accessToken: 'NEW_ACCESS',
      refreshToken: 'NEW_REFRESH',
      userId: 1, email: 'a@b.c', role: 'USER',
    });
    const setAccessToken = vi.fn();
    mockedGetState.mockReturnValue({
      accessToken: 'OLD_ACCESS',
      user: null,
      setSession: vi.fn(),
      setAccessToken,
      clear: vi.fn(),
    });

    let call = 0;
    const adapter = vi.fn().mockImplementation((config) => {
      call += 1;
      if (config.url === '/x' && call === 1) {
        return Promise.reject(makeAxiosError(401, config));
      }
      return Promise.resolve({ status: 200, data: 'ok', headers: {}, config });
    });
    (api.defaults.adapter as unknown) = adapter;

    await api.get('/x');

    expect(mockedRefreshSession).toHaveBeenCalledWith('REFRESH');
    expect(mockedWriteRefresh).toHaveBeenCalledWith('NEW_REFRESH');
    expect(setAccessToken).toHaveBeenCalledWith('NEW_ACCESS');
    expect(adapter).toHaveBeenCalledTimes(2); // retry произошёл
  });

  it('on refresh failure clears state and rejects', async () => {
    mockedReadRefresh.mockReturnValue('REFRESH');
    mockedRefreshSession.mockRejectedValue(new Error('refresh exploded'));
    const clear = vi.fn();
    mockedGetState.mockReturnValue({
      accessToken: 'OLD', user: null,
      setSession: vi.fn(), setAccessToken: vi.fn(), clear,
    });
    // jsdom не реализует location.assign и запрещает его подмену. Подменяем весь location объект
    // через defineProperty window.location с геттером, возвращающим прокси.
    const origLocation = window.location;
    const fakeLocation = new Proxy(origLocation, {
      get(target, prop) {
        if (prop === 'assign') return () => undefined;
        // @ts-expect-error динамический prop-доступ к Location
        return target[prop];
      },
    });
    Object.defineProperty(window, 'location', { configurable: true, get: () => fakeLocation });

    const adapter = vi.fn().mockImplementation((config) => {
      return Promise.reject(makeAxiosError(401, config));
    });
    (api.defaults.adapter as unknown) = adapter;

    await expect(api.get('/x')).rejects.toThrow();
    expect(mockedClearRefresh).toHaveBeenCalled();
    expect(clear).toHaveBeenCalled();
    Object.defineProperty(window, 'location', { configurable: true, value: origLocation });
  });

  it('concurrent 401s share one refresh call', async () => {
    mockedReadRefresh.mockReturnValue('REFRESH');
    mockedRefreshSession.mockResolvedValue({
      accessToken: 'NEW',
      refreshToken: 'NEW_R',
      userId: 1, email: 'a@b.c', role: 'USER',
    });
    mockedGetState.mockReturnValue({
      accessToken: 'OLD', user: null,
      setSession: vi.fn(), setAccessToken: vi.fn(), clear: vi.fn(),
    });

    const adapter = vi.fn().mockImplementation((config) => {
      return Promise.reject(makeAxiosError(401, config));
    });
    (api.defaults.adapter as unknown) = adapter;

    await Promise.all([api.get('/a').catch(() => undefined), api.get('/b').catch(() => undefined)]);

    // Один refresh-вызов на оба запроса
    expect(mockedRefreshSession).toHaveBeenCalledTimes(1);
    // Оба запроса ретрайнуты (после первого 401 каждый)
    expect(adapter).toHaveBeenCalledTimes(4); // 2 исходных + 2 retry
  });
});
