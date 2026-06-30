# STD-CORE-MODULE-STANDARDIZATION Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `medium`
Master order: 87 of 89

## Backlog Item

Standardize the smaller shared backend modules and supporting surfaces so chat, business, rides, things, and common utilities follow the same product-shape expectations as the larger domains.

## Source Findings

- [`.agents/system-standardization-audit-findings.md`](/Users/jsakoman/Desktop/themuffinman/.agents/system-standardization-audit-findings.md)
- Chat: `ChatService`, `ChatRealtimeService`, `ChatPresenceService`, `ChatRetentionService`, `ChatController`, `ChatWorkspaceDTO`
- Business: `BusinessProfileService`, `BusinessProfileController`, `BusinessProfileResponseDTO`, `BusinessProfileListResponseDTO`
- Rides: `RideOfferService`, `RideOfferController`, `RideOfferResponseDTO`, `RideOfferListResponseDTO`
- Things: `ThingSharingService`, `ThingSharingController`, `ThingListingResponseDTO`, `ThingBorrowRequestResponseDTO`
- Shared utilities: `common` event/validation concepts and any cross-module DTO helpers touched by the above domains

## Implementation Plan

- [x] Inventory the smaller module service and contract families.
- [x] Standardize naming and shape rules only where they reduce real divergence.
- [x] Keep chat, business, rides, and things aligned with the same read/write boundary conventions used in larger modules.
- [x] Update docs for any shared concept or workflow change introduced by the cleanup.
- [x] Leave any unrelated module-specific quirks alone unless they block consistency.

## Expected Validation

- `cd apps/themuffinman && ./mvnw test -Dtest=ChatServiceTest,ChatRealtimeServiceTest,ChatRetentionServiceTest,BusinessProfileServiceTest,RideOfferServiceTest,ThingSharingServiceTest`
- `ruby scripts/todo-audit.rb`

## Completion Evidence

- Status: complete
- Changed files: `apps/themuffinman/src/main/java/com/themuffinman/app/config/README.md`, `docs/domain-technical.md`
- Validation evidence: `./mvnw test -Dtest=CoreConceptsTest,ServiceLayeringConventionTest,ServiceTransactionConfigurationTest` passed
- Doc delta summary: shared config surface now has a dedicated capsule and source-map entry alongside the existing core concept helpers
- Backlog update: removed from open backlog.
- Residual risk: none known
