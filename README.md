# TheMuffinMan

TheMuffinMan is a single application workspace with multiple product modules inside one backend and one frontend.

Planned product modules inside `apps/themuffinman`:
- work marketplace
- business mini websites and appointment booking
- thing sharing / lending
- voluntary car sharing for selected circles
- shared chat across modules

## Current State

Today this repository contains one runnable application:
- `apps/themuffinman`

`TheMuffinMan` now serves as the main Spring Boot + Vue application. The existing work marketplace remains its first implemented module.

## Repository Structure

```text
themuffinman/
  apps/
    themuffinman/
  docs/
    business-logic.md
    domain-technical.md
```

## Living Documentation

- `docs/business-logic.md` explains product behavior in user-facing language.
- `docs/domain-technical.md` tracks entities, relations, validations, permissions, and workflow rules.
- `docs/domain-technical.md` is the technical source of truth for domain behavior and workflows.
- Keep both files aligned with the current codebase when meaningful domain behavior changes.

## IntelliJ Setup

1. Open the `themuffinman` folder in IntelliJ IDEA.
2. Import `apps/themuffinman/pom.xml` as a Maven project.
3. Treat `apps/themuffinman/frontend` as the Vue frontend workspace.
4. Add future modules incrementally inside `apps/themuffinman` instead of creating placeholder applications.

## Development Direction

- Keep core product logic in the backend.
- Keep frontend code thin and UI-focused.
- Design shared concepts carefully across modules: users, circles, scheduling, chat, visibility, and consent flows.
- Prefer incremental module extraction over large rewrites.

## Main App

See `apps/themuffinman/README.md` for TheMuffinMan setup and commands.

## Running Locally

The only runnable application at the moment is `apps/themuffinman`.

Prerequisites:
- Java 21
- PostgreSQL
- Node.js and npm

Database expected by the backend:
- host: `localhost`
- port: `5432`
- database: `side_quest_db`
- username: `jsakoman`
- password: empty

Run from the repository root:

```bash
npm run themuffinman:frontend:install
npm run themuffinman:dev
```

This starts:
- backend on `http://localhost:8080`
- frontend on `http://localhost:5173`

Useful root scripts:
- `npm run themuffinman:backend`
- `npm run themuffinman:frontend`
- `npm run themuffinman:test`
- `npm run themuffinman:build:backend`
- `npm run themuffinman:build:frontend`
- `npm run themuffinman:type-check`

Compatibility aliases remain available for now:
- `npm run sidequest:dev`
- `npm run sidequest:backend`
