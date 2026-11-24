import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { StatisticsPage } from '@/pages/StatisticsPage';

vi.mock('@/api/stats', () => ({
  listMyStats: vi.fn(),
}));
import { listMyStats } from '@/api/stats';
const mockedListMyStats = vi.mocked(listMyStats);

vi.mock('@/auth/useAuth', () => ({
  useAuth: () => ({
    user: { id: 1, email: 'alice@example.com', fullName: 'Alice Alisson', role: 'ROLE_USER' },
    setSession: vi.fn(),
  }),
}));
vi.mock('@/api/client', async () => {
  const actual = await vi.importActual<typeof import('@/api/client')>('@/api/client');
  return {
    ...actual,
    logoutAndRedirect: vi.fn(),
  };
});

const sample = [
  { questionId: 1, questionTitle: 'What is Java?', correctCount: 3, wrongCount: 1, lastAnsweredAt: '2026-10-20T10:00:00Z' },
  { questionId: 2, questionTitle: 'What is React?', correctCount: 0, wrongCount: 2, lastAnsweredAt: '2026-10-21T10:00:00Z' },
];

describe('StatisticsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders summary tiles, accuracy and per-question bars', async () => {
    mockedListMyStats.mockResolvedValueOnce(sample);
    render(
      <MemoryRouter>
        <StatisticsPage />
      </MemoryRouter>,
    );

    expect((await screen.findAllByText('What is Java?')).length).toBeGreaterThan(0);
    expect((await screen.findAllByText('What is React?')).length).toBeGreaterThan(0);

    // KPI: Answered 6, accuracy 50% (числа Correct/Wrong дублируются в таблице,
    // поэтому точные количества не проверяем)
    expect(screen.getByText('6')).toBeInTheDocument();
    expect(screen.getAllByText('Correct').length).toBeGreaterThanOrEqual(2); // плитка + легенда
    expect(screen.getByText('50%')).toBeInTheDocument();
    expect(screen.getByText(/3 of 6 answers correct/i)).toBeInTheDocument();

    // таблица со всеми данными
    expect(screen.getByText(/show all data \(2 questions\)/i)).toBeInTheDocument();
  });

  it('shows tooltip with counts on row hover', async () => {
    mockedListMyStats.mockResolvedValueOnce(sample);
    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <StatisticsPage />
      </MemoryRouter>,
    );

    const row = (await screen.findAllByText('What is Java?'))[0].closest('li')!;
    await user.hover(row);
    expect(await screen.findByRole('tooltip')).toHaveTextContent(/last answered: 2026-10-20/i);
  });

  it('shows empty state when there are no stats', async () => {
    mockedListMyStats.mockResolvedValueOnce([]);
    render(
      <MemoryRouter>
        <StatisticsPage />
      </MemoryRouter>,
    );
    expect(await screen.findByText(/no statistics yet/i)).toBeInTheDocument();
  });

  it('shows error message when listMyStats fails', async () => {
    mockedListMyStats.mockRejectedValueOnce(new Error('boom'));
    render(
      <MemoryRouter>
        <StatisticsPage />
      </MemoryRouter>,
    );
    expect(await screen.findByRole('alert')).toHaveTextContent(/failed to load/i);
  });
});
