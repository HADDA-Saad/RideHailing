// Tokens in localStorage are accessible to page JavaScript; prevent XSS.
import type { Role } from '../api/types';

const TOKEN_KEY = 'ridehailing.token';
const ROLE_KEY = 'ridehailing.role';
const USER_ID_KEY = 'ridehailing.userId';

export interface StoredSession {
  token: string;
  role: Role;
  userId: number;
}

export const sessionStore = {
  save(session: StoredSession): void {
    localStorage.setItem(TOKEN_KEY, session.token);
    localStorage.setItem(ROLE_KEY, session.role);
    localStorage.setItem(USER_ID_KEY, String(session.userId));
  },

  load(): StoredSession | null {
    const token = localStorage.getItem(TOKEN_KEY);
    const role = localStorage.getItem(ROLE_KEY) as Role | null;
    const userIdRaw = localStorage.getItem(USER_ID_KEY);

    if (!token || !role || !userIdRaw) {
      return null;
    }
    return { token, role, userId: Number(userIdRaw) };
  },

  clear(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(ROLE_KEY);
    localStorage.removeItem(USER_ID_KEY);
  },

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  },
};
