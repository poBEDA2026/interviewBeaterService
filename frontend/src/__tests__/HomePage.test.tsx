/**
 * Тесты HomePage — страница списка вопросов.
 *
 * Инфраструктура:
 *  - `@/api/questions` замокан целиком: сетевых вызовов нет, список вопросов
 *    задаётся через mockedListQuestions.mockResolvedValueOnce(...);
 *  - `useAuth` возвращает захардкоженного пользователя alice@example.com;
 *  - из `@/api/client` замокан только logoutAndRedirect (остальное — реальное).
 *
 * Фикстуры вопросов обязаны содержать category и difficulty: HomePage рендерит
 * бейджи через QuestionBadges, без этих полей компонент упадёт.
 */
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

  // Рендер email вошедшего пользователя и загрузка списка вопросов.
  it('renders signed in email and loads questions', async () => {
    mockedListQuestions.mockResolvedValueOnce([
      {
        id: 1,
        title: 'What is React?',
        category: { id: 1, name: 'Core Java' },
        difficulty: { id: 2, name: 'middle', level: 2 },
      },
      {
        id: 2,
        title: 'What is TypeScript?',
        category: { id: 2, name: 'Spring' },
        difficulty: { id: 4, name: 'senior', level: 4 },
      },
    ]);
    render(<MemoryRouter><HomePage /></MemoryRouter>);
    expect(screen.getByText(/alice@example.com/i)).toBeInTheDocument();
    expect(await screen.findByText('What is React?')).toBeInTheDocument();
    expect(screen.getByText('What is TypeScript?')).toBeInTheDocument();
  });

  // Коммит 1aff8d7: рядом с каждым вопросом рендерятся бейджи категории
  // и сложности — проверяем обе пары (Core Java/middle, Spring/senior).
  it('renders category and difficulty widgets for each question', async () => {
    mockedListQuestions.mockResolvedValueOnce([
      {
        id: 1,
        title: 'What is React?',
        category: { id: 1, name: 'Core Java' },
        difficulty: { id: 2, name: 'middle', level: 2 },
      },
      {
        id: 2,
        title: 'What is TypeScript?',
        category: { id: 2, name: 'Spring' },
        difficulty: { id: 4, name: 'senior', level: 4 },
      },
    ]);
    render(<MemoryRouter><HomePage /></MemoryRouter>);

    expect(await screen.findByText('What is React?')).toBeInTheDocument();
    expect(screen.getByText('Core Java')).toBeInTheDocument();
    expect(screen.getByText('middle')).toBeInTheDocument();
    expect(screen.getByText('Spring')).toBeInTheDocument();
    expect(screen.getByText('senior')).toBeInTheDocument();
  });

  // Каждый вопрос — ссылка на свою страницу ответа /questions/:id.
  it('links each question to its answer page', async () => {
    mockedListQuestions.mockResolvedValueOnce([
      {
        id: 1,
        title: 'What is React?',
        category: { id: 1, name: 'Core Java' },
        difficulty: { id: 2, name: 'middle', level: 2 },
      },
      {
        id: 2,
        title: 'What is TypeScript?',
        category: { id: 2, name: 'Spring' },
        difficulty: { id: 4, name: 'senior', level: 4 },
      },
    ]);
    render(<MemoryRouter><HomePage /></MemoryRouter>);
    expect(await screen.findByText('What is React?')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /what is react\?/i })).toHaveAttribute(
      'href',
      '/questions/1',
    );
    expect(screen.getByRole('link', { name: /what is typescript\?/i })).toHaveAttribute(
      'href',
      '/questions/2',
    );
  });

  // Ошибка загрузки списка показывается как alert.
  it('shows error message when listQuestions fails', async () => {
    mockedListQuestions.mockRejectedValueOnce(new Error('boom'));
    render(<MemoryRouter><HomePage /></MemoryRouter>);
    expect(await screen.findByRole('alert')).toHaveTextContent(/failed to load/i);
  });

  // Пустой список — заглушка «no questions yet».
  it('shows empty state when questions list is empty', async () => {
    mockedListQuestions.mockResolvedValueOnce([]);
    render(<MemoryRouter><HomePage /></MemoryRouter>);
    expect(await screen.findByText(/no questions yet/i)).toBeInTheDocument();
  });

  // Кнопка Log out вызывает logoutAndRedirect из api/client.
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
