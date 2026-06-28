## Deferred Audit Scope

Topic: `QuestApplicationResponseDTO` assembly, lazy-loading safety, and duplicated viewer/action wiring.

### Why this exists

Two production-facing regressions followed the same pattern:

1. A backend read surface mapped `QuestApplication` through `QuestApplicationMgr.toDto(...)`.
2. The mapper touched lazy relations like `quest.creator` and `applicant`.
3. The read surface either ran outside a read transaction or duplicated role-specific DTO assembly instead of using one service-level path.

### Confirmed fixed paths

- `QuestService.getQuestDetailResponseById`
- `QuestService.getApplicationDetailResponseById`
- `DashboardService.getMyDashboard`

### Deferred audit checklist

- Review all non-mutating methods in `QuestApplicationService` and decide whether the service should default to `@Transactional(readOnly = true)`.
- Review owner-side application reads in `QuestApplicationService.getApplicationsForQuest`.
- Review owner-side application reads in `QuestApplicationService.getApplicationsViewForQuest`.
- Review public approved application reads in `QuestApplicationService.getPublicApprovedApplicationsViewForQuest`.
- Review admin application reads in `QuestApplicationService.getAllApplicationsForAdmin`.
- Review admin application reads in `QuestApplicationService.searchApplicationsForAdmin`.
- Decide whether owner, applicant, admin, and public DTO assembly should be exposed through named service helpers instead of repeated `withAllowedActions(questApplicationMgr.toDto(...), ...)` calls.
- Decide whether `QuestApplicationMgr.toDto(...)` should remain a rich mapper at all, or whether fields that require lazy relations should move behind fetch-safe assembler methods.
- Add one integration-style regression test per major read surface that maps real persisted applications outside an outer test transaction.

### Candidate end state

- Read services that assemble application DTOs are read-only transactional by default.
- Viewer-specific DTO assembly goes through explicit helper methods such as applicant, owner/admin, and public variants.
- Plain direct calls to `questApplicationMgr.toDto(application)` are limited to contexts where fetch guarantees are explicit and tested.
