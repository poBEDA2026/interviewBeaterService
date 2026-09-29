import { api } from './client';

export interface Question {
  id: number;
  title: string;
}

export async function listQuestions(): Promise<Question[]> {
  const { data } = await api.get<Question[]>('/questions');
  return data;
}
