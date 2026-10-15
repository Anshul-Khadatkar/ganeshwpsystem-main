import { useEffect, useMemo, useState } from 'react';
import {
  createColumnHelper,
  flexRender,
  getCoreRowModel,
  useReactTable,
  type SortingState,
} from '@tanstack/react-table';
import { useBuildings } from '@/hooks/useBuildings';
import { donationService } from '@/services/donationService';
import { formatINR, orDash } from '@/utils/currency';
import { formatDate, resolveDatePreset, type DatePreset } from '@/utils/date';
import { Card } from '@/components/ui/Card';
import { Field } from '@/components/ui/Field';
import { Select } from '@/components/ui/Select';
import { Button } from '@/components/ui/Button';
import { WhatsAppStatusBadge } from '@/components/ui/Badge';
import { EmptyState } from '@/components/ui/EmptyState';
import { PaginationBar } from '@/components/ui/PaginationBar';
import { Modal } from '@/components/ui/Modal';
import { ApiRequestError } from '@/services/api';
import type { Donation, WhatsAppStatus } from '@/types';

const AMOUNT_PRESETS = [
  { label: 'Any amount', min: undefined },
  { label: '> ₹500', min: 500 },
  { label: '> ₹1,000', min: 1000 },
  { label: '> ₹2,500', min: 2500 },
  { label: '> ₹5,000', min: 5000 },
  { label: '> ₹10,000', min: 10000 },
];

const columnHelper = createColumnHelper<Donation>();

