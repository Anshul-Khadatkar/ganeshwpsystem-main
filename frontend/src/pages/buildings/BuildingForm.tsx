import { useState, FormEvent } from 'react';
import { Field } from '@/components/ui/Field';
import { Textarea } from '@/components/ui/Textarea';
import { Button } from '@/components/ui/Button';
import type { Building, BuildingRequest } from '@/types';
import { ApiRequestError } from '@/services/api';

interface BuildingFormProps {
  initial?: Building | null;
  onSubmit: (request: BuildingRequest) => Promise<void>;
  onCancel: () => void;
}

export function BuildingForm({ initial, onSubmit, onCancel }: BuildingFormProps) {
  const [name, setName] = useState(initial?.name ?? '');
  const [description, setDescription] = useState(initial?.description ?? '');
  const [requiresFloor, setRequiresFloor] = useState(initial?.requiresFloor ?? true);
  const [requiresFlat, setRequiresFlat] = useState(initial?.requiresFlat ?? true);
  const [active, setActive] = useState(initial?.active ?? true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await onSubmit({
        name: name.trim(),
        description: description.trim() || null,
        active,
        requiresFloor,
        requiresFlat,
        sortOrder: initial?.sortOrder ?? 0,
      });
    } catch (err) {
      setError(err instanceof ApiRequestError ? err.message : 'Failed to save building');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <Field
        label="Building / category name"
        required
        value={name}
        onChange={(e) => setName(e.target.value)}
        placeholder="e.g. Magnolia Heights"
        maxLength={100}
      />
      <Textarea
        label="Description"
        value={description ?? ''}
        onChange={(e) => setDescription(e.target.value)}
        placeholder="e.g. Tower located near the main gate."
      />

      <div className="rounded-md border border-hairline p-3 flex flex-col gap-2">
        <p className="text-sm font-medium text-ink">Location fields for this category</p>
        <p className="text-xs text-ink50">
          Controls whether the donation form asks for a floor / flat number when this category is
          selected — no code change needed for new category types.
        </p>
        <label className="flex items-center gap-2 text-sm text-ink mt-1">
          <input
            type="checkbox"
            checked={requiresFloor}
            onChange={(e) => setRequiresFloor(e.target.checked)}
            className="rounded border-hairline text-ink focus-visible:ring-2 focus-visible:ring-marigold"
          />
          Contributors have a floor number
        </label>
        <label className="flex items-center gap-2 text-sm text-ink">
          <input
            type="checkbox"
            checked={requiresFlat}
            onChange={(e) => setRequiresFlat(e.target.checked)}
            className="rounded border-hairline text-ink focus-visible:ring-2 focus-visible:ring-marigold"
          />
          Contributors have a flat number
        </label>
      </div>

      <label className="flex items-center gap-2 text-sm text-ink">
        <input
          type="checkbox"
          checked={active}
          onChange={(e) => setActive(e.target.checked)}
          className="rounded border-hairline text-ink focus-visible:ring-2 focus-visible:ring-marigold"
        />
        Active (visible in building selectors)
      </label>

      {error && (
        <p className="text-sm text-vermilion bg-vermilion/5 border border-vermilion/20 rounded-md px-3 py-2">
          {error}
        </p>
      )}

      <div className="flex justify-end gap-2 pt-2">
        <Button type="button" variant="secondary" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit" disabled={submitting || !name.trim()}>
          {submitting ? 'Saving…' : initial ? 'Save changes' : 'Add building'}
        </Button>
      </div>
    </form>
  );
}
