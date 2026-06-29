# Business Backend Capsule

## Responsibility

Owns business mini-site profile data: profile identity, public directory visibility, slug uniqueness, and owner-managed profile editing.

## Main Entry Points

- Controller: `controller/BusinessProfileController.java`
- Service: `service/BusinessProfileService.java`
- Repository: `repository/BusinessProfileRepository.java`
- Mapper and DTOs: `mapper/`, `dto/`

## Tests

- `src/test/java/com/themuffinman/app/business/service/BusinessProfileServiceTest.java`

## Living Docs

- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/agent-operating-model/sections/api.yaml`

## Forbidden Shortcuts

- Do not let users edit another owner profile outside admin-specific rules.
- Do not build frontend-only slug or visibility rules.
- Do not edit old Flyway migrations for schema changes.
