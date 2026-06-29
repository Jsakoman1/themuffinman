# AGENT-DOC-SYNC-POLICY-CLEANUP Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `high`
Master order: 58 of 82

## Backlog Item

Remove duplicated and fragmentary documentation-sync wording while preserving protected canonical phrases exactly.

Source notes:
  Problem:
  - `docs/documentation-sync-policy.md` contains repeated protected wording and fragment-only bullets.
  - The current meaning is strong, but repeated near-duplicates make future edits riskier.
  Acceptance criteria:
  - Protected phrases still satisfy `AgentOperatingModelValidationTest`.
  - Duplicate bullets are consolidated into one authoritative rule per concept.
  - `docs/change-completion-checklist.md`, `docs/documentation-sync-policy.md`, and `AGENTS.md` no longer have conflicting strength for the same requirement.

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
- Changed files:
  - `docs/documentation-sync-policy.md`
  - `docs/agent-improvement-backlog.md`
  - `.agents/todo-plans/58-agent-doc-sync-policy-cleanup.md`
  - `.agents/todo-master-plan.md`
- Validation evidence:
  - `ruby scripts/todo-audit.rb` passed.
  - `./mvnw -Dtest=AgentOperatingModelValidationTest test` passed.
  - `make audit-documentation` passed.
  - `make audit-agent-safety` passed.
- Backlog update: removed `AGENT-DOC-SYNC-POLICY-CLEANUP` from open items and recorded the completed policy cleanup in current state.
- Residual risk: none known; protected canonical phrases still satisfy the validation test.
