import { Card } from '@/components/ui/Card';
import type { WhatsAppStats } from '@/types';

export function WhatsAppStatisticsWidget({ data }: { data: WhatsAppStats }) {
  const total = data.sent + data.pending + data.failed + data.notSent;

  const rows: { label: string; value: number; className: string }[] = [
    { label: 'Sent', value: data.sent, className: 'text-moss' },
    { label: 'Pending', value: data.pending, className: 'text-marigold-dark' },
    { label: 'Failed', value: data.failed, className: 'text-vermilion' },
    { label: 'Not sent', value: data.notSent, className: 'text-ink50' },
  ];

  return (
    <Card className="p-5">
      <p className="text-sm font-medium text-ink mb-4">WhatsApp statistics</p>
      <div className="grid grid-cols-2 gap-4">
        {rows.map((row) => (
          <div key={row.label}>
            <p className={`figures text-xl font-semibold ${row.className}`}>{row.value}</p>
            <p className="text-xs text-ink50 mt-0.5">{row.label}</p>
          </div>
        ))}
      </div>
      {total === 0 && <p className="text-xs text-ink30 mt-3">No WhatsApp sends recorded yet.</p>}
    </Card>
  );
}
