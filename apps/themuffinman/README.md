# TheMuffinMan

TheMuffinMan is the main application shell for the broader product platform.

Today it contains the first implemented module:
- `work marketplace`

The current work marketplace flow allows users to post, browse, apply for, manage, and review small paid jobs.

This module does not handle payments, billing, invoicing, or accounting. It focuses on task discovery, applications, coordination, visibility, and user reputation.

## Current Scope

The current module includes:
- user registration and login
- role-based access with user and admin accounts
- profile editing with avatar and description
- quest creation and editing
- quest search, filters, and detail views
- quest applications with approve and decline flows
- quest lifecycle management: open, assigned, in progress, waiting confirmation, completed, cancelled
- circle-based visibility and private connection management
- quest news and activity notifications
- employer and worker review system with 1-5 star ratings and short comments
- admin pages for users, quests, applications, and circles

## Tech Stack

- Backend: Java 21, Spring Boot, Spring Security, Spring Data JPA, Flyway
- Database: PostgreSQL
- Frontend: Vue 3, TypeScript, Vite, Vue Router, Axios

## Module Structure

Backend:
- `src/main/java/com/sidequest/sidequest/controller` REST endpoints
- `src/main/java/com/sidequest/sidequest/service` business logic
- `src/main/java/com/sidequest/sidequest/repository` JPA repositories
- `src/main/java/com/sidequest/sidequest/model` entities and enums
- `src/main/java/com/sidequest/sidequest/dto` request and response DTOs
- `src/main/java/com/sidequest/sidequest/mapper` entity/DTO mapping helpers
- `src/main/java/com/sidequest/sidequest/security` JWT and user details logic
- `src/main/java/com/sidequest/sidequest/config` application configuration

Database:
- `src/main/resources/db/migration` Flyway migrations

Frontend:
- `frontend/src/pages` page-level admin and listing pages
- `frontend/src/views` route views such as quest detail, profile, login, register
- `frontend/src/components` reusable UI and dashboard components
- `frontend/src/composables` stateful Vue composition helpers
- `frontend/src/api` HTTP client and API definitions
- `frontend/src/shared` domain helpers and shared formatting logic

## Local Development

Run commands from `apps/themuffinman` unless stated otherwise.

### Backend

- `./mvnw spring-boot:run`
- `./mvnw test`
- `./mvnw package`
- `make dev`
- `make backend-dev`
- `make backend-test`
- `make backend-package`

The backend expects PostgreSQL at:
- `jdbc:postgresql://localhost:5432/side_quest_db`
- with `PostGIS` available for nearby quest search

Current local configuration is defined in:
- `src/main/resources/application.properties`
- optional local env overrides:
  - `.env.backend.dev`
  - `.env.backend.dev.local`

Important notes:
- Flyway is enabled and manages schema changes
- JPA uses `ddl-auto=validate`
- when changing the database schema, add a new Flyway migration instead of editing an old one
- the base `application.properties` is production-oriented and expects security env vars
- for local development, run with `SPRING_PROFILES_ACTIVE=dev`
- `make dev` and `make backend-dev` provide local JWT/CORS defaults
- `make dev` auto-loads values from `.env.backend.dev` and `.env.backend.dev.local` and skips starting another backend if port `8080` is already in use
- `make backend-dev` also auto-loads values from `.env.backend.dev` and `.env.backend.dev.local`
- production should not run with the `dev` profile

### Frontend

Run from `apps/themuffinman/frontend`:
- `npm install`
- `npm run dev`
- `npm run build`
- `npm run type-check`

If needed, set `VITE_API_BASE_URL` to point the frontend at a different backend URL.

## Review System

The work marketplace currently supports two reputation tracks per user:
- `as employer`
- `as worker`

Rules:
- reviews are tied to a specific quest
- reviews are allowed only after the quest is completed
- the approved worker can review the employer
- the employer can review the approved worker
- rating is from 1 to 5 stars
- a short comment is optional

