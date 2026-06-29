# Feature Delivery Workflow

This document is the canonical end-to-end workflow for implementing a feature in this repository with Codex and the local tooling stack.

Keep it synchronized with `AGENTS.md`, `docs/documentation-sync-policy.md`, `docs/change-completion-checklist.md`, `docs/agent-operating-model.md`, and the local audit entrypoints whenever the delivery process changes.

## Purpose

- Explain the real execution flow from prompt intake through planning, discovery, implementation, documentation sync, validation evidence, closeout, and final response.
- Reduce process drift between human instructions, local tooling, generated artifacts, and agent-safe closeout requirements.
- Give one maintained reference for future workflow automation and improvement work.

## End-To-End Flow

1. Prompt intake

Codex receives the user request inside the workspace context, reads `AGENTS.md`, and identifies the required operating constraints:

- whether the task is implementation, analysis, or review
- whether a plan or master plan is required
- whether backend, frontend, docs, generated artifacts, or agent-safety surfaces are in scope
- whether any command requires escalation

The prompt itself is not treated as sufficient context. It is only the intent signal that drives the next discovery steps.

2. Plan and manifest bootstrapping

For multi-file, multi-layer, high-risk, or broad autonomous work, Codex creates a temporary plan under `.agents/`. For broader batches, it creates a master plan plus narrower child plans in explicit sequence. When the change meets manifest-required conditions, Codex also keeps a matching machine-readable manifest under `.agents/feature-manifests/`.

Typical bootstrap helpers:

- `make bootstrap-feature-work topic=<short-topic> [risk=<tier>] [mode=<mode>] [impact=<impact>] [profiles=<csv>]`
- manual creation of a master plan when the batch spans multiple tooling or documentation surfaces

The plan is the editable execution source of truth for the current change. The manifest is the structured closeout control surface.

3. Context-first discovery

Before broad repository exploration, Codex should prefer compact local context:

- `make diff-summary`
- `make audit-summary-index`
- `make context-pack topic=<topic>`
- `make codex-context budget=<tokens> mode=<mode> topic=<topic> intent='<intent>'`

The context gateway is the smallest fresh summary path when the task has a concrete implementation target. It composes compact packs for:

- changed files and diff stats
- changed symbols and AST slices
- targeted tests
- DTO, endpoint, frontend, and workflow relationships
- hotspot ranking
- validation and session handoff context

4. Focused routing and required-surface resolution

After the initial context pass, Codex resolves the smallest relevant audit and propagation surface:

- `make audit-router files=<csv>`
- `make audit-doc-sync-preflight files=<csv>`
- `make audit-doc-sync-required-surfaces files=<csv>`
- `make audit-manifest-decision files=<csv>`
- `make resolve-manifest-path files=<csv>`
- `make recommend-validation-preset files=<csv>`

This stage answers:

- which living docs are likely required
- which generated artifacts are expected to move
- which validation commands are the deterministic baseline
- whether a manifest is required and which manifest path matches the work

5. Implementation slices

Codex implements the change in the smallest meaningful slices instead of editing every surface at once.

Expected slice order:

- plan checkpoint
- first backend slice when backend is in scope
- first frontend slice when frontend is in scope
- docs and generated-artifact sync
- validation and closeout preparation

Implementation itself should stay repo-native:

- backend business rules in services and use cases
- thin controllers
- minimal frontend logic
- forward-only Flyway migrations
- plan-driven and documentation-aware closeout

6. Documentation synchronization

No logic-only change is complete until the affected docs, agent artifacts, and validation tests are updated together.

The minimum review set depends on the changed surfaces, but usually includes:

- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml` when machine-operational rules changed
- `docs/documentation-sync-policy.md`
- `docs/change-completion-checklist.md`
- this file when the delivery process itself changed

Codex should use the required-surface resolver outputs instead of manually guessing propagation scope.

7. Validation execution and evidence capture

Validation should be targeted first and broaden only when risk or profile requires it.

Typical helpers:

- `make recommend-targeted-tests`
- `make recommend-validation-preset files=<csv>`
- `make codex-context-explain`
- `make audit-agent-safety`
- `make audit-todo`

When commands are run, evidence should be recorded directly into the manifest and validation evidence files instead of being copied manually later.

Evidence helpers:

- `make record-validation manifest=<manifest-file> command='<command>'`
- `ruby scripts/audits/record-validation-evidence.rb manifest=<manifest-file> mode=generated_artifact path=<csv> summary='<summary>'`
- `ruby scripts/audits/record-validation-evidence.rb manifest=<manifest-file> mode=skipped_check check='<check>' reason='<reason>'`

Validation evidence now writes:

- manifest `validationEvidence.commands`
- `.agents/validation-evidence/<feature-id>.yaml`
- `docs/generated/local-tooling/validation-evidence/<feature-id>.json`

8. Manifest autofill and closeout preparation

When implementation and validation are underway, Codex should reduce manual closeout bookkeeping by autofilling what can be derived deterministically:

- `make autofill-feature-closeout manifest=<manifest-file> [files=<csv>] [generated=<csv>] [docs=<csv>]`

Autofill updates:

- refreshed generated artifact paths
- doc paths
- plan completion open-task count
- checklist booleans inferred from changed files and passed validation commands
- closeout summary report under `docs/generated/local-tooling/closeout-autofill/`

Autofill is a preparation step, not a replacement for review.

9. Final closeout audits

Before the task is considered complete, Codex should run the closeout gates that match the change:

- `make audit-todo`
- `make audit-plan-completion plan=<plan-file> [manifest=<manifest-file>]`
- `make audit-validation-evidence-quality`
- `make feature-closeout-audit manifest=<manifest-file>` for manifest-backed changes
- `make closeout-report manifest=<manifest-file>` when a structured final review summary is useful

These checks verify:

- no open plan tasks remain unless explicitly deferred
- backlog state is synchronized
- validation evidence is concrete
- manifest state matches reality
- required docs and generated artifacts were not skipped silently

10. Final response

Only after implementation, docs, generated artifacts, and validation are synchronized should Codex send the final user response.

The final response should state:

- what now works
- what changed at a high level
- what was validated
- any remaining risks or not-run checks

It should not claim completion if validation, documentation propagation, or closeout state is still open.

## Supporting Local Tools

Primary context and routing tools:

- `make codex-context`
- `make codex-context-explain`
- `make diff-summary`
- `make audit-summary-index`
- `make context-pack topic=<topic>`
- `make audit-router files=<csv>`
- `make audit-doc-sync-required-surfaces files=<csv>`

Closeout and evidence tools:

- `make record-validation manifest=<manifest-file> command='<command>'`
- `make autofill-feature-closeout manifest=<manifest-file>`
- `make audit-validation-evidence-quality`
- `make audit-plan-completion plan=<plan-file> [manifest=<manifest-file>]`
- `make feature-closeout-audit manifest=<manifest-file>`
- `make closeout-report manifest=<manifest-file>`

## Maintenance Rule

If the implementation workflow, planning workflow, context gateway workflow, evidence capture path, manifest workflow, or closeout commands change, update this document in the same change.
