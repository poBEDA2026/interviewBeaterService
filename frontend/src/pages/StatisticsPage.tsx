import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { listMyStats, QuestionStats } from '@/api/stats';
import { LogoutButton } from '@/components/LogoutButton';
import { Card } from '@/components/ui/card';
import { useAuth } from '@/auth/useAuth';

// Палитра согласована с dataviz-валидатором (пара blue/red, светлая поверхность)
const CORRECT_COLOR = '#2a78d6';
const WRONG_COLOR = '#e34948';
const TRACK_COLOR = '#f0efec';

const TOP_QUESTIONS = 10;

interface TooltipState {
  x: number;
  y: number;
  stats: QuestionStats;
}

function totalOf(s: QuestionStats): number {
  return s.correctCount + s.wrongCount;
}

function formatDate(iso: string): string {
  return new Date(iso).toISOString().slice(0, 10);
}

export function StatisticsPage() {
  const { user } = useAuth();
  const [stats, setStats] = useState<QuestionStats[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [tooltip, setTooltip] = useState<TooltipState | null>(null);

  useEffect(() => {
    listMyStats()
      .then(setStats)
      .catch(() => setError('Failed to load statistics.'));
  }, []);

  const totals = (stats ?? []).reduce(
    (acc, s) => ({
      correct: acc.correct + s.correctCount,
      wrong: acc.wrong + s.wrongCount,
    }),
    { correct: 0, wrong: 0 },
  );
  const answered = totals.correct + totals.wrong;
  const accuracy = answered === 0 ? 0 : Math.round((totals.correct / answered) * 100);
  const sorted = [...(stats ?? [])].sort((a, b) => totalOf(b) - totalOf(a));
  const top = sorted.slice(0, TOP_QUESTIONS);
  const maxTotal = top.length > 0 ? totalOf(top[0]) : 1;

  return (
    <div className="mx-auto max-w-3xl px-4 py-10">
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900">Statistics</h1>
          {user && <p className="text-sm text-gray-500">Signed in as {user.email}</p>}
        </div>
        <LogoutButton />
      </div>

      {error && <p className="text-sm text-red-600" role="alert">{error}</p>}
      {stats === null && !error && <p className="text-sm text-gray-500">Loading…</p>}
      {stats && stats.length === 0 && (
        <p className="text-sm text-gray-500">No statistics yet — answer some questions first.</p>
      )}

      {stats && stats.length > 0 && (
        <div className="space-y-6">
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
            <Card className="p-4">
              <p className="text-sm text-gray-500">Answered</p>
              <p className="text-2xl font-semibold text-gray-900">{answered}</p>
            </Card>
            <Card className="p-4">
              <p className="text-sm text-gray-500">Correct</p>
              <p className="text-2xl font-semibold text-gray-900">{totals.correct}</p>
            </Card>
            <Card className="p-4">
              <p className="text-sm text-gray-500">Wrong</p>
              <p className="text-2xl font-semibold text-gray-900">{totals.wrong}</p>
            </Card>
            <Card className="p-4">
              <p className="text-sm text-gray-500">Questions</p>
              <p className="text-2xl font-semibold text-gray-900">{stats.length}</p>
            </Card>
          </div>

          <Card>
            <p className="text-sm font-medium text-gray-500">Accuracy</p>
            <p className="text-5xl font-semibold text-gray-900">{accuracy}%</p>
            <div
              className="mt-4 h-2 rounded-full"
              style={{ backgroundColor: TRACK_COLOR }}
              role="meter"
              aria-valuenow={accuracy}
              aria-valuemin={0}
              aria-valuemax={100}
              aria-label="Accuracy percent"
            >
              <div
                className="h-2 rounded-full"
                style={{ width: `${accuracy}%`, backgroundColor: CORRECT_COLOR }}
              />
            </div>
            <p className="mt-2 text-sm text-gray-500">
              {totals.correct} of {answered} answers correct
            </p>
          </Card>

          <Card>
            <div className="mb-4 flex items-center justify-between">
              <p className="text-sm font-medium text-gray-500">Top questions by attempts</p>
              <div className="flex items-center gap-4 text-sm text-gray-600">
                <span className="flex items-center gap-2">
                  <span
                    aria-hidden="true"
                    className="inline-block h-3 w-3 rounded-sm"
                    style={{ backgroundColor: CORRECT_COLOR }}
                  />
                  Correct
                </span>
                <span className="flex items-center gap-2">
                  <span
                    aria-hidden="true"
                    className="inline-block h-3 w-3 rounded-sm"
                    style={{ backgroundColor: WRONG_COLOR }}
                  />
                  Wrong
                </span>
              </div>
            </div>
            <ul className="space-y-1">
              {top.map((s) => {
                const total = totalOf(s);
                const widthPct = (total / maxTotal) * 100;
                const correctPct = total === 0 ? 0 : (s.correctCount / total) * widthPct;
                const wrongPct = total === 0 ? 0 : (s.wrongCount / total) * widthPct;
                return (
                  <li
                    key={s.questionId}
                    className="flex min-h-6 items-center gap-3 rounded-md px-2 py-1 hover:bg-gray-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500"
                    tabIndex={0}
                    onMouseEnter={(e) => setTooltip({ x: e.clientX, y: e.clientY, stats: s })}
                    onMouseMove={(e) => setTooltip({ x: e.clientX, y: e.clientY, stats: s })}
                    onMouseLeave={() => setTooltip(null)}
                    onFocus={(e) =>
                      setTooltip({ x: e.currentTarget.getBoundingClientRect().left, y: e.currentTarget.getBoundingClientRect().top, stats: s })
                    }
                    onBlur={() => setTooltip(null)}
                  >
                    <p className="w-40 shrink-0 truncate text-sm text-gray-700" title={s.questionTitle}>
                      {s.questionTitle}
                    </p>
                    <div className="flex flex-1 items-center">
                      <div
                        className={'h-4 rounded-l' + (s.wrongCount > 0 ? ' border-r-2 border-white' : ' rounded-r')}
                        style={{ width: `${correctPct}%`, backgroundColor: s.correctCount > 0 ? CORRECT_COLOR : 'transparent' }}
                      />
                      <div
                        className="h-4 rounded-r"
                        style={{ width: `${wrongPct}%`, backgroundColor: s.wrongCount > 0 ? WRONG_COLOR : 'transparent' }}
                      />
                    </div>
                    <span className="w-8 shrink-0 text-right text-xs tabular-nums text-gray-500">{total}</span>
                  </li>
                );
              })}
            </ul>
            {sorted.length > TOP_QUESTIONS && (
              <p className="mt-3 text-sm text-gray-500">
                Showing top {TOP_QUESTIONS} of {sorted.length} questions — see the full table below.
              </p>
            )}
          </Card>

          <details className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
            <summary className="cursor-pointer text-sm font-medium text-gray-700">
              Show all data ({sorted.length} questions)
            </summary>
            <table className="mt-4 w-full text-sm">
              <thead>
                <tr className="border-b border-gray-200 text-left text-gray-500">
                  <th className="py-2 pr-4 font-medium">Question</th>
                  <th className="py-2 pr-4 font-medium">Correct</th>
                  <th className="py-2 pr-4 font-medium">Wrong</th>
                  <th className="py-2 pr-4 font-medium">Total</th>
                  <th className="py-2 font-medium">Last answered</th>
                </tr>
              </thead>
              <tbody>
                {sorted.map((s) => (
                  <tr key={s.questionId} className="border-b border-gray-100">
                    <td className="py-2 pr-4 text-gray-900">{s.questionTitle}</td>
                    <td className="py-2 pr-4 tabular-nums text-gray-900">{s.correctCount}</td>
                    <td className="py-2 pr-4 tabular-nums text-gray-900">{s.wrongCount}</td>
                    <td className="py-2 pr-4 tabular-nums text-gray-900">{totalOf(s)}</td>
                    <td className="py-2 text-gray-900">{formatDate(s.lastAnsweredAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </details>
        </div>
      )}

      <p className="mt-6">
        <Link to="/account" className="text-sm text-blue-600 hover:underline">
          ← Back to account
        </Link>
      </p>

      {tooltip && (
        <div
          className="pointer-events-none fixed z-50 rounded-md border border-gray-200 bg-white p-3 text-xs shadow-lg"
          style={{ left: Math.min(tooltip.x + 12, window.innerWidth - 220), top: tooltip.y + 12 }}
          role="tooltip"
        >
          <p className="mb-1 max-w-48 truncate font-medium text-gray-900">{tooltip.stats.questionTitle}</p>
          <p className="flex items-center gap-2 text-gray-600">
            <span aria-hidden="true" className="inline-block h-0.5 w-3" style={{ backgroundColor: CORRECT_COLOR }} />
            <span className="font-semibold tabular-nums text-gray-900">{tooltip.stats.correctCount}</span> correct
          </p>
          <p className="flex items-center gap-2 text-gray-600">
            <span aria-hidden="true" className="inline-block h-0.5 w-3" style={{ backgroundColor: WRONG_COLOR }} />
            <span className="font-semibold tabular-nums text-gray-900">{tooltip.stats.wrongCount}</span> wrong
          </p>
          <p className="mt-1 text-gray-500">Last answered: {formatDate(tooltip.stats.lastAnsweredAt)}</p>
        </div>
      )}
    </div>
  );
}
