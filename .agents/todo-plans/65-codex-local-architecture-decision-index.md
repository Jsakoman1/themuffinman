# CODEX-LOCAL-ARCHITECTURE-DECISION-INDEX Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `medium`
Master order: 65 of 82

## Backlog Item

Maintain a compact local index of important architectural decisions, current conventions, and non-obvious invariants that Codex should read before touching a domain.

Source notes:
  Proposed entrypoints:
  - `ruby scripts/audits/generate-architecture-decision-index.rb`
  - `make architecture-decision-index`
  Proposed outputs:
  - `docs/generated/local-tooling/architecture-decision-index.json`
  - `docs/generated/local-tooling/architecture-decision-index-summary.md`
  Notes:
  - Pull from living docs, agent operating docs, and stable source conventions.
  - This should reduce repeated explanation of why business logic belongs in backend services, why docs sync matters, and which generated artifacts are authoritative.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-documentation`
- [x] `make audit-local-tooling-incremental`

## Completion Evidence

- Status: complete
- Changed files:
  - `scripts/audits/generate-architecture-decision-index.rb`
  - `Makefile`
  - `scripts/audits/local_tooling_extended_tools.rb`
  - `docs/codex-local-tooling-todo.md`
  - `docs/domain-technical.md`
  - `docs/generated/local-tooling/architecture-decision-index.json`
  - `docs/generated/local-tooling/architecture-decision-index-summary.md`
- Validation evidence:
  - `ruby -c scripts/audits/generate-architecture-decision-index.rb` passed.
  - `make architecture-decision-index` passed.
  - `make audit-plan-completion plan=.agents/todo-plans/65-codex-local-architecture-decision-index.md` passed after closeout.
  - `ruby scripts/todo-audit.rb` passed.
  - `make audit-documentation` passed.
  - `make audit-local-tooling-incremental` passed.
- Backlog update: removed `CODEX-LOCAL-ARCHITECTURE-DECISION-INDEX` from open local-tooling TODO items and added it to available local audits.
- Residual risk: index entries are curated stable decisions; update the generator when repository-wide conventions change.
