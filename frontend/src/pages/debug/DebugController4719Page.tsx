import { FormEvent, useState } from 'react';
import { debugService } from '@/services/debugService';
import { Card } from '@/components/ui/Card';
import { Field } from '@/components/ui/Field';
import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import { ApiRequestError } from '@/services/api';

export default function DebugController4719Page() {
  const [password, setPassword] = useState('');
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<string | null>(null);

  const openConfirm = (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setResult(null);
    if (!password.trim()) {
      setError('Enter the debug password from DEBUG_CLEAR_PASSWORD.');
      return;
    }
    setConfirmOpen(true);
  };

  const runClear = async () => {
    setSubmitting(true);
    setError(null);
    setResult(null);
    try {
      const res = await debugService.clearData(password);
      const rows = Object.entries(res.deletedRows || {})
        .map(([table, count]) => `${table}: ${count}`)
        .join(', ');
      setResult(`${res.message}${rows ? ` (${rows})` : ''}`);
      setPassword('');
      setConfirmOpen(false);
    } catch (err) {
      setError(err instanceof ApiRequestError ? err.message : 'Clear failed.');
      setConfirmOpen(false);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-lg flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-semibold">Debug clear</h1>
        <p className="text-sm text-ink50 mt-1">
          Wipe donors (contributors), all donations / WhatsApp thank-you history, and broadcast history. Templates,
          buildings, admins, and settings are kept.
        </p>
      </div>

      <Card className="p-6 border border-vermilion/30">
        <p className="text-sm text-vermilion mb-4">
          Permanently deletes: contributors (donor names), donations, broadcasts, broadcast recipients, and audit
          logs. Cannot be undone.
        </p>

        <form onSubmit={openConfirm} className="flex flex-col gap-4">
          <Field
            label="Debug password"
            type="password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            hint="Must match DEBUG_CLEAR_PASSWORD on the server"
            autoComplete="off"
          />

          {error && (
            <p className="text-sm text-vermilion bg-vermilion/5 border border-vermilion/20 rounded-md px-3 py-2">
              {error}
            </p>
          )}

          {result && (
            <p className="text-sm text-moss bg-moss/5 border border-moss/20 rounded-md px-3 py-2">{result}</p>
          )}

          <Button type="submit" variant="danger" disabled={submitting || !password}>
            Clear database…
          </Button>
        </form>
      </Card>

      <Modal open={confirmOpen} onClose={() => !submitting && setConfirmOpen(false)} title="Confirm clear">
        <p className="text-sm text-ink50">
          Delete all donations, contributors, and broadcasts? Message templates will stay. Type was
          already checked — this runs immediately.
        </p>
        <div className="mt-6 flex flex-col-reverse sm:flex-row sm:justify-end gap-2">
          <Button type="button" variant="secondary" disabled={submitting} onClick={() => setConfirmOpen(false)}>
            Cancel
          </Button>
          <Button type="button" variant="danger" disabled={submitting} onClick={runClear}>
            {submitting ? 'Clearing…' : 'Yes, clear data'}
          </Button>
        </div>
      </Modal>
    </div>
  );
}
