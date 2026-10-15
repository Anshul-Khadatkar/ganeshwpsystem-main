import { ReactNode, useEffect } from 'react';
import { createPortal } from 'react-dom';

interface ModalProps {
  open: boolean;
  onClose: () => void;
  title: string;
  children: ReactNode;
}

export function Modal({ open, onClose, title, children }: ModalProps) {
  useEffect(() => {
    if (!open) return;
    const handleKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', handleKey);
    return () => window.removeEventListener('keydown', handleKey);
  }, [open, onClose]);

  if (!open) return null;

  return createPortal(
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4">
      <div className="absolute inset-0 bg-ink/40" onClick={onClose} aria-hidden="true" />
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title"
        className="relative w-full sm:max-w-lg rounded-t-2xl sm:rounded-card bg-surface shadow-card border border-hairline max-h-[92vh] sm:max-h-[90vh] overflow-y-auto"
      >
        <div className="flex items-center justify-between px-4 sm:px-6 py-4 border-b border-hairline sticky top-0 bg-surface z-10">
          <h2 id="modal-title" className="font-display text-lg text-ink pr-2">
            {title}
          </h2>
          <button
            onClick={onClose}
            aria-label="Close"
            className="text-ink50 hover:text-ink text-xl leading-none rounded focus-visible:ring-2 focus-visible:ring-marigold shrink-0"
          >
            ×
          </button>
        </div>
        <div className="p-4 sm:p-6">{children}</div>
      </div>
    </div>,
    document.body
  );
}
