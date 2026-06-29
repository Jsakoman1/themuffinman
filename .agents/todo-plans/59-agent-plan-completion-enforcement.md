# AGENT-PLAN-COMPLETION-ENFORCEMENT Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `high`
Master order: 59 of 82

## Backlog Item

Add machine-checkable completion markers for temporary plans and master plans.

Source notes:
  Problem:
  - Plans are useful working memory, but completion is currently human-discipline only.
  - Master plans can claim completion without checking child plan state or validation evidence.
  Proposed approach:
  - Add a required `Completion Evidence` section to plan templates.
  - Add explicit child-plan status rows to master plans.
  - Extend closeout audit to verify the manifest plan file exists and contains completion evidence when manifest status is complete.
  Acceptance criteria:
  - Completed manifests fail if their referenced plan is still mostly unchecked or lacks completion evidence.
  - Master plan closeout fails if any listed child plan is incomplete without a reason.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-agent-safety`

## Completion Evidence

- Status: complete
- Changed files:
  - `scripts/audits/audit-plan-completion.rb`
  - `scripts/feature-closeout-audit.sh`
  - `Makefile`
  - `scripts/audits/local_tooling_extended_tools.rb`
  - historical completed plans under `.agents/` that now include completion evidence
  - `docs/agent-operating-model/sections/policies.yaml`
  - `docs/agent-operating-model.yaml`
  - `docs/agent-operating-model.schema.json`
  - `docs/domain-technical.md`
  - `docs/documentation-sync-policy.md`
  - `docs/change-completion-checklist.md`
  - `docs/agent-improvement-backlog.md`
  - `docs/codex-local-tooling-todo.md`
  - `docs/tooling/codex-local-audits.yml`
  - `docs/generated/local-tooling/audit-summary-index.json`
  - `docs/generated/local-tooling/audit-summary-index.md`
  - `docs/generated/local-tooling/audit-registry-artifacts.json`
  - `docs/generated/local-tooling/audit-registry-artifacts-summary.md`
- Validation evidence:
  - `ruby -c scripts/audits/audit-plan-completion.rb` passed.
  - `zsh -n scripts/feature-closeout-audit.sh` passed.
  - `make audit-plan-completion plan=.agents/backend-audit-tiering-plan.md manifest=.agents/feature-manifests/backend-audit-tiering-manifest.yaml` passed.
  - `make feature-closeout-audit manifest=.agents/feature-manifests/backend-audit-tiering-manifest.yaml` passed.
  - `make audit-plan-completion plan=.agents/todo-plans/59-agent-plan-completion-enforcement.md` passed after closeout.
  - `ruby scripts/todo-audit.rb` passed.
  - `make audit-agent-safety` passed.
- Backlog update: removed `AGENT-PLAN-COMPLETION-ENFORCEMENT` from open items and recorded the completed enforcement in current state.
- Residual risk: existing master plan remains intentionally incomplete until the full TODO batch closes; `make audit-plan-completion plan=.agents/todo-master-plan.md` should fail until remaining child plans are complete or explicitly deferred.
