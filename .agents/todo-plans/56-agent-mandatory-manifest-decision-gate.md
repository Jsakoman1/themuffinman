# AGENT-MANDATORY-MANIFEST-DECISION-GATE Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `high`
Master order: 56 of 82

## Backlog Item

Add an explicit rule that every non-trivial change must record either a feature manifest or a documented reason why the manifest workflow was not used.

Source notes:
  Problem:
  - The current checklist says the feature manifest is optional for plan-driven workflow, but it does not force a decision record for multi-layer changes.
  - This leaves room for broad changes to finish with only conversational closeout.
  Proposed rule:
  - Cosmetic and single-file contract-neutral refactors may skip manifests.
  - Multi-file, multi-layer, high-risk, executor-critical, workflow-expansion, agent-contract, or generated-artifact changes must use a manifest.
  - If a change is borderline, the agent must record a one-line manifest decision in the temporary plan or final closeout.
  Acceptance criteria:
  - `docs/change-completion-checklist.md` distinguishes `manifest required`, `manifest optional`, and `manifest skipped with reason`.
  - `docs/documentation-sync-policy.md` explains the same decision rule without weaker wording.
  - `make bootstrap-feature-work` remains the preferred way to create required manifests.

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
- Changed files: updated `docs/change-completion-checklist.md`, `docs/documentation-sync-policy.md`, `docs/domain-technical.md`, and `docs/agent-improvement-backlog.md`.
- Validation evidence: `ruby scripts/todo-audit.rb`, `make audit-documentation`, `make audit-agent-safety`, and `cd apps/themuffinman && ./mvnw test` all passed.
- Backlog update: removed `AGENT-MANDATORY-MANIFEST-DECISION-GATE` from `docs/agent-improvement-backlog.md`.
- Residual risk: this plan records the mandatory decision policy in docs; machine enforcement of missing manifest decisions remains covered by later local-tooling plan `CODEX-LOCAL-MANIFEST-DECISION-AUDIT`.
