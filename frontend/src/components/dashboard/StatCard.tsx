import { Card } from '@/components/ui/Card';

interface StatCardProps {
  label: string;
  value: string;
  tone?: 'default' | 'marigold' | 'moss' | 'vermilion';
}

const TONE_STYLES: Record<NonNullable<StatCardProps['tone']>, string> = {
  default: 'text-ink',
  marigold: 'text-marigold-dark',
  moss: 'text-moss',
  vermilion: 'text-vermilion',
};

export function StatCard({ label, value, tone = 'default' }: StatCardProps) {
  return (
    <Card className="p-5">
      <p className="text-xs uppercase tracking-wide text-ink50 font-medium">{label}</p>
      <p className={`figures text-2xl font-semibold mt-2 ${TONE_STYLES[tone]}`}>{value}</p>
    </Card>
  );
}
