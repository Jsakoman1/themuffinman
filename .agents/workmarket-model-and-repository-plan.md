---
machine_kind: plan
machine_status: complete
machine_title: Workmarket Model And Repository Plan
machine_goal: Move the core quest, application, review, and news persistence boundary onto workmarket-owned model and repository types while keeping vision as an adapter layer.
---

# Workmarket Model And Repository Plan

## Status

Complete.

## Goal

Move the core quest, application, review, and news persistence boundary onto workmarket-owned model and repository types while keeping vision as an adapter layer.

## Scope

- Included: workmarket service typing, repository contract ownership, fetch-profile coverage, and source-of-truth alignment for quest/application persistence.
- Excluded: DTO package moves, frontend contract rewrites, and full removal of the legacy vision persistence types.

## Slices

- [x] Audit workmarket services that still type against `vision.model` or legacy repositories.
- [x] Switch workmarket read/write services and helpers to workmarket-owned model and enum types at their internal boundary.
- [x] Update repository fetch-profile contract coverage and source-of-truth docs so workmarket repositories are the canonical ownership surface.
- [x] Run targeted backend validation and close the slice only if the evidence matches the implemented state.

## Validation

- `./mvnw -q -Dtest=RepositoryFetchProfileContractTest,ServiceLayeringConventionTest,VisionQuestFacadeServiceTest,VisionQuestApplicationFacadeServiceTest test`
- `./mvnw -q -Dtest=AgentOperatingModelValidationTest test`
- `make generate-agent-operating-model`
- `make generate-agent-artifacts`

## Notes

- Keep `vision` facade methods stable for now; convert there only when the adapter needs to translate between workmarket-owned and legacy types.
- Prefer narrowing ownership by package and tests first instead of forcing a one-pass delete of the legacy classes.

## Completion Evidence

- Status: complete
- Validation evidence:
  - `./mvnw -q -Dtest=RepositoryFetchProfileContractTest,ServiceLayeringConventionTest,VisionQuestFacadeServiceTest,VisionQuestApplicationFacadeServiceTest test`
  - `make generate-agent-operating-model`
  - `./mvnw -q -Dtest=AgentOperatingModelValidationTest test`
  - `make generate-agent-artifacts`
  - `make control-refresh-full`
- Doc delta summary: workmarket README now states that repository fetch-profile coverage is owned by workmarket repositories and that remaining `vision` enum usage is an explicit DTO adapter layer.
- Remaining adapter boundary: `vision` DTO contracts and a few DTO-facing assembler conversions still translate enum values by design; persistence and internal workmarket service ownership moved onto workmarket types for this slice.
