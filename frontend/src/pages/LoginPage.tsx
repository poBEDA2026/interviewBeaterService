import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { AuthLayout } from '@/components/AuthLayout';
import { AuthForm, SubmitResult } from '@/components/AuthForm';
import { login } from '@/api/auth';
import { useAuth } from '@/auth/useAuth';
import { writeRefresh } from '@/auth/store';
import { scheduleRefresh } from '@/auth/refreshScheduler';
import { parseExp } from '@/lib/jwt';
import { AxiosError } from 'axios';

export function LoginPage() {
  const { setSession } = useAuth();
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const defaultEmail = params.get('email') ?? '';

  async function onSubmit({ email, password }: { email: string; password: string }): Promise<SubmitResult> {
    try {
      const res = await login(email, password);
      writeRefresh(res.refreshToken);
      setSession({ accessToken: res.accessToken, user: { id: res.userId, email: res.email, role: res.role } });
      const exp = parseExp(res.accessToken);
      if (exp !== null) {
        scheduleRefresh(exp, () => { /* best-effort: 401 fallback covers it */ });
      }
      navigate('/', { replace: true });
      return { ok: true };
    } catch (e) {
      if (e instanceof AxiosError && e.response?.status === 401) {
        return { ok: false, formError: 'Invalid email or password' };
      }
      return { ok: false, formError: 'Network error. Try again.' };
    }
  }

  return (
    <AuthLayout title="Log in" subtitle="Welcome back.">
      <AuthForm mode="login" onSubmit={onSubmit} defaultEmail={defaultEmail} />
      <p className="mt-6 text-center text-sm text-gray-600">
        Don&apos;t have an account?{' '}
        <Link to="/signup" className="font-medium text-blue-600 hover:underline">Sign up</Link>
      </p>
    </AuthLayout>
  );
}
