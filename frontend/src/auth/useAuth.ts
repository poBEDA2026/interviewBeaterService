import { useAuthStore } from './store';

export function useAuth() {
  const accessToken = useAuthStore((s) => s.accessToken);
  const user = useAuthStore((s) => s.user);
  const setSession = useAuthStore((s) => s.setSession);
  const setAccessToken = useAuthStore((s) => s.setAccessToken);
  const clear = useAuthStore((s) => s.clear);

  return {
    isAuthenticated: accessToken !== null,
    accessToken,
    user,
    setSession,
    setAccessToken,
    clear,
  };
}
