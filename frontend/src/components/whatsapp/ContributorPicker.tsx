import { useEffect, useState } from 'react';
import { contributorService } from '@/services/contributorService';
import { Field } from '@/components/ui/Field';
import type { Contributor } from '@/types';

interface ContributorPickerProps {
  selected: Contributor[];
  onChange: (contributors: Contributor[]) => void;
}

export function ContributorPicker({ selected, onChange }: ContributorPickerProps) {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<Contributor[]>([]);
  const [searching, setSearching] = useState(false);

  useEffect(() => {
    if (query.trim().length < 2) {
      setResults([]);
      return;
    }
    let cancelled = false;
    setSearching(true);
    const t = setTimeout(() => {
      contributorService
        .search({ search: query, whatsappOptInOnly: true, size: 8 })
        .then((res) => !cancelled && setResults(res.content))
        .finally(() => !cancelled && setSearching(false));
    }, 300);
    return () => {
      cancelled = true;
      clearTimeout(t);
    };
  }, [query]);

  const add = (c: Contributor) => {
    if (!selected.some((s) => s.id === c.id)) {
      onChange([...selected, c]);
    }
    setQuery('');
    setResults([]);
  };

  const remove = (id: number) => {
    onChange(selected.filter((s) => s.id !== id));
  };

  return (
    <div className="flex flex-col gap-2">
      <div className="relative">
        <Field
          label="Search contributors"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Name or mobile number"
        />
        {query.trim().length >= 2 && (
          <div className="absolute z-10 mt-1 w-full bg-white border border-hairline rounded-md shadow-card max-h-56 overflow-y-auto">
            {searching ? (
              <p className="px-3 py-2 text-xs text-ink50">Searching…</p>
            ) : results.length === 0 ? (
              <p className="px-3 py-2 text-xs text-ink50">No opted-in contributors found</p>
            ) : (
              results.map((c) => (
                <button
                  key={c.id}
                  type="button"
                  onClick={() => add(c)}
                  className="w-full text-left px-3 py-2 text-sm hover:bg-paper flex justify-between"
                >
                  <span className="text-ink">{c.name}</span>
                  <span className="text-ink50 text-xs">{c.buildingName}</span>
                </button>
              ))
            )}
          </div>
        )}
      </div>

      {selected.length > 0 && (
        <div className="flex flex-wrap gap-1.5">
          {selected.map((c) => (
            <span
              key={c.id}
              className="inline-flex items-center gap-1 pl-2.5 pr-1.5 py-1 rounded-full bg-paper border border-hairline text-xs text-ink"
            >
              {c.name}
              <button
                type="button"
                onClick={() => remove(c.id)}
                aria-label={`Remove ${c.name}`}
                className="text-ink30 hover:text-vermilion leading-none"
              >
                ×
              </button>
            </span>
          ))}
        </div>
      )}
    </div>
  );
}
