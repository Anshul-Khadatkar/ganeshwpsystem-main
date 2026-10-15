import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { broadcastService } from '@/services/broadcastService';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { BroadcastStatusBadge, WhatsAppStatusBadge } from '@/components/ui/Badge';
import { PaginationBar } from '@/components/ui/PaginationBar';
import { formatDateTime } from '@/utils/date';
import type { BroadcastDetail } from '@/types';

export default function BroadcastDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [detail, setDetail] = useState<BroadcastDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [retrying, setRetrying] = useState(false);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(50);

  const reload = () => {
    if (!id) return;
    setLoading(true);
    broadcastService
      .getDetail(Number(id), page, pageSize)
      .then(setDetail)
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    setPage(0);
  }, [id, pageSize]);

  useEffect(reload, [id, page, pageSize]);

  // Live progress while background dispatch is running
  useEffect(() => {
    if (!id || detail?.broadcast.status !== 'IN_PROGRESS') return;
    const timer = window.setInterval(() => {
      broadcastService.getDetail(Number(id), page, pageSize).then(setDetail).catch(() => undefined);
    }, 3000);
    return () => window.clearInterval(timer);
  }, [id, page, pageSize, detail?.broadcast.status]);

  const handleRetry = async () => {
    if (!id) return;
    setRetrying(true);
    try {
      await broadcastService.retryFailed(Number(id));
      reload();
    } finally {
      setRetrying(false);
    }
  };

  if (loading && !detail) {
    return <p className="text-sm text-ink50">Loading…</p>;
  }

  if (!detail) {
    return <p className="text-sm text-ink50">Broadcast not found</p>;
  }

  const { broadcast, recipients } = detail;

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <div>
          <Link to="/whatsapp/history" className="text-xs text-ink50 hover:text-ink">
            ← Back to history
          </Link>
          <h1 className="text-2xl font-semibold mt-1">Broadcast detail</h1>
          <p className="text-sm text-ink50 mt-1">{formatDateTime(broadcast.createdAt)} · {broadcast.recipientCriteria}</p>
        </div>
        {broadcast.failedCount > 0 && broadcast.status !== 'IN_PROGRESS' && (
          <Button onClick={handleRetry} disabled={retrying}>
            {retrying ? 'Retrying…' : `Retry ${broadcast.failedCount} failed`}
          </Button>
        )}
      </div>

      <Card className="p-6 flex flex-col gap-4">
        <div className="flex items-center gap-4">
          <BroadcastStatusBadge status={broadcast.status} />
          <span className="text-sm text-ink50">
            <span className="figures text-ink font-medium">{broadcast.totalRecipients}</span> recipients ·{' '}
            <span className="figures text-moss font-medium">{broadcast.successfulCount}</span> sent ·{' '}
            <span className="figures text-vermilion font-medium">{broadcast.failedCount}</span> failed
            {broadcast.status === 'IN_PROGRESS' && (
              <span className="ml-2 text-marigold">· sending in background…</span>
            )}
          </span>
        </div>
        <div className="rounded-md bg-paper border border-hairline p-3 text-sm text-ink whitespace-pre-wrap">
          {broadcast.message}
        </div>
        {broadcast.imageUrl && (
          <img src={broadcast.imageUrl} alt="Broadcast attachment" className="rounded-md border border-hairline max-h-48" />
        )}
      </Card>

      <Card className="overflow-hidden">
        <div className="table-scroll">
        <table>
          <thead>
            <tr className="border-b border-hairline text-left text-xs uppercase tracking-wide text-ink50">
              <th className="px-4 py-3 font-medium">Contributor</th>
              <th className="px-4 py-3 font-medium">Phone</th>
              <th className="px-4 py-3 font-medium">Status</th>
              <th className="px-4 py-3 font-medium">Sent at</th>
              <th className="px-4 py-3 font-medium">Error</th>
            </tr>
          </thead>
          <tbody>
            {recipients.content.map((r) => (
              <tr key={r.id} className="border-b border-hairline last:border-0 hover:bg-paper/60">
                <td className="px-4 py-3 text-ink">{r.contributorName}</td>
                <td className="px-4 py-3 text-ink50 figures">{r.phoneNumber}</td>
                <td className="px-4 py-3">
                  <WhatsAppStatusBadge status={r.status} />
                </td>
                <td className="px-4 py-3 text-ink50">{r.sentAt ? formatDateTime(r.sentAt) : '—'}</td>
                <td className="px-4 py-3 text-vermilion text-xs max-w-xs truncate">{r.errorMessage || '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
        </div>
        <PaginationBar
          page={page}
          totalPages={recipients.totalPages}
          totalElements={recipients.totalElements}
          pageSize={pageSize}
          disabled={loading}
          onPageChange={setPage}
          onPageSizeChange={setPageSize}
          pageSizeOptions={[25, 50, 100]}
        />
      </Card>
    </div>
  );
}
