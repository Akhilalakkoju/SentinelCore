import { test, expect } from '@playwright/test';
import { ReportGenerator } from '../utils/reportGenerator';

test.describe('Module 1: Security Headers & Cookie Configuration Verification', () => {

  test.afterAll(() => {
    ReportGenerator.generateSummaryReports();
  });

  test('Verify Security Headers on Primary Endpoints', async ({ request }) => {
    const response = await request.get('/index.html');
    const headers = response.headers();

    const expectedHeaders = [
      { name: 'x-frame-options', required: true, severity: 'MEDIUM' as const },
      { name: 'x-content-type-options', required: true, severity: 'LOW' as const },
      { name: 'content-security-policy', required: false, severity: 'MEDIUM' as const },
      { name: 'strict-transport-security', required: false, severity: 'HIGH' as const },
    ];

    for (const item of expectedHeaders) {
      const isPresent = Boolean(headers[item.name]);
      ReportGenerator.recordFinding({
        checkName: `Security Header Verification: ${item.name}`,
        category: 'Header Verification',
        endpointOrPage: '/',
        severity: item.severity,
        status: isPresent ? 'PASSED' : 'FAILED',
        details: isPresent ? `Header ${item.name} present: ${headers[item.name]}` : `Header ${item.name} is missing`,
        recommendation: `Configure Spring Security or Web server to include ${item.name} header.`,
        timestamp: new Date().toISOString(),
      });
    }

    expect(response.status()).toBeLessThan(400);
  });

  test('Verify Cookie Attributes Security Policies', async ({ page }) => {
    await page.goto('/login');
    const cookies = await page.context().cookies();

    for (const cookie of cookies) {
      const isHttpOnly = cookie.httpOnly;
      const isSecure = cookie.secure;
      const sameSiteValid = ['Strict', 'Lax'].includes(cookie.sameSite);

      ReportGenerator.recordFinding({
        checkName: `Cookie Security Verification (${cookie.name})`,
        category: 'Cookie Security',
        endpointOrPage: '/login',
        severity: 'HIGH',
        status: (isHttpOnly && isSecure && sameSiteValid) ? 'PASSED' : 'FAILED',
        details: `Cookie ${cookie.name}: HttpOnly=${isHttpOnly}, Secure=${isSecure}, SameSite=${cookie.sameSite}`,
        recommendation: 'Ensure all session cookies set HttpOnly=true, Secure=true, and SameSite=Lax/Strict.',
        timestamp: new Date().toISOString(),
      });
    }
  });
});
