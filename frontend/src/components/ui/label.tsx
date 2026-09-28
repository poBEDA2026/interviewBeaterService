import { forwardRef, LabelHTMLAttributes } from 'react';
import { cn } from '@/lib/utils';

export const Label = forwardRef<HTMLLabelElement, LabelHTMLAttributes<HTMLLabelElement>>(
  ({ className, ...rest }, ref) => (
    <label ref={ref} className={cn('text-sm font-medium text-gray-700', className)} {...rest} />
  ),
);
Label.displayName = 'Label';
