import axios from 'axios';
import type { ApiErrorResponse } from '@/types';
import { clearAuthStorage, markSessionExpired } from '@/utils/authToken';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export class ApiRequestError extends Error {
  fieldErrors?: ApiErrorResponse['fieldErrors'];
  constructor(message: string, fieldErrors?: ApiErrorResponse['fieldErrors']) {
    super(message);
    this.fieldErrors = fieldErrors;
  }
}

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const data: ApiErrorResponse | undefined = error?.response?.data;
    const message = data?.message || 'Something went wrong. Please try again.';
    const status = error?.response?.status;
    const url = String(error?.config?.url || '');
    const isLoginCall = url.includes('/auth/login');

    if (status === 401 && !isLoginCall) {
      clearAuthStorage();
      markSessionExpired();
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login';
      }
    }

    return Promise.reject(new ApiRequestError(message, data?.fieldErrors));
  }
);
