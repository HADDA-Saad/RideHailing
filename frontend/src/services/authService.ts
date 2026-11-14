import { httpClient } from '../api/httpClient';
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  Role,
} from '../api/types';

export const authService = {
  // Paths match Spring; the client's /api base URL is stripped by the proxy.
  async login(payload: LoginRequest): Promise<LoginResponse> {
    const { data } = await httpClient.post<LoginResponse>('/api/auth/login', payload);
    return data;
  },

  async register(role: Role, payload: RegisterRequest): Promise<RegisterResponse> {
    const path =
      role === 'DRIVER' ? '/api/auth/register/driver' : '/api/auth/register/rider';
    const { data } = await httpClient.post<RegisterResponse>(path, payload);
    return data;
  },

  async logout(): Promise<void> {
    await httpClient.post('/api/auth/logout');
  },
};
