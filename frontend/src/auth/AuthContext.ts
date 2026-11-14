import { createContext } from 'react';
import type { Role } from '../api/types';

export interface AuthState {
  initializing: boolean;
  isAuthenticated: boolean;
  role: Role | null;
  userId: number | null;
}

export interface AuthContextValue extends AuthState {
  login: (email: string, password: string) => Promise<void>;
  register: (
    role: Role,
    data: { email: string; password: string; name: string; phone: string },
  ) => Promise<void>;
  logout: () => Promise<void>;
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined);
