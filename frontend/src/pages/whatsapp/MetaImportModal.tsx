import { useEffect, useState } from 'react';
import { templateService } from '@/services/templateService';
import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import { ApiRequestError } from '@/services/api';
import type { MetaRemoteTemplate } from '@/types';

interface MetaImportModalProps {
  open: boolean;
  onClose: () => void;
  onImported: () => void;
}

export function MetaImportModal({ open, onClose, onImported }: MetaImportModalProps) {
  const [templates, setTemplates] = useState<MetaRemoteTemplate[]>([]);
  const [loading, setLoading] = useState(false);
  const [importing, setImporting] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const load = () => {
    setLoading(true);
    setError(null);
    templateService
      .listApprovedFromMeta()
      .then(setTemplates)
      .catch((err) =>
        setError(err instanceof ApiRequestError ? err.message : 'Failed to fetch templates from Meta')
      )
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    if (open) {
      load();
    }
  }, [open]);

  const handleImport = async (remote: MetaRemoteTemplate) => {
    const key = `${remote.name}:${remote.language}`;
    setImporting(key);
    setError(null);
    try {
      await templateService.importFromMeta({
        metaTemplateName: remote.name,
        metaLanguageCode: remote.language,
      });
      onImported();
      load();
    } catch (err) {
      setError(err instanceof ApiRequestError ? err.message : 'Import failed');
    } finally {
      setImporting(null);
    }
  };

  return (
    <Modal open={open} onClose={onClose} title="Import from Meta">
      <div className="flex flex-col gap-4">
        <p className="text-sm text-ink50">
          These templates are already approved in your Meta WhatsApp Business account. Import one to
          use it for broadcasts and messages in this app.
        </p>

        {loading ? (
          <p className="text-sm text-ink50 py-6 text-center">Fetching approved templates from Meta…</p>
        ) : templates.length === 0 ? (
          <p className="text-sm text-ink50 py-6 text-center">
            No approved templates found in your Meta account.
          </p>
        ) : (
          <div className="flex flex-col gap-2 max-h-80 overflow-y-auto">
            {templates.map((t) => {
              const key = `${t.name}:${t.language}`;
              return (
                <div
                  key={key}
                  className="flex items-start justify-between gap-3 rounded-md border border-hairline p-3"
                >
                  <div className="min-w-0 flex-1">
                    <p className="font-medium text-ink text-sm">{t.name}</p>
                    <p className="text-xs text-ink50 mt-0.5">
                      {t.language} · {t.category}
                    </p>
                    <p className="text-xs text-ink50 mt-1 truncate">{t.bodyPreview}</p>
                    {t.linkedLocally && (
                      <span className="inline-block mt-1 text-xs text-moss">Already imported</span>
                    )}
                  </div>
                  <Button
                    size="sm"
                    variant={t.linkedLocally ? 'ghost' : 'secondary'}
                    disabled={importing === key}
                    onClick={() => handleImport(t)}
                  >
                    {importing === key ? 'Importing…' : t.linkedLocally ? 'Re-import' : 'Import'}
                  </Button>
                </div>
              );
            })}
          </div>
        )}

        {error && (
          <p className="text-sm text-vermilion bg-vermilion/5 border border-vermilion/20 rounded-md px-3 py-2">
            {error}
          </p>
        )}

        <div className="flex justify-end gap-2 pt-2">
          <Button variant="ghost" onClick={load} disabled={loading}>
            Refresh
          </Button>
          <Button variant="secondary" onClick={onClose}>
            Close
          </Button>
        </div>
      </div>
    </Modal>
  );
}
