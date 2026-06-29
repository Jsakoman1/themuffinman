# AGENT-POST-MERGE-LEARNING-LOOP Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 54 of 82

## Backlog Item

After large changes, generate a short retrospective artifact with failure points, missing tools, docs gaps, and reusable patterns.

Source notes:
  Purpose: turn every expensive implementation into a cheaper future implementation.

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

## Completion Evidence

- Status: complete
- Changed files: added `scripts/audits/generate-post-merge-retrospective.rb`; extended `scripts/audits/local_tooling_extended_tools.rb`, `Makefile`, `.gitignore`, `docs/generated/artifact-policy.yaml`, `docs/generated/README.md`, `docs/change-completion-checklist.md`, `docs/domain-technical.md`, `docs/agent-improvement-backlog.md`, and refreshed local audit registry/target index artifacts.
- Validation evidence: `ruby -c scripts/audits/generate-post-merge-retrospective.rb`, `ruby -c scripts/audits/local_tooling_extended_tools.rb`, `make post-merge-retrospective topic=master-plan`, `ruby scripts/audits/generate-audit-registry-artifacts.rb`, `ruby scripts/audits/audit-make-target-index.rb`, `ruby scripts/todo-audit.rb`, `make audit-documentation`, `make audit-agent-safety`, and `ruby scripts/audits/audit-generated-artifact-freshness.rb` all passed.
- Backlog update: removed `AGENT-POST-MERGE-LEARNING-LOOP` from `docs/agent-improvement-backlog.md`.
- Residual risk: the retrospective is report-first and depends on existing local audit outputs; it summarizes available signals but does not enforce merge policy or guarantee every lesson is captured.
