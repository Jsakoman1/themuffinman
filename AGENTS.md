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
- Prefer backend-prepared screen DTOs and sections over frontend-derived view models when the same behavior will be needed on mobile clients.
- Keep labels, statuses, badges, action availability, navigation targets, and workflow decisions centralized in backend contracts when they reflect product behavior rather than purely local UI mechanics.
- When logic is moved to backend, delete the corresponding frontend duplication instead of keeping fallback copies unless there is a clear offline or resilience requirement.
- Reuse core concepts carefully across modules: users, circles, scheduling, bookings, visibility, consent, and messaging.

## Monorepo Structure

Use `apps/` for product modules and `services/` for shared backend capabilities.
Keep module boundaries explicit. Do not merge unrelated domains prematurely.

## Working Style

Treat the user as a junior developer. Prefer simple, readable, incremental implementations over broad speculative abstractions.
- Optimize for standardization, simplicity, and maintainability over visual flourish.
- Default to minimal UI logic. Frontend should mostly render backend data, manage transient form state, and handle platform-specific interaction details.
- Standardize the authenticated frontend around one shared app shell.
- Use one reusable header/navbar pattern across authenticated pages unless a route is intentionally public or intentionally isolated for a strong product reason.
- Use one reusable footer pattern across the app unless a route is intentionally public or intentionally isolated for a strong product reason.
- Use shared modal, dialog, toast, error, and empty-state components instead of per-page variants.
- When a new shared component is introduced, remove or consolidate older duplicate implementations instead of keeping parallel UI patterns alive.
- Reduce frontend-only layout and presentation branching where possible; prefer reusable sections, route metadata, and backend-prepared view contracts over duplicated page-specific orchestration.
- Favor small helpers and explicit service boundaries over large smart components or broad utility layers.
- Add or extend JUnit tests whenever backend behavior, presentation mapping, validation, permission checks, or workflow transitions change.

## Collaboration Rules

Do not commit or push changes unless explicitly asked. Keep code, comments, documentation, API text, and user-facing copy in English.
