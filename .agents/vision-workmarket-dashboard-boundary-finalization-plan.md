---
machine_kind: plan
machine_status: complete
machine_title: Vision Workmarket Dashboard Boundary Finalization Plan
machine_goal: Remove the remaining workmarket to vision dashboard prompt and voice compile-time dependency while preserving the existing dashboard prompt and voice behavior.
---

# Vision Workmarket Dashboard Boundary Finalization Plan

## Status

Complete.

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

## Completion Evidence

- Status: complete
- Changed files: `DashboardController`, workmarket dashboard prompt/voice DTOs and service interfaces, vision dashboard prompt/voice service implementations, tests, and control docs/artifacts
- Validation evidence: `./mvnw test`, `npm run type-check`, `npm run build`, `make control-start`, `make audit-generated-artifact-freshness`
- Residual risk: none recorded for the completed boundary cut
