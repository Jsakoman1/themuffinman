# AGENT-DOC-TEMPLATE-BY-CHANGE-TYPE Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `high`
Master order: 45 of 82

## Backlog Item

Define short documentation templates for new workflow, new endpoint, new DTO contract, new module, new permission rule, and schema migration changes.

Source notes:
  Purpose: make docs updates faster and less inconsistent while keeping living docs concise.

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
- [x] `cd apps/themuffinman && ./mvnw test`

## Completion Evidence

- Status: complete
- Changed files: `.agents/templates/docs/new-workflow.template.md`, `.agents/templates/docs/new-endpoint.template.md`, `.agents/templates/docs/new-dto-contract.template.md`, `.agents/templates/docs/new-module.template.md`, `.agents/templates/docs/new-permission-rule.template.md`, `.agents/templates/docs/schema-migration.template.md`, `docs/agent-operating-model/sections/documentation_ownership.yaml`, `docs/agent-operating-model.yaml`, `docs/agent-operating-model.schema.json`, `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`, `docs/agent-operating-model.md`, `docs/change-completion-checklist.md`, `docs/documentation-sync-policy.md`, `docs/domain-technical.md`, `docs/agent-improvement-backlog.md`
- Validation evidence: `ruby scripts/generate-agent-operating-model.rb` passed; `cd apps/themuffinman && ./mvnw test -Dtest=AgentOperatingModelValidationTest` passed; `ruby scripts/todo-audit.rb` passed; `make audit-documentation` passed; `make audit-agent-safety` passed; `cd apps/themuffinman && ./mvnw test` passed with 273 tests.
- Backlog update: removed `AGENT-DOC-TEMPLATE-BY-CHANGE-TYPE` from `docs/agent-improvement-backlog.md`.
- Residual risk: templates are concise starter checklists and do not yet enforce template use for every future change; later audit/enforcement backlog items can harden that behavior.
