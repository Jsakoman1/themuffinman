# Documentation Sync Policy

This file defines how logical code changes must propagate into living documentation and agent-safety artifacts.

## Goal

- Prevent silent drift between backend logic and documentation.
- Prevent future chatbot or voice-agent behavior from relying on stale assumptions.
- Make logical changes fail review when affected documentation is not updated.

## Rule

- A logic change is not complete when only code and tests are updated.
- If a change affects business meaning, permissions, validations, workflows, state transitions, automation assumptions, contact eligibility, visibility, or endpoint contracts, all affected documentation files must be updated in the same change.
- If a change introduces or expands entity capabilities, generation inputs, workflow branches, or edge cases that matter for admin or sandbox data generation, the related admin-generation and synthetic-flow definitions must also be reviewed and expanded in the same change.
- review and extend affected admin or sandbox generation flows
- admin-generation or sandbox-generation coverage for entities and workflows
- For multi-file, multi-layer, or high-risk logical changes, create a temporary implementation plan in `.agents/` before substantial edits and close it only after validation is green.
- Prefer bootstrapping that plan and its matching feature manifest through `make bootstrap-feature-work` when the change is large enough to justify the plan-driven workflow.
- Purely cosmetic edits are excluded.

## Authoring Discipline

- When `docs/agent-operating-model.yaml` uses `documentation_sync.rules[*].must_contain_all`, treat those phrases as canonical wording, not as paraphrase prompts.
- Copy protected phrases directly between YAML and target docs when the rule is meant to enforce the same meaning in multiple places.
- Validation should ignore case, punctuation, markdown markers, and whitespace differences, but it should not ignore wording drift that changes or weakens meaning.
- If a phrase keeps drifting, reduce synonyms and near-duplicates instead of adding more variants just to satisfy the test.
- Run `AgentOperatingModelValidationTest` after editing protected docs, YAML rules, generation-flow docs, or agent-safety instructions.

update all affected living docs in the same change

No logic-only change is complete until the affected docs, agent artifacts, and validation tests are updated together.

Use `docs/change-completion-checklist.md` as the fast pre-finish review layer, but keep this policy and `AGENTS.md` as the stronger source rules.

## Mandatory Files

For logical product changes, review and update as needed:
- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml`
- `docs/documentation-sync-policy.md`
- `docs/change-completion-checklist.md`
- `docs/feature-completion-manifest.schema.json`
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

Rule:
- extend affected admin-generation, batch-generation, or sandbox-generation workflows whenever a new feature changes what safe generated entities or transitions should contain
- extend read-before-write resolution flows whenever new natural-language management commands need exact target lookup, deterministic selection, or destructive confirmation
- keep executor-critical read DTO inventory aligned with required resolution fields, deterministic selection fields, producer references, and verifier tests
- keep mutating intent risk groups, exact-target resolution links, multilingual prompt matrix coverage, and documentation coverage manifests aligned with the code surface
- keep capability registries, intent lineage mappings, dry-run simulation contracts, and prompt drift fingerprints aligned with the actual planner surface
- keep backend contract snapshots, service workflow inventory, permission matrix rules, state-transition audit, and request-validation gates aligned with the actual backend mutation surface
- keep generated frontend contracts, automation-safe UI safety layers, frontend regression scenarios, and frontend feature expectations aligned with the actual planner and simulation surface
- keep frontend planner-response contract gates, dead-path checks, and feature completion manifests aligned with the actual implementation state
- keep feature risk tiers, bootstrap workflow, and close-out audit workflow aligned with the actual delivery process

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
- synthetic admin-generation flows must be kept current with newly introduced feature logic, validations, and edge cases

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
