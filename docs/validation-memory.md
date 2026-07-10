# Validation Memory

This document is the canonical read-first memory for validation, manifest, and closeout expectations that repeatedly matter during feature delivery.

It exists so future sessions do not have to rediscover validator rules by failing `AgentOperatingModelValidationTest`, manifest audits, or closeout gates.

Machine-readable companion:

- `docs/validation-memory.json`
- `docs/validation-memory.schema.json`

## When To Read This

Read this file early when:

- a feature uses a manifest
- the change touches backend, frontend, docs, or generated artifacts together
- the change is agent/tooling/workflow-related
- a closeout audit fails and the expected command or evidence is unclear

`docs/codex-fast-path.md` remains the primary entrypoint. This file is the compact validation-memory add-on when the work is closeout-sensitive.

The local `codex-context` gateway should also auto-include this memory when it detects manifest-backed or closeout-sensitive work.

Generated helpers:

- `scripts/audits/generate-validation-memory-closeout-card.rb`
- `docs/generated/local-tooling/validation-memory-closeout-card-summary.md`
- `docs/generated/local-tooling/validation-memory-closeout-card.json`

Safety boundary:

- `docs/validation-memory.schema.json` should fail hard only when the machine-readable structure is invalid.
- `recommend-validation-preset` should use `docs/validation-memory.json` only as an additive canonical-command overlay, not as a replacement for the existing deterministic preset logic.

## Core Principle

Canonical command strings matter.

If the validator expects `npm run type-check`, evidence recorded only as `npm --prefix apps/themuffinman/frontend run type-check` may be operationally true but still fail the manifest validator. Record the exact canonical command string when the manifest or validator expects one.

## Canonical Validation Commands

### Frontend-contract changes

Use and record these exact commands:

- `npm run validate:contracts`
- `npm run type-check`
- `npm run build`

If the commands are executed from another working directory for convenience, still record the canonical command string in manifest evidence and audit command lists when validator rules expect it.

### Backend-logic changes

Use and record at least one canonical backend validation signal:

- `./mvnw test`
- `make audit-agent-safety`

Targeted backend tests remain useful, but manifest-backed backend logic often still needs one canonical backend validation command in audit commands and passed evidence.

### Agent-contract changes

Use and record these canonical commands when the change affects workflow docs, agent docs, manifests, or machine-operational surfaces:

- `make generate-agent-operating-model`
- `make generate-agent-artifacts`
- `make audit-agent-safety`

If the manifest is complete, passed evidence for the generator step and `make audit-agent-safety` should both be present.

### Workflow-expansion changes

When the manifest declares `workflow-expansion`:

- include at least one `*ScenarioTest` in `testPaths`
- record passed evidence for a scenario or use-case contract command, for example:
  - `./mvnw test -Dtest=AdminAgentExecutionServiceTest,AdminSyntheticQuestExecutionPlannerTest`

### Closeout and backlog hygiene

Completed manifest-backed changes should also record:

- `make closeout-driver plan=<plan-file> manifest=<manifest-file>`
- `make cleanup-generated-history`
- `make audit-todo`
- `make audit-plan-completion plan=<plan-file> [manifest=<manifest-file>]`
- for control-system plan hygiene, also record `make plan-index` and `make control-start` so the routing snapshot matches the active master-plan set

Control-system closeout should use these canonical commands when routing or status hygiene changes:

- `make plan-index`
- `make control-start`
- `make audit-plan-completion plan=<plan-file>`

The standard closeout-sensitive audit loop should also rerun:

- `make audit-validation-memory-drift`
- `make validation-memory-closeout-card`

These prove that backlog drift, unfinished plan checkboxes, and manifest-plan mismatch are closed.
Completed master plans also need their child statuses to be final; pending, draft, or in_progress child rows are a closeout failure.
Archive-only paths under `docs/generated/local-tooling/.history/`, `docs/generated/local-tooling/.cache/`, and `.agents/archive/` are not valid live closeout evidence.

## Manifest Heuristics That Recur

- `codePaths` must not include test-only files.
- Test files belong in `testPaths`.
- `scope` values in validation evidence must use allowed enum values only.
- Do not add ad hoc manifest properties that the schema does not define.
- `generatedArtifacts.notApplicableReason` must satisfy schema constraints when present.
- Completed manifests should prefer exact command evidence over vague summaries.

## Practical Recording Pattern

For complete manifest-backed work, prefer a compact evidence set like:

1. targeted backend or frontend checks for the changed slice
2. canonical validator-facing commands for the change profile
3. plan completion and backlog closeout audits
4. generated-artifact refresh commands when machine-readable docs or inventories changed

This keeps evidence minimal while still satisfying the validator.

## Common Failure Patterns

### Command alias drift

- The work was validated correctly.
- The manifest failed because the recorded command string did not match the canonical validator expectation.

Fix:

- keep the operational command if useful
- also record the canonical command string the validator expects

### Manifest bucket drift

- A test file was stored in `codePaths`
- a generated artifact was omitted from refreshed paths
- a schema-only field was added ad hoc

Fix:

- keep runtime code, docs, generated artifacts, and tests in their correct manifest buckets

### Plan-manifest drift

- child plans were implemented but not marked complete
- master plan still showed open checkboxes
- manifest was marked complete before plan audits passed

Fix:

- update child plans first
- update master plan second
- prune archive-only history and close plan-owned temp work products
- run `make audit-plan-completion`
- only then keep the manifest at `complete`

## Recommended Read Order During Closeout

1. `docs/codex-fast-path.md`
2. this file
3. the active plan or master plan
4. the active manifest
5. `docs/generated/local-tooling/audit-summary-index.md` when a failure needs a smaller focused audit; treat it as a
   routing aid, not as authoritative current state

## Related Files

- `docs/codex-fast-path.md`
- `docs/feature-delivery-workflow.md`
- `docs/change-completion-checklist.md`
- `docs/documentation-sync-policy.md`
- `docs/agent-operating-model.md`
- `scripts/audits/audit-validation-memory-drift.rb`
- `scripts/audits/generate-validation-memory-closeout-card.rb`
- `docs/generated/local-tooling/failure-knowledge-base-summary.md`
