import { api } from './client';

export interface Question {
  id: number;
  title: string;
}

export interface AnswerOption {
  id: number;
  text: string;
}

export interface AnswerSubmissionResult {
  correct: boolean;
  selectedAnswerId: number;
  correctAnswerId: number;
  description: string | null;
}

export async function listQuestions(): Promise<Question[]> {
  const { data } = await api.get<Question[]>('/questions');
  return data;
}

export async function getQuestion(id: number): Promise<Question> {
  const { data } = await api.get<Question>(`/questions/${id}`);
  return data;
}

export async function listAnswerOptions(questionId: number): Promise<AnswerOption[]> {
  const { data } = await api.get<AnswerOption[]>(`/questions/${questionId}/answers`);
  return data;
}

export async function submitAnswer(
  questionId: number,
  answerId: number,
): Promise<AnswerSubmissionResult> {
  const { data } = await api.post<AnswerSubmissionResult>(`/questions/${questionId}/answers`, {
    answerId,
  });
  return data;
}
