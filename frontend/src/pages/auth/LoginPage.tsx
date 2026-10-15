import { useState, FormEvent, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { Field } from '@/components/ui/Field';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { ApiRequestError } from '@/services/api';
import { consumeSessionExpiredFlag } from '@/utils/authToken';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [sessionNotice, setSessionNotice] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (consumeSessionExpiredFlag()) {
      setSessionNotice('Your session has expired. Please sign in again.');
    }
  }, []);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    setSessionNotice(null);
    try {
      await login(username, password);
      navigate('/');
    } catch (err) {
      setError(err instanceof ApiRequestError ? err.message : 'Login failed. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-paper px-4">
      <Card className="w-full max-w-sm p-8">
        <h1 className="font-display text-2xl text-ink text-center">Ganpati Vargani</h1>
        <p className="text-sm text-ink50 text-center mt-1 mb-6">Admin sign in</p>

        {sessionNotice && (
          <p
            role="status"
            className="mb-4 text-sm text-ink bg-marigold/15 border border-marigold/40 rounded-md px-3 py-2"
          >
            {sessionNotice}
          </p>
        )}

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <Field label="Username" required value={username} onChange={(e) => setUsername(e.target.value)} autoFocus />
          <Field
            label="Password"
            required
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />

          {error && (
            <p className="text-sm text-vermilion bg-vermilion/5 border border-vermilion/20 rounded-md px-3 py-2">
              {error}
            </p>
          )}

          <Button type="submit" disabled={submitting || !username || !password} className="mt-2">
            {submitting ? 'Signing in…' : 'Sign in'}
          </Button>
        </form>
      </Card>
    </div>
  );
}
