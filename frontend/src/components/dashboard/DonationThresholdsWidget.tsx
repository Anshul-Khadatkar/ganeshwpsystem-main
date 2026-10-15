import { Card } from '@/components/ui/Card';
import { EmptyState } from '@/components/ui/EmptyState';
import type { ThresholdBucket } from '@/types';

export function DonationThresholdsWidget({ data }: { data: ThresholdBucket[] }) {
  const max = Math.max(1, ...data.map((b) => b.donationCount));
  const hasAny = data.some((b) => b.donationCount > 0);

  return (
    <Card className="p-5">
      <p className="text-sm font-medium text-ink mb-4">Donation thresholds</p>
      {!hasAny ? (
        <EmptyState title="No donations yet" />
      ) : (
        <div className="flex flex-col gap-3">
          {data.map((bucket) => (
            <div key={bucket.label} className="flex items-center gap-3 text-sm">
              <span className="w-32 shrink-0 text-ink50 text-xs">{bucket.label}</span>
              <div className="flex-1 h-2 rounded-full bg-paper overflow-hidden">
                <div
                  className="h-full bg-marigold rounded-full"
                  style={{ width: `${(bucket.donationCount / max) * 100}%` }}
                />
              </div>
              <span className="figures w-8 text-right text-ink shrink-0">{bucket.donationCount}</span>
            </div>
          ))}
        </div>
      )}
    </Card>
  );
}
