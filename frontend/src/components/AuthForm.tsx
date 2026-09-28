import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';

const schema = z.object({
  email: z.string().email('Enter a valid email'),
  password: z.string().min(8, 'At least 8 characters'),
});

type FormValues = z.infer<typeof schema>;

export type SubmitResult =
  | { ok: true }
  | { ok: false; fieldErrors?: Partial<Record<keyof FormValues, string>>; formError?: string };

interface Props {
  mode: 'login' | 'signup';
  defaultEmail?: string;
  onSubmit: (values: FormValues) => Promise<SubmitResult>;
}

export function AuthForm({ mode, defaultEmail = '', onSubmit }: Props) {
  const [formError, setFormError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { email: defaultEmail, password: '' },
  });

  async function submit(values: FormValues) {
    setFormError(null);
    const result = await onSubmit(values);
    if (result.ok) return;

    if (result.fieldErrors) {
      for (const [field, message] of Object.entries(result.fieldErrors)) {
        if (message) setError(field as keyof FormValues, { message });
      }
    }
    if (result.formError) setFormError(result.formError);
  }

  return (
    <form className="space-y-4" onSubmit={handleSubmit(submit)} noValidate>
      <div className="space-y-1">
        <Label htmlFor="email">Email</Label>
        <Input id="email" type="email" autoComplete="email" {...register('email')} />
        {errors.email && <p className="text-sm text-red-600">{errors.email.message}</p>}
      </div>

      <div className="space-y-1">
        <Label htmlFor="password">Password</Label>
        <Input
          id="password"
          type="password"
          autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
          {...register('password')}
        />
        {errors.password && <p className="text-sm text-red-600">{errors.password.message}</p>}
      </div>

      {formError && (
        <p className="text-sm text-red-600" role="alert">{formError}</p>
      )}

      <Button type="submit" isLoading={isSubmitting} className="w-full">
        Continue
      </Button>
    </form>
  );
}
