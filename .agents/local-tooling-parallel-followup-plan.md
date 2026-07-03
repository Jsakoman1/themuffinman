---
machine_kind: plan
machine_status: unknown
machine_title: Local Tooling Parallel Follow-up Plan
---

# Local Tooling Parallel Follow-up Plan

Goal: implement the next three deferred local-tooling helpers in one coordinated batch while keeping each helper independently auditable.

Scope source:
- `docs/codex-local-tooling-todo.md`

Execution rules:
- Implement the three child plans in parallel where code paths do not conflict.
- Shared surfaces (`Makefile`, `scripts/audits/local_tooling_extended_tools.rb`, `docs/codex-local-tooling-todo.md`, generated registry artifacts) close only after all child tools are implemented.
- Remove completed backlog items from the persistent backlog source in the same change that completes them.
- Close this plan only after each child plan has completion evidence and the shared closeout validation passes.

Up-front approval assessment:
- No approval is currently required for workspace file edits, local Ruby execution, registry regeneration, or local validation commands used by these three tools.

Child plan inventory:
- [x] 83 `CODEX-LOCAL-CHANGESET-PLAYBOOK` (local-tooling, medium risk): `.agents/todo-plans/83-codex-local-changeset-playbook.md`
- [x] 84 `CODEX-LOCAL-AUDIT-DELTA-REPORT` (local-tooling, medium risk): `.agents/todo-plans/84-codex-local-audit-delta-report.md`
- [x] 85 `CODEX-LOCAL-VALIDATION-PRESET-RECOMMENDER` (local-tooling, medium risk): `.agents/todo-plans/85-codex-local-validation-preset-recommender.md`

Execution phases:
- [x] Phase 1: create child plans and inspect existing local-tooling patterns.
- [x] Phase 2: implement changeset playbook, audit delta report, and validation preset recommender.
- [x] Phase 3: wire shared registry, Make targets, docs, and generated examples.
- [x] Phase 4: run targeted validation plus plan/todo closeout audits.

## Completion Evidence

- Status: complete
- Validation evidence:
  - `make changeset-playbook` passed.
  - `make recommend-validation-preset` passed.
  - `make audit-delta-report audit=diff-summary` passed.
  - `make audit-delta-report audit=audit-router` passed.
  - `make generate-audit-registry-artifacts` passed with 52 registered audits.
  - `make audit-local-tooling-incremental` passed and exercised the new targets in the shared batch flow.
  - `ruby scripts/todo-audit.rb` passed.
  - `make audit-plan-completion plan=.agents/local-tooling-parallel-followup-plan.md` passed.
- Backlog delta: all three deferred local-tooling items were implemented and removed from the remaining open TODO set.
