import { APIRequestContext } from '@playwright/test';

export interface UserCredentials {
  email: string;
  password: string;
  role: 'ADMIN' | 'ANALYST' | 'VIEWER';
}

export interface AuthSession {
  accessToken: string;
  refreshToken?: string;
  role: string;
  email: string;
}

export class AuthHelper {
  private static backendApiUrl = process.env.BACKEND_URL || 'http://127.0.0.1:8080/api';

  /**
   * Authenticate against Spring Boot backend to obtain JWT bearer token
   */
  static async login(request: APIRequestContext, credentials: UserCredentials): Promise<AuthSession> {
    const response = await request.post(`${this.backendApiUrl}/auth/login`, {
      data: {
        email: credentials.email,
        password: credentials.password,
      },
    });

    if (!response.ok()) {
      throw new Error(`Authentication failed for role ${credentials.role}: HTTP ${response.status()}`);
    }

    const data = await response.json();
    return {
      accessToken: data.accessToken || data.token,
      refreshToken: data.refreshToken,
      role: credentials.role,
      email: credentials.email,
    };
  }

  /**
   * Helper method providing standard test credentials for standard SentinelCore roles
   */
  static getStandardCredentials(role: 'ADMIN' | 'ANALYST' | 'VIEWER'): UserCredentials {
    switch (role) {
      case 'ADMIN':
        return { email: 'admin@sentinelcore.local', password: 'AdminPassword123!', role: 'ADMIN' };
      case 'ANALYST':
        return { email: 'analyst@sentinelcore.local', password: 'AnalystPassword123!', role: 'ANALYST' };
      case 'VIEWER':
      default:
        return { email: 'viewer@sentinelcore.local', password: 'ViewerPassword123!', role: 'VIEWER' };
    }
  }
}
