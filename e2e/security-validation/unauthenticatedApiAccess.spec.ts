import { test, expect } from '@playwright/test';
import { ReportGenerator } from '../utils/reportGenerator';

test.describe('Module 1: Unauthenticated Backend API Protection Verification', () => {

  const protectedEndpoints = [
    { path: '/api/users', method: 'GET' },
    { path: '/api/roles', method: 'GET' },
    { path: '/api/threats', method: 'GET' },
    { path: '/api/alerts', method: 'GET' },
    { path: '/api/reports/dashboard', method: 'GET' },
    { path: '/api/ioc', method: 'GET' },
  ];

  test.afterAll(() => {
    ReportGenerator.generateSummaryReports();
  });

  for (const endpoint of protectedEndpoints) {
    test(`Verify Endpoint Protection without JWT: ${endpoint.method} ${endpoint.path}`, async ({ request }) => {
      const response = await request.fetch(`http://127.0.0.1:8080${endpoint.path}`, {
        method: endpoint.method,
      });

      const status = response.status();
      const isProtected = status === 401 || status === 403;

      ReportGenerator.recordFinding({
        checkName: `Unauthenticated API Endpoint Access: ${endpoint.path}`,
        category: 'API Authorization',
        endpointOrPage: endpoint.path,
        severity: 'HIGH',
        status: isProtected ? 'PASSED' : 'FAILED',
        details: `HTTP Response Status: ${status}`,
        recommendation: 'Ensure Spring Security restricts API endpoint to authenticated JWT sessions.',
        timestamp: new Date().toISOString(),
      });

      expect(isProtected).toBe(true);
    });
  }
});
