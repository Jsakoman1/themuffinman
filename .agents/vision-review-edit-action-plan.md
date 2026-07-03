---
machine_kind: plan
machine_status: unknown
machine_title: Vision Review Edit Action Plan
machine_goal: The review loop should no longer depend on prompt strings like `change
  reward`.
---

# Vision Review Edit Action Plan

## Scope

Replace prompt-based review-edit requests in `/vision` with a deterministic typed action plus target-slot contract.

## Goal

The review loop should no longer depend on prompt strings like `change reward`.

Instead:

- frontend sends a typed review-edit action
- backend resolves the exact editable slot deterministically
- conversation returns one next field request

## Locked Decisions

- `CONFIRM_REVIEW` remains a separate typed action.
- review-edit should use one explicit target slot only.
- fallback natural-language review-edit detection may remain temporarily for compatibility, but the primary contract becomes typed.
- the same target-slot mapping should be reusable across future executors.

## Planned Work

1. Extend the request DTO with a typed review target field.
2. Add a typed action for review edits.
3. Introduce a reusable backend mapping from review targets to editable slots.
4. Switch frontend review buttons to the typed action.
5. Update docs and generated contract surfaces.

## Validation

- targeted vision backend tests
- `./mvnw test -Dtest=AgentOperatingModelValidationTest`
- `npm run generate:contracts`
- `npm run type-check`
- `npm run build`
- generator refresh if machine-operational docs change
