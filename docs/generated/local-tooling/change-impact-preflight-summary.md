# Change Impact Preflight

- Generated at: `2026-06-28T20:30:13Z`
- Changed files: `107`
- Unique docs to review: `7`
- Unique tests to consider: `19`
- Generated artifacts to check: `4`

## `.agents/feature-manifests/agent-control-phase-two-manifest.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/feature-manifests/agent-operating-refactor-manifest.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/feature-manifests/agent-safety-enforcement-round2-manifest.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/feature-manifests/agent-safety-upgrade-manifest.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/feature-manifests/backend-audit-domain-tagging-manifest.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/feature-manifests/backend-audit-identity-dto-tightening-manifest.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/feature-manifests/backend-audit-location-dto-tightening-manifest.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/feature-manifests/backend-audit-manifest-cleanup-manifest.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/feature-manifests/backend-audit-tiering-manifest.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/feature-manifests/backend-audit-tightening-manifest.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/feature-manifests/executor-readiness-program-manifest.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/templates/feature-completion-manifest.template.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `AGENTS.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `Makefile`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/frontend/src/modules/workmarket/composables/quest-detail/createQuestDetailViewState.ts`

- Category: `frontend_composable`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: `npm`, `run`, `type-check`, `npm`, `run`, `build`

## `apps/themuffinman/frontend/src/modules/workmarket/composables/quest-detail/useQuestDetailMutationActions.ts`

- Category: `frontend_composable`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: `npm`, `run`, `type-check`, `npm`, `run`, `build`

## `apps/themuffinman/frontend/src/modules/workmarket/composables/quest-detail/useQuestDetailUiActions.ts`

- Category: `frontend_composable`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: `npm`, `run`, `type-check`, `npm`, `run`, `build`

## `apps/themuffinman/frontend/src/modules/workmarket/views/QuestDetailView.vue`

- Category: `frontend_view`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: `npm`, `run`, `type-check`, `npm`, `run`, `build`

## `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AdminUserDetailService.java`

- Category: `backend_service`
- Domain: `identity`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/identity/service/AdminUserDetailServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/identity/service/AppUserServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/identity/service/AuthServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/identity/service/UserProfileViewServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AppUserLookupService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AppUserService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AuthService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/UserProfileViewService.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AppUserService.java`

- Category: `backend_service`
- Domain: `identity`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/identity/service/AdminUserDetailServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/identity/service/AppUserServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/identity/service/AuthServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/identity/service/UserProfileViewServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AdminUserDetailService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AppUserLookupService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AuthService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/UserProfileViewService.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/UserProfileViewService.java`

- Category: `backend_service`
- Domain: `identity`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/identity/service/AdminUserDetailServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/identity/service/AppUserServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/identity/service/AuthServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/identity/service/UserProfileViewServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AdminUserDetailService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AppUserLookupService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AppUserService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AuthService.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/AdminDatabaseMetricsService.java`

- Category: `backend_service`
- Domain: `location`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/location-services.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/location/service/LocationLookupServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/GeoapifyLocationLookupClient.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationLookupService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationSettingsService.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationLookupService.java`

- Category: `backend_service`
- Domain: `location`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/location-services.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/location/service/LocationLookupServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/AdminDatabaseMetricsService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/GeoapifyLocationLookupClient.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationSettingsService.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleDiscoveryService.java`

- Category: `backend_service`
- Domain: `social`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleDiscoveryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleMembershipServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleRelationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleMembershipService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleRelationService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleViewAssembler.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/SocialRelationActionHelper.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleMembershipService.java`

- Category: `backend_service`
- Domain: `social`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleDiscoveryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleMembershipServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleRelationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleDiscoveryService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleRelationService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleViewAssembler.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/SocialRelationActionHelper.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleRelationService.java`

- Category: `backend_service`
- Domain: `social`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleDiscoveryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleMembershipServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleRelationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleDiscoveryService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleMembershipService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleViewAssembler.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/SocialRelationActionHelper.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleService.java`

- Category: `backend_service`
- Domain: `social`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleDiscoveryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleMembershipServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleRelationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleDiscoveryService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleMembershipService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleRelationService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleViewAssembler.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/SocialRelationActionHelper.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CompleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ConfirmQuestTermChangeUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestExecutionPrimitiveService.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CompleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ConfirmQuestTermChangeUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestExecutionPrimitiveService.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestExecutionPrimitiveService.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CompleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ConfirmQuestTermChangeUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestNewsService.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CompleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ConfirmQuestTermChangeUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestService.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CompleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ConfirmQuestTermChangeUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestViewAssembler.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CompleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ConfirmQuestTermChangeUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsService.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CompleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ConfirmQuestTermChangeUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`

- Category: `other`
- Domain: `docs`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: _none_
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: _none_
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: _none_
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/agent-improvement-backlog.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/agent-operating-model.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/agent-operating-model.yaml`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/agent-operating-model/sections/documentation_sync.yaml`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/agent-operating-model/sections/source_of_truth.yaml`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/business-logic.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/change-completion-checklist.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/documentation-sync-policy.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/domain-technical.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/feature-completion-manifest.schema.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/backend-audit-inventory.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/source-of-truth-audit.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `scripts/feature-closeout-audit.sh`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/generate-source-of-truth-audit.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/agent-model-feature-coverage-audit-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/aggregate-dead-code-audit-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/api-contract-drift-audit-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/async-mutation-flow-audit-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/audit-documentation-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/automation-readiness-gap-audit-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/backend-dead-code-audit-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/change-impact-preflight-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/config-sprawl-audit-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/doc-coverage-gap-audit-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/docs-to-code-drift-audit-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/domain-ownership-inventory-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/dormant-code-audit-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/duplicate-logic-audit-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/endpoint-callsite-linker-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/error-pattern-audit-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/feature-intro-check-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/feature-manifests/persistent-backlog-system-manifest.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/file-relation-graph-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/frontend-dead-code-audit-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/frontend-route-surface-inventory-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/frontend-stale-surface-audit-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/frontend-state-logic-duplication-audit-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/generated-artifact-freshness-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/local-tooling-audits-master-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/local-tooling-followup-cleanup-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/local-tooling-remaining-23-master-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/make-target-index-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/manual-cleanup-candidate-report-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/mapper-usage-audit-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/naming-consistency-audit-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/permission-rule-duplication-audit-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/persistent-backlog-system-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/quest-application-dto-audit-todo.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/quest-application-lazy-proxy-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/quest-application-withdraw-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/read-surface-inventory-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/read-transaction-final-gap-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/read-transaction-second-pass-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/read-transaction-third-pass-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/repository-fetch-audit-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/rich-text-safety-audit-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/sandbox-generation-coverage-audit-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/state-transition-coverage-audit-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/style-token-usage-audit-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/test-surface-inventory-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/test/java/com/themuffinman/app/config/`

- Category: `other`
- Domain: `config`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `docs/codex-local-tooling-todo.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/dead-code-audit/`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/implementation-backlog.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/tooling/`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `scripts/audits/`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/local_tooling_common.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/todo-audit.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_
