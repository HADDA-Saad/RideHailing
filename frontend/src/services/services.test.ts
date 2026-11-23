import { type AxiosAdapter } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { httpClient } from '../api/httpClient';
import { sessionStore } from '../auth/sessionStore';
import { authService } from './authService';
import { profileService } from './profileService';

describe('API services', () => {
  const adapter = vi.fn<AxiosAdapter>();

  beforeEach(() => {
    localStorage.clear();
    httpClient.defaults.adapter = adapter;
  });

  function respondWith(data: unknown, status = 200) {
    adapter.mockImplementation(async (config) => ({
      config, data, status, statusText: 'OK', headers: {},
    }));
  }

  function lastRequest() {
    const calls = adapter.mock.calls;
    return calls[calls.length - 1][0];
  }

  it('posts login credentials to the URL expected by the proxy and returns the session', async () => {
    const credentials = { email: 'rider@example.com', password: 'test-password' };
    const session = { token: 'jwt', userId: 1, role: 'RIDER' };
    respondWith(session);
    expect(await authService.login(credentials)).toEqual(session);
    expect(httpClient.getUri(lastRequest())).toBe('/api/api/auth/login');
    expect(lastRequest().method).toBe('post');
    expect(JSON.parse(lastRequest().data)).toEqual(credentials);
  });

  it.each(['RIDER', 'DRIVER'] as const)('registers a %s using the correct endpoint and payload', async (role) => {
    const account = { email: 'new@example.com', password: 'test-password', name: 'Test User', phone: '0600000000' };
    const registered = { userId: 2, email: account.email };
    respondWith(registered, 201);
    expect(await authService.register(role, account)).toEqual(registered);
    expect(httpClient.getUri(lastRequest())).toBe(`/api/api/auth/register/${role.toLowerCase()}`);
    expect(lastRequest().method).toBe('post');
    expect(JSON.parse(lastRequest().data)).toEqual(account);
  });

  it('posts logout with the existing token and accepts an empty 204 response', async () => {
    sessionStore.save({ token: 'jwt', role: 'RIDER', userId: 1 });
    respondWith(undefined, 204);
    await expect(authService.logout()).resolves.toBeUndefined();
    expect(httpClient.getUri(lastRequest())).toBe('/api/api/auth/logout');
    expect(lastRequest().method).toBe('post');
    expect(lastRequest().headers.get('Authorization')).toBe('Bearer jwt');
  });

  it.each(['RIDER', 'DRIVER'] as const)('loads the %s profile from its role-specific endpoint', async (role) => {
    const profile = { id: 1, name: 'Test User', email: 'test@example.com', phone: '0600000000' };
    respondWith(profile);
    expect(await profileService.getMyProfile(role)).toEqual(profile);
    expect(httpClient.getUri(lastRequest())).toBe(`/api/${role === 'RIDER' ? 'riders' : 'drivers'}/me`);
    expect(lastRequest().method).toBe('get');
  });
});
