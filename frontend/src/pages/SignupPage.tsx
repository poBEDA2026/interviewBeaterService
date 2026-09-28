import { Link, useNavigate } from 'react-router-dom';
import { AxiosError } from 'axios';
import { AuthLayout } from '@/components/AuthLayout';
import { AuthForm, SubmitResult } from '@/components/AuthForm';
import { signup } from '@/api/auth';

export function SignupPage() {
  const navigate = useNavigate();

  async function onSubmit({ email, password }: { email: string; password: string }): Promise<SubmitResult> {
    try {
      await signup(email, password);
      navigate(`/login?email=${encodeURIComponent(email)}`, { replace: true });
      return { ok: true };
    } catch (e) {
      if (e instanceof AxiosError) {
        if (e.response?.status === 409) {
          return { ok: false, fieldErrors: { email: 'Email already registered' } };
        }
        if (e.response?.status === 400 && e.response.data?.fields) {
          return { ok: false, fieldErrors: mapServerFields(e.response.data.fields) };
        }
      }
      return { ok: false, formError: 'Sign up failed. Try again.' };
    }
  }

  return (
    <AuthLayout title="Sign up" subtitle="Create your InterviewBeater account.">
      <AuthForm mode="signup" onSubmit={onSubmit} />
      <p className="mt-6 text-center text-sm text-gray-600">
        Already have an account?{' '}
        <Link to="/login" className="font-medium text-blue-600 hover:underline">Log in</Link>
      </p>
    </AuthLayout>
  );
}

function mapServerFields(fields: Array<{ field: string; message: string }>): Record<string, string> {
  const out: Record<string, string> = {};
  for (const f of fields) out[f.field] = f.message;
  return out;
}
