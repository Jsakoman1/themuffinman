# Location Backend Capsule

## Responsibility

Owns user and quest location settings, visibility decisions, location DTOs, and provider-backed location behavior.

## Main Entry Points

- Controllers: `controller/`
- Services: `service/LocationSettingsService.java`, `service/LocationAccessPolicyService.java`, `service/LocationDebugStatusAssembler.java`, provider services
- Repositories and models: `repository/`, `model/`

## Tests

- `src/test/java/com/themuffinman/app/location/`
- Add cross-domain tests when location changes affect quest search or visibility.

## Living Docs

- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/agent-operating-model/sections/api.yaml`

## Forbidden Shortcuts

- Do not expose provider credentials or raw provider internals to frontend config.
- Do not hard-code operational settings outside typed config classes.
- Do not duplicate quest location application rules outside `LocationSettingsService`.
- Do not duplicate exact-location permission decisions outside `LocationAccessPolicyService`.
