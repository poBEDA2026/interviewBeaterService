import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { AxiosError } from 'axios';
import { SignupPage } from '@/pages/SignupPage';

vi.mock('@/api/auth', () => ({ signup: vi.fn() }));
import { signup } from '@/api/auth';
const mockedSignup = vi.mocked(signup);

function LoginStub() {
  return <h1>Log in</h1>;
}

describe('SignupPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders Sign up title', () => {
    render(<MemoryRouter><SignupPage /></MemoryRouter>);
    expect(screen.getByRole('heading', { name: /sign up/i })).toBeInTheDocument();
  });

  it('on 409 shows email already registered', async () => {
    const user = userEvent.setup();
    mockedSignup.mockRejectedValueOnce(
      new AxiosError('Conflict', '409', undefined, undefined, {
        status: 409,
        data: null,
        headers: {},
        config: {} as any,
        statusText: 'Conflict',
      }),
    );
    render(<MemoryRouter><SignupPage /></MemoryRouter>);
    await user.type(screen.getByLabelText(/email/i), 'alice@example.com');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /continue/i }));

    expect(await screen.findByText(/already registered/i)).toBeInTheDocument();
  });

  it('on success navigates to /login with email prefilled', async () => {
    const user = userEvent.setup();
    mockedSignup.mockResolvedValueOnce({ id: 42 });
    render(
      <MemoryRouter initialEntries={['/signup']}>
        <Routes>
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/login" element={<LoginStub />} />
        </Routes>
      </MemoryRouter>,
    );
    await user.type(screen.getByLabelText(/email/i), 'alice@example.com');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /continue/i }));

    expect(await screen.findByRole('heading', { name: /log in/i })).toBeInTheDocument();
  });
});
