---
machine_kind: plan
machine_status: complete
machine_title: Workmarket Cross Module DTO Separation Plan
machine_goal: Reduce remaining cross-module dependence on legacy vision read DTOs and move dashboard prompt and voice DTOs into a capability-specific package.
---

# Workmarket Cross Module DTO Separation Plan

## Status

Complete.

## Goal

Reduce remaining cross-module dependence on legacy vision read DTOs and move dashboard prompt and voice DTOs into a capability-specific package.

## Scope

- Included: identity-side read consumption of workmarket-owned DTOs, workmarket options/review service wiring where applicable, dashboard prompt and voice DTO package separation, and docs/control-surface sync.
- Excluded: deleting legacy vision DTOs that still back direct vision service contracts and broader removal of legacy vision write services.

## Slices

- [x] Rewire identity and other non-vision consumers that read workmarket-owned surfaces away from `vision.dto` types and onto `workmarket.dto`.
- [x] Move dashboard prompt and voice DTOs out of `vision/dto` into a narrower capability package and update all imports.
- [x] Sync docs and generated control artifacts to the refined ownership boundaries.
- [x] Validate and close the plan only after completion audit passes.

## Validation

- `./mvnw -q -Dtest=AdminUserDetailServiceTest,UserProfileViewServiceTest,DashboardVisionPromptServiceTest,DashboardVoiceServiceTest,VisionQuestFacadeServiceTest,VisionQuestApplicationFacadeServiceTest,AgentOperatingModelValidationTest test`
- `npm --prefix apps/themuffinman/frontend run generate:contracts`
- `make control-refresh-full`
- `ruby scripts/audits/audit-plan-completion.rb plan=.agents/workmarket-cross-module-dto-separation-plan.md`

## Completion Evidence

- Status: complete
- Identity-side cross-module reads now use workmarket-owned contracts directly:
  - `AppUserResponseDTO`, `AdminUserDetailDTO`, `UserProfileViewDTO`, `AppUserMgr`, `AppUserReadService`, `IdentityUserSummaryAssembler`, `AdminUserDetailService`, `UserProfileViewService`, and `AppUserController` now import `workmarket.dto` read contracts and use `WorkmarketQuestRepository`, `WorkmarketQuestMgr`, `WorkmarketOptionsService`, and `WorkmarketUserReviewService` where those surfaces are workmarket-owned.
- Dashboard prompt and voice DTOs were moved from `apps/themuffinman/src/main/java/com/themuffinman/app/vision/dto/` into `apps/themuffinman/src/main/java/com/themuffinman/app/vision/dashboard/dto/`, and all backend/test imports were updated to the narrower capability package.
- Control-surface docs were synced in `docs/agent-operating-model/sections/source_of_truth.yaml`, `docs/source-of-truth-inventory.md`, and `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/README.md`.
- Validation passed on `2026-07-07`:
  - `./mvnw -q -Dtest=AdminUserDetailServiceTest,UserProfileViewServiceTest,DashboardVisionPromptServiceTest,DashboardVoiceServiceTest,VisionQuestFacadeServiceTest,VisionQuestApplicationFacadeServiceTest,AgentOperatingModelValidationTest test`
  - `npm --prefix apps/themuffinman/frontend run generate:contracts`
  - `make generate-agent-operating-model generate-agent-artifacts`
  - `make control-refresh-full`
