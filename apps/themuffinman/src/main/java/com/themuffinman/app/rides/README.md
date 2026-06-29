# Rides Backend Capsule

## Responsibility

Owns voluntary ride sharing offers, seat counts, departure windows, notes, and optional circle-scoped visibility.

## Main Entry Points

- Controller: `controller/RideOfferController.java`
- Service: `service/RideOfferService.java`
- Repository: `repository/RideOfferRepository.java`
- Mapper and DTOs: `mapper/`, `dto/`

## Tests

- `src/test/java/com/themuffinman/app/rides/service/RideOfferServiceTest.java`

## Living Docs

- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/agent-operating-model/sections/api.yaml`

## Forbidden Shortcuts

- Do not treat rides as commercial bookings.
- Do not bypass owner checks for offer updates and cancellation.
- Do not implement visibility filtering only in the frontend.
