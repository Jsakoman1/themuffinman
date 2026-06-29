# Social Backend Capsule

## Responsibility

Owns circles, circle membership, circle requests, social relationships, user profile surfaces, and selected-people visibility inputs used by other modules.

## Main Entry Points

- Controllers: `controller/`
- Services: `service/CircleService.java`, `service/UserProfileViewService.java`
- Repositories: `repository/`
- DTOs and mappers: `dto/`, `mapper/`

## Tests

- `src/test/java/com/themuffinman/app/social/`
- Cross-domain circle visibility behavior may also be covered by workmarket scenario tests.

## Living Docs

- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/agent-operating-model/sections/api.yaml`

## Forbidden Shortcuts

- Do not duplicate circle membership or request-state checks in unrelated domains.
- Do not infer selected-people visibility from frontend labels; resolve concrete circle/user ids.
- Do not change circle request states without updating docs and automation assumptions.
