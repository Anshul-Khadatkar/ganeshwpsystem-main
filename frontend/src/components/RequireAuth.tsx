import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { clearAuthStorage, isJwtExpired, markSessionExpired } from '@/utils/authToken';

export function RequireAuth() {
  const { isAuthenticated } = useAuth();
  const location = useLocation();
  const token = localStorage.getItem('authToken');

  if (token && isJwtExpired(token)) {
    clearAuthStorage();
    markSessionExpired();
    return <Navigate to="/login" replace />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <Outlet />;
}
