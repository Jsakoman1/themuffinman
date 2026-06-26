# Repository Guidelines

## Product

`TheMuffinMan` is a multi-module product. `SideQuest` is one module inside it.

Planned modules:
1. work marketplace
2. business mini websites and appointment booking
3. thing sharing / lending
4. voluntary car sharing for selected circles
5. shared chat across modules

## Structure

- Product modules live in `apps/`.
- Shared backend capabilities live in `services/`.
- `apps/themuffinman` contains the current Spring Boot app and frontend.
- Backend code lives in `apps/themuffinman/src/main/java/com/themuffinman/app`.
- Keep Spring code organized by domain and layer: `controller`, `service`, `repository`, `model`, `dto`, `mapper`, `config`.
- Flyway migrations live in `apps/themuffinman/src/main/resources/db/migration`.
- Frontend lives in `apps/themuffinman/frontend/`.

## Commands

- Backend: `./mvnw spring-boot:run`, `./mvnw test`, `./mvnw package`
- Frontend: `npm run dev`, `npm run type-check`, `npm run build`
- Full local dev: `make dev`

## Architecture

- Prefer backend-centric domain design.
- Put business rules, permissions, validations, workflow rules, and state transitions in backend services.
- Keep frontend code thin so the same logic can later support mobile clients.
- Prefer backend-prepared DTOs and sections over frontend-derived product logic.
- When logic moves to backend, remove duplicated frontend logic unless there is a clear offline need.
- Reuse core concepts carefully across modules: users, circles, scheduling, bookings, visibility, consent, messaging.

## Config

- Put operational backend config in typed central config objects under `config/`, using `@ConfigurationProperties`.
- This applies to retention, cleanup jobs, cache settings, TTLs, rate limits, polling intervals, timeouts, and similar runtime behavior.
- Avoid scattering those settings across many `@Value` fields when they belong to one operational area.
- Keep environment variable mapping in `application.properties`.
- Keep shared local defaults in `.env.backend.dev` and developer-specific overrides in `.env.backend.dev.local`.
- Add new operational config to those central classes first, then wire it into services or jobs.

## Working Style

- Treat the user as a junior developer.
- Prefer simple, readable, incremental implementations over speculative abstractions.
- Optimize for standardization, simplicity, and maintainability over visual flourish.
- Default to minimal UI logic.
- Standardize authenticated pages around one shared app shell.
- Prefer shared components, shared tokens, and shared layout patterns over one-off implementations.
- Standardize dialogs and detail surfaces.
- For dialogs with enough content, default to two columns: main content left, utility/actions right.
- Prefer inline editing on an already open detail surface over opening a second nested edit popup.
- Keep controllers thin.
- Add a new Flyway migration for schema changes; do not edit old migrations.
- Add or extend JUnit tests whenever backend behavior changes.
- Keep `./mvnw test` passing for backend changes.
- Run `npm run type-check` and `npm run build` for frontend changes.

## Collaboration

- Do not commit or push changes unless explicitly asked.
- Keep code, comments, docs, API text, and user-facing copy in English.
