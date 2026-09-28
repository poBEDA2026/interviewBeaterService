import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';

vi.mock('@/auth/store', () => ({
  useAuthStore: { getState: vi.fn() },
  readRefresh: vi.fn(),
  writeRefresh: vi.fn(),
  clearRefresh: vi.fn(),
}));
vi.mock('@/api/auth', () => ({ refreshSession: vi.fn() }));
vi.mock('@/auth/refreshScheduler', () => ({ scheduleRefresh: vi.fn(() => () => undefined) }));
vi.mock('@/lib/jwt', () => ({ parseExp: vi.fn() }));

import { AuthBootstrapper } from '@/auth/AuthBootstrapper';
import { useAuthStore, readRefresh, writeRefresh } from '@/auth/store';
import { refreshSession } from '@/api/auth';
import { parseExp } from '@/lib/jwt';
import { scheduleRefresh } from '@/auth/refreshScheduler';

const mockedGetState = vi.mocked(useAuthStore.getState as unknown as ReturnType<typeof vi.fn>);
const mockedReadRefresh = vi.mocked(readRefresh);
const mockedWriteRefresh = vi.mocked(writeRefresh);
const mockedRefreshSession = vi.mocked(refreshSession);
const mockedParseExp = vi.mocked(parseExp);
const mockedSchedule = vi.mocked(scheduleRefresh);

describe('AuthBootstrapper', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows children immediately when no refresh token', async () => {
    mockedReadRefresh.mockReturnValue(null);
    mockedGetState.mockReturnValue({ accessToken: null, user: null, setSession: vi.fn(), setAccessToken: vi.fn(), clear: vi.fn() });

    render(<AuthBootstrapper><div>App</div></AuthBootstrapper>);

    await waitFor(() => expect(screen.getByText('App')).toBeInTheDocument());
    expect(mockedRefreshSession).not.toHaveBeenCalled();
  });

  it('restores session from refresh token and schedules refresh', async () => {
    mockedReadRefresh.mockReturnValue('REFRESH');
    mockedRefreshSession.mockResolvedValue({
      accessToken: 'NEW_ACCESS',
      refreshToken: 'NEW_REFRESH',
      userId: 1, email: 'a@b.c', role: 'USER',
    });
    mockedParseExp.mockReturnValue(1234567890);
    const setSession = vi.fn();
    mockedGetState.mockReturnValue({ accessToken: null, user: null, setSession, setAccessToken: vi.fn(), clear: vi.fn() });

    render(<AuthBootstrapper><div>App</div></AuthBootstrapper>);

    await waitFor(() => expect(screen.getByText('App')).toBeInTheDocument());
    expect(mockedWriteRefresh).toHaveBeenCalledWith('NEW_REFRESH');
    expect(setSession).toHaveBeenCalledWith({
      accessToken: 'NEW_ACCESS',
      user: { id: 1, email: 'a@b.c', role: 'USER' },
    });
    expect(mockedSchedule).toHaveBeenCalledWith(1234567890, expect.any(Function));
  });

  it('clears state on refresh failure', async () => {
    mockedReadRefresh.mockReturnValue('REFRESH');
    mockedRefreshSession.mockRejectedValue(new Error('boom'));
    const clear = vi.fn();
    mockedGetState.mockReturnValue({ accessToken: null, user: null, setSession: vi.fn(), setAccessToken: vi.fn(), clear });

    render(<AuthBootstrapper><div>App</div></AuthBootstrapper>);

    await waitFor(() => expect(screen.getByText('App')).toBeInTheDocument());
    expect(clear).toHaveBeenCalled();
    expect(mockedSchedule).not.toHaveBeenCalled();
  });
});
