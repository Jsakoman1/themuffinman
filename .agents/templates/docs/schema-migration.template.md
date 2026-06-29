# Schema Migration Documentation Template

## Migration Summary

- Migration file:
- Tables changed:
- Columns, indexes, constraints, or backfill behavior:

## Domain Impact

- Describe the entity, workflow, validation, or permission meaning of the schema change.
- State whether existing data needs defaults, cleanup, or compatibility handling.

## Runtime Compatibility

- Note nullable-to-required transitions, enum changes, cascade behavior, and rollout assumptions.

## Documentation Delta

- Update `docs/domain-technical.md` with changed entities, relations, constraints, and invariants.
- Update `docs/business-logic.md` if the migration changes user-facing behavior.
- Update generated inventories or agent artifacts when automation depends on the changed schema surface.

## Validation Evidence

- Record migration tests, backend tests, generated-artifact refreshes, and any frontend checks required by exposed contract changes.
