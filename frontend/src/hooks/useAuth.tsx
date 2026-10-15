import { createContext, useContext, useState, useCallback, ReactNode } from 'react';
import { authService } from '@/services/authService';
import { clearAuthStorage, isJwtExpired } from '@/utils/authToken';

interface AuthContextValue {
  username: string | null;
  isAuthenticated: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function readInitialUsername(): string | null {
  const token = localStorage.getItem('authToken');
  const username = localStorage.getItem('authUsername');
  if (!token || !username || isJwtExpired(token)) {
    clearAuthStorage();
    return null;
  }
  return username;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [username, setUsername] = useState<string | null>(() => readInitialUsername());

  const login = useCallback(async (u: string, password: string) => {
    const response = await authService.login({ username: u, password });
    localStorage.setItem('authToken', response.token);
    localStorage.setItem('authUsername', response.username);
    setUsername(response.username);
  }, []);

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } catch {
      // Always clear local session even if revoke call fails
    }
    clearAuthStorage();
    setUsername(null);
    window.location.href = '/login';
  }, []);

  const token = typeof window !== 'undefined' ? localStorage.getItem('authToken') : null;
  const isAuthenticated = Boolean(username && token && !isJwtExpired(token));

  return (
    <AuthContext.Provider value={{ username, isAuthenticated, login, logout }}>{children}</AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
