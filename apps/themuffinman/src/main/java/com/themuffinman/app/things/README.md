# Things Backend Capsule

## Responsibility

Owns thing sharing listings, borrower requests, listing visibility, and owner/borrower request workflow rules.

## Main Entry Points

- Controller: `controller/ThingSharingController.java`
- Service: `service/ThingSharingService.java`
- Repositories: `repository/ThingListingRepository.java`, `repository/ThingBorrowRequestRepository.java`
- Mapper and DTOs: `mapper/`, `dto/`

## Tests

- `src/test/java/com/themuffinman/app/things/service/ThingSharingServiceTest.java`

## Living Docs

- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/agent-operating-model/sections/api.yaml`

## Forbidden Shortcuts

- Do not allow owners to borrow their own listings.
- Do not duplicate pending-request uniqueness rules in frontend code.
- Do not mix synthetic sandbox data with production request behavior.
