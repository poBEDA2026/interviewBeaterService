import { useEffect, useState } from 'react';
import { listQuestions, Question } from '@/api/questions';
import { LogoutButton } from '@/components/LogoutButton';
import { useAuth } from '@/auth/useAuth';

export function HomePage() {
  const { user } = useAuth();
  const [questions, setQuestions] = useState<Question[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    listQuestions()
      .then(setQuestions)
      .catch(() => setError('Failed to load questions.'));
  }, []);

  return (
    <div className="mx-auto max-w-2xl px-4 py-10">
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900">Questions</h1>
          {user && <p className="text-sm text-gray-500">Signed in as {user.email}</p>}
        </div>
        <LogoutButton />
      </div>

      {error && <p className="text-sm text-red-600" role="alert">{error}</p>}

      {questions === null && !error && (
        <p className="text-sm text-gray-500">Loading…</p>
      )}

      {questions && questions.length === 0 && (
        <p className="text-sm text-gray-500">No questions yet.</p>
      )}

      {questions && questions.length > 0 && (
        <ul className="divide-y divide-gray-200 rounded-md border border-gray-200 bg-white">
          {questions.map((q) => (
            <li key={q.id} className="p-4">
              <p className="text-sm text-gray-500">#{q.id}</p>
              <p className="text-gray-900">{q.title}</p>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
