import { test, expect } from '@playwright/test';
import { ReportGenerator } from '../utils/reportGenerator';

test.describe('Module 2: Authorization & RBAC UI Component Visibility Matrix', () => {

  test.afterAll(() => {
    ReportGenerator.generateSummaryReports();
  });

  const roles = [
    { name: 'VIEWER', allowedPages: ['/dashboard', '/alerts', '/threats', '/ioc'], restrictedPages: ['/users', '/roles'] },
    { name: 'ANALYST', allowedPages: ['/dashboard', '/alerts', '/threats', '/ioc'], restrictedPages: ['/users', '/roles'] },
    { name: 'ADMIN', allowedPages: ['/dashboard', '/alerts', '/threats', '/ioc', '/users', '/roles'], restrictedPages: [] },
  ];

  for (const roleInfo of roles) {
    test(`Verify Protected Navigation & Pages for Role: ${roleInfo.name}`, async ({ page }) => {
      // Set session role in client
      await page.goto('/login');
      await page.evaluate((role) => {
        localStorage.setItem('token', 'mock.jwt.token');
        localStorage.setItem('role', role);
        localStorage.setItem('isLoggedIn', 'true');
      }, roleInfo.name);

      // Verify restricted pages trigger redirect or restricted UI state
      for (const pagePath of roleInfo.restrictedPages) {
        await page.goto(pagePath);
        const currentUrl = page.url();
        const isAccessBlocked = currentUrl.includes('/login') || currentUrl.includes('/unauthorized') || currentUrl.includes('/dashboard');

        ReportGenerator.recordFinding({
          checkName: `RBAC UI Navigation Restriction: ${roleInfo.name} -> ${pagePath}`,
          category: 'RBAC Matrix',
          endpointOrPage: pagePath,
          severity: 'HIGH',
          status: isAccessBlocked ? 'PASSED' : 'FAILED',
          details: `Role ${roleInfo.name} attempted navigation to ${pagePath}. Current URL: ${currentUrl}`,
          recommendation: 'Ensure frontend router enforces role-based guard for admin-only pages.',
          timestamp: new Date().toISOString(),
        });
      }
    });
  }
});
