import { Link } from 'react-router-dom';
import { Card } from '@/components/ui/Card';
import { EmptyState } from '@/components/ui/EmptyState';
import { WhatsAppStatusBadge } from '@/components/ui/Badge';
import { formatINR, orDash } from '@/utils/currency';
import { formatDate } from '@/utils/date';
import type { Donation } from '@/types';

export function RecentDonationsWidget({ data }: { data: Donation[] }) {
  return (
    <Card className="p-5">
      <div className="flex items-center justify-between gap-3 mb-4">
        <p className="text-sm font-medium text-ink">Recent donations</p>
        <Link to="/donations" className="text-xs font-medium text-marigold hover:text-marigold-dark shrink-0">
          View more
        </Link>
      </div>
      {data.length === 0 ? (
        <EmptyState title="No donations yet" />
      ) : (
        <ul className="flex flex-col gap-3">
          {data.slice(0, 10).map((d) => (
            <li key={d.id} className="flex items-center justify-between text-sm gap-3">
              <div className="min-w-0">
                <p className="text-ink font-medium truncate">{d.contributor.name}</p>
                <p className="text-xs text-ink50">
                  {d.contributor.buildingName}
                  {d.contributor.flatNumber ? ` · ${orDash(d.contributor.flatNumber)}` : ''} · {formatDate(d.donationDate)}
                </p>
              </div>
              <div className="flex items-center gap-2 shrink-0">
                <span className="figures font-medium text-ink">{formatINR(d.amount)}</span>
                <WhatsAppStatusBadge status={d.whatsappStatus} />
              </div>
            </li>
          ))}
        </ul>
      )}
    </Card>
  );
}
