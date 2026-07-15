# Business Backend Capsule

## Responsibility

Owns the business module backend: business profile identity, public mini-site reads, service catalog, booking policy defaults, owner-defined availability, booking workflow, owner schedule/dashboard reads, gallery metadata, and booking audit history.

## Main Entry Points

- Controllers:
  - `controller/BusinessProfileController.java`
  - `controller/BusinessOfferingController.java`
  - `controller/BusinessBookingPolicyController.java`
  - `controller/BusinessAvailabilityController.java`
  - `controller/BusinessPublicController.java`
  - `controller/BusinessBookingController.java`
  - `controller/BusinessOwnerDashboardController.java`
  - `controller/BusinessGalleryController.java`
- Mutation workflow services:
  - `service/BusinessProfileService.java`
  - `service/BusinessOfferingService.java`
  - `service/BusinessBookingPolicyService.java`
  - `service/BusinessAvailabilityService.java`
  - `service/BusinessCreateBookingUseCase.java`
  - `service/BusinessConfirmBookingUseCase.java`
  - `service/BusinessRejectBookingUseCase.java`
  - `service/BusinessCancelBookingUseCase.java`
  - `service/BusinessCompleteBookingUseCase.java`
  - `service/BusinessNoShowBookingUseCase.java`
- Read-model services:
  - `service/BusinessPublicReadService.java`
  - `service/BusinessAvailabilityReadService.java`
  - `service/BusinessBookingReadSupport.java`
  - `service/BusinessBookingReadService.java`
  - `service/BusinessOwnerScheduleReadService.java`
  - `service/BusinessOwnerCalendarReadService.java`
  - `service/BusinessOwnerDashboardReadService.java`
- Supporting boundaries:
  - `service/BusinessBookingValidationService.java`
  - `service/BusinessBookingPrimitiveService.java`
  - `service/BusinessBookingPresentationService.java`
  - `service/BusinessGalleryService.java`
  - `service/BusinessBookingAuditService.java`
  - `event/BusinessBookingEventHandler.java`

## Tests

- `src/test/java/com/themuffinman/app/business/service/BusinessProfileServiceTest.java`
- `src/test/java/com/themuffinman/app/business/service/BusinessOfferingServiceTest.java`
- `src/test/java/com/themuffinman/app/business/service/BusinessBookingPolicyServiceTest.java`
- `src/test/java/com/themuffinman/app/business/service/BusinessAvailabilityComputationServiceTest.java`
- `src/test/java/com/themuffinman/app/business/service/BusinessPublicReadServiceTest.java`
- `src/test/java/com/themuffinman/app/business/service/BusinessCreateBookingUseCaseTest.java`
- `src/test/java/com/themuffinman/app/business/service/BusinessBookingValidationServiceTest.java`
- `src/test/java/com/themuffinman/app/business/service/BusinessCancelBookingUseCaseTest.java`

## Living Docs

- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/domain-technical.md`

## Domain Notes

- `business_profile` stays the public business identity root.
- `business_offering` is the bookable service root.
- Owner availability is entered in local business time; bookings persist absolute `starts_at` and `ends_at`.
- Booking DTOs are backend-prepared and return status meaning plus allowed actions for web, iPhone, and future `/vision` clients.
- Booking presentation and read paths resolve policy defaults without creating new policy rows; only mutation flows persist missing policy state.
- `PENDING_CONFIRMATION` and `CONFIRMED` both consume capacity.
- Owner booking list and owner dashboard summary share the same owner schedule read-model layer.
- Owner calendar projection groups bookings by the business's local day so calendar consumers can reuse a backend-prepared schedule shape.
- Offering deactivation is archival-safe; booking rows keep offering title, price, duration, and timezone snapshots.

## Forbidden Shortcuts

- Do not let clients derive booking permissions, status labels, or cancellation rules on their own.
- Do not put availability, capacity, or workflow policy logic in controllers or frontend-only helpers.
- Do not hard-delete offering history in a way that makes old bookings unreadable.
- Do not bypass typed `app.business.*` config by scattering operational defaults through ad-hoc `@Value` fields.
- Do not edit old Flyway migrations for schema changes.
