import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { Card } from '@/components/ui/Card';
import { EmptyState } from '@/components/ui/EmptyState';
import { formatINR } from '@/utils/currency';
import type { BuildingAnalytics } from '@/types';

export function BuildingCollectionWidget({ data }: { data: BuildingAnalytics[] }) {
  const chartData = data.map((d) => ({ name: d.buildingName, total: d.totalDonation }));

  return (
    <Card className="p-5">
      <p className="text-sm font-medium text-ink mb-4">Building-wise collection</p>
      {chartData.length === 0 ? (
        <EmptyState title="No donations yet" />
      ) : (
        <ResponsiveContainer width="100%" height={260}>
          <BarChart data={chartData} margin={{ left: 8, right: 8 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#E7E2DA" vertical={false} />
            <XAxis dataKey="name" tick={{ fontSize: 11, fill: '#6B6560' }} interval={0} angle={-20} textAnchor="end" height={50} />
            <YAxis tick={{ fontSize: 11, fill: '#6B6560' }} tickFormatter={(v) => `₹${(v / 1000).toFixed(0)}k`} />
            <Tooltip formatter={(value: number) => formatINR(value)} contentStyle={{ fontSize: 12, borderRadius: 8, borderColor: '#E7E2DA' }} />
            <Bar dataKey="total" fill="#E8A33D" radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      )}
    </Card>
  );
}
