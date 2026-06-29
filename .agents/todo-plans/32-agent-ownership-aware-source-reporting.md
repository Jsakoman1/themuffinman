# AGENT-OWNERSHIP-AWARE-SOURCE-REPORTING Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 32 of 82

## Backlog Item

Add ownership-aware reporting to source-of-truth audit output, not only backend inventory output.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-agent-safety`
- [x] `make audit-local-tooling-incremental`
- [x] `cd apps/themuffinman && ./mvnw test`

## Completion Evidence

- Status: complete
- Changed files: source-of-truth audit generator now emits ownership-aware candidate entries, domain counts, owner counts, and ownership-aware gap details; validation now asserts those fields; docs and backlog current state describe the routing behavior.
- Validation evidence: `./mvnw test -Dtest=AgentOperatingModelValidationTest` passed; `make audit-agent-safety` passed; `make audit-local-tooling-incremental` passed; `cd apps/themuffinman && ./mvnw test` passed with 269 tests; `ruby scripts/todo-audit.rb` passed with 28 open backlog items remaining.
- Backlog update: removed `AGENT-OWNERSHIP-AWARE-SOURCE-REPORTING`.
- Residual risk: generated report shape changed additively; downstream readers using existing string gap arrays remain compatible.
