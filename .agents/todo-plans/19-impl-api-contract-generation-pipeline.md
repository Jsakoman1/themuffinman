# IMPL-API-CONTRACT-GENERATION-PIPELINE Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `high`
Master order: 19 of 82

## Backlog Item

Move API contract generation into a first-class build/documentation pipeline with explicit ownership and freshness checks.

Source notes:
  Goal: prevent backend/frontend contract drift and make generated contracts trustworthy for Codex.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-documentation`
- [x] `make audit-agent-safety`
- [x] `cd apps/themuffinman && ./mvnw test`
- [x] `cd apps/themuffinman/frontend && npm run type-check`
- [x] `cd apps/themuffinman/frontend && npm run build`

## Completion Evidence

- Status: complete
- Changed files: added `--check` freshness mode to `frontend/scripts/generate-workmarket-contracts.mjs`; added `generate:contracts` and `validate:contracts` package/root scripts; wired `npm run build` and Make targets through the contract freshness check; recorded `frontend_contract_generation.check_command` in agent-operating docs/schema/test; updated frontend docs, domain docs, documentation policy, manifest expectations, and generated inventories.
- Validation evidence: `make validate-frontend-contracts` passed; `make audit-generated-artifact-freshness` passed with stale `0`; `make audit-agent-safety` passed; `make audit-documentation` passed; `ruby scripts/todo-audit.rb` passed; `cd apps/themuffinman && ./mvnw test` passed with 265 tests; `cd apps/themuffinman/frontend && npm run type-check` passed; `cd apps/themuffinman/frontend && npm run build` passed and includes the contract check.
- Backlog update: removed `IMPL-API-CONTRACT-GENERATION-PIPELINE` from `docs/implementation-backlog.md`.
- Residual risk: broader API drift analysis remains report-first through existing local tooling; this plan closes the freshness/build gate for the generated frontend contract itself.
