import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { AccountPage } from '@/pages/AccountPage';

let mockUser: { id: number; email: string; fullName: string | null; role: string } | null = null;

vi.mock('@/auth/useAuth', () => ({
  useAuth: () => ({ user: mockUser, setSession: vi.fn() }),
}));
vi.mock('@/api/client', async () => {
  const actual = await vi.importActual<typeof import('@/api/client')>('@/api/client');
  return {
    ...actual,
    logoutAndRedirect: vi.fn(),
  };
});

function StatsStub() {
  return <h1>Stats page</h1>;
}

describe('AccountPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUser = { id: 1, email: 'alice@example.com', fullName: 'Alice Alisson', role: 'ROLE_USER' };
  });

  it('renders full name, email and role', () => {
    render(
      <MemoryRouter>
        <AccountPage />
      </MemoryRouter>,
    );
    expect(screen.getByText('Alice Alisson')).toBeInTheDocument();
    expect(screen.getByText('alice@example.com')).toBeInTheDocument();
    expect(screen.getByText('USER')).toBeInTheDocument();
  });

  it('falls back to dash when full name is missing', () => {
    mockUser = { id: 1, email: 'alice@example.com', fullName: null, role: 'ROLE_USER' };
    render(
      <MemoryRouter>
        <AccountPage />
      </MemoryRouter>,
    );
    expect(screen.getAllByText('—').length).toBeGreaterThan(0);
    expect(screen.getByText('alice@example.com')).toBeInTheDocument();
  });

  it('navigates to statistics page on Show statistics click', async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={['/account']}>
        <Routes>
          <Route path="/account" element={<AccountPage />} />
          <Route path="/account/stats" element={<StatsStub />} />
        </Routes>
      </MemoryRouter>,
    );
    await user.click(screen.getByRole('button', { name: /show statistics/i }));
    expect(await screen.findByRole('heading', { name: /stats page/i })).toBeInTheDocument();
  });
});
