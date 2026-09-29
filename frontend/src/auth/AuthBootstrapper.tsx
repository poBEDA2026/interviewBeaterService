import { ReactNode, useEffect, useState } from 'react';
import { useAuthStore, readRefresh, writeRefresh, clearRefresh } from './store';
import { refreshSession } from '@/api/auth';
import { scheduleRefresh } from './refreshScheduler';
import { parseExp } from '@/lib/jwt';

export function AuthBootstrapper({ children }: { children: ReactNode }) {
  const [ready, setReady] = useState(false);

  useEffect(() => {
    let cancel: (() => void) | undefined;
    const refresh = readRefresh();
    if (!refresh) {
      setReady(true);
      return;
    }
    refreshSession(refresh)
      .then((session) => {
        writeRefresh(session.refreshToken);
        const exp = parseExp(session.accessToken);
        if (exp != null) {
          cancel = scheduleRefresh(exp, () => {
            // on expire — clear state; bootstrap won't fire again in this session
            clearRefresh();
            useAuthStore.getState().clear();
          });
        }
        useAuthStore.getState().setSession({
          accessToken: session.accessToken,
          user: { id: session.userId, email: session.email, role: session.role },
        });
      })
      .catch(() => {
        clearRefresh();
        useAuthStore.getState().clear();
      })
      .finally(() => setReady(true));
    return () => cancel?.();
  }, []);

  if (!ready) return <div>Loading…</div>;
  return <>{children}</>;
}
