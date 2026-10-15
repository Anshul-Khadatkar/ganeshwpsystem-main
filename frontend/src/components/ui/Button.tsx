import { ButtonHTMLAttributes, forwardRef } from 'react';
import clsx from 'clsx';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger';
  size?: 'sm' | 'md';
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = 'primary', size = 'md', ...props }, ref) => {
    return (
      <button
        ref={ref}
        className={clsx(
          'inline-flex items-center justify-center gap-2 rounded-md font-medium transition-colors',
          'disabled:opacity-50 disabled:cursor-not-allowed',
          size === 'md' ? 'px-4 py-2 text-sm' : 'px-3 py-1.5 text-xs',
          variant === 'primary' && 'bg-ink text-white hover:bg-ink-light',
          variant === 'secondary' && 'bg-white text-ink border border-hairline hover:bg-paper',
          variant === 'ghost' && 'text-ink50 hover:text-ink hover:bg-paper',
          variant === 'danger' && 'bg-vermilion text-white hover:bg-vermilion-light',
          className
        )}
        {...props}
      />
    );
  }
);
Button.displayName = 'Button';
