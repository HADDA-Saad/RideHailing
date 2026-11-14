import axios, { AxiosError } from 'axios';
import { sessionStore } from '../auth/sessionStore';

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';

export const httpClient = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
});

httpClient.interceptors.request.use((config) => {
  const token = sessionStore.getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let onUnauthorized: (() => void) | null = null;
export function setUnauthorizedHandler(handler: () => void): void {
  onUnauthorized = handler;
}

httpClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401 && onUnauthorized) {
      onUnauthorized();
    }
    return Promise.reject(new Error(extractMessage(error)));
  },
);

function extractMessage(error: AxiosError): string {
  const data = error.response?.data as
    | { message?: string; detail?: string; error?: string }
    | undefined;

  if (data?.message) return data.message;
  if (data?.detail) return data.detail;
  if (data?.error) return data.error;

  if (error.response?.status === 401) return 'Invalid email or password.';
  if (error.response?.status === 403) return 'You are not allowed to do that.';
  if (error.code === 'ERR_NETWORK') {
    return 'Cannot reach the server. Is the backend running on port 8080?';
  }
  return error.message || 'Something went wrong.';
}
