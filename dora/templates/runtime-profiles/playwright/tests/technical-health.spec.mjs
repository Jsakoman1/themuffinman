import { test, expect } from '@playwright/test';

test('technical health endpoint responds with the neutral Dora marker', async ({ request }) => {
  const response = await request.get('/health');
  await expect(response).toBeOK();
  await expect(response.json()).resolves.toEqual({ dora: 'technical-health' });
});