export default function DonationsPage() {
  const { buildings } = useBuildings(false);

  const [buildingId, setBuildingId] = useState<number | ''>('');
  const [minAmount, setMinAmount] = useState<number | undefined>(undefined);
  const [floor, setFloor] = useState('');
  const [flat, setFlat] = useState('');
  const [datePreset, setDatePreset] = useState<DatePreset>('custom');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const [search, setSearch] = useState('');
  const [whatsappStatus, setWhatsappStatus] = useState<WhatsAppStatus | ''>('');

  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sorting, setSorting] = useState<SortingState>([{ id: 'createdAt', desc: true }]);

  const [data, setData] = useState<Donation[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<number | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Donation | null>(null);

  const selectedBuilding = buildings.find((b) => b.id === buildingId);

  // Debounce free-text search so we don't fire a request per keystroke.
  const [debouncedSearch, setDebouncedSearch] = useState(search);
  useEffect(() => {
    const t = setTimeout(() => setDebouncedSearch(search), 350);
    return () => clearTimeout(t);
  }, [search]);

  useEffect(() => {
    setPage(0);
  }, [buildingId, minAmount, floor, flat, datePreset, dateFrom, dateTo, debouncedSearch, whatsappStatus, pageSize]);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);

    const range = datePreset === 'custom' ? { dateFrom: dateFrom || undefined, dateTo: dateTo || undefined } : resolveDatePreset(datePreset);
    const sort = sorting[0];

    donationService
      .search({
        buildingId: buildingId || undefined,
        minAmount,
        floor: floor || undefined,
        flat: flat || undefined,
        search: debouncedSearch || undefined,
        whatsappStatus: whatsappStatus || undefined,
        page,
        size: pageSize,
        sortBy: sort?.id,
        sortDir: sort?.desc ? 'desc' : 'asc',
        ...range,
      })
      .then((res) => {
        if (cancelled) return;
        setData(res.content);
        setTotalPages(res.totalPages);
        setTotalElements(res.totalElements);
      })
      .catch((e) => !cancelled && setError(e.message))
      .finally(() => !cancelled && setLoading(false));

    return () => {
      cancelled = true;
    };
  }, [buildingId, minAmount, floor, flat, datePreset, dateFrom, dateTo, debouncedSearch, whatsappStatus, page, pageSize, sorting]);

  const reloadCurrentPage = async () => {
    const range =
      datePreset === 'custom'
        ? { dateFrom: dateFrom || undefined, dateTo: dateTo || undefined }
        : resolveDatePreset(datePreset);
    const sort = sorting[0];
    const res = await donationService.search({
      buildingId: buildingId || undefined,
      minAmount,
      floor: floor || undefined,
      flat: flat || undefined,
      search: debouncedSearch || undefined,
      whatsappStatus: whatsappStatus || undefined,
      page,
      size: pageSize,
      sortBy: sort?.id,
      sortDir: sort?.desc ? 'desc' : 'asc',
      ...range,
    });
    setData(res.content);
    setTotalPages(res.totalPages);
    setTotalElements(res.totalElements);
  };

  const handleResend = async (donation: Donation) => {
    setActionError(null);
    setBusyId(donation.id);
    try {
      await donationService.retryWhatsApp(donation.id);
      await reloadCurrentPage();
    } catch (e) {
      setActionError(e instanceof ApiRequestError ? e.message : 'Resend failed');
    } finally {
      setBusyId(null);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setActionError(null);
    setBusyId(deleteTarget.id);
    try {
      await donationService.remove(deleteTarget.id);
      setDeleteTarget(null);
      await reloadCurrentPage();
    } catch (e) {
      setActionError(e instanceof ApiRequestError ? e.message : 'Delete failed');
      setDeleteTarget(null);
    } finally {
      setBusyId(null);
    }
  };

  const columns = useMemo(
    () => [
      columnHelper.accessor((row) => row.contributor.name, {
        id: 'name',
        header: 'Contributor',
        cell: (info) => (
          <div>
            <p className="font-medium text-ink">{info.getValue()}</p>
            <p className="text-xs text-ink50">{info.row.original.contributor.mobileNumber}</p>
          </div>
        ),
      }),
      columnHelper.accessor((row) => row.contributor.buildingName, {
        id: 'building',
        header: 'Building',
      }),
      columnHelper.accessor((row) => row.contributor.floorNumber, {
        id: 'floor',
        header: 'Floor',
        cell: (info) => orDash(info.getValue()),
      }),
      columnHelper.accessor((row) => row.contributor.flatNumber, {
        id: 'flat',
        header: 'Flat',
        cell: (info) => orDash(info.getValue()),
      }),
      columnHelper.accessor('amount', {
        header: 'Amount',
        cell: (info) => <span className="figures font-medium">{formatINR(info.getValue())}</span>,
      }),
      columnHelper.accessor('donationDate', {
        header: 'Date',
        cell: (info) => formatDate(info.getValue()),
      }),
      columnHelper.accessor('whatsappStatus', {
        header: 'WhatsApp',
        cell: (info) => <WhatsAppStatusBadge status={info.getValue()} />,
      }),
      columnHelper.display({
        id: 'actions',
        header: () => <span className="block text-right">Actions</span>,
        cell: ({ row }) => {
          const d = row.original;
          const canResend = d.whatsappStatus === 'FAILED' || d.whatsappStatus === 'NOT_SENT';
          const busy = busyId === d.id;
          return (
            <div className="flex flex-wrap justify-end gap-1">
              {canResend && (
                <Button
                  type="button"
                  variant="ghost"
                  size="sm"
                  disabled={busy}
                  onClick={() => handleResend(d)}
                >
                  {busy ? '…' : 'Resend'}
                </Button>
              )}
              <Button
                type="button"
                variant="ghost"
                size="sm"
                className="text-vermilion hover:text-vermilion"
                disabled={busy}
                onClick={() => setDeleteTarget(d)}
              >
                Delete
              </Button>
            </div>
          );
        },
      }),
    ],
    [busyId]
  );

  const table = useReactTable({
    data,
    columns,
    state: { sorting },
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    manualSorting: true,
    manualPagination: true,
  });

  const SORTABLE_IDS = new Set(['amount', 'donationDate', 'name', 'building', 'floor', 'flat']);

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-semibold">Donations</h1>
        <p className="text-sm text-ink50 mt-1">{totalElements.toLocaleString('en-IN')} donations recorded</p>
      </div>

      <Card className="p-4">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
          <Select
            label="Building"
            value={buildingId}
            onChange={(e) => setBuildingId(e.target.value ? Number(e.target.value) : '')}
          >
            <option value="">All buildings</option>
            {buildings.map((b) => (
              <option key={b.id} value={b.id}>
                {b.name}
              </option>
            ))}
          </Select>

          <Select
            label="Amount"
            value={minAmount ?? ''}
            onChange={(e) => setMinAmount(e.target.value ? Number(e.target.value) : undefined)}
          >
            {AMOUNT_PRESETS.map((p) => (
              <option key={p.label} value={p.min ?? ''}>
                {p.label}
              </option>
            ))}
          </Select>

          {selectedBuilding?.requiresFloor && (
            <Field label="Floor" value={floor} onChange={(e) => setFloor(e.target.value)} placeholder="e.g. 3" />
          )}
          {selectedBuilding?.requiresFlat && (
            <Field label="Flat" value={flat} onChange={(e) => setFlat(e.target.value)} placeholder="e.g. 302" />
          )}

          <Select label="WhatsApp status" value={whatsappStatus} onChange={(e) => setWhatsappStatus(e.target.value as WhatsAppStatus | '')}>
            <option value="">Any status</option>
            <option value="SENT">Sent</option>
            <option value="PENDING">Pending</option>
            <option value="FAILED">Failed</option>
            <option value="NOT_SENT">Not sent</option>
          </Select>

          <Field
            label="Search"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Name, mobile, or flat"
          />
        </div>

        <div className="flex flex-wrap items-end gap-3 mt-4 pt-4 border-t border-hairline">
          <div className="flex gap-1.5">
            {(['today', 'thisWeek', 'thisMonth', 'custom'] as DatePreset[]).map((preset) => (
              <button
                key={preset}
                onClick={() => setDatePreset(preset)}
                className={`px-3 py-1.5 rounded-md text-xs font-medium border ${
                  datePreset === preset
                    ? 'bg-ink text-white border-ink'
                    : 'bg-white text-ink50 border-hairline hover:text-ink'
                }`}
              >
                {preset === 'today' ? 'Today' : preset === 'thisWeek' ? 'This week' : preset === 'thisMonth' ? 'This month' : 'Custom range'}
              </button>
            ))}
          </div>
          {datePreset === 'custom' && (
            <div className="flex gap-2 items-end">
              <Field label="From" type="date" value={dateFrom} onChange={(e) => setDateFrom(e.target.value)} />
              <Field label="To" type="date" value={dateTo} onChange={(e) => setDateTo(e.target.value)} />
            </div>
          )}
        </div>
      </Card>

      {error && (
        <p className="text-sm text-vermilion bg-vermilion/5 border border-vermilion/20 rounded-md px-3 py-2">{error}</p>
      )}
      {actionError && (
        <p className="text-sm text-vermilion bg-vermilion/5 border border-vermilion/20 rounded-md px-3 py-2">{actionError}</p>
      )}

      <Card className="overflow-hidden">
        {loading ? (
          <div className="p-8 text-center text-sm text-ink50">Loading donations…</div>
        ) : data.length === 0 ? (
          <EmptyState title="No donations match these filters" description="Try widening the date range or clearing a filter." />
        ) : (
          <>
            <div className="table-scroll">
            <table>
              <thead>
                {table.getHeaderGroups().map((hg) => (
                  <tr key={hg.id} className="border-b border-hairline text-left text-xs uppercase tracking-wide text-ink50">
                    {hg.headers.map((header) => {
                      const sortable = SORTABLE_IDS.has(header.column.id);
                      const sortState = sorting[0]?.id === header.column.id ? sorting[0] : undefined;
                      return (
                        <th
                          key={header.id}
                          className={`px-4 py-3 font-medium select-none ${sortable ? 'cursor-pointer hover:text-ink' : ''}`}
                          onClick={sortable ? header.column.getToggleSortingHandler() : undefined}
                        >
                          {flexRender(header.column.columnDef.header, header.getContext())}
                          {sortState && <span className="ml-1">{sortState.desc ? '↓' : '↑'}</span>}
                        </th>
                      );
                    })}
                  </tr>
                ))}
              </thead>
              <tbody>
                {table.getRowModel().rows.map((row) => (
                  <tr key={row.id} className="border-b border-hairline last:border-0 hover:bg-paper/60">
                    {row.getVisibleCells().map((cell) => (
                      <td key={cell.id} className="px-4 py-3">
                        {flexRender(cell.column.columnDef.cell, cell.getContext())}
                      </td>
                    ))}
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

      <Modal open={Boolean(deleteTarget)} onClose={() => setDeleteTarget(null)} title="Delete donation">
        {deleteTarget && (
          <div className="flex flex-col gap-4">
            <p className="text-sm text-ink50">
              Delete donation from{' '}
              <span className="font-medium text-ink">{deleteTarget.contributor.name}</span> (
              {formatINR(deleteTarget.amount)})? This removes it from the list. Use{' '}
              <span className="font-medium text-ink">Resend</span> on Failed rows if you only want to retry WhatsApp.
            </p>
            <div className="flex flex-col-reverse sm:flex-row sm:justify-end gap-2">
              <Button type="button" variant="secondary" onClick={() => setDeleteTarget(null)}>
                Cancel
              </Button>
              <Button type="button" variant="danger" disabled={busyId === deleteTarget.id} onClick={handleDelete}>
                {busyId === deleteTarget.id ? 'Deleting…' : 'Delete donation'}
              </Button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
