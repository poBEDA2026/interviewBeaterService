import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { AxiosError } from 'axios';
import { LoginPage } from '@/pages/LoginPage';

vi.mock('@/api/auth', () => ({
  login: vi.fn(),
}));
import { login } from '@/api/auth';
const mockedLogin = vi.mocked(login);

vi.mock('@/api/client', async () => {
  const actual = await vi.importActual<typeof import('@/api/client')>('@/api/client');
  return {
    ...actual,
    logoutAndRedirect: vi.fn(),
  };
});

function HomeStub() {
  return <h1>Questions</h1>;
}

describe('LoginPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders Log in title and form', () => {
    render(<MemoryRouter><LoginPage /></MemoryRouter>);
    expect(screen.getByRole('heading', { name: /log in/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
  });

  it('on 401 from login shows form error', async () => {
    const user = userEvent.setup();
    mockedLogin.mockRejectedValueOnce(
      new AxiosError('Unauthorized', '401', undefined, undefined, {
        status: 401,
        data: null,
        headers: {},
        config: {} as any,
        statusText: 'Unauthorized',
      }),
    );

    render(<MemoryRouter><LoginPage /></MemoryRouter>);
    await user.type(screen.getByLabelText(/email/i), 'alice@example.com');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /continue/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent(/invalid email or password/i);
  });

  it('on success navigates to /', async () => {
    const user = userEvent.setup();
    mockedLogin.mockResolvedValueOnce({
      accessToken: 'a.b.c',
      refreshToken: 'r',
      userId: 1,
      email: 'alice@example.com',
      role: 'USER',
    });

    render(
      <MemoryRouter initialEntries={['/login']}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/" element={<HomeStub />} />
        </Routes>
      </MemoryRouter>,
    );
    await user.type(screen.getByLabelText(/email/i), 'alice@example.com');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /continue/i }));

    expect(await screen.findByRole('heading', { name: /questions/i })).toBeInTheDocument();
  });
});
