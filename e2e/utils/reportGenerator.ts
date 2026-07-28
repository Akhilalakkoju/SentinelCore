import * as fs from 'fs';
import * as path from 'path';

export interface SecurityFinding {
  checkName: string;
  category: 'Header Verification' | 'Cookie Security' | 'API Authorization' | 'Session Handling' | 'RBAC Matrix';
  endpointOrPage: string;
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'INFORMATIONAL';
  status: 'PASSED' | 'FAILED';
  details: string;
  recommendation: string;
  timestamp: string;
}

export class ReportGenerator {
  private static findings: SecurityFinding[] = [];

  static recordFinding(finding: SecurityFinding) {
    this.findings.push(finding);
  }

  static generateSummaryReports(outputDir: string = './reports') {
    if (!fs.existsSync(outputDir)) {
      fs.mkdirSync(outputDir, { recursive: true });
    }

    const summaryData = {
      executionTime: new Date().toISOString(),
      totalChecks: this.findings.length,
      passedChecks: this.findings.filter(f => f.status === 'PASSED').length,
      failedChecks: this.findings.filter(f => f.status === 'FAILED').length,
      severityBreakdown: {
        CRITICAL: this.findings.filter(f => f.severity === 'CRITICAL' && f.status === 'FAILED').length,
        HIGH: this.findings.filter(f => f.severity === 'HIGH' && f.status === 'FAILED').length,
        MEDIUM: this.findings.filter(f => f.severity === 'MEDIUM' && f.status === 'FAILED').length,
        LOW: this.findings.filter(f => f.severity === 'LOW' && f.status === 'FAILED').length,
        INFORMATIONAL: this.findings.filter(f => f.severity === 'INFORMATIONAL' && f.status === 'FAILED').length,
      },
      findings: this.findings,
    };

    fs.writeFileSync(
      path.join(outputDir, 'security-summary.json'),
      JSON.stringify(summaryData, null, 2)
    );

    const htmlContent = `
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>SentinelCore Defensive Security Verification Report</title>
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; margin: 20px; background: #0f172a; color: #f8fafc; }
        h1 { color: #38bdf8; border-bottom: 2px solid #334155; padding-bottom: 10px; }
        .summary-cards { display: flex; gap: 15px; margin-bottom: 20px; }
        .card { background: #1e293b; padding: 15px; border-radius: 8px; flex: 1; text-align: center; border: 1px solid #334155; }
        .card .number { font-size: 24px; font-weight: bold; margin-top: 5px; }
        .passed { color: #4ade80; }
        .failed { color: #f87171; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; background: #1e293b; border-radius: 8px; overflow: hidden; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #334155; }
        th { background: #0f172a; color: #94a3b8; }
        .badge-PASSED { background: #166534; color: #4ade80; padding: 4px 8px; border-radius: 4px; font-weight: bold; }
        .badge-FAILED { background: #991b1b; color: #f87171; padding: 4px 8px; border-radius: 4px; font-weight: bold; }
    </style>
</head>
<body>
    <h1>SentinelCore Defensive Security & Authorization Report</h1>
    <div class="summary-cards">
        <div class="card">Total Checks<div class="number">${summaryData.totalChecks}</div></div>
        <div class="card">Passed<div class="number passed">${summaryData.passedChecks}</div></div>
        <div class="card">Failed<div class="number failed">${summaryData.failedChecks}</div></div>
    </div>
    <h2>Verification Findings</h2>
    <table>
        <thead>
            <tr>
                <th>Check Name</th>
                <th>Category</th>
                <th>Target</th>
                <th>Severity</th>
                <th>Status</th>
                <th>Recommendation</th>
            </tr>
        </thead>
        <tbody>
            ${this.findings.map(f => `
                <tr>
                    <td>${f.checkName}</td>
                    <td>${f.category}</td>
                    <td>${f.endpointOrPage}</td>
                    <td>${f.severity}</td>
                    <td><span class="badge-${f.status}">${f.status}</span></td>
                    <td>${f.recommendation}</td>
                </tr>
            `).join('')}
        </tbody>
    </table>
</body>
</html>
    `;

    fs.writeFileSync(path.join(outputDir, 'security-summary.html'), htmlContent);
  }
}
