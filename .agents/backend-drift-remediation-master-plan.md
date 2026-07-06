---
machine_kind: master-plan
machine_status: complete
machine_title: Backend Drift Remediation Master Plan
machine_goal: Reduce the open implementation backlog by tightening backend service boundaries on the current backend path, while excluding legacy frontend-only cleanup unless a backend contract still depends on it.
---

# Backend Drift Remediation Master Plan

## Status

Complete.

## Goal

Reduce the open implementation backlog by tightening backend service boundaries on the current backend path, while excluding legacy frontend-only cleanup unless a backend contract still depends on it.

## Scope Rules

- Focus on backend services, query surfaces, validation, and domain orchestration that the current backend routes still use.
- Do not spend time on legacy frontend-only refactors that are no longer part of the active Vision surface.
- Prefer moving read/query concerns into narrower services before touching mutation flows.

## Slice Order

1. Social read/query boundary cleanup for backend consumers still on the current backend path.
2. Quest, admin, and execution boundary cleanup around the remaining workflow hotspots.
3. Identity and location boundary cleanup, especially user-profile and location-lookup seams.
4. Validation, regression tests, and documentation/backlog closeout.

## Notes

- Keep controller cleanup only when it is still needed by the current backend flow.
- When a legacy-facing surface is not used by the current Vision path, treat it as deferred unless the backend contract still requires it.
- Close backlog items only after the current backend behavior is implemented and validated.

## Validation

- Targeted checks: `VisionCapabilityPreviewServiceAliasResolutionTest`, `VisionConversationServiceTest`, `VisionPromptUnderstandingServiceTest`, `VisionIntentRouterTest`.
- Broader checks: full backend Maven test suite for `apps/themuffinman`.
- Closeout checks: plan completion audit and generated local-tooling refresh.

## Completion Evidence

- Status: complete
- Validation evidence: `./mvnw -q -Dtest=DashboardReadQueryServiceTest,DashboardServiceTest test`, `./mvnw -q -Dtest=VisionPromptTextSupportTest,VisionIntentRouterTest,VisionCapabilityPreviewServiceAliasResolutionTest test`, `./mvnw -q test`, `make generate-agent-artifacts`, `make generate-frontend-contracts`, `make audit-generated-artifact-freshness`
- Doc delta summary: tightened the dashboard read boundary into a dedicated read query service, added a shared prompt-text helper, and preserved generated audit freshness after the backend boundary cleanup
- Deferred work: broader quest/admin/execution boundary tightening can continue as separate slices if needed, but no open child plan remains for this master plan