## Testing

Backend:
- JUnit 5 via `spring-boot-starter-test`
- run `./mvnw test`

Frontend:
- no dedicated test runner is configured yet
- run `npm run type-check`
- manually verify affected flows in the browser

## Local Dev Accounts

When you run with `SPRING_PROFILES_ACTIVE=dev`, the app seeds local development accounts:
- admin email: `admin@sidequest.local`
- admin password: `admin123`
- test email: `test@test.com`
- test password: `test123`

These are for local development only.

Local backend startup example:

```bash
cd apps/themuffinman
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

Backend + frontend together:

```bash
cd apps/themuffinman
make dev
```

Equivalent shortcut:

```bash
cd apps/themuffinman
make backend-dev
```

Example env file:
- `.env.backend.dev.example`

If you do not run with `SPRING_PROFILES_ACTIVE=dev`, the local default admin account is not created from fallback config anymore.

## Security Configuration

The base `src/main/resources/application.properties` is intended to be production-oriented:
- no fallback JWT secret
- no default seeded admin password
- no default seeded test password
- default user seed is disabled
- bootstrap admin flow is disabled by default

Local-only defaults live in:
- `src/main/resources/application-dev.properties`
- `.env.backend.dev.example`

Production should provide these values through environment variables or another secure configuration source:
- `SIDEQUEST_JWT_SECRET`
- `SIDEQUEST_CORS_ALLOWED_ORIGINS`

Optional production overrides:
- `SIDEQUEST_JWT_EXPIRATION_MILLIS`
- `SIDEQUEST_SEED_USERS_ENABLED`
- `SIDEQUEST_BOOTSTRAP_ADMIN_ENABLED`
- `SIDEQUEST_BOOTSTRAP_ADMIN_EMAIL`
- `SIDEQUEST_BOOTSTRAP_ADMIN_USERNAME`
- `SIDEQUEST_BOOTSTRAP_ADMIN_PASSWORD`

Example production env file:
- `.env.backend.production.example`
- `make backend-bootstrap-example` prints the one-time bootstrap command shape

## Production Admin Bootstrap

Production should not rely on seeded default accounts.

Recommended options:
- create an admin user through an existing admin account using the admin user management flow
- or run a one-time bootstrap with explicit env vars:

```bash
SIDEQUEST_BOOTSTRAP_ADMIN_ENABLED=true
SIDEQUEST_BOOTSTRAP_ADMIN_EMAIL=owner@example.com
SIDEQUEST_BOOTSTRAP_ADMIN_USERNAME=owner
SIDEQUEST_BOOTSTRAP_ADMIN_PASSWORD=replace-this
```

After the admin account is created and verified, turn `SIDEQUEST_BOOTSTRAP_ADMIN_ENABLED` back to `false`.

Example:

```bash
cd apps/themuffinman
SIDEQUEST_JWT_SECRET=replace-with-long-random-secret \
SIDEQUEST_CORS_ALLOWED_ORIGINS=https://your-app.example.com \
SIDEQUEST_BOOTSTRAP_ADMIN_ENABLED=true \
SIDEQUEST_BOOTSTRAP_ADMIN_EMAIL=owner@example.com \
SIDEQUEST_BOOTSTRAP_ADMIN_USERNAME=owner \
SIDEQUEST_BOOTSTRAP_ADMIN_PASSWORD=replace-this \
./mvnw spring-boot:run
```

That startup ensures the admin account exists or updates it to the supplied credentials. This is intended for one-time setup or controlled recovery, not for permanent day-to-day production startup.

## Future Admin Accounts

Recommended ways to add admin access in the future:
- use an existing admin account and update another user through the admin user management flow
- use the one-time bootstrap admin env vars only for initial setup or controlled recovery

Recommended operational rule:
- never rely on long-lived default admin credentials
- never leave bootstrap admin mode enabled permanently
- never run production with local `dev` profile defaults
