# New DTO Contract Documentation Template

## DTO Contract

- DTO name:
- Producer:
- Consumers:
- Source entity or read model:

## Field Semantics

- List each field, nullability, allowed values, and display meaning.
- Note fields prepared by the backend so clients do not derive product logic.

## Permissions And Visibility

- Document whether fields are viewer-dependent.
- Document fields hidden, redacted, or synthesized for admin, owner, participant, or public viewers.

## Compatibility Notes

- Note additive fields, renamed fields, removed fields, and migration needs.
- Document frontend contract generation impact.

## Documentation Delta

- Update `docs/domain-technical.md` with DTO semantics and invariants.
- Update `docs/business-logic.md` if field meaning changes user behavior.
- Update `docs/agent-operating-model.md` or generated contract artifacts when automation consumes the DTO.

## Validation Evidence

- Record mapper/service tests, contract-generation checks, frontend type-check, and build status.
