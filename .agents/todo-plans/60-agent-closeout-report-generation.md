# AGENT-CLOSEOUT-REPORT-GENERATION Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `high`
Master order: 60 of 82

## Backlog Item

Generate a compact final closeout report from manifest evidence, changed files, docs delta, backlog delta, generated artifacts, and validation results.

Source notes:
  Purpose:
  - Make final user summaries factual and cheap.
  - Reduce repeated manual inspection before commit/push.
  Proposed output:
  - `docs/generated/local-tooling/closeout-reports/<feature-id>.json`
  - `docs/generated/local-tooling/closeout-reports/<feature-id>-summary.md`
  Acceptance criteria:
  - Report lists changed code/docs/tests/generated artifacts.
  - Report lists validation commands with pass/fail/skipped status.
  - Report lists created/resolved backlog IDs.
  - Report lists known residual risks.

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
- [x] `make audit-local-tooling-incremental`

## Completion Evidence

- Status: complete
- Changed files:
  - `scripts/audits/generate-closeout-report.rb`
  - `Makefile`
  - `scripts/audits/local_tooling_extended_tools.rb`
  - `docs/codex-local-tooling-todo.md`
  - `docs/agent-improvement-backlog.md`
  - `docs/change-completion-checklist.md`
  - `docs/documentation-sync-policy.md`
  - `docs/domain-technical.md`
  - `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`
  - `docs/generated/source-of-truth-audit.json`
  - `docs/tooling/codex-local-audits.yml`
  - `docs/generated/local-tooling/closeout-reports/backend_audit_tiering.json`
  - `docs/generated/local-tooling/closeout-reports/backend_audit_tiering-summary.md`
- Validation evidence:
  - `ruby -c scripts/audits/generate-closeout-report.rb` passed.
  - `make closeout-report manifest=.agents/feature-manifests/backend-audit-tiering-manifest.yaml` passed.
  - `make audit-plan-completion plan=.agents/todo-plans/60-agent-closeout-report-generation.md` passed after closeout.
  - `ruby scripts/todo-audit.rb` passed.
  - `make audit-documentation` passed.
  - `make audit-agent-safety` passed.
  - `make audit-local-tooling-incremental` passed.
  - `make audit-generated-artifact-freshness` passed after regenerating `docs/generated/source-of-truth-audit.json`.
- Backlog update: removed `AGENT-CLOSEOUT-REPORT-GENERATION` from open items and recorded closeout-report generation in current state.
- Residual risk: none known; report quality depends on manifest evidence quality and current `git status` changed-file inventory.
