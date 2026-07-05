# Backend Drift Remediation Master Plan

## Goal

Reduce the open implementation backlog by tightening backend service boundaries on the current Vision path, while excluding legacy frontend-only cleanup unless a backend contract still depends on it.

## Scope Rules

- Focus on backend services, query surfaces, validation, and domain orchestration that Vision or current backend routes still use.
- Do not spend time on legacy frontend-only refactors that are no longer part of the active Vision surface.
- Prefer moving read/query concerns into narrower services before touching mutation flows.

## Slice Order

1. Social and workmarket read/query boundary cleanup for backend consumers still on the Vision path.
2. Identity and location boundary cleanup, especially user-profile and location-lookup seams.
3. Validation, regression tests, and documentation/backlog closeout.

## Notes

- Keep controller cleanup only when it is still needed by the current backend flow.
- When a legacy-facing surface is not used by the current Vision path, treat it as deferred unless the backend contract still requires it.
- Close backlog items only after the current backend behavior is implemented and validated.
