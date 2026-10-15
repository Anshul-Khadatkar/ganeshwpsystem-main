import { useEffect, useState, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBuildings } from '@/hooks/useBuildings';
import { donationService } from '@/services/donationService';
import { templateService } from '@/services/templateService';
import { ApiRequestError } from '@/services/api';
import { Card } from '@/components/ui/Card';
import { Field } from '@/components/ui/Field';
import { Select } from '@/components/ui/Select';
import { Textarea } from '@/components/ui/Textarea';
import { Button } from '@/components/ui/Button';
import type { MessageTemplate } from '@/types';

export default function AddDonationPage() {
  const navigate = useNavigate();
  const { buildings, loading: loadingBuildings } = useBuildings(true);

  const [name, setName] = useState('');
  const [mobileNumber, setMobileNumber] = useState('');
  const [buildingId, setBuildingId] = useState<number | ''>('');
  const [floorNumber, setFloorNumber] = useState('');
  const [flatNumber, setFlatNumber] = useState('');
  const [amount, setAmount] = useState('');
  const [donationDate, setDonationDate] = useState(() => new Date().toISOString().slice(0, 10));
  const [description, setDescription] = useState('');
  const [sendWhatsAppThankYou, setSendWhatsAppThankYou] = useState(true);
  const [thankYouTemplates, setThankYouTemplates] = useState<MessageTemplate[]>([]);
  const [thankYouTemplateId, setThankYouTemplateId] = useState<number | ''>('');
  const [loadingTemplates, setLoadingTemplates] = useState(true);

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const selectedBuilding = buildings.find((b) => b.id === buildingId);
  const selectedThankYou = thankYouTemplates.find((t) => t.id === thankYouTemplateId);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      setLoadingTemplates(true);
      try {
        const all = await templateService.list('DONATION_THANK_YOU');
        const usable = all.filter((t) => t.active && t.metaStatus === 'APPROVED');
        // Prefer Meta-approved templates first for reliable delivery outside the 24h window.
        usable.sort((a, b) => a.id - b.id);
        if (cancelled) return;
        setThankYouTemplates(usable);
        setThankYouTemplateId(usable[0]?.id ?? '');
      } catch {
        if (!cancelled) setThankYouTemplates([]);
      } finally {
        if (!cancelled) setLoadingTemplates(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    setFieldErrors({});
    setSuccessMessage(null);

    try {
      await donationService.create({
        name: name.trim(),
        mobileNumber: mobileNumber.trim(),
        buildingId: buildingId as number,
        floorNumber: selectedBuilding?.requiresFloor ? floorNumber.trim() : null,
        flatNumber: selectedBuilding?.requiresFlat ? flatNumber.trim() : null,
        amount: parseFloat(amount),
        donationDate,
        description: description.trim() || null,
        sendWhatsAppThankYou,
        thankYouTemplateId:
          sendWhatsAppThankYou && thankYouTemplateId ? Number(thankYouTemplateId) : null,
      });

      setSuccessMessage(`Donation of ₹${amount} recorded for ${name}.`);
      // Reset donation-specific fields; keep building selection for faster repeat entry.
      setName('');
      setMobileNumber('');
      setFloorNumber('');
      setFlatNumber('');
      setAmount('');
      setDescription('');
    } catch (err) {
      if (err instanceof ApiRequestError) {
        setError(err.message);
        if (err.fieldErrors) {
          setFieldErrors(Object.fromEntries(err.fieldErrors.map((fe) => [fe.field, fe.message])));
        }
      } else {
        setError('Failed to save donation. Please try again.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-2xl flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-semibold">Add donation</h1>
        <p className="text-sm text-ink50 mt-1">
          The form below adjusts automatically once you pick a building — Row Houses and Shops
          won't ask for a floor or flat.
        </p>
      </div>

      {successMessage && (
        <div className="rounded-md bg-moss/10 text-moss text-sm px-4 py-3 flex items-center justify-between">
          <span>{successMessage}</span>
          <button
            type="button"
            className="text-moss underline text-xs"
            onClick={() => navigate('/donations')}
          >
            View donations →
          </button>
        </div>
      )}

      <Card className="p-6">
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Field
              label="Contributor name"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              error={fieldErrors.name}
            />
            <Field
              label="Mobile number"
              required
              value={mobileNumber}
              onChange={(e) => setMobileNumber(e.target.value)}
              placeholder="9812345678"
              error={fieldErrors.mobileNumber}
              hint="Used for the WhatsApp thank-you message"
            />
          </div>

          <Select
            label="Building"
            required
            value={buildingId}
            onChange={(e) => {
              setBuildingId(e.target.value ? Number(e.target.value) : '');
              setFloorNumber('');
              setFlatNumber('');
            }}
            disabled={loadingBuildings}
          >
            <option value="">{loadingBuildings ? 'Loading…' : 'Select a building'}</option>
            {buildings.map((b) => (
              <option key={b.id} value={b.id}>
                {b.name}
              </option>
            ))}
          </Select>

          {selectedBuilding && (selectedBuilding.requiresFloor || selectedBuilding.requiresFlat) && (
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              {selectedBuilding.requiresFloor && (
                <Field
                  label="Floor number"
                  required
                  value={floorNumber}
                  onChange={(e) => setFloorNumber(e.target.value)}
                  placeholder="e.g. 3 or Ground"
                  error={fieldErrors.floorNumber}
                />
              )}
              {selectedBuilding.requiresFlat && (
                <Field
                  label="Flat number"
                  required
                  value={flatNumber}
                  onChange={(e) => setFlatNumber(e.target.value)}
                  placeholder="e.g. 302"
                  error={fieldErrors.flatNumber}
                />
              )}
            </div>
          )}

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Field
              label="Donation amount"
              required
              type="number"
              min="1"
              step="0.01"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              error={fieldErrors.amount}
            />
            <Field
              label="Donation date"
              required
              type="date"
              value={donationDate}
              onChange={(e) => setDonationDate(e.target.value)}
              error={fieldErrors.donationDate}
            />
          </div>

          <Textarea
            label="Description / note"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Anything worth noting about this donation"
          />

          <div className="flex flex-col gap-3 border-t border-hairline pt-4">
            <label className="flex items-center gap-2 text-sm text-ink">
              <input
                type="checkbox"
                checked={sendWhatsAppThankYou}
                onChange={(e) => setSendWhatsAppThankYou(e.target.checked)}
                className="rounded border-hairline text-ink focus-visible:ring-2 focus-visible:ring-marigold"
              />
              Send WhatsApp thank-you message
            </label>

            {sendWhatsAppThankYou && (
              <>
                <Select
                  label="Donation thank-you template"
                  required
                  value={thankYouTemplateId}
                  onChange={(e) =>
                    setThankYouTemplateId(e.target.value ? Number(e.target.value) : '')
                  }
                  disabled={loadingTemplates || thankYouTemplates.length === 0}
                >
                  {loadingTemplates ? (
                    <option value="">Loading templates…</option>
                  ) : thankYouTemplates.length === 0 ? (
                    <option value="">No active Donation thank you template</option>
                  ) : (
                    thankYouTemplates.map((t) => (
                      <option key={t.id} value={t.id}>
                        {t.name}
                        {t.metaStatus === 'APPROVED' ? ' (Meta-approved)' : ''}
                      </option>
                    ))
                  )}
                </Select>
                <p className="text-xs text-ink50 -mt-1">
                  Only templates with type “Donation thank you” are listed here.
                </p>

                {selectedThankYou && (
                  <p className="text-xs text-ink50 leading-relaxed">
                    Will send via{' '}
                    <span className="font-medium text-ink">{selectedThankYou.name}</span>
                    {selectedThankYou.metaStatus === 'APPROVED' && selectedThankYou.metaTemplateName
                      ? ` → Meta template “${selectedThankYou.metaTemplateName}” (${selectedThankYou.metaLanguageCode})`
                      : ' (free-form — only works if the recipient messaged you within 24 hours)'}
                    .{' '}
                    <button
                      type="button"
                      className="underline"
                      onClick={() => navigate('/whatsapp/templates')}
                    >
                      Manage templates
                    </button>
                  </p>
                )}

                {!loadingTemplates && thankYouTemplates.length === 0 && (
                  <p className="text-xs text-vermilion">
                    Create an active template with type “Donation thank you” under WhatsApp →
                    Templates.
                  </p>
                )}
              </>
            )}
          </div>

          {error && (
            <p className="text-sm text-vermilion bg-vermilion/5 border border-vermilion/20 rounded-md px-3 py-2">
              {error}
            </p>
          )}

          <div className="flex justify-end pt-2">
            <Button
              type="submit"
              disabled={
                submitting ||
                !buildingId ||
                (sendWhatsAppThankYou && (!thankYouTemplateId || thankYouTemplates.length === 0))
              }
            >
              {submitting ? 'Saving…' : 'Save donation'}
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
}
