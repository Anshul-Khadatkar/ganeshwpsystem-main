import clsx from 'clsx';
import type { WhatsAppStatus } from '@/types';

const WHATSAPP_STYLES: Record<WhatsAppStatus, string> = {
  SENT: 'bg-moss/10 text-moss',
  PENDING: 'bg-marigold/15 text-marigold-dark',
  FAILED: 'bg-vermilion/10 text-vermilion',
  NOT_SENT: 'bg-ink30/10 text-ink50',
};

const WHATSAPP_LABELS: Record<WhatsAppStatus, string> = {
  SENT: 'Sent',
  PENDING: 'Pending',
  FAILED: 'Failed',
  NOT_SENT: 'Not sent',
};

export function WhatsAppStatusBadge({ status }: { status: WhatsAppStatus }) {
  return (
    <span className={clsx('inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium', WHATSAPP_STYLES[status])}>
      {WHATSAPP_LABELS[status]}
    </span>
  );
}

export function StatusBadge({ active }: { active: boolean }) {
  return (
    <span
      className={clsx(
        'inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium',
        active ? 'bg-moss/10 text-moss' : 'bg-ink30/10 text-ink50'
      )}
    >
      {active ? 'Active' : 'Inactive'}
    </span>
  );
}

const BROADCAST_STYLES: Record<string, string> = {
  COMPLETED: 'bg-moss/10 text-moss',
  IN_PROGRESS: 'bg-marigold/15 text-marigold-dark',
  PENDING: 'bg-marigold/15 text-marigold-dark',
  COMPLETED_WITH_ERRORS: 'bg-vermilion/10 text-vermilion',
  FAILED: 'bg-vermilion/10 text-vermilion',
};

const BROADCAST_LABELS: Record<string, string> = {
  COMPLETED: 'Completed',
  IN_PROGRESS: 'In progress',
  PENDING: 'Pending',
  COMPLETED_WITH_ERRORS: 'Completed with errors',
  FAILED: 'Failed',
};

export function BroadcastStatusBadge({ status }: { status: string }) {
  return (
    <span className={clsx('inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium', BROADCAST_STYLES[status])}>
      {BROADCAST_LABELS[status] ?? status}
    </span>
  );
}
