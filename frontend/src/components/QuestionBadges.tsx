import { Question } from '@/api/questions';
import { cn } from '@/lib/utils';

const BADGE_BASE =
  'inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-medium';

const CATEGORY_BADGE = 'border-gray-200 bg-gray-100 text-gray-700';

const DIFFICULTY_BADGES: Record<string, string> = {
  junior: 'border-green-200 bg-green-100 text-green-700',
  middle: 'border-blue-200 bg-blue-100 text-blue-700',
  'middle+': 'border-amber-200 bg-amber-100 text-amber-700',
  senior: 'border-red-200 bg-red-100 text-red-700',
};

const DIFFICULTY_FALLBACK = 'border-gray-200 bg-gray-100 text-gray-700';

/**
 * Закруглённые виджеты с категорией и сложностью вопроса.
 * Сложность подсвечивается по грейду: junior — зелёный, middle — синий,
 * middle+ — янтарный, senior — красный.
 */
export function QuestionBadges({ question }: { question: Question }) {
  const difficultyClass = DIFFICULTY_BADGES[question.difficulty.name] ?? DIFFICULTY_FALLBACK;

  return (
    <div className="flex flex-wrap items-center gap-2">
      <span className={cn(BADGE_BASE, CATEGORY_BADGE)}>{question.category.name}</span>
      <span className={cn(BADGE_BASE, difficultyClass)}>{question.difficulty.name}</span>
    </div>
  );
}
