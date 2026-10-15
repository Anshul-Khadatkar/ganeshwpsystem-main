import { Card } from '@/components/ui/Card';
import { EmptyState } from '@/components/ui/EmptyState';
import { formatINR } from '@/utils/currency';
import type { TopDonor } from '@/types';

export function TopDonorsWidget({ data }: { data: TopDonor[] }) {
  return (
    <Card className="p-5">
      <p className="text-sm font-medium text-ink mb-4">Top donors</p>
      {data.length === 0 ? (
        <EmptyState title="No donations yet" />
      ) : (
        <ul className="flex flex-col gap-3">
          {data.map((donor, i) => (
            <li key={donor.contributorId} className="flex items-center justify-between text-sm">
              <div className="flex items-center gap-3 min-w-0">
                <span className="figures text-xs text-ink30 w-4 shrink-0">{i + 1}</span>
                <div className="min-w-0">
                  <p className="text-ink font-medium truncate">{donor.contributorName}</p>
                  <p className="text-xs text-ink50">{donor.buildingName}</p>
                </div>
              </div>
              <span className="figures font-medium text-ink shrink-0">{formatINR(donor.totalDonation)}</span>
            </li>
          ))}
        </ul>
      )}
    </Card>
  );
}
