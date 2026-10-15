import { useEffect, useState, FormEvent } from 'react';
import { settingsService } from '@/services/settingsService';
import { authService } from '@/services/authService';
import { Card } from '@/components/ui/Card';
import { Field } from '@/components/ui/Field';
import { Button } from '@/components/ui/Button';
import { ApiRequestError } from '@/services/api';

export default function SettingsPage() {
  const [organizationName, setOrganizationName] = useState('');
  const [defaultImageUrl, setDefaultImageUrl] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    settingsService.get().then((s) => {
      setOrganizationName(s.organizationName);
      setDefaultImageUrl(s.defaultImageUrl ?? '');
      setLoading(false);
    });
  }, []);

  const handleSave = async (e: FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setSaved(false);
    await settingsService.update({ organizationName, defaultImageUrl: defaultImageUrl || null });
    setSaving(false);
    setSaved(true);
  };

  return (
    <div className="max-w-lg flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-semibold">Settings</h1>
        <p className="text-sm text-ink50 mt-1">
          Non-secret configuration. WhatsApp API credentials live in environment variables — see the README.
        </p>
      </div>

      <Card className="p-6">
        {loading ? (
          <p className="text-sm text-ink50">Loading…</p>
        ) : (
          <form onSubmit={handleSave} className="flex flex-col gap-4">
            <Field
              label="Organization name"
              required
              value={organizationName}
              onChange={(e) => setOrganizationName(e.target.value)}
              hint="Used as {{organizationName}} in message templates"
            />
            <Field
              label="Default image URL"
              value={defaultImageUrl}
              onChange={(e) => setDefaultImageUrl(e.target.value)}
              placeholder="https://…"
              hint="Used when a template doesn't specify its own image"
            />
            <div className="flex items-center gap-3 pt-2">
              <Button type="submit" disabled={saving}>
                {saving ? 'Saving…' : 'Save settings'}
              </Button>
              {saved && <span className="text-sm text-moss">Saved</span>}
            </div>
          </form>
        )}
      </Card>

      <ChangePasswordCard />
    </div>
  );
}

function ChangePasswordCard() {
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    setSuccess(false);
    try {
      await authService.changePassword(currentPassword, newPassword);
      setCurrentPassword('');
      setNewPassword('');
      setSuccess(true);
    } catch (err) {
      setError(err instanceof ApiRequestError ? err.message : 'Failed to change password');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Card className="p-6">
      <p className="text-sm font-medium text-ink mb-4">Change password</p>
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <Field
          label="Current password"
          type="password"
          required
          value={currentPassword}
          onChange={(e) => setCurrentPassword(e.target.value)}
        />
        <Field
          label="New password"
          type="password"
          required
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
          hint="At least 8 characters"
        />
        {error && (
          <p className="text-sm text-vermilion bg-vermilion/5 border border-vermilion/20 rounded-md px-3 py-2">{error}</p>
        )}
        <div className="flex items-center gap-3">
          <Button type="submit" disabled={submitting}>
            {submitting ? 'Updating…' : 'Update password'}
          </Button>
          {success && <span className="text-sm text-moss">Password updated</span>}
        </div>
      </form>
    </Card>
  );
}
