# AGENT-CHANGESET-SCOPE-GUARDRAILS Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 37 of 82

## Backlog Item

Add deterministic guardrails that warn when a requested change mixes unrelated feature work, generated reports, and infrastructure/tooling changes in one commit-sized scope.

Source notes:
  Purpose: keep future Codex work cheaper to review, easier to validate, and less likely to preserve accidental unrelated edits.

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

## Completion Evidence

- Status: complete
- Changed files:
  - `scripts/audits/audit-change-impact-preflight.rb`
  - `scripts/audits/local_tooling_batch_audits.rb`
  - `docs/generated/local-tooling/change-impact-preflight.json`
  - `docs/generated/local-tooling/change-impact-preflight-summary.md`
  - `docs/tooling/codex-local-audits.md`
  - `docs/domain-technical.md`
  - `docs/agent-improvement-backlog.md`
  - `.agents/todo-master-plan.md`
  - `.agents/todo-plans/37-agent-changeset-scope-guardrails.md`
- Validation evidence:
  - `ruby scripts/audits/audit-change-impact-preflight.rb`
  - `make audit-documentation`
  - `make audit-agent-safety`
  - `make audit-local-tooling-incremental`
  - `ruby scripts/todo-audit.rb`
- Backlog update: removed `AGENT-CHANGESET-SCOPE-GUARDRAILS` from `docs/agent-improvement-backlog.md`.
- Residual risk: guardrails are report-only and intentionally do not fail builds; later closeout enforcement can decide which warnings should block commits.
