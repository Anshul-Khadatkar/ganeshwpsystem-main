import { useState } from 'react';
import { Field } from '@/components/ui/Field';
import { Textarea } from '@/components/ui/Textarea';
import { Select } from '@/components/ui/Select';
import { Button } from '@/components/ui/Button';
import { ApiRequestError } from '@/services/api';
import { templateService } from '@/services/templateService';
import type {
  MessageTemplate,
  MessageTemplateRequest,
  MessageTemplateType,
  MetaTemplateCategory,
  MetaTemplateStatus,
} from '@/types';

const VARIABLES = ['name', 'mobile', 'building', 'floor', 'flat', 'amount', 'date', 'organizationName'];

const META_STATUS_LABELS: Record<MetaTemplateStatus, string> = {
  NOT_SUBMITTED: 'Not submitted',
  PENDING: 'Pending Meta review',
  APPROVED: 'Approved by Meta',
  REJECTED: 'Rejected by Meta',
};

interface TemplateFormProps {
  initial?: MessageTemplate | null;
  /** Called when Submit to Meta succeeds — parent closes the modal and reloads. */
  onSaved: (template: MessageTemplate) => void;
  onCancel: () => void;
}

export function TemplateForm({ initial, onSaved, onCancel }: TemplateFormProps) {
  const [name, setName] = useState(initial?.name ?? '');
  const [message, setMessage] = useState(initial?.message ?? '');
  const [imageUrl, setImageUrl] = useState(initial?.imageUrl ?? '');
  const [type, setType] = useState<MessageTemplateType>(initial?.type ?? 'CUSTOM');
  const [active, setActive] = useState(initial?.active ?? true);
  const [metaTemplateName, setMetaTemplateName] = useState(initial?.metaTemplateName ?? '');
  const [metaLanguageCode, setMetaLanguageCode] = useState(initial?.metaLanguageCode ?? 'en_US');
  const [metaParameterFormat, setMetaParameterFormat] = useState<'NAMED' | 'POSITIONAL'>(
    initial?.metaParameterFormat ?? 'NAMED'
  );
  const [metaCategory, setMetaCategory] = useState<MetaTemplateCategory>(initial?.metaCategory ?? 'UTILITY');
  const [metaStatus, setMetaStatus] = useState<MetaTemplateStatus | null>(initial?.metaStatus ?? null);
  const [metaRejectionReason, setMetaRejectionReason] = useState(initial?.metaRejectionReason ?? null);
  const [savedId, setSavedId] = useState<number | null>(initial?.id ?? null);
  const [metaActionLoading, setMetaActionLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const insertVariable = (variable: string) => {
    setMessage((prev) => `${prev}{{${variable}}}`);
  };

  const buildRequest = (): MessageTemplateRequest => ({
    name: name.trim(),
    message,
    imageUrl: imageUrl.trim() || null,
    type,
    active,
    metaTemplateName: metaTemplateName.trim() || null,
    metaLanguageCode,
    metaParameterFormat,
    metaCategory,
  });

  const persistLocal = async (): Promise<MessageTemplate> => {
    const request = buildRequest();
    if (savedId != null) {
      return templateService.update(savedId, request);
    }
    return templateService.create(request);
  };

  /**
   * Save locally + submit to Meta, then close the form immediately.
   * Do not leave the modal open (that remounted with Submit again and caused duplicate Meta errors).
   */
  const handleSubmitToMeta = async () => {
    if (!name.trim() || !message.trim()) {
      setError('Template name and message are required before submitting to Meta.');
      return;
    }
    if (metaStatus === 'PENDING') {
      setError('This template is already pending Meta review. Close and use Check status on the list.');
      return;
    }

    setMetaActionLoading(true);
    setError(null);
    try {
      const saved = await persistLocal();
      setSavedId(saved.id);
      setMetaStatus(saved.metaStatus);
      setMetaRejectionReason(saved.metaRejectionReason);
      setMetaTemplateName(saved.metaTemplateName ?? metaTemplateName);

      if (saved.metaStatus === 'PENDING') {
        onSaved(saved);
        return;
      }

      const submitted = await templateService.submitToMeta(saved.id);
      onSaved(submitted);
    } catch (err) {
      setError(err instanceof ApiRequestError ? err.message : 'Failed to submit to Meta');
    } finally {
      setMetaActionLoading(false);
    }
  };

  const handleSyncMetaStatus = async () => {
    if (savedId == null) return;
    setMetaActionLoading(true);
    setError(null);
    try {
      const updated = await templateService.syncMetaStatus(savedId);
      setMetaStatus(updated.metaStatus);
      setMetaRejectionReason(updated.metaRejectionReason);
      if (updated.metaStatus === 'APPROVED' || updated.metaStatus === 'REJECTED') {
        onSaved(updated);
      }
    } catch (err) {
      setError(err instanceof ApiRequestError ? err.message : 'Failed to check Meta status');
    } finally {
      setMetaActionLoading(false);
    }
  };

  const alreadyPending = metaStatus === 'PENDING';
  const canSyncMeta = savedId != null && metaStatus !== 'NOT_SUBMITTED' && metaStatus !== null;
  const formReady = Boolean(name.trim() && message.trim());

  return (
    <div className="flex flex-col gap-4">
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Field label="Template name" required value={name} onChange={(e) => setName(e.target.value)} />
        <Select label="Type" required value={type} onChange={(e) => setType(e.target.value as MessageTemplateType)}>
          <option value="DONATION_THANK_YOU">Donation thank you</option>
          <option value="ANNOUNCEMENT">Announcement</option>
          <option value="CUSTOM">Custom</option>
        </Select>
      </div>

      <Textarea
        label="Message"
        required
        rows={6}
        value={message}
        onChange={(e) => setMessage(e.target.value)}
        placeholder="Write the message. Use the variable buttons below to insert placeholders."
      />

      <div className="flex flex-wrap gap-1.5">
        {VARIABLES.map((v) => (
          <button
            key={v}
            type="button"
            onClick={() => insertVariable(v)}
            className="text-xs px-2 py-1 rounded-md bg-paper border border-hairline text-ink50 hover:text-ink hover:border-ink30 figures"
          >
            {`{{${v}}}`}
          </button>
        ))}
      </div>

      <Field
        label="Image URL (for Utility template with photo)"
        value={imageUrl}
        onChange={(e) => setImageUrl(e.target.value)}
        placeholder="https://example.com/ganpati.jpg"
        hint="Optional. Public HTTPS .jpg/.png (max 5 MB). If set, Submit to Meta creates a Utility template with an IMAGE header — same Meta price as text-only."
      />

      {initial?.metaHasImageHeader && (
        <p className="text-xs text-moss -mt-2">
          This Meta template has an IMAGE header. Keep a public image URL set for sends.
        </p>
      )}

      <label className="flex items-center gap-2 text-sm text-ink">
        <input
          type="checkbox"
          checked={active}
          onChange={(e) => setActive(e.target.checked)}
          className="rounded border-hairline text-ink focus-visible:ring-2 focus-visible:ring-marigold"
        />
        Active
      </label>

      <div className="rounded-md border border-hairline p-3 flex flex-col gap-3">
        <div>
          <p className="text-sm font-medium text-ink">WhatsApp / Meta delivery</p>
          <p className="text-xs text-ink50 mt-0.5">
            Click <strong>Submit to Meta</strong> once — the form saves, submits, and{' '}
            <strong>closes</strong>. Status becomes Pending until Meta approves. Editing an approved
            template creates a new Meta version. Keep category <strong>Utility</strong> for thank-yous.
            Use lowercase variables like {'{{name}}'}. Avoid ₹ before {'{{amount}}'} — write Rs.{' '}
            {'{{amount}}'} instead.
          </p>
        </div>

        {metaStatus && (
          <div className="text-sm">
            <span className="font-medium text-ink">Meta status: </span>
            <span
              className={
                metaStatus === 'APPROVED'
                  ? 'text-moss'
                  : metaStatus === 'REJECTED'
                    ? 'text-vermilion'
                    : metaStatus === 'PENDING'
                      ? 'text-marigold-dark'
                      : 'text-ink50'
              }
            >
              {META_STATUS_LABELS[metaStatus]}
            </span>
            {metaRejectionReason && (
              <p className="text-xs text-vermilion mt-1">{metaRejectionReason}</p>
            )}
          </div>
        )}

        <Field
          label="Meta template name"
          value={metaTemplateName}
          onChange={(e) => setMetaTemplateName(e.target.value)}
          placeholder="e.g. donation_thank_you_img_v1 (must be unique)"
          hint="Lowercase with underscores. Must be a NEW name — reusing an existing Meta name causes “already English (US) content”."
        />
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Field
            label="Language code"
            value={metaLanguageCode}
            onChange={(e) => setMetaLanguageCode(e.target.value)}
            placeholder="en_US"
          />
          <Select
            label="Parameter format"
            value={metaParameterFormat}
            onChange={(e) => setMetaParameterFormat(e.target.value as 'NAMED' | 'POSITIONAL')}
          >
            <option value="NAMED">Named ({'{{name}}'})</option>
            <option value="POSITIONAL">Positional ({'{{1}}'})</option>
          </Select>
        </div>
        <Select
          label="Meta category"
          value={metaCategory}
          onChange={(e) => setMetaCategory(e.target.value as MetaTemplateCategory)}
        >
          <option value="UTILITY">Utility (thank-yous, updates)</option>
          <option value="MARKETING">Marketing (promotions, events)</option>
        </Select>

        {canSyncMeta && (
          <div className="flex flex-wrap gap-2 pt-1">
            <Button
              type="button"
              variant="ghost"
              disabled={metaActionLoading}
              onClick={handleSyncMetaStatus}
            >
              Check Meta status
            </Button>
          </div>
        )}
      </div>

      {error && (
        <p className="text-sm text-vermilion bg-vermilion/5 border border-vermilion/20 rounded-md px-3 py-2">
          {error}
        </p>
      )}

      <div className="flex justify-end gap-2 pt-2">
        <Button type="button" variant="secondary" onClick={onCancel} disabled={metaActionLoading}>
          {alreadyPending ? 'Close' : 'Cancel'}
        </Button>

        {!alreadyPending && (
          <Button
            type="button"
            disabled={!formReady || metaActionLoading}
            onClick={handleSubmitToMeta}
          >
            {metaActionLoading ? 'Submitting…' : 'Submit to Meta'}
          </Button>
        )}
      </div>
    </div>
  );
}
