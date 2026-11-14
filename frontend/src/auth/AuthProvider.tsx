import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react';
import { setUnauthorizedHandler } from '../api/httpClient';
import type { Role } from '../api/types';
import { authService } from '../services/authService';
import { AuthContext, type AuthContextValue } from './AuthContext';
import { sessionStore } from './sessionStore';

export function AuthProvider({ children }: { children: ReactNode }) {
  const [initializing, setInitializing] = useState(true);
  const [role, setRole] = useState<Role | null>(null);
  const [userId, setUserId] = useState<number | null>(null);

  const clearSession = useCallback(() => {
    sessionStore.clear();
    setRole(null);
    setUserId(null);
  }, []);

  useEffect(() => {
    const saved = sessionStore.load();
    if (saved) {
      setRole(saved.role);
      setUserId(saved.userId);
    }
    setInitializing(false);

    setUnauthorizedHandler(() => clearSession());
  }, [clearSession]);

  const login = useCallback(async (email: string, password: string) => {
    const res = await authService.login({ email, password });
    sessionStore.save({ token: res.token, role: res.role, userId: res.userId });
    setRole(res.role);
    setUserId(res.userId);
  }, []);

  const register = useCallback(
    async (
      newRole: Role,
      data: { email: string; password: string; name: string; phone: string },
    ) => {
      await authService.register(newRole, data);
      await login(data.email, data.password);
    },
    [login],
  );

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } catch {
      // Even if the server call fails, we still clear local state below.
    } finally {
      clearSession();
    }
  }, [clearSession]);

  const value = useMemo<AuthContextValue>(
    () => ({
      initializing,
      isAuthenticated: role !== null,
      role,
      userId,
      login,
      register,
      logout,
    }),
    [initializing, role, userId, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
