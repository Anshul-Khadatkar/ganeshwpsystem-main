import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { broadcastService } from '@/services/broadcastService';
import { Card } from '@/components/ui/Card';
import { BroadcastStatusBadge } from '@/components/ui/Badge';
import { EmptyState } from '@/components/ui/EmptyState';
import { PaginationBar } from '@/components/ui/PaginationBar';
import { formatDateTime } from '@/utils/date';
import type { Broadcast } from '@/types';

export default function BroadcastHistoryPage() {
  const [broadcasts, setBroadcasts] = useState<Broadcast[]>([]);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setPage(0);
  }, [pageSize]);

  useEffect(() => {
    setLoading(true);
    broadcastService
      .history(page, pageSize)
      .then((res) => {
        setBroadcasts(res.content);
        setTotalPages(res.totalPages);
        setTotalElements(res.totalElements);
      })
      .finally(() => setLoading(false));
  }, [page, pageSize]);

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-xl sm:text-2xl font-semibold">Broadcast history</h1>
        <p className="text-sm text-ink50 mt-1">Every message sent, with delivery results.</p>
      </div>

      <Card className="overflow-hidden">
        {loading ? (
          <div className="p-8 text-center text-sm text-ink50">Loading…</div>
        ) : broadcasts.length === 0 ? (
          <EmptyState title="No broadcasts yet" />
        ) : (
          <>
            <div className="table-scroll">
              <table>
                <thead>
                  <tr className="border-b border-hairline text-left text-xs uppercase tracking-wide text-ink50">
                    <th className="px-4 py-3 font-medium">When</th>
                    <th className="px-4 py-3 font-medium">Message</th>
                    <th className="px-4 py-3 font-medium">Recipients</th>
                    <th className="px-4 py-3 font-medium">Status</th>
                    <th className="px-4 py-3 font-medium text-right"> </th>
                  </tr>
                </thead>
                <tbody>
                  {broadcasts.map((b) => (
                    <tr key={b.id} className="border-b border-hairline last:border-0 hover:bg-paper/60">
                      <td className="px-4 py-3 text-ink50 whitespace-nowrap">{formatDateTime(b.createdAt)}</td>
                      <td className="px-4 py-3">
                        <p className="text-ink truncate max-w-md">{b.message}</p>
                        <p className="text-xs text-ink50">{b.recipientCriteria}</p>
                      </td>
                      <td className="px-4 py-3 figures">
                        {b.successfulCount}/{b.totalRecipients}
                      </td>
                      <td className="px-4 py-3">
                        <BroadcastStatusBadge status={b.status} />
                      </td>
                      <td className="px-4 py-3 text-right">
                        <Link to={`/whatsapp/history/${b.id}`} className="text-sm text-marigold hover:text-marigold-dark">
                          Details
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <PaginationBar
              page={page}
              totalPages={totalPages}
              totalElements={totalElements}
              pageSize={pageSize}
              disabled={loading}
              onPageChange={setPage}
              onPageSizeChange={setPageSize}
            />
          </>
        )}
      </Card>
    </div>
  );
}
