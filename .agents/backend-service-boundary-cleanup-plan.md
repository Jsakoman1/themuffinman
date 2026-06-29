# Backend Service Boundary Cleanup Plan

Purpose: reduce oversized workmarket and sibling services by extracting read-only orchestration, admin-specific flows, and repeated policy/assembly logic into smaller collaborators.

## Current Focus

- `QuestService` read-side orchestration has been split into `QuestReadService`.
- Keep command/write methods in the thin façade and move read orchestration into dedicated collaborators.
- Prefer one boundary split per pass so tests stay readable and the refactor remains auditable.

## Completed Slices

- `QuestService` read-side orchestration moved into `QuestReadService`.
- `QuestApplicationService` read/public/application-view orchestration moved into `QuestApplicationReadService`.
- `QuestApplicationService` admin query/paging moved into `QuestApplicationAdminQueryService`.
- `QuestApplicationService` admin mutation handling moved into `QuestApplicationAdminService`.
- `CircleService` read-side discovery orchestration moved into `CircleDiscoveryService`.
- `CircleService` overview and connection read orchestration moved into `CircleReadService`.
- `LocationSettingsService` read/presentation helpers moved into `LocationSettingsViewService`.
- `LocationSettingsService` geo/query helpers moved into `LocationGeoService`.
- `QuestPresentationAssembler` quest-location presentation moved into `LocationQuestPresentationService`.

## Next Candidate Slices

- Workmarket application admin flow extraction.
- Social service boundary cleanup where query/policy/mapping responsibilities are mixed.
- Circle read-service slimming if the current read facade remains above the audit threshold.
- Location lookup boundary cleanup if the same pattern is repeated there.

## Validation

- `./mvnw test -Dtest=QuestServiceTest,QuestPresentationAssemblerTest,QuestApplicationPresentationAssemblerTest,DashboardServiceTest,QuestMgrTest,QuestApplicationServiceTest`
