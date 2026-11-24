import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { QuestionPage } from '@/pages/QuestionPage';

vi.mock('@/api/questions', () => ({
  getQuestion: vi.fn(),
  listAnswerOptions: vi.fn(),
  listQuestions: vi.fn(),
  submitAnswer: vi.fn(),
}));
import { getQuestion, listAnswerOptions, listQuestions, submitAnswer } from '@/api/questions';

const mockedGetQuestion = vi.mocked(getQuestion);
const mockedListAnswerOptions = vi.mocked(listAnswerOptions);
const mockedListQuestions = vi.mocked(listQuestions);
const mockedSubmitAnswer = vi.mocked(submitAnswer);

const QUESTION_1 = { id: 1, title: 'What is DI?' };
const QUESTION_2 = { id: 2, title: 'What is AOP?' };
const OPTIONS = [
  { id: 10, text: 'Dependency Injection' },
  { id: 11, text: 'Direct Injection' },
];

function renderPage() {
  return render(
    <MemoryRouter initialEntries={['/questions/1']}>
      <Routes>
        <Route path="/questions/:id" element={<QuestionPage />} />
        <Route path="/" element={<div>HOME</div>} />
      </Routes>
    </MemoryRouter>,
  );
}

function mockLoad() {
  mockedGetQuestion.mockResolvedValueOnce(QUESTION_1);
  mockedListAnswerOptions.mockResolvedValueOnce(OPTIONS);
  mockedListQuestions.mockResolvedValueOnce([QUESTION_1, QUESTION_2]);
}

describe('QuestionPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders question title, options and disabled submit until an option is chosen', async () => {
    mockLoad();
    renderPage();

    expect(await screen.findByText('What is DI?')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Dependency Injection' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Direct Injection' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /submit/i })).toBeDisabled();
  });

  it('highlights the chosen answer green and shows description when correct', async () => {
    mockLoad();
    mockedSubmitAnswer.mockResolvedValueOnce({
      correct: true,
      selectedAnswerId: 10,
      correctAnswerId: 10,
      description: 'DI is a pattern',
    });
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole('button', { name: 'Dependency Injection' }));
    await user.click(screen.getByRole('button', { name: /submit/i }));

    const chosen = await screen.findByRole('button', { name: 'Dependency Injection' });
    expect(chosen).toHaveClass('bg-green-50');
    expect(chosen).toHaveClass('border-green-500');
    expect(screen.getByText('DI is a pattern')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /submit/i })).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: /next question/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /finish/i })).toBeInTheDocument();
  });

  it('highlights the wrong answer red and the correct one green when incorrect', async () => {
    mockLoad();
    mockedSubmitAnswer.mockResolvedValueOnce({
      correct: false,
      selectedAnswerId: 11,
      correctAnswerId: 10,
      description: 'DI is a pattern',
    });
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole('button', { name: 'Direct Injection' }));
    await user.click(screen.getByRole('button', { name: /submit/i }));

    const chosen = await screen.findByRole('button', { name: 'Direct Injection' });
    expect(chosen).toHaveClass('bg-red-50');
    expect(chosen).toHaveClass('border-red-500');
    const correct = screen.getByRole('button', { name: 'Dependency Injection' });
    expect(correct).toHaveClass('bg-green-50');
    expect(correct).toHaveClass('border-green-500');
    expect(screen.getByText('DI is a pattern')).toBeInTheDocument();
  });

  it('navigates to the next question on "Next question"', async () => {
    mockLoad();
    mockedGetQuestion.mockResolvedValueOnce(QUESTION_2);
    mockedListAnswerOptions.mockResolvedValueOnce([]);
    mockedListQuestions.mockResolvedValueOnce([QUESTION_1, QUESTION_2]);
    mockedSubmitAnswer.mockResolvedValueOnce({
      correct: true,
      selectedAnswerId: 10,
      correctAnswerId: 10,
      description: 'ok',
    });
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole('button', { name: 'Dependency Injection' }));
    await user.click(screen.getByRole('button', { name: /submit/i }));
    await user.click(await screen.findByRole('button', { name: /next question/i }));

    await waitFor(() => {
      expect(mockedGetQuestion).toHaveBeenCalledWith(2);
    });
    expect(await screen.findByText('What is AOP?')).toBeInTheDocument();
  });

  it('returns to the questions list when "Next question" pressed on the last question', async () => {
    mockedGetQuestion.mockResolvedValueOnce(QUESTION_1);
    mockedListAnswerOptions.mockResolvedValueOnce(OPTIONS);
    mockedListQuestions.mockResolvedValueOnce([QUESTION_1]);
    mockedSubmitAnswer.mockResolvedValueOnce({
      correct: true,
      selectedAnswerId: 10,
      correctAnswerId: 10,
      description: 'ok',
    });
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole('button', { name: 'Dependency Injection' }));
    await user.click(screen.getByRole('button', { name: /submit/i }));
    await user.click(await screen.findByRole('button', { name: /next question/i }));

    expect(await screen.findByText('HOME')).toBeInTheDocument();
  });

  it('returns to the questions list when "Finish" pressed', async () => {
    mockLoad();
    mockedSubmitAnswer.mockResolvedValueOnce({
      correct: true,
      selectedAnswerId: 10,
      correctAnswerId: 10,
      description: 'ok',
    });
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole('button', { name: 'Dependency Injection' }));
    await user.click(screen.getByRole('button', { name: /submit/i }));
    await user.click(await screen.findByRole('button', { name: /finish/i }));

    expect(await screen.findByText('HOME')).toBeInTheDocument();
  });

  it('shows error message when loading fails', async () => {
    mockedGetQuestion.mockRejectedValueOnce(new Error('boom'));
    mockedListAnswerOptions.mockRejectedValueOnce(new Error('boom'));
    mockedListQuestions.mockRejectedValueOnce(new Error('boom'));
    renderPage();

    expect(await screen.findByRole('alert')).toHaveTextContent(/failed to load/i);
  });
});
