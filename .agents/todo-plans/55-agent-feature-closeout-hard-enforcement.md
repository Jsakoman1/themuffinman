# AGENT-FEATURE-CLOSEOUT-HARD-ENFORCEMENT Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `high`
Master order: 55 of 82

## Backlog Item

Convert the current feature closeout flow from advisory checklist output into a hard-fail enforcement layer for plan-driven and high-risk changes.

Source notes:
  Problem:
  - `scripts/feature-closeout-audit.sh` currently prints required checks and checklist status but does not fail when checklist fields remain false.
  - Feature manifests can claim `backendTestsPassed`, `frontendValidationPassed`, `docsSynced`, or `agentModelSynced` without structured evidence.
  - Plan-driven workflow is documented, but high-risk changes can still bypass manifest completion unless the agent voluntarily uses it.
  Implementation phases:
  - Phase 1: extend `docs/feature-completion-manifest.schema.json` with `validationEvidence`, `docDelta`, `generatedArtifacts`, `planCompletion`, and `closeoutDecision` sections.
  - Phase 2: update `.agents/templates/feature-completion-manifest.template.yaml` so new manifests start with explicit evidence placeholders and skipped-check reason fields.
  - Phase 3: make `scripts/feature-closeout-audit.sh` fail if `status: complete` is missing, required checklist fields are false, required profile commands lack evidence, or skipped checks have no reason.
  - Phase 4: add profile-specific enforcement rules: `backend-logic` requires backend test evidence and domain docs review; `agent-contract` requires generated model/artifact evidence plus `make audit-agent-safety`; `frontend-contract` requires type-check/build evidence; `workflow-expansion` requires scenario or use-case contract test evidence.
  - Phase 5: add a validation test that loads all `.agents/feature-manifests/*.yaml`, validates schema, verifies referenced artifact paths exist, and verifies completed manifests have evidence for every declared required check.
  Acceptance criteria:
  - A manifest with `status: complete` and `backendTestsPassed: false` fails closeout.
  - A manifest with `backendTestsPassed: true` but no command evidence fails closeout.
  - A high-risk or executor-critical manifest without `make audit-agent-safety` evidence fails closeout.
  - A frontend-contract manifest without both `npm run type-check` and `npm run build` evidence fails closeout.
  - A workflow-expansion manifest without scenario-style or use-case contract test evidence fails closeout.
  - A manifest that lists the same file in multiple artifact buckets fails closeout.
  - The closeout script exits non-zero on all enforcement failures instead of only printing warnings.
  Files likely affected:
  - `docs/feature-completion-manifest.schema.json`
  - `.agents/templates/feature-completion-manifest.template.yaml`
  - `scripts/feature-closeout-audit.sh`
  - `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`
  - `docs/change-completion-checklist.md`
  - `docs/documentation-sync-policy.md`

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
- Changed files: extended `docs/feature-completion-manifest.schema.json`, `.agents/templates/feature-completion-manifest.template.yaml`, all existing `.agents/feature-manifests/*-manifest.yaml`, `scripts/feature-closeout-audit.sh`, `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`, `docs/change-completion-checklist.md`, `docs/documentation-sync-policy.md`, `docs/domain-technical.md`, `docs/agent-improvement-backlog.md`, and refreshed `docs/generated/source-of-truth-audit.json`.
- Validation evidence: `/bin/zsh -n scripts/feature-closeout-audit.sh`, `cd apps/themuffinman && ./mvnw -Dtest=AgentOperatingModelValidationTest test`, `make feature-closeout-audit` across every existing feature manifest, `ruby scripts/generate-source-of-truth-audit.rb`, `ruby scripts/todo-audit.rb`, `make audit-documentation`, `make audit-agent-safety`, `ruby scripts/audits/audit-generated-artifact-freshness.rb`, and `cd apps/themuffinman && ./mvnw test` all passed.
- Backlog update: removed `AGENT-FEATURE-CLOSEOUT-HARD-ENFORCEMENT` from `docs/agent-improvement-backlog.md`.
- Residual risk: historical manifests use migrated evidence summaries because older changes did not store full structured command transcripts; new completed manifests now hard-fail without structured evidence.
