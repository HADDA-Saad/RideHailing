import { beforeEach, describe, expect, it } from 'vitest';
import { sessionStore } from './sessionStore';

const session = { token: 'test-token', role: 'RIDER', userId: 42 } as const;

describe('session storage', () => {
  beforeEach(() => localStorage.clear());

  it('returns no session or token before login', () => {
    expect(sessionStore.load()).toBeNull();
    expect(sessionStore.getToken()).toBeNull();
  });

  it('restores a saved session with a numeric user ID', () => {
    sessionStore.save(session);
    expect(sessionStore.load()).toEqual(session);
    expect(sessionStore.getToken()).toBe(session.token);
  });

  it.each(['ridehailing.token', 'ridehailing.role', 'ridehailing.userId'])(
    'does not restore an incomplete session missing %s', (key) => {
      sessionStore.save(session);
      localStorage.removeItem(key);
      expect(sessionStore.load()).toBeNull();
    },
  );

  it('clears authentication while preserving unrelated browser data', () => {
    localStorage.setItem('theme', 'dark');
    sessionStore.save(session);
    sessionStore.clear();
    expect(sessionStore.load()).toBeNull();
    expect(sessionStore.getToken()).toBeNull();
    expect(localStorage.getItem('theme')).toBe('dark');
  });
});
