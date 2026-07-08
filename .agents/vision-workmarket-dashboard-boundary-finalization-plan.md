# Vision Workmarket Dashboard Boundary Finalization Plan

## Goal

Remove the remaining `workmarket -> vision` dashboard prompt and voice compile-time dependency while preserving the existing `/dashboard/me/vision/*` and `/dashboard/me/voice/*` behavior.

## Steps

1. Move dashboard prompt and voice request/response DTO ownership under `workmarket.dto`.
2. Introduce workmarket-facing dashboard assistant service interfaces and update the dashboard controller to depend only on workmarket types.
3. Re-home the current vision implementations behind those interfaces without changing behavior.
4. Update tests, contract generation inputs, and agent-operating docs/generated artifacts.
5. Run backend tests, frontend type-check/build, and control-system freshness validation.

## Completion Criteria

- No `com.themuffinman.app.vision.*` imports remain under `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket`.
- Dashboard prompt/voice endpoints still compile and behave identically.
- Required validation passes and generated/control artifacts are fresh.
