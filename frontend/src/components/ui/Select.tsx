import { SelectHTMLAttributes, forwardRef } from 'react';
import clsx from 'clsx';

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label?: string;
  error?: string;
  required?: boolean;
}

export const Select = forwardRef<HTMLSelectElement, SelectProps>(
  ({ label, error, required, className, id, children, ...props }, ref) => {
    const inputId = id || label?.toLowerCase().replace(/\s+/g, '-');
    return (
      <div className="flex flex-col gap-1.5">
        {label && (
          <label htmlFor={inputId} className="text-sm font-medium text-ink">
            {label}
            {required && <span className="text-vermilion ml-0.5">*</span>}
          </label>
        )}
        <select
          ref={ref}
          id={inputId}
          className={clsx(
            'rounded-md border border-hairline bg-white px-3 py-2 text-sm text-ink',
            'focus:outline-none focus-visible:ring-2 focus-visible:ring-marigold',
            error && 'border-vermilion',
            className
          )}
          {...props}
        >
          {children}
        </select>
        {error && <span className="text-xs text-vermilion">{error}</span>}
      </div>
    );
  }
);
Select.displayName = 'Select';
