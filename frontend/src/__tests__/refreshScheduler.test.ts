import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { scheduleRefresh } from '@/auth/refreshScheduler';

beforeEach(() => vi.useFakeTimers());
afterEach(() => vi.useRealTimers());

describe('scheduleRefresh', () => {
  it('fires onExpire 60s before exp', () => {
    const cb = vi.fn();
    const nowSec = 1_000_000;
    vi.setSystemTime(nowSec * 1000);

    scheduleRefresh(nowSec + 600, cb); // exp через 600 сек, refresh за 60 сек до = через 540 сек

    vi.advanceTimersByTime(539_999);
    expect(cb).not.toHaveBeenCalled();

    vi.advanceTimersByTime(1);
    expect(cb).toHaveBeenCalledOnce();
  });

  it('fires immediately when exp is in the past', () => {
    const cb = vi.fn();
    vi.setSystemTime(1_000_000_000);

    scheduleRefresh(999_000, cb);

    vi.advanceTimersByTime(0);
    expect(cb).toHaveBeenCalledOnce();
  });

  it('cancel function prevents the callback', () => {
    const cb = vi.fn();
    vi.setSystemTime(1_000_000_000);

    const cancel = scheduleRefresh(1_000_600, cb);
    cancel();

    vi.advanceTimersByTime(1_000_000);
    expect(cb).not.toHaveBeenCalled();
  });

  it('cancel is safe to call after fire', () => {
    const cb = vi.fn();
    vi.setSystemTime(1_000_000_000);
    const cancel = scheduleRefresh(999_000, cb); // fires immediately
    vi.advanceTimersByTime(0);
    expect(cb).toHaveBeenCalledOnce();
    expect(() => cancel()).not.toThrow();
  });
});
