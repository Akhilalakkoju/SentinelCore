# SentinelCore

SentinelCore is a cyber threat intelligence platform with Spring Boot, React, and PostgreSQL.

## Role access

| Capability | Admin | Analyst | Viewer |
| --- | --- | --- | --- |
| Dashboard and reports | Yes | Yes | Yes |
| View threats, IOCs, and alerts | Yes | Yes | Yes |
| Create or update threats, IOCs, and alerts | Yes | Yes | No |
| Delete threats, IOCs, and alerts | Yes | No | No |
| Manage users and roles | Yes | No | No |

The backend enforces these permissions for every API request, while the frontend hides unavailable navigation and blocks direct navigation to write-only pages.

## Sprint status

Completed: backend and frontend foundation, PostgreSQL, registration/login, JWT authentication, protected routes, user CRUD, role management, dashboard navigation, and Admin/Analyst/Viewer authorization.

Planned: refresh tokens, MFA, team management, and Docker Compose for PostgreSQL, MongoDB, Redis, and Kafka.

## License

This project is licensed under the MIT License - see the [LICENSE](file:///c:/Users/USER/Downloads/SentinelCore/LICENSE) file for details.

