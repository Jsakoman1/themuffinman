# Backend Drift Remediation Findings

Purpose: freeze the remaining backend standardisation drift into a domain-grouped execution backlog.

## Current Control Signals

- `docs/generated/local-tooling/architecture-drift-summary.md`
- `docs/generated/local-tooling/doc-sync-preflight-summary.md`
- `docs/generated/local-tooling/test-gap-recommendations-summary.md`
- `docs/generated/source-of-truth-audit.json`
- `docs/generated/backend-audit-inventory.json`
- `docs/generated/automation-read-model-inventory.json`

## Workmarket Hotspots

- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestStateTransitionService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestNewsService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestValidationService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestExecutionPrimitiveService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationAdminService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestApplicationController.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestController.java`

Why it matters:
- The current audit still flags mixed responsibilities and oversized read/mutation surfaces.
- Workmarket remains the highest-leverage domain for boundary cleanup because it carries the densest read-model and contract surface.

## Social Hotspots

- `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleReadService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleDiscoveryService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleRelationService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleMembershipService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleViewAssembler.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/social/controller/CircleController.java`

Why it matters:
- Social still has the largest controller/service fanout after workmarket.
- The controller and read services still mix query, policy, and assembly responsibilities.

## Identity And Location Follow-Ups

- `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AppUserService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/UserProfileViewService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationLookupService.java`

Why it matters:
- These surfaces are not the primary next execution slice, but they remain visible in the current architecture-drift report.
- They should be queued after workmarket and social are reduced so the same boundary pattern can be reused.

## Docs And Validation Control Surface

- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml`
- `docs/domain-technical.md`
- `docs/business-logic.md`
- `docs/documentation-sync-policy.md`
- `docs/change-completion-checklist.md`

Why it matters:
- The source-of-truth and generated audit mechanism is currently healthy, but the docs are still the control plane for drift detection.
- Future implementation slices must update living docs and generated artifacts together.

## Execution Order

1. Freeze the remaining drift into grouped backlog items.
2. Reduce workmarket service and controller hotspots.
3. Reduce social service and controller hotspots.
4. Queue identity and location cleanup after the first two domains settle.
5. Re-run validation, audit, and generated artifact refreshes after each slice.
