import { AxiosError, type AxiosAdapter } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { sessionStore } from '../auth/sessionStore';

describe('HTTP client', () => {
  let httpClient: typeof import('./httpClient').httpClient;
  let setUnauthorizedHandler: typeof import('./httpClient').setUnauthorizedHandler;

  beforeEach(async () => {
    localStorage.clear();
    vi.resetModules();
    ({ httpClient, setUnauthorizedHandler } = await import('./httpClient'));
  });

  function rejectResponse(status: number, data: unknown = {}) {
    httpClient.defaults.adapter = (config) => Promise.reject(new AxiosError(
      'Request failed', 'ERR_BAD_REQUEST', config, undefined,
      { config, status, statusText: 'Error', headers: {}, data },
    ));
  }

  it('uses the same-origin API prefix and does not send credentials before login', async () => {
    const adapter = vi.fn<AxiosAdapter>(async (config) => ({
      config, status: 200, statusText: 'OK', headers: {}, data: {},
    }));
    httpClient.defaults.adapter = adapter;
    await httpClient.get('/riders/me');
    const config = adapter.mock.calls[0][0];
    expect(httpClient.getUri(config)).toBe('/api/riders/me');
    expect(config.headers.has('Authorization')).toBe(false);
  });

  it('uses the current token on each request and stops sending it after logout', async () => {
    const adapter = vi.fn<AxiosAdapter>(async (config) => ({
      config, status: 200, statusText: 'OK', headers: {}, data: {},
    }));
    httpClient.defaults.adapter = adapter;
    sessionStore.save({ token: 'first', role: 'RIDER', userId: 1 });
    await httpClient.get('/riders/me');
    sessionStore.save({ token: 'second', role: 'RIDER', userId: 1 });
    await httpClient.get('/riders/me');
    sessionStore.clear();
    await httpClient.get('/riders/me');
    expect(adapter.mock.calls.map(([config]) => config.headers.get('Authorization')))
      .toEqual(['Bearer first', 'Bearer second', undefined]);
  });

  it('invokes the logout handler when the backend rejects authentication', async () => {
    const unauthorized = vi.fn();
    setUnauthorizedHandler(unauthorized);
    rejectResponse(401, { detail: 'Authentication required' });
    await expect(httpClient.get('/riders/me')).rejects.toThrow('Authentication required');
    expect(unauthorized).toHaveBeenCalledOnce();
  });

  it('keeps the session when access is forbidden instead of unauthenticated', async () => {
    const unauthorized = vi.fn();
    setUnauthorizedHandler(unauthorized);
    rejectResponse(403);
    await expect(httpClient.get('/drivers/me')).rejects.toThrow('You are not allowed to do that.');
    expect(unauthorized).not.toHaveBeenCalled();
  });

  it.each([
    [{ message: 'Email already used' }, 'Email already used'],
    [{ detail: 'Invalid input' }, 'Invalid input'],
    [{ error: 'Internal Server Error' }, 'Internal Server Error'],
  ])('turns backend error bodies into readable errors: %j', async (body, expected) => {
    rejectResponse(400, body);
    await expect(httpClient.post('/api/auth/register/rider')).rejects.toThrow(expected);
  });

  it('provides a useful error when the backend cannot be reached', async () => {
    httpClient.defaults.adapter = () => Promise.reject(new AxiosError('Network Error', 'ERR_NETWORK'));
    await expect(httpClient.get('/riders/me')).rejects.toThrow('Cannot reach the server');
  });
});
