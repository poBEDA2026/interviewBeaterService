import { api } from './client';

export interface QuestionStats {
  questionId: number;
  questionTitle: string;
  correctCount: number;
  wrongCount: number;
  lastAnsweredAt: string;
}

export async function listMyStats(): Promise<QuestionStats[]> {
  const { data } = await api.get<QuestionStats[]>('/users/me/stats');
  return data;
}
