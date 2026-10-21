import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import {
  AnswerOption,
  AnswerSubmissionResult,
  Question,
  getQuestion,
  listAnswerOptions,
  listQuestions,
  submitAnswer,
} from '@/api/questions';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';

export function QuestionPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const questionId = Number(id);

  const [question, setQuestion] = useState<Question | null>(null);
  const [options, setOptions] = useState<AnswerOption[] | null>(null);
  const [allQuestions, setAllQuestions] = useState<Question[] | null>(null);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [result, setResult] = useState<AnswerSubmissionResult | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setQuestion(null);
    setOptions(null);
    setSelectedId(null);
    setResult(null);
    setError(null);

    Promise.all([getQuestion(questionId), listAnswerOptions(questionId), listQuestions()])
      .then(([q, opts, all]) => {
        setQuestion(q);
        setOptions(opts);
        setAllQuestions(all);
      })
      .catch(() => setError('Failed to load question.'));
  }, [questionId]);

  const nextQuestion = useMemo(() => {
    if (!allQuestions || !question) return null;
    const index = allQuestions.findIndex((q) => q.id === question.id);
    return index >= 0 && index + 1 < allQuestions.length ? allQuestions[index + 1] : null;
  }, [allQuestions, question]);

  async function handleSubmit() {
    if (selectedId === null) return;
    setSubmitting(true);
    setError(null);
    try {
      setResult(await submitAnswer(questionId, selectedId));
    } catch {
      setError('Failed to submit answer.');
    } finally {
      setSubmitting(false);
    }
  }

  function optionClasses(option: AnswerOption): string {
    const base = 'w-full rounded-md border p-4 text-left transition-colors';
    if (!result) {
      return cn(
        base,
        option.id === selectedId
          ? 'border-blue-600 bg-blue-50'
          : 'border-gray-200 bg-white hover:bg-gray-50',
      );
    }
    if (option.id === result.selectedAnswerId) {
      return cn(base, result.correct ? 'border-green-500 bg-green-50' : 'border-red-500 bg-red-50');
    }
    if (option.id === result.correctAnswerId) {
      return cn(base, 'border-green-500 bg-green-50');
    }
    return cn(base, 'border-gray-200 bg-white opacity-60');
  }

  return (
    <div className="mx-auto max-w-2xl px-4 py-10">
      <div className="mb-6">
        <Link to="/" className="text-sm text-blue-600 hover:underline">← All questions</Link>
      </div>

      {error && <p className="text-sm text-red-600" role="alert">{error}</p>}

      {question === null && !error && (
        <p className="text-sm text-gray-500">Loading…</p>
      )}

      {question && (
        <>
          <h1 className="mb-6 text-2xl font-semibold text-gray-900">{question.title}</h1>

          {options && options.length === 0 && (
            <p className="text-sm text-gray-500">No answer options for this question.</p>
          )}

          {options && options.length > 0 && (
            <div className="space-y-2">
              {options.map((option) => (
                <button
                  key={option.id}
                  type="button"
                  className={optionClasses(option)}
                  onClick={() => setSelectedId(option.id)}
                  disabled={result !== null || submitting}
                >
                  {option.text}
                </button>
              ))}
            </div>
          )}

          {result?.description && (
            <p className="mt-6 rounded-md border border-gray-200 bg-white p-4 text-gray-700">
              {result.description}
            </p>
          )}

          {result ? (
            <div className="mt-6 flex gap-2">
              <Button
                type="button"
                onClick={() => navigate(nextQuestion ? `/questions/${nextQuestion.id}` : '/')}
              >
                Next question
              </Button>
              <Button type="button" variant="secondary" onClick={() => navigate('/')}>
                Finish
              </Button>
            </div>
          ) : (
            <Button
              type="button"
              className="mt-6"
              onClick={handleSubmit}
              disabled={selectedId === null}
              isLoading={submitting}
            >
              Submit
            </Button>
          )}
        </>
      )}
    </div>
  );
}
