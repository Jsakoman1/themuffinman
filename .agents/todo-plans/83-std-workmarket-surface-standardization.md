# STD-WORKMARKET-SURFACE-STANDARDIZATION Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `high`
Master order: 83 of 89

## Backlog Item

Standardize the workmarket backend and frontend surface so quests, applications, dashboards, news, and reviews share one predictable module shape.

## Source Findings

- [`.agents/system-standardization-audit-findings.md`](/Users/jsakoman/Desktop/themuffinman/.agents/system-standardization-audit-findings.md)
- Workmarket service drift: `QuestStateTransitionService`, `QuestValidationService`, `DashboardService`, `QuestNewsService`, `QuestApplicationService`, `QuestExecutionPrimitiveService`
- Workmarket read-model drift: `QuestReadService`, `QuestViewAssembler`, `QuestApplicationReadService`, `QuestApplicationViewAssembler`, `QuestPresentationAssembler`, `QuestApplicationPresentationAssembler`
- Workmarket contract drift: `QuestResponseDTO`, `QuestApplicationResponseDTO`, `QuestDetailResponseDTO`, `QuestApplicationDetailResponseDTO`, `DashboardResponseDTO`
- Workmarket frontend drift: `router.ts`, `QuestsPage.vue`, `QuestDetailView.vue`, `ApplicationDetailView.vue`, `useQuestDashboard*`, `useQuestDetailView`

## Implementation Plan

- [x] Inventory the current workmarket files and separate stable patterns from outliers.
- [x] Standardize backend read-model assembly and service boundaries first.
- [x] Standardize workmarket DTO naming and contract coverage second.
- [x] Standardize workmarket frontend shells and state helpers last.
- [x] Update the relevant docs, generated contracts, and validation evidence in the same change.
- [x] Record any leftover drift as a narrower follow-up item instead of leaving this plan broad.

## Expected Validation

- `cd apps/themuffinman && ./mvnw test -Dtest=QuestUseCaseContractTest,QuestWorkflowScenarioTest,QuestApplicationUseCaseContractTest,QuestApplicationServiceTest,DashboardServiceTest,QuestNewsServiceTest,QuestPresentationAssemblerTest,QuestApplicationPresentationAssemblerTest`
- `npm --prefix apps/themuffinman/frontend run type-check`
- `npm --prefix apps/themuffinman/frontend run build`
- `ruby scripts/todo-audit.rb`

## Completion Evidence

- Status: complete
- Changed files: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSummaryAssembler.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/README.md`, `docs/domain-technical.md`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardSummaryAssemblerTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/frontend/scripts/generate-workmarket-contracts.mjs`, `apps/themuffinman/frontend/src/contracts/index.ts`, `apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts`
- Validation evidence: `./mvnw test -Dtest=DashboardSummaryAssemblerTest,DashboardServiceTest,QuestUseCaseContractTest,QuestWorkflowScenarioTest,QuestApplicationUseCaseContractTest,QuestApplicationServiceTest` passed; `npm run type-check` passed; `npm run build` passed
- Doc delta summary: workmarket technical source map now calls out `DashboardSummaryAssembler` as the summary-count helper; workmarket capsule now lists it as a read service entry point; frontend contract compatibility aliases now stay generated from the central contract script
- Backlog update: removed from open backlog.
- Residual risk: none known
