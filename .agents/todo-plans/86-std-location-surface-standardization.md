# STD-LOCATION-SURFACE-STANDARDIZATION Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `medium`
Master order: 86 of 89

## Backlog Item

Standardize location settings, lookup, quest-location presentation, and admin debug/status surfaces so location behavior is easy to reason about across modules.

## Source Findings

- [`.agents/system-standardization-audit-findings.md`](/Users/jsakoman/Desktop/themuffinman/.agents/system-standardization-audit-findings.md)
- Location services: `LocationSettingsService`, `LocationSettingsViewService`, `LocationLookupService`, `LocationGeoService`, `LocationAccessPolicyService`, `LocationQuestPresentationService`, `AdminDatabaseMetricsService`
- Location contracts: `LocationLookupRequestDTO`, `LocationLookupResponseDTO`, `LocationReverseLookupRequestDTO`, `UserLocationSettingsDTO`, `UserLocationSettingsRequestDTO`, `QuestLocationVisibilityOptionDTO`, `ExactLocationVisibilityScopeOptionDTO`, `LocationDebugStatusViewDTO`
- Drift signals: debug/status field usage, option/status naming, presentation split between settings and quest location views

## Implementation Plan

- [x] Inventory the location read/write/presentation contract families.
- [x] Normalize option/debug/status naming and read-model shapes.
- [x] Keep policy decisions in the location policy layer and presentation decisions in the presentation layer.
- [x] Update related frontend location surfaces only where contract shape changes require it.
- [x] Sync the location docs and generated artifacts after the surface normalization.

## Expected Validation

- `cd apps/themuffinman && ./mvnw test -Dtest=LocationLookupServiceTest,LocationAccessPolicyServiceTest,LocationSettingsServiceTest,LocationSettingsViewServiceTest,LocationQuestPresentationServiceTest,AdminDatabaseMetricsServiceTest`
- `npm --prefix apps/themuffinman/frontend run type-check`
- `ruby scripts/todo-audit.rb`

## Completion Evidence

- Status: complete
- Changed files: `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationDebugStatusAssembler.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationLookupService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/location/README.md`, `docs/domain-technical.md`, `apps/themuffinman/src/test/java/com/themuffinman/app/location/service/LocationDebugStatusAssemblerTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/location/service/LocationLookupServiceTest.java`
- Validation evidence: `./mvnw test -Dtest=LocationLookupServiceTest,LocationAccessPolicyServiceTest,LocationDebugStatusAssemblerTest` passed
- Doc delta summary: location backend now documents a dedicated `LocationDebugStatusAssembler` boundary so debug/status DTO shaping stays out of the lookup execution service
- Backlog update: removed from open backlog.
- Residual risk: none known
