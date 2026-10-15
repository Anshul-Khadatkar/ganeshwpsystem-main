import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { Card } from '@/components/ui/Card';
import { EmptyState } from '@/components/ui/EmptyState';
import { formatINR } from '@/utils/currency';
import type { FloorAnalytics } from '@/types';

export function FloorCollectionWidget({ data }: { data: FloorAnalytics[] }) {
  return (
    <Card className="p-5">
      <p className="text-sm font-medium text-ink mb-4">Floor-wise collection</p>
      {data.length === 0 ? (
        <EmptyState title="No floor-wise donations yet" />
      ) : (
        <ResponsiveContainer width="100%" height={240}>
          <BarChart data={data} margin={{ left: 8, right: 8 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#E7E2DA" vertical={false} />
            <XAxis dataKey="floorNumber" tick={{ fontSize: 11, fill: '#6B6560' }} />
            <YAxis tick={{ fontSize: 11, fill: '#6B6560' }} tickFormatter={(v) => `₹${(v / 1000).toFixed(0)}k`} />
            <Tooltip formatter={(value: number) => formatINR(value)} contentStyle={{ fontSize: 12, borderRadius: 8, borderColor: '#E7E2DA' }} />
            <Bar dataKey="totalDonation" fill="#1F3A3D" radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      )}
    </Card>
  );
}
