import { Button } from '@/components/ui/Button';

interface PaginationBarProps {
  page: number;
  totalPages: number;
  totalElements: number;
  pageSize: number;
  onPageChange: (page: number) => void;
  onPageSizeChange?: (size: number) => void;
  pageSizeOptions?: number[];
  disabled?: boolean;
}

/** Compact server-side pagination controls for large lists (thousands of rows). */
export function PaginationBar({
  page,
  totalPages,
  totalElements,
  pageSize,
  onPageChange,
  onPageSizeChange,
  pageSizeOptions = [20, 50, 100],
  disabled = false,
}: PaginationBarProps) {
  const safeTotalPages = Math.max(totalPages, 1);
  const from = totalElements === 0 ? 0 : page * pageSize + 1;
  const to = Math.min((page + 1) * pageSize, totalElements);

  const pageNumbers = buildPageWindow(page, safeTotalPages);

  return (
    <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between px-4 py-3 border-t border-hairline text-sm text-ink50">
      <div className="flex flex-wrap items-center gap-3">
        <span>
          Showing <span className="figures text-ink">{from}</span>–
          <span className="figures text-ink">{to}</span> of{' '}
          <span className="figures text-ink">{totalElements.toLocaleString('en-IN')}</span>
        </span>
        {onPageSizeChange && (
          <label className="inline-flex items-center gap-1.5">
            <span className="text-xs uppercase tracking-wide">Per page</span>
            <select
              className="rounded-md border border-hairline bg-white px-2 py-1 text-sm text-ink"
              value={pageSize}
              disabled={disabled}
              onChange={(e) => onPageSizeChange(Number(e.target.value))}
            >
              {pageSizeOptions.map((n) => (
                <option key={n} value={n}>
                  {n}
                </option>
              ))}
            </select>
          </label>
        )}
      </div>

      <div className="flex flex-wrap items-center gap-1">
        <Button variant="secondary" size="sm" disabled={disabled || page === 0} onClick={() => onPageChange(0)}>
          First
        </Button>
        <Button
          variant="secondary"
          size="sm"
          disabled={disabled || page === 0}
          onClick={() => onPageChange(page - 1)}
        >
          Prev
        </Button>
        {pageNumbers.map((n, i) =>
          n === '…' ? (
            <span key={`ellipsis-${i}`} className="px-1 text-ink50">
              …
            </span>
          ) : (
            <Button
              key={n}
              variant={n === page ? 'primary' : 'secondary'}
              size="sm"
              disabled={disabled}
              onClick={() => onPageChange(n)}
              className="min-w-[2.25rem]"
            >
              {n + 1}
            </Button>
          )
        )}
        <Button
          variant="secondary"
          size="sm"
          disabled={disabled || page + 1 >= safeTotalPages}
          onClick={() => onPageChange(page + 1)}
        >
          Next
        </Button>
        <Button
          variant="secondary"
          size="sm"
          disabled={disabled || page + 1 >= safeTotalPages}
          onClick={() => onPageChange(safeTotalPages - 1)}
        >
          Last
        </Button>
      </div>
    </div>
  );
}

function buildPageWindow(current: number, total: number): Array<number | '…'> {
  if (total <= 7) {
    return Array.from({ length: total }, (_, i) => i);
  }
  const pages = new Set<number>();
  pages.add(0);
  pages.add(total - 1);
  for (let i = current - 1; i <= current + 1; i++) {
    if (i >= 0 && i < total) pages.add(i);
  }
  const sorted = [...pages].sort((a, b) => a - b);
  const out: Array<number | '…'> = [];
  let prev = -1;
  for (const p of sorted) {
    if (prev >= 0 && p - prev > 1) out.push('…');
    out.push(p);
    prev = p;
  }
  return out;
}
