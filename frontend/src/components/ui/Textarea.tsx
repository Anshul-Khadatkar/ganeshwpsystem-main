import { TextareaHTMLAttributes, forwardRef } from 'react';
import clsx from 'clsx';

interface TextareaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string;
  error?: string;
}

export const Textarea = forwardRef<HTMLTextAreaElement, TextareaProps>(
  ({ label, error, className, id, ...props }, ref) => {
    const inputId = id || label?.toLowerCase().replace(/\s+/g, '-');
    return (
      <div className="flex flex-col gap-1.5">
        {label && (
          <label htmlFor={inputId} className="text-sm font-medium text-ink">
            {label}
            <span className="text-ink30 font-normal ml-1">(optional)</span>
          </label>
        )}
        <textarea
          ref={ref}
          id={inputId}
          rows={3}
          className={clsx(
            'rounded-md border border-hairline bg-white px-3 py-2 text-sm text-ink resize-y',
            'placeholder:text-ink30 focus:outline-none focus-visible:ring-2 focus-visible:ring-marigold',
            error && 'border-vermilion',
            className
          )}
          {...props}
        />
        {error && <span className="text-xs text-vermilion">{error}</span>}
      </div>
    );
  }
);
Textarea.displayName = 'Textarea';
