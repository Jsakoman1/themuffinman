# SideQuest

SideQuest is the work marketplace module inside `TheMuffinMan`.

It allows users to post, browse, apply for, manage, and review small paid jobs.

SideQuest does not handle payments, billing, invoicing, or accounting. It focuses on task discovery, applications, coordination, visibility, and user reputation.

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

Run commands from `apps/sidequest` unless stated otherwise.

### Backend

- `./mvnw spring-boot:run`
- `./mvnw test`
- `./mvnw package`

The backend expects PostgreSQL at:
- `jdbc:postgresql://localhost:5432/side_quest_db`

Current local configuration is defined in:
- `src/main/resources/application.properties`

Important notes:
- Flyway is enabled and manages schema changes
- JPA uses `ddl-auto=validate`
- when changing the database schema, add a new Flyway migration instead of editing an old one

### Frontend

Run from `apps/sidequest/frontend`:
- `npm install`
- `npm run dev`
- `npm run build`
- `npm run type-check`

If needed, set `VITE_API_BASE_URL` to point the frontend at a different backend URL.

## Review System

SideQuest currently supports two reputation tracks per user:
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

## Default Local Accounts

The local configuration currently seeds default accounts from `application.properties`:
- admin email: `admin@sidequest.local`
- admin password: `admin123`
- test email: `test@test.com`
- test password: `test123`

These are for local development only.
