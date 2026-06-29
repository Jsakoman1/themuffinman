# AGENT-VALIDATION-EVIDENCE-MANIFEST Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `high`
Master order: 42 of 82

## Backlog Item

Add a lightweight per-change validation evidence record that captures commands run, generated reports refreshed, skipped checks, and reasons.

Source notes:
  Purpose: make closeout cheaper to audit and reduce repeated user questions about what was or was not validated.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-agent-safety`
- [x] `make audit-local-tooling-incremental`

## Completion Evidence

- Status: complete
- Changed files: `docs/validation-evidence.schema.json`, `.agents/templates/validation-evidence.template.yaml`, `.agents/validation-evidence/.gitkeep`, `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`, `docs/agent-operating-model/sections/documentation_sync.yaml`, `docs/agent-operating-model.yaml`, `docs/agent-operating-model.md`, `docs/domain-technical.md`, `docs/change-completion-checklist.md`, `docs/documentation-sync-policy.md`, `docs/agent-improvement-backlog.md`, `docs/generated/source-of-truth-audit.json`
- Validation evidence: `ruby -rjson -e 'JSON.parse(File.read("docs/validation-evidence.schema.json")); puts "validation evidence schema json ok"'` passed; `ruby scripts/generate-agent-operating-model.rb` passed; `./mvnw test -Dtest=AgentOperatingModelValidationTest` passed; `ruby scripts/generate-source-of-truth-audit.rb` passed; `ruby scripts/todo-audit.rb` passed; `make audit-agent-safety` passed; `make audit-local-tooling-incremental` passed.
- Backlog update: removed `AGENT-VALIDATION-EVIDENCE-MANIFEST` from `docs/agent-improvement-backlog.md`
- Residual risk: evidence records are schema-backed and test-validated, but hard closeout enforcement and automatic recording remain deferred to later explicit backlog items.
