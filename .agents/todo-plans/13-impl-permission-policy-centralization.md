# IMPL-PERMISSION-POLICY-CENTRALIZATION Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `high`
Master order: 13 of 82

## Backlog Item

Consolidate repeated visibility, ownership, role, circle-membership, and quest-application permission checks into explicit policy services with named decisions.

Source notes:
  Goal: preserve logic across modules and prevent future feature work from duplicating subtly different authorization rules.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-documentation`
- [x] `cd apps/themuffinman && ./mvnw test`

## Completion Evidence

- Status: complete
- Changed files: `QuestAccessPolicyService.java`, `QuestApplicationWorkflowSupport.java`, `QuestStateTransitionService.java`, `QuestVisibilityService.java`, `QuestExecutionPrimitiveService.java`, `LocationAccessPolicyService.java`, policy tests, living docs, agent-operating model sections/generated artifacts, `docs/implementation-backlog.md`
- Validation evidence: `cd apps/themuffinman && ./mvnw test` passed with 259 tests; `make audit-documentation` passed; `ruby scripts/todo-audit.rb` passed.
- Backlog update: removed `IMPL-PERMISSION-POLICY-CENTRALIZATION` from `docs/implementation-backlog.md`.
- Residual risk: full role-policy unification for unrelated admin dashboard/controller checks remains outside this slice; quest/application and exact-location permission decisions now have named backend policy services and tests.
