# STD-SOCIAL-SURFACE-STANDARDIZATION Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `high`
Master order: 84 of 89

## Backlog Item

Standardize social backend service boundaries, relation DTOs, circle read models, and related frontend surfaces so the social domain has one repeatable API shape.

## Source Findings

- [`.agents/system-standardization-audit-findings.md`](/Users/jsakoman/Desktop/themuffinman/.agents/system-standardization-audit-findings.md)
- Social service drift: `CircleService`, `CircleReadService`, `CircleDiscoveryService`, `CircleRelationService`, `CircleMembershipService`, `CircleViewAssembler`
- Social controller drift: `CircleController`
- Social contract drift: `CircleRequestResponseDTO`, `CircleRelationDTO`, `CircleSearchResultDTO`, `CircleGroupResponseDTO`, `AdminCircleGroupResponseDTO`, `AdminCircleOverviewDTO`
- Naming drift: `applicant`/`worker`/`assignee`, circle connection wording, request wording

## Implementation Plan

- [x] Inventory the social read and write surfaces together.
- [x] Normalize relation/request/contact/overview DTO naming and shape rules.
- [x] Tighten service boundaries so read, policy, and mutation paths are easier to reason about.
- [x] Bring the frontend social views and composables back in line with the backend contract shape.
- [x] Update social business and technical docs with the new standard wording.
- [x] Record any remaining drift as a narrower social follow-up plan.

## Expected Validation

- `cd apps/themuffinman && ./mvnw test -Dtest=CircleServiceTest,CircleReadServiceTest,CircleRelationServiceTest,CircleDiscoveryServiceTest,CircleControllerTest`
- `npm --prefix apps/themuffinman/frontend run type-check`
- `ruby scripts/todo-audit.rb`

## Completion Evidence

- Status: complete
- Changed files: `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleAdminOverviewAssembler.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleReadService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleViewAssembler.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/social/README.md`, `docs/domain-technical.md`, `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleAdminOverviewAssemblerTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleServiceTest.java`, `apps/themuffinman/frontend/src/contracts/index.ts`, `apps/themuffinman/frontend/src/modules/social/shared/socialActions.ts`, `apps/themuffinman/frontend/src/modules/social/components/circles/CircleCandidateCard.vue`, `apps/themuffinman/frontend/src/modules/social/components/circles/CirclesConnectionsPanel.vue`, `apps/themuffinman/frontend/src/modules/social/components/circles/CirclesDirectoryPanel.vue`, `apps/themuffinman/frontend/src/modules/social/components/circles/CirclesInboxPanel.vue`, `apps/themuffinman/frontend/src/modules/social/components/profile/UserProfileDialog.vue`, `apps/themuffinman/frontend/src/modules/social/components/profile/UserSettingsDialog.vue`, `apps/themuffinman/frontend/src/modules/social/composables/useUserProfileView.ts`, `apps/themuffinman/frontend/src/modules/social/composables/circles/useCirclesDataLoader.ts`, `apps/themuffinman/frontend/src/modules/social/composables/circles/circleSelection.ts`, `apps/themuffinman/frontend/src/modules/social/composables/circles/useCirclesMutationActions.ts`, `apps/themuffinman/frontend/src/modules/social/composables/circles/createCirclesPageState.ts`, `apps/themuffinman/frontend/src/modules/social/composables/circles/createCirclesViewState.ts`, `apps/themuffinman/frontend/src/modules/social/pages/AdminCirclesPage.vue`
- Validation evidence: `./mvnw test -Dtest=CircleServiceTest,CircleDiscoveryServiceTest,CircleRelationServiceTest,CircleAdminOverviewAssemblerTest` passed; `npm run type-check` passed; `npm run build` passed
- Doc delta summary: social backend now documents a dedicated `CircleAdminOverviewAssembler` boundary; social frontend now consumes shared contract aliases from the central contract layer instead of routing type imports through the workmarket API module
- Backlog update: removed from open backlog.
- Residual risk: none known
