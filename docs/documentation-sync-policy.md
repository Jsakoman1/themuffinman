# Documentation Sync Policy

This file defines how logical code changes must propagate into living documentation and agent-safety artifacts.

## Goal

- Prevent silent drift between backend logic and documentation.
- Prevent future chatbot or voice-agent behavior from relying on stale assumptions.
- Make logical changes fail review when affected documentation is not updated.

## Rule

- A logic change is not complete when only code and tests are updated.
- If a change affects business meaning, permissions, validations, workflows, state transitions, automation assumptions, contact eligibility, visibility, or endpoint contracts, all affected documentation files must be updated in the same change.
- Purely cosmetic edits are excluded.

update all affected living docs in the same change

No logic-only change is complete until the affected docs, agent artifacts, and validation tests are updated together.

## Mandatory Files

For logical product changes, review and update as needed:
- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml`
- `docs/documentation-sync-policy.md`
- `AGENTS.md`

## Mandatory Tests

For logical product changes, keep passing:
- `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`
- domain tests that cover the changed behavior, such as service tests for the affected module

## Change Matrix

### Business or permission logic

Update:
- `docs/business-logic.md`
- `docs/domain-technical.md`
- `AGENTS.md` only if the repository-wide maintenance rule itself changes

Test:
- add or extend domain tests around the changed service behavior

### Agent or automation safety logic

Update:
- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml`
- `docs/business-logic.md`
- `docs/domain-technical.md`

Test:
- `AgentOperatingModelValidationTest`

### Sandbox or synthetic-data logic

Update:
- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml`

Test:
- `AgentOperatingModelValidationTest`

Rule:
- sandbox behavior must stay explicitly separated from production-like user flows

### Social or chat eligibility logic

Update:
- chat sections in `docs/business-logic.md`
- chat sections in `docs/domain-technical.md`
- `docs/agent-operating-model.md` and YAML when the rule affects future automation safety

Test:
- chat service tests
- `AgentOperatingModelValidationTest` when machine-operational rules or doc-sync rules change

### Schema or migration logic

Update:
- technical docs and source-of-truth inventory when migration lineage changes

Test:
- migration-related startup or integration verification

## Current Protected Rules

- Pending circle requests do not create chat eligibility.
- Chat workspace only includes current accepted circle contacts.
- Existing conversation history does not preserve chat access after the accepted relation is lost.
