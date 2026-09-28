import { forwardRef, ButtonHTMLAttributes } from 'react';
import { cn } from '@/lib/utils';

type Variant = 'primary' | 'secondary';

interface Props extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  isLoading?: boolean;
}

const base =
  'inline-flex items-center justify-center rounded-md text-sm font-medium transition-colors ' +
  'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 focus-visible:ring-offset-2 ' +
  'disabled:pointer-events-none disabled:opacity-50 h-10 px-4';

const variants: Record<Variant, string> = {
  primary: 'bg-blue-600 text-white hover:bg-blue-700',
  secondary: 'bg-white text-gray-900 border border-gray-300 hover:bg-gray-50',
};

export const Button = forwardRef<HTMLButtonElement, Props>(
  ({ className, variant = 'primary', isLoading, disabled, children, ...rest }, ref) => (
    <button
      ref={ref}
      className={cn(base, variants[variant], className)}
      disabled={disabled || isLoading}
      {...rest}
    >
      {isLoading ? '…' : children}
    </button>
  ),
);
Button.displayName = 'Button';
