# AGENT-SOCIAL-DTO-SEARCH-CONTACT Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 24 of 82

## Backlog Item

Tighten `social/dto` search and contact-list DTOs after the overview and member slice is stable.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-agent-safety`
- [x] `cd apps/themuffinman && ./mvnw test`

## Completion Evidence

- Status: complete
- Changed files: promoted social search/contact DTOs into a strict `automation_relevant` backend audit slice, added source-of-truth and documentation coverage entries, updated operating docs, and regenerated agent/backend/source audit artifacts.
- Validation evidence: `AgentOperatingModelValidationTest` passed; `make audit-agent-safety` passed; `./mvnw test` passed with 269 tests; `make audit-documentation` passed; `make audit-generated-artifact-freshness` passed; `ruby scripts/todo-audit.rb` passed.
- Backlog update: removed `AGENT-SOCIAL-DTO-SEARCH-CONTACT` from `docs/agent-improvement-backlog.md`.
- Residual risk: no runtime DTO fields or endpoint behavior changed; admin social DTO slice remains open in the next plan.
