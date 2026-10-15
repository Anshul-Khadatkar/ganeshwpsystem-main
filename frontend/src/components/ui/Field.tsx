import { InputHTMLAttributes, forwardRef } from 'react';
import clsx from 'clsx';

interface FieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  hint?: string;
  required?: boolean;
}

export const Field = forwardRef<HTMLInputElement, FieldProps>(
  ({ label, error, hint, required, className, id, ...props }, ref) => {
    const inputId = id || label?.toLowerCase().replace(/\s+/g, '-');
    return (
      <div className="flex flex-col gap-1.5">
        {label && (
          <label htmlFor={inputId} className="text-sm font-medium text-ink">
            {label}
            {required && <span className="text-vermilion ml-0.5">*</span>}
            {!required && <span className="text-ink30 font-normal ml-1">(optional)</span>}
          </label>
        )}
        <input
          ref={ref}
          id={inputId}
          className={clsx(
            'rounded-md border border-hairline bg-white px-3 py-2 text-sm text-ink',
            'placeholder:text-ink30 focus:outline-none focus-visible:ring-2 focus-visible:ring-marigold',
            error && 'border-vermilion',
            className
          )}
          {...props}
        />
        {hint && !error && <span className="text-xs text-ink50">{hint}</span>}
        {error && <span className="text-xs text-vermilion">{error}</span>}
      </div>
    );
  }
);
Field.displayName = 'Field';
