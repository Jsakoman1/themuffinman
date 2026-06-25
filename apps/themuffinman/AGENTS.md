# Repository Guidelines

## Project Structure & Module Organization

The backend is a Spring Boot application under `src/main/java/com/sidequest/sidequest`, organized by layer:
`controller`, `service`, `repository`, `model`, `dto`, `mapper`, `security`, and `config`. Database configuration lives
in `src/main/resources/application.properties`, and Flyway migrations are in `src/main/resources/db/migration` with
versioned files such as `V3__create_quest_application_table.sql`. Backend tests live in `src/test/java`. The Vue
frontend is isolated in `frontend/`, with pages in `frontend/src/pages`, route-level views in `frontend/src/views`,
shared assets in `frontend/src/assets`, and static files in `frontend/public`.

## Build, Test, and Development Commands

Run the backend from the repo root:

- `./mvnw spring-boot:run` - starts the API against the local PostgreSQL database.
- `./mvnw test` - runs JUnit tests.
- `./mvnw package` - builds the backend artifact.

Run the frontend from `frontend/`:

- `npm install` - installs Vite, Vue, and TypeScript dependencies.
- `npm run dev` - starts the Vite dev server, usually on `http://localhost:5173`.
- `npm run build` - creates a production bundle.
- `npm run type-check` - runs `vue-tsc --noEmit`.

## Coding Style & Naming Conventions

Use 4-space indentation in Java and keep the existing Spring layering intact. Class names use PascalCase (
`QuestApplicationService`), methods and fields use camelCase, and DTO/model suffixes should stay explicit. Keep REST
controllers small and push business logic into `service`. In the frontend, use TypeScript, PascalCase for Vue component
filenames (`QuestsPage.vue`), and camelCase for exported symbols and helpers.

## Testing Guidelines

Backend tests use JUnit 5 through `spring-boot-starter-test`. Name test classes after the target class with a `Test` or
`Tests` suffix, and add focused service/controller tests when behavior changes. Keep `./mvnw test` passing before
opening a PR. The frontend currently has no test runner configured, so at minimum run `npm run type-check` and verify
affected flows manually.

## Commit & Pull Request Guidelines

Recent commits use short, imperative messages such as `implement login logout frontend`. Follow that style and keep each
commit scoped to one change. PRs should include a clear summary, note database or auth impacts, link the relevant issue
when available, and attach screenshots for frontend changes under `frontend/` or user-visible flows.

## Security & Configuration Tips

The backend expects PostgreSQL at `jdbc:postgresql://localhost:5432/side_quest_db` and currently reads credentials from
`application.properties`. Do not commit real secrets or environment-specific credentials. When changing the schema, add
a new Flyway migration instead of editing an existing versioned script.

## Product Context

`SideQuest` is a platform that connects people who need help with a task and offer money in return with people looking
for extra work. The platform is used for posting and browsing quests. It does not handle payments, billing, invoicing,
or accounting. Keep this boundary clear in backend logic, API design, and UI text.

`SideQuest` is not the final standalone product. It is planned as one module inside a larger application called
`TheMuffinMan`.

`TheMuffinMan` is planned to include at least these modules:

1. work marketplace
2. custom mini websites for user-owned businesses, including appointment booking
3. thing sharing / lending
4. car sharing based on voluntary route matches between selected circles

When making architectural decisions, prefer backend models and APIs that can evolve toward this broader multi-module
product without forcing a rewrite of core concepts such as users, circles, scheduling, bookings, visibility, and
messaging.

Additional planned product context:

- A user may own multiple businesses.
- Each business may need its own scheduling configuration, calendar, and booking slots.
- Some booking slots are single-capacity, for example hairdresser or nails.
- Some booking slots are multi-capacity, for example doggy daycare.
- Thing sharing should support free lending and paid lending.
- Car sharing suggestions must be voluntary, privacy-aware, and GDPR-conscious for both sides.
- A shared chat model is expected across all modules and should be considered a cross-cutting concern.

## Working Preferences

Treat the user as a junior developer and keep implementations simple, readable, and incremental. Prefer step-by-step
changes over large all-at-once implementations. Follow modern `Java 21+` conventions while keeping the code approachable
for a junior-level codebase. Focus primarily on the backend. The frontend should stay minimal and functional unless the
user explicitly asks for more polish.

Prefer putting business logic, permissions, workflow rules, validations, and state transitions in the backend.
Keep the frontend as thin and clean as reasonably possible so that the same product logic can later be reused more
easily for native iPhone and Android applications.

Avoid pushing important domain rules into frontend-only code. Frontend code should mainly orchestrate API calls, render
state, and handle UI-specific concerns.

Standardize dialogs and detail surfaces around a consistent two-column pattern when the content supports it: main
content on the left, utility metadata and actions on the right. For profile/detail views that are already open, prefer
inline editing inside the same surface instead of opening a second nested edit popup.

## Collaboration Rules

Do not commit or push changes. Only modify files in the workspace. Write all code, comments, API text, and user-facing
copy in English, even when the user gives instructions in Croatian. Do not leave trailing empty lines at the end of
files when creating or editing them.
