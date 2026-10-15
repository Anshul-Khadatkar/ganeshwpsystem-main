import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { templateService } from '@/services/templateService';
import { broadcastService } from '@/services/broadcastService';
import { useBuildings } from '@/hooks/useBuildings';
import { Card } from '@/components/ui/Card';
import { Field } from '@/components/ui/Field';
import { Textarea } from '@/components/ui/Textarea';
import { Select } from '@/components/ui/Select';
import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import { ContributorPicker } from './ContributorPicker';
import { ApiRequestError } from '@/services/api';
import type {
  Broadcast,
  BroadcastCreateRequest,
  BroadcastPreview,
  BroadcastStatus,
  Contributor,
  MessageTemplate,
  RecipientMode,
} from '@/types';

interface MessageComposerProps {
  defaultRecipientMode?: RecipientMode;
}

interface SendProgress {
  id: number;
  totalRecipients: number;
  successfulCount: number;
  failedCount: number;
  status: BroadcastStatus;
}

const TERMINAL: BroadcastStatus[] = ['COMPLETED', 'COMPLETED_WITH_ERRORS', 'FAILED'];

export function MessageComposer({ defaultRecipientMode = 'FILTERED' }: MessageComposerProps) {
  const navigate = useNavigate();
  const { buildings } = useBuildings(true);
  const [templates, setTemplates] = useState<MessageTemplate[]>([]);

  const [templateId, setTemplateId] = useState<number | ''>('');
  const [message, setMessage] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [sendImage, setSendImage] = useState(false);

  const [recipientMode, setRecipientMode] = useState<RecipientMode>(defaultRecipientMode);
  const [buildingId, setBuildingId] = useState<number | ''>('');
  const [minDonationAmount, setMinDonationAmount] = useState('');
  const [donatedFrom, setDonatedFrom] = useState('');
  const [donatedTo, setDonatedTo] = useState('');
  const [selectedContributors, setSelectedContributors] = useState<Contributor[]>([]);

  const [preview, setPreview] = useState<BroadcastPreview | null>(null);
  const [previewing, setPreviewing] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [progress, setProgress] = useState<SendProgress | null>(null);
  const pollRef = useRef<number | null>(null);

  useEffect(() => {
    templateService.list().then(setTemplates);
  }, []);

  useEffect(() => {
    return () => {
      if (pollRef.current != null) window.clearInterval(pollRef.current);
    };
  }, []);

  const toProgress = (b: Broadcast): SendProgress => ({
    id: b.id,
    totalRecipients: b.totalRecipients,
    successfulCount: b.successfulCount,
    failedCount: b.failedCount,
    status: b.status,
  });

  const stopPolling = () => {
    if (pollRef.current != null) {
      window.clearInterval(pollRef.current);
      pollRef.current = null;
    }
  };

  const startPolling = (broadcastId: number) => {
    stopPolling();
    pollRef.current = window.setInterval(async () => {
      try {
        const detail = await broadcastService.getDetail(broadcastId, 0, 1);
        const next = toProgress(detail.broadcast);
        setProgress(next);
        if (TERMINAL.includes(next.status)) {
          stopPolling();
          setSuccess(
            `Finished — ${next.successfulCount} of ${next.totalRecipients} accepted by WhatsApp` +
              (next.failedCount > 0 ? `, ${next.failedCount} failed` : '') +
              '.'
          );
        }
      } catch {
        // keep polling; transient network blips shouldn't wipe the panel
      }
    }, 2000);
  };
  const applyTemplate = (id: number | '') => {
    setTemplateId(id);
    const t = templates.find((tpl) => tpl.id === id);
    if (t) {
      setMessage(t.message);
      if (t.imageUrl) {
        setImageUrl(t.imageUrl);
        setSendImage(true);
      }
    }
  };

  const buildRequest = (): BroadcastCreateRequest => ({
    templateId: templateId || null,
    message,
    imageUrl: sendImage ? imageUrl || null : null,
    recipientMode,
    buildingId: recipientMode === 'FILTERED' ? buildingId || null : null,
    minDonationAmount: recipientMode === 'FILTERED' && minDonationAmount ? Number(minDonationAmount) : null,
    donatedFrom: recipientMode === 'FILTERED' ? donatedFrom || null : null,
    donatedTo: recipientMode === 'FILTERED' ? donatedTo || null : null,
    contributorIds: recipientMode === 'EXPLICIT' ? selectedContributors.map((c) => c.id) : null,
  });

  const handlePreview = async () => {
    setError(null);
    setPreviewing(true);
    try {
      const result = await broadcastService.preview(buildRequest());
      setPreview(result);
      setConfirmOpen(true);
    } catch (err) {
      setError(err instanceof ApiRequestError ? err.message : 'Failed to load recipient preview');
    } finally {
      setPreviewing(false);
    }
  };

  const handleSend = async () => {
    setSending(true);
    setError(null);
    setSuccess(null);
    stopPolling();
    try {
      const result = await broadcastService.create(buildRequest());
      setConfirmOpen(false);
      setProgress(toProgress(result));
      if (TERMINAL.includes(result.status)) {
        setSuccess(
          `Sent to ${result.totalRecipients} recipient(s) — ${result.successfulCount} accepted by WhatsApp, ${result.failedCount} failed.`
        );
      } else {
        startPolling(result.id);
      }
      setMessage('');
      setImageUrl('');
      setSendImage(false);
      setTemplateId('');
      setSelectedContributors([]);
    } catch (err) {
      setError(err instanceof ApiRequestError ? err.message : 'Failed to send message');
      setConfirmOpen(false);
      setProgress(null);
    } finally {
      setSending(false);
    }
  };

  const pendingCount = progress
    ? Math.max(0, progress.totalRecipients - progress.successfulCount - progress.failedCount)
    : 0;
  const percentDone = progress && progress.totalRecipients > 0
    ? Math.round(((progress.successfulCount + progress.failedCount) / progress.totalRecipients) * 100)
    : 0;
  const isLive = progress != null && !TERMINAL.includes(progress.status);

  const canPreview =
    message.trim().length > 0 && (recipientMode !== 'EXPLICIT' || selectedContributors.length > 0);

  return (
    <div className="max-w-2xl flex flex-col gap-6">
      {progress && (
        <div className="rounded-md border border-hairline bg-paper px-4 py-4 flex flex-col gap-3">
          <div className="flex items-start justify-between gap-3">
            <div>
              <p className="text-sm font-medium text-ink">
                {isLive ? 'Sending broadcast…' : 'Broadcast finished'}
              </p>
              <p className="text-xs text-ink50 mt-0.5">
                {isLive
                  ? 'Counts update as WhatsApp accepts each message.'
                  : success || 'All recipients processed.'}
              </p>
            </div>
            <button
              type="button"
              className="underline text-xs text-ink50 shrink-0"
              onClick={() => navigate(`/whatsapp/history/${progress.id}`)}
            >
              View details →
            </button>
          </div>

          <div className="grid grid-cols-3 gap-3 text-center">
            <div className="rounded-md bg-white border border-hairline px-2 py-2">
              <p className="figures text-lg font-semibold text-moss">{progress.successfulCount}</p>
              <p className="text-[11px] uppercase tracking-wide text-ink50">Sent</p>
            </div>
            <div className="rounded-md bg-white border border-hairline px-2 py-2">
              <p className="figures text-lg font-semibold text-vermilion">{progress.failedCount}</p>
              <p className="text-[11px] uppercase tracking-wide text-ink50">Failed</p>
            </div>
            <div className="rounded-md bg-white border border-hairline px-2 py-2">
              <p className="figures text-lg font-semibold text-ink">
                {isLive ? pendingCount : progress.totalRecipients}
              </p>
              <p className="text-[11px] uppercase tracking-wide text-ink50">
                {isLive ? 'Remaining' : 'Total'}
              </p>
            </div>
          </div>

          <div>
            <div className="flex justify-between text-xs text-ink50 mb-1">
              <span>
                {progress.successfulCount + progress.failedCount} / {progress.totalRecipients} processed
              </span>
              <span className="figures">{percentDone}%</span>
            </div>
            <div className="h-2 rounded-full bg-hairline overflow-hidden">
              <div
                className={`h-full transition-all duration-500 ${
                  progress.failedCount > 0 && !isLive ? 'bg-vermilion' : 'bg-moss'
                }`}
                style={{ width: `${percentDone}%` }}
              />
            </div>
          </div>
        </div>
      )}

      {success && !progress && (
        <div className="rounded-md bg-moss/10 text-moss text-sm px-4 py-3 flex items-center justify-between">
          <span>{success}</span>
          <button type="button" className="underline text-xs" onClick={() => navigate('/whatsapp/history')}>
            View history →
          </button>
        </div>
      )}

      <Card className="p-6 flex flex-col gap-4">
        <Select
          label="Start from a template (optional)"
          value={templateId}
          onChange={(e) => applyTemplate(e.target.value ? Number(e.target.value) : '')}
        >
          <option value="">Blank message</option>
          {templates.map((t) => (
            <option key={t.id} value={t.id}>
              {t.name}
            </option>
          ))}
        </Select>

        <Textarea label="Message" required rows={6} value={message} onChange={(e) => setMessage(e.target.value)} />

        <label className="flex items-center gap-2 text-sm text-ink">
          <input
            type="checkbox"
            checked={sendImage}
            onChange={(e) => setSendImage(e.target.checked)}
            className="rounded border-hairline text-ink focus-visible:ring-2 focus-visible:ring-marigold"
          />
          Send image
        </label>
        {sendImage && (
          <Field label="Image URL" value={imageUrl} onChange={(e) => setImageUrl(e.target.value)} placeholder="https://…" />
        )}
      </Card>

      <Card className="p-6 flex flex-col gap-4">
        <p className="text-sm font-medium text-ink">Recipients</p>

        <div className="flex gap-1.5">
          {(['ALL', 'FILTERED', 'EXPLICIT'] as RecipientMode[]).map((mode) => (
            <button
              key={mode}
              type="button"
              onClick={() => setRecipientMode(mode)}
              className={`px-3 py-1.5 rounded-md text-xs font-medium border ${
                recipientMode === mode ? 'bg-ink text-white border-ink' : 'bg-white text-ink50 border-hairline hover:text-ink'
              }`}
            >
              {mode === 'ALL' ? 'All contributors' : mode === 'FILTERED' ? 'Filter by building / donation' : 'Pick specific people'}
            </button>
          ))}
        </div>

        {recipientMode === 'FILTERED' && (
          <div className="grid grid-cols-2 gap-4">
            <Select label="Building" value={buildingId} onChange={(e) => setBuildingId(e.target.value ? Number(e.target.value) : '')}>
              <option value="">Any building</option>
              {buildings.map((b) => (
                <option key={b.id} value={b.id}>
                  {b.name}
                </option>
              ))}
            </Select>
            <Field
              label="Donated at least"
              type="number"
              value={minDonationAmount}
              onChange={(e) => setMinDonationAmount(e.target.value)}
              placeholder="e.g. 2500"
            />
            <Field label="Donated from" type="date" value={donatedFrom} onChange={(e) => setDonatedFrom(e.target.value)} />
            <Field label="Donated to" type="date" value={donatedTo} onChange={(e) => setDonatedTo(e.target.value)} />
          </div>
        )}

        {recipientMode === 'EXPLICIT' && (
          <ContributorPicker selected={selectedContributors} onChange={setSelectedContributors} />
        )}
      </Card>

      {error && (
        <p className="text-sm text-vermilion bg-vermilion/5 border border-vermilion/20 rounded-md px-3 py-2">{error}</p>
      )}

      <div className="flex justify-end">
        <Button onClick={handlePreview} disabled={!canPreview || previewing}>
          {previewing ? 'Loading…' : 'Preview & send'}
        </Button>
      </div>

      <Modal open={confirmOpen} onClose={() => setConfirmOpen(false)} title="Confirm broadcast">
        {preview && (
          <div className="flex flex-col gap-4">
            <div>
              <p className="text-xs uppercase tracking-wide text-ink50 font-medium">Recipients</p>
              <p className="text-lg font-semibold text-ink mt-0.5">{preview.recipientCount} contributors</p>
              <p className="text-xs text-ink50 mt-0.5">{preview.recipientCriteria}</p>
              {preview.sampleNames.length > 0 && (
                <p className="text-xs text-ink30 mt-1">
                  e.g. {preview.sampleNames.join(', ')}
                  {preview.recipientCount > preview.sampleNames.length ? '…' : ''}
                </p>
              )}
            </div>

            <div>
              <p className="text-xs uppercase tracking-wide text-ink50 font-medium mb-1.5">Message</p>
              <div className="rounded-md bg-paper border border-hairline p-3 text-sm text-ink whitespace-pre-wrap">
                {message}
              </div>
            </div>

            {sendImage && imageUrl && (
              <div>
                <p className="text-xs uppercase tracking-wide text-ink50 font-medium mb-1.5">Image</p>
                <img src={imageUrl} alt="Message attachment preview" className="rounded-md border border-hairline max-h-40" />
              </div>
            )}

            {preview.recipientCount === 0 ? (
              <p className="text-sm text-vermilion bg-vermilion/5 border border-vermilion/20 rounded-md px-3 py-2">
                No opted-in contributors match these criteria.
              </p>
            ) : (
              <div className="flex justify-end gap-2 pt-2 border-t border-hairline">
                <Button variant="secondary" onClick={() => setConfirmOpen(false)}>
                  Cancel
                </Button>
                <Button onClick={handleSend} disabled={sending}>
                  {sending ? 'Sending…' : `Send to ${preview.recipientCount}`}
                </Button>
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
}
