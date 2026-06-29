# AGENT-VALIDATION-EVIDENCE-SCHEMA Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `high`
Master order: 57 of 82

## Backlog Item

Standardize evidence records for validation commands and generated artifacts.

Source notes:
  Proposed evidence fields:
  - `command`: exact command that was run.
  - `scope`: backend, frontend, docs, generated-artifact, local-tooling, or smoke.
  - `result`: passed, failed, skipped, or not_applicable.
  - `ranAt`: ISO timestamp or `unknown` for historical migration entries.
  - `summary`: short result summary.
  - `outputPath`: optional generated report or diagnostic summary path.
  - `skippedReason`: required when `result` is skipped or not_applicable.
  Acceptance criteria:
  - Closeout rejects skipped checks without `skippedReason`.
  - Closeout rejects required checks with `result: failed`.
  - Closeout accepts optional checks only when they are not required by risk tier or profile.

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
- Changed files: updated `docs/validation-evidence.schema.json`, `.agents/templates/validation-evidence.template.yaml`, `scripts/audits/audit-validation-evidence-quality.rb`, `docs/change-completion-checklist.md`, `docs/documentation-sync-policy.md`, `docs/domain-technical.md`, `docs/agent-improvement-backlog.md`, and refreshed local tooling generated reports via `make audit-local-tooling-incremental`.
- Validation evidence: `ruby -c scripts/audits/audit-validation-evidence-quality.rb`, `make audit-validation-evidence-quality`, `cd apps/themuffinman && ./mvnw -Dtest=AgentOperatingModelValidationTest test`, `ruby scripts/todo-audit.rb`, `make audit-agent-safety`, `make audit-local-tooling-incremental`, `ruby scripts/audits/audit-generated-artifact-freshness.rb`, and `cd apps/themuffinman && ./mvnw test` all passed.
- Backlog update: removed `AGENT-VALIDATION-EVIDENCE-SCHEMA` from `docs/agent-improvement-backlog.md`.
- Residual risk: feature-manifest embedded evidence remains a closeout-specific structure from plan 55; this plan standardizes the standalone `.agents/validation-evidence/*.yaml` schema and quality audit.
