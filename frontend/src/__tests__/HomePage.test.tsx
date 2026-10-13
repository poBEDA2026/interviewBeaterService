import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { HomePage } from '@/pages/HomePage';

vi.mock('@/api/questions', () => ({
  listQuestions: vi.fn(),
}));
import { listQuestions } from '@/api/questions';
const mockedListQuestions = vi.mocked(listQuestions);

vi.mock('@/api/client', async () => {
  const actual = await vi.importActual<typeof import('@/api/client')>('@/api/client');
  return {
    ...actual,
    logoutAndRedirect: vi.fn(),
  };
});

vi.mock('@/auth/useAuth', () => ({
  useAuth: () => ({ user: { email: 'alice@example.com', role: 'USER' }, setSession: vi.fn() }),
}));

describe('HomePage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders signed in email and loads questions', async () => {
    mockedListQuestions.mockResolvedValueOnce([
      { id: 1, title: 'What is React?' },
      { id: 2, title: 'What is TypeScript?' },
    ]);
    render(<MemoryRouter><HomePage /></MemoryRouter>);
    expect(screen.getByText(/alice@example.com/i)).toBeInTheDocument();
    expect(await screen.findByText('What is React?')).toBeInTheDocument();
    expect(screen.getByText('What is TypeScript?')).toBeInTheDocument();
  });

  it('shows error message when listQuestions fails', async () => {
    mockedListQuestions.mockRejectedValueOnce(new Error('boom'));
    render(<MemoryRouter><HomePage /></MemoryRouter>);
    expect(await screen.findByRole('alert')).toHaveTextContent(/failed to load/i);
  });

  it('shows empty state when questions list is empty', async () => {
    mockedListQuestions.mockResolvedValueOnce([]);
    render(<MemoryRouter><HomePage /></MemoryRouter>);
    expect(await screen.findByText(/no questions yet/i)).toBeInTheDocument();
  });

  it('logout button calls logoutAndRedirect', async () => {
    const { logoutAndRedirect } = await import('@/api/client');
    mockedListQuestions.mockResolvedValueOnce([]);
    const user = userEvent.setup();
    render(<MemoryRouter><HomePage /></MemoryRouter>);
    await screen.findByText(/no questions yet/i);
    await user.click(screen.getByRole('button', { name: /log out/i }));
    await waitFor(() => {
      expect(vi.mocked(logoutAndRedirect)).toHaveBeenCalledTimes(1);
    });
  });
});
