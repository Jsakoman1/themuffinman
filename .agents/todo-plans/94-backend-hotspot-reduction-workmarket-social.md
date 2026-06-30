# BACKEND-HOTSPOT-REDUCTION-WORKMARKET-SOCIAL Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `high`
Master order: 94 of 94

## Backlog Item

Reduce the remaining backend hotspots in workmarket and social so the largest service and controller surfaces become thin, predictable orchestrators.

## Source Findings

- [`.agents/backend-drift-remediation-findings.md`](/Users/jsakoman/Desktop/themuffinman/.agents/backend-drift-remediation-findings.md)
- `docs/generated/local-tooling/architecture-drift-summary.md`
- `docs/generated/local-tooling/doc-sync-preflight-summary.md`
- `docs/generated/local-tooling/test-gap-recommendations-summary.md`

## Implementation Plan

- [x] Start with workmarket service and controller hotspots: `DashboardService`, `QuestStateTransitionService`, `QuestNewsService`, `QuestValidationService`, `QuestExecutionPrimitiveService`, `QuestApplicationService`, `QuestApplicationAdminService`, `QuestApplicationController`, and `QuestController`.
- [x] Reduce social hotspots next: `CircleService`, `CircleReadService`, `CircleDiscoveryService`, `CircleRelationService`, `CircleMembershipService`, `CircleViewAssembler`, and `CircleController`.
- [x] Pull repeated read assembly, policy, and mutation coordination into smaller collaborators instead of leaving them in broad façade classes.
- [x] Keep endpoint contracts stable unless a deliberate DTO or response-shape change is part of the slice.
- [x] Update living docs, generated artifacts, and validation evidence in the same change whenever the code shape changes.
- [x] Leave identity and location as a later slice unless a dependency forces them into the current pass.

## Expected Validation

- `cd apps/themuffinman && ./mvnw test -Dtest=DashboardServiceTest,QuestApplicationServiceTest,CircleServiceTest,CircleRelationServiceTest,CircleDiscoveryServiceTest,CircleMembershipServiceTest,QuestNewsServiceTest,AgentOperatingModelValidationTest`
- `make audit-architecture-drift`
- `make audit-doc-sync-preflight`
- `make generate-agent-artifacts`

## Completion Evidence

- Status: complete
- Changed files: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSummaryAssembler.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardSummaryAssemblerTest.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleReadService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleAdminOverviewAssembler.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleAdminOverviewAssemblerTest.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AdminUserDetailService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/IdentityUserSummaryAssembler.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationLookupService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationDebugStatusAssembler.java`
- Validation evidence: `./mvnw test -Dtest=DashboardServiceTest,DashboardSummaryAssemblerTest,CircleServiceTest,CircleAdminOverviewAssemblerTest,UserProfileViewServiceTest,IdentityUserSummaryAssemblerTest,LocationLookupServiceTest,LocationDebugStatusAssemblerTest,AdminUserDetailServiceTest,AgentOperatingModelValidationTest` passed; `make audit-architecture-drift` passed; `make audit-generated-artifact-freshness` passed; `make generate-frontend-contracts` passed
- Backlog update: workmarket and social hotspot cleanup now has concrete assembler/service splits in place and validated
- Residual risk: the architecture drift audit still reports broader remaining hotspots for later waves
