import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AuthForm } from '@/components/AuthForm';

describe('AuthForm', () => {
  it('shows field validation errors', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    render(<AuthForm mode="login" onSubmit={onSubmit} />);

    await user.type(screen.getByLabelText(/email/i), 'not-an-email');
    await user.type(screen.getByLabelText(/password/i), 'short');
    await user.click(screen.getByRole('button', { name: /continue/i }));

    expect(await screen.findByText(/valid email/i)).toBeInTheDocument();
    expect(screen.getByText(/at least 8/i)).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('calls onSubmit with parsed values on valid input', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue({ ok: true });
    render(<AuthForm mode="login" onSubmit={onSubmit} />);

    await user.type(screen.getByLabelText(/email/i), 'alice@example.com');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /continue/i }));

    expect(onSubmit).toHaveBeenCalledWith({ email: 'alice@example.com', password: 'password123' });
  });

  it('maps server fieldErrors onto fields', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue({
      ok: false,
      fieldErrors: { email: 'Email already registered' },
    });
    render(<AuthForm mode="signup" onSubmit={onSubmit} />);

    await user.type(screen.getByLabelText(/email/i), 'alice@example.com');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /continue/i }));

    expect(await screen.findByText(/already registered/i)).toBeInTheDocument();
  });

  it('shows formError from server', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue({
      ok: false,
      formError: 'Invalid email or password',
    });
    render(<AuthForm mode="login" onSubmit={onSubmit} />);

    await user.type(screen.getByLabelText(/email/i), 'alice@example.com');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /continue/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent(/invalid email or password/i);
  });
});

describe('AuthForm (signup → 409)', () => {
  it('signup with duplicate email surfaces server error on email field', async () => {
    const user = userEvent.setup();
    // Симулируем ответ бэка после правки ControllerAdvice: 409 → фронт мапит в fieldErrors.email
    const onSubmit = vi.fn().mockImplementation(async () => {
      return { ok: false, fieldErrors: { email: 'Email already registered' } } as const;
    });
    render(<AuthForm mode="signup" onSubmit={onSubmit} />);

    await user.type(screen.getByLabelText(/email/i), 'alice@example.com');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /continue/i }));

    expect(await screen.findByText(/already registered/i)).toBeInTheDocument();
  });
});
