# IMPL-GENERATED-ARTIFACT-SCOPE-CLEANUP Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `medium`
Master order: 9 of 82

## Backlog Item

Decide which generated audit outputs belong in version control and move disposable local run outputs behind ignored paths or opt-in commit guidance.

Source notes:
  Goal: reduce large diffs, cheaper code review, and fewer tokens spent reading generated noise.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-local-tooling-incremental`
- [x] `cd apps/themuffinman && ./mvnw test`
- [x] `cd apps/themuffinman/frontend && npm run type-check`
- [x] `cd apps/themuffinman/frontend && npm run build`

## Completion Evidence

- Status: complete
- Changed files: `.gitignore`, `docs/generated/README.md`, generated local-tooling snapshots, disposable generated-file deletions, `docs/domain-technical.md`, `docs/implementation-backlog.md`
- Validation evidence: `make audit-local-tooling-incremental` passed; `ruby scripts/todo-audit.rb` passed.
- Backlog update: removed `IMPL-GENERATED-ARTIFACT-SCOPE-CLEANUP` from `docs/implementation-backlog.md`.
- Residual risk: full backend/frontend validation deferred to master closeout; generated snapshot refresh changed many stable generated outputs because the audit suite observed the current broad working tree.
