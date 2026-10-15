/** Decode JWT payload without verifying signature (client-side expiry check only). */
export function getJwtExpiryMs(token: string): number | null {
  try {
    const parts = token.split('.');
    if (parts.length < 2) return null;
    const payload = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const padded = payload + '='.repeat((4 - (payload.length % 4)) % 4);
    const json = JSON.parse(atob(padded)) as { exp?: number };
    return typeof json.exp === 'number' ? json.exp * 1000 : null;
  } catch {
    return null;
  }
}

export function isJwtExpired(token: string, skewMs = 5_000): boolean {
  const expMs = getJwtExpiryMs(token);
  if (expMs == null) return true;
  return Date.now() >= expMs - skewMs;
}

export const SESSION_EXPIRED_KEY = 'sessionExpired';

export function markSessionExpired(): void {
  sessionStorage.setItem(SESSION_EXPIRED_KEY, '1');
}

export function consumeSessionExpiredFlag(): boolean {
  const flagged = sessionStorage.getItem(SESSION_EXPIRED_KEY) === '1';
  if (flagged) sessionStorage.removeItem(SESSION_EXPIRED_KEY);
  return flagged;
}

export function clearAuthStorage(): void {
  localStorage.removeItem('authToken');
  localStorage.removeItem('authUsername');
}
