import { useEffect, useState } from 'react';
import { templateService } from '@/services/templateService';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { StatusBadge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { EmptyState } from '@/components/ui/EmptyState';
import { TemplateForm } from './TemplateForm';
import { MetaImportModal } from './MetaImportModal';
import { ApiRequestError } from '@/services/api';
import type { MessageTemplate } from '@/types';

const TYPE_LABELS: Record<string, string> = {
  DONATION_THANK_YOU: 'Donation thank you',
  ANNOUNCEMENT: 'Announcement',
  CUSTOM: 'Custom',
};

/** Only Meta-pipeline templates: approved, or pending/rejected after a Meta submit. */
function isMetaLinked(t: MessageTemplate): boolean {
  return t.metaStatus === 'APPROVED' || t.metaStatus === 'PENDING' || t.metaStatus === 'REJECTED';
}

function DeliveryBadge({ template }: { template: MessageTemplate }) {
  if (template.metaStatus === 'APPROVED') {
    return (
      <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-moss/10 text-moss">
        {template.metaHasImageHeader ? 'Meta-approved + image' : 'Meta-approved'}
      </span>
    );
  }
  if (template.metaStatus === 'PENDING') {
    return (
      <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-marigold/15 text-marigold-dark">
        Pending Meta review
      </span>
    );
  }
  if (template.metaStatus === 'REJECTED') {
    return (
      <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-vermilion/10 text-vermilion">
        Meta rejected
      </span>
    );
  }
  return (
    <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-marigold/15 text-marigold-dark">
      Free-form only
    </span>
  );
}

export default function TemplatesPage() {
  const [templates, setTemplates] = useState<MessageTemplate[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [metaImportOpen, setMetaImportOpen] = useState(false);
  const [editing, setEditing] = useState<MessageTemplate | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<MessageTemplate | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const reload = () => {
    setLoading(true);
    templateService
      .list()
      .then((all) => setTemplates(all.filter(isMetaLinked)))
      .finally(() => setLoading(false));
  };

  useEffect(reload, []);

  const openCreate = () => {
    setEditing(null);
    setModalOpen(true);
  };

  const openEdit = (t: MessageTemplate) => {
    setEditing(t);
    setModalOpen(true);
  };

  const closeModal = () => {
    setModalOpen(false);
    setEditing(null);
  };

  const handleSaved = () => {
    closeModal();
    reload();
  };

  const toggleActive = async (t: MessageTemplate) => {
    await templateService.setActive(t.id, !t.active);
    reload();
  };

  const quickSync = async (t: MessageTemplate) => {
    await templateService.syncMetaStatus(t.id);
    reload();
  };

  const confirmDelete = async () => {
    if (!deleteTarget) return;
    setDeleting(true);
    setDeleteError(null);
    try {
      await templateService.delete(deleteTarget.id);
      setDeleteTarget(null);
      reload();
    } catch (err) {
      setDeleteError(err instanceof ApiRequestError ? err.message : 'Could not delete template.');
    } finally {
      setDeleting(false);
    }
  };

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div className="min-w-0">
          <h1 className="text-xl sm:text-2xl font-semibold">Message templates</h1>
          <p className="text-sm text-ink50 mt-1">
            Only Meta-linked templates are shown. Delete removes the template here and from Meta.
            Meta may block reusing the same template name for about 30 days.
          </p>
        </div>
        <div className="flex flex-wrap gap-2 shrink-0">
          <Button variant="secondary" onClick={() => setMetaImportOpen(true)}>
            Import from Meta
          </Button>
          <Button onClick={openCreate}>+ New template</Button>
        </div>
      </div>

      <Card className="overflow-hidden">
        {loading ? (
          <div className="p-8 text-center text-sm text-ink50">Loading templates…</div>
        ) : templates.length === 0 ? (
          <EmptyState
            title="No Meta templates yet"
            action={<Button onClick={openCreate}>+ New template</Button>}
          />
        ) : (
          <div className="table-scroll">
          <table>
            <thead>
              <tr className="border-b border-hairline text-left text-xs uppercase tracking-wide text-ink50">
                <th className="px-4 py-3 font-medium">Name</th>
                <th className="px-4 py-3 font-medium">Type</th>
                <th className="px-4 py-3 font-medium">Preview</th>
                <th className="px-4 py-3 font-medium">Meta delivery</th>
                <th className="px-4 py-3 font-medium">Status</th>
                <th className="px-4 py-3 font-medium text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {templates.map((t) => (
                <tr key={t.id} className="border-b border-hairline last:border-0 hover:bg-paper/60">
                  <td className="px-4 py-3 font-medium text-ink">{t.name}</td>
                  <td className="px-4 py-3 text-ink50">{TYPE_LABELS[t.type]}</td>
                  <td className="px-4 py-3 text-ink50 max-w-xs truncate">{t.message}</td>
                  <td className="px-4 py-3">
                    <DeliveryBadge template={t} />
                  </td>
                  <td className="px-4 py-3">
                    <StatusBadge active={t.active} />
                  </td>
                  <td className="px-4 py-3 text-right">
                    <div className="inline-flex flex-wrap justify-end gap-1">
                    {t.metaStatus === 'PENDING' && (
                      <Button variant="ghost" size="sm" onClick={() => quickSync(t)}>
                        Check status
                      </Button>
                    )}
                    <Button variant="ghost" size="sm" onClick={() => openEdit(t)}>
                      Edit
                    </Button>
                    <Button variant="ghost" size="sm" onClick={() => toggleActive(t)}>
                      {t.active ? 'Deactivate' : 'Activate'}
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="text-vermilion hover:text-vermilion"
                      onClick={() => {
                        setDeleteError(null);
                        setDeleteTarget(t);
                      }}
                    >
                      Delete
                    </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          </div>
        )}
      </Card>

      <MetaImportModal
        open={metaImportOpen}
        onClose={() => setMetaImportOpen(false)}
        onImported={reload}
      />

      <Modal
        open={modalOpen}
        onClose={closeModal}
        title={editing ? 'Edit template' : 'New template'}
      >
        <TemplateForm
          key={editing?.id ?? 'new'}
          initial={editing}
          onSaved={handleSaved}
          onCancel={closeModal}
        />
      </Modal>

      <Modal
        open={Boolean(deleteTarget)}
        onClose={() => !deleting && setDeleteTarget(null)}
        title="Delete template"
      >
        {deleteTarget && (
          <div className="flex flex-col gap-4">
            <p className="text-sm text-ink50">
              Delete <span className="font-medium text-ink">{deleteTarget.name}</span>
              {deleteTarget.metaTemplateName ? (
                <>
                  {' '}
                  (Meta name <span className="figures">{deleteTarget.metaTemplateName}</span>)
                </>
              ) : null}
              ? This removes it from your app and from Meta.
            </p>
            {deleteError && (
              <p className="text-sm text-vermilion bg-vermilion/5 border border-vermilion/20 rounded-md px-3 py-2">
                {deleteError}
              </p>
            )}
            <div className="flex flex-col-reverse sm:flex-row sm:justify-end gap-2">
              <Button type="button" variant="secondary" disabled={deleting} onClick={() => setDeleteTarget(null)}>
                Cancel
              </Button>
              <Button type="button" variant="danger" disabled={deleting} onClick={confirmDelete}>
                {deleting ? 'Deleting…' : 'Delete from app + Meta'}
              </Button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
