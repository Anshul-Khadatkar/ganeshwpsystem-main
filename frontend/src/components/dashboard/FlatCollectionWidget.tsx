import { Card } from '@/components/ui/Card';
import { EmptyState } from '@/components/ui/EmptyState';
import { formatINR, orDash } from '@/utils/currency';
import type { FlatAnalytics } from '@/types';

export function FlatCollectionWidget({ data }: { data: FlatAnalytics[] }) {
  return (
    <Card className="p-5">
      <p className="text-sm font-medium text-ink mb-4">Flat-wise collection</p>
      {data.length === 0 ? (
        <EmptyState title="No donations yet" />
      ) : (
        <div className="max-h-72 overflow-y-auto">
          <table>
            <thead>
              <tr className="text-left text-xs uppercase tracking-wide text-ink50 border-b border-hairline sticky top-0 bg-surface">
                <th className="py-2 pr-2 font-medium">Floor</th>
                <th className="py-2 pr-2 font-medium">Flat</th>
                <th className="py-2 pr-2 font-medium">Contributor</th>
                <th className="py-2 font-medium text-right">Donation</th>
              </tr>
            </thead>
            <tbody>
              {data.map((row) => (
                <tr key={row.contributorId} className="border-b border-hairline last:border-0">
                  <td className="py-2 pr-2 text-ink50">{orDash(row.floorNumber)}</td>
                  <td className="py-2 pr-2 text-ink50">{orDash(row.flatNumber)}</td>
                  <td className="py-2 pr-2 text-ink">{row.contributorName}</td>
                  <td className="py-2 text-right figures font-medium">{formatINR(row.totalDonation)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </Card>
  );
}
