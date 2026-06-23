# Repository Guidelines

## Product Direction

`TheMuffinMan` is a multi-module product. `SideQuest` is only one module inside it.

Planned modules:
1. work marketplace
2. business mini websites and appointment booking
3. thing sharing / lending
4. voluntary car sharing for selected circles
5. shared chat across modules as a cross-cutting capability

## Architecture Direction

Prefer backend-centric domain design.

- Put business rules, permissions, workflow rules, validations, and state transitions in backend services.
- Keep frontend code as thin and clean as practical so the same logic can later support iPhone and Android apps.
- Avoid putting important product rules only in frontend code.
- Reuse core concepts carefully across modules: users, circles, scheduling, bookings, visibility, consent, and messaging.

## Monorepo Structure

Use `apps/` for product modules and `services/` for shared backend capabilities.
Keep module boundaries explicit. Do not merge unrelated domains prematurely.

## Working Style

Treat the user as a junior developer. Prefer simple, readable, incremental implementations over broad speculative abstractions.

## Collaboration Rules

Do not commit or push changes unless explicitly asked. Keep code, comments, documentation, API text, and user-facing copy in English.
