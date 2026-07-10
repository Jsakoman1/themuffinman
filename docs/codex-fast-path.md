# Codex Fast Path

`AGENTS.md` remains the authoritative repository instruction set.

`docs/codex-fast-path.md` is the compact execution entrypoint for most feature work.

Use the full workflow only when the change is high-risk, multi-layer, agent/tooling/workflow-related, or when a resolver requires it.

Manifest usage is tier-driven and conditional instead of being the default for every non-trivial backend change.

## Purpose

- Start feature work with the smallest useful context.
- Pick the lightest safe workflow tier before loading deeper process docs.
- Route Codex toward existing local audits instead of carrying the whole workflow in prompt context.
- Prefer machine-readable control and validation sources first when the task touches active behavior, workflow state,
  or automation-facing facts; use human-readable docs as the next layer of context or explanation.

## Default Startup

1. Read `AGENTS.md`.
2. Read this file.
3. If the task touches active control state, planning state, or automation-facing rules, open the machine-readable
   sources listed in `docs/control-surface-map.md` before broad narrative docs.
4. If the task is product-direction, UX, interaction design, or Social Useful Network vision work, read `docs/product-memory.md` and `docs/product-vision.md` before broad discovery so you start from stable lessons and the canonical direction layer.
5. If the task implements or changes `/vision`, read `docs/vision-architecture-patterns.md` before backend orchestration, API, frontend canvas, prompt-handling, or executor decisions.
6. If the task implements or changes `/vision`, also read the compact vision memory set before broad repo search:
   - `docs/vision-context-gateway.md`
   - `docs/vision-decision-record.md`
   - `docs/vision-feature-slice-checklist.md`
   - `docs/vision-generated-artifact-policy.md` when contracts, generated docs, or agent-operating surfaces may change
   - `docs/vision-failure-memory.md` when the work is validation-heavy or similar drift already appeared
   - `docs/vision-status-ledger.md` for current done, deferred, and design-blocked capability state
7. If the task is manifest-backed, closeout-sensitive, or agent/workflow-heavy, read `docs/validation-memory.md` and `docs/validation-memory.json` before broad validation so canonical command strings and manifest evidence expectations are explicit up front.
8. Run compact context first:
   - `make codex-context topic=<topic> intent='<intent>'`
   - `make recommend-targeted-tests`
   - `make clean-text-noise max_lines=80` when you need to strip Maven, audit, or generated log noise before summarizing evidence.
9. `make codex-context` now uses the diff summary, audit summary index, the most relevant audit, targeted tests, and a concise evidence bundle as its default read chain, writes a canonical execution manifest at `docs/generated/local-tooling/codex-context/latest.execution.json` with schema `docs/codex-context-execution-manifest.schema.json`, and auto-includes validation memory when the batch is manifest-backed or closeout-sensitive.
10. `make control-start`, `make codex-context`, and `make context-pack` surface the topic's layered-analysis artifact and temp work-product inventory when they exist.
11. Load deeper workflow docs only if the chosen tier or a resolver requires them.

For `/vision` work, the compact context should usually be opened after the vision memory set so the repo search starts from the right backend, API, frontend, test, and doc surfaces.

For broad, long-running, or high-complexity work, prefer a master plan with explicit plan files and checkboxes instead of treating the entire task as one flat edit.
For broad, long-running, or high-complexity work, prefer a master plan that coordinates a group of narrower plans in explicit sequence instead of treating the entire task as one flat block.
Use the master-plan pattern when it safely reduces unnecessary human interaction, increases automation, or makes a larger program auditable through one final validation pass.
Use the master-plan pattern when it safely reduces unnecessary human interaction, increases automation, or makes a larger program auditable through one final closeout pass.
Never mark a plan complete unless all of its checkboxes are complete, the required validation has passed or been explicitly skipped with a recorded reason, and the completion evidence matches the real state.
Never mark a batch complete unless the work it covers is actually implemented, the required validation has passed or been explicitly skipped with a recorded reason, and the completion evidence matches the real state.
Do not treat `docs/generated/local-tooling/.history/`, `docs/generated/local-tooling/.cache/`, or `.agents/archive/` as live closeout evidence; they are archive-only support paths.

## Program Planning Model

- Start broad work with analysis, then draft a master plan, then derive narrower plans with concrete checkboxes.
- The master plan holds the shared context, plan inventory, ordering, and final consistency review.
- Each plan covers one bounded slice and should list the actual implementation steps as checkboxes.
- When a broad task is safe to continue, do not stop between plans or ask for continuation unless a real blocker appears.
- Close a plan only when its checkboxes are complete and the evidence is real.
- Close the master plan only when every plan is complete.

For broad, long-running, or high-complexity work, prefer a sequenced batch with explicit slices instead of treating the entire task as one flat edit.
For broad, long-running, or high-complexity work, prefer a sequenced batch that coordinates a group of narrower implementation slices in explicit sequence instead of treating the entire task as one flat block.
Use the sequenced-batch pattern when it safely reduces unnecessary human interaction, increases automation, or makes a larger batch auditable through one final validation pass.
Use the sequenced-batch pattern when it safely reduces unnecessary human interaction, increases automation, or makes a larger batch auditable through one final closeout pass.

When `AGENTS.md` records a standing autonomous continuation preference, do not stop only to ask which safe offered follow-up slice should run next; continue with the best sequenced slice unless scope changes, approval is required, or a real blocker appears.
In a safe active batch, do not ask the user whether to continue between slices, phases, or follow-up passes; continue automatically through the full planned sequence and only stop for a real blocker, scope change, or required approval.
When the user asks for a broad safe batch, such as many improvements or an entire workstream, assemble the full safe slice list up front and execute it in order without asking after each slice, unless a real blocker, scope change, or required approval appears.

When `AGENTS.md` records the standing follow-up capture preference, record safe discovered improvements or repeated failure patterns in the appropriate follow-up or backlog surface during the active slice, then continue with the best sequenced follow-up slice after the current slice closes.

During a safe batch, do not stop after one or two phases just to ask whether to continue; carry the batch through all planned phases in sequence, record any safe follow-up items in the appropriate backlog during the same batch, and close the batch only after the final closeout pass.

During broad implementation work, review the product, control-system, and implementation-workflow layers before substantial edits, and capture the review in a temporary analysis artifact when the batch is broad or high-risk.

## Tier Decision

| Tier | Use When | Plan | Manifest | Validation | Closeout |
| --- | --- | --- | --- | --- | --- |
| `tier1-tiny-change` | one-file bugfix, rename, test-only tweak, docs wording, no business-rule or contract change | not required by default | not required by default | targeted only | `make audit-todo` |
| `tier2-normal-feature` | normal backend/frontend change in one bounded area, small DTO/API change, small business-rule update | short batch note recommended | resolver-driven | targeted, broaden only if risk requires | `make audit-todo` |
| `tier3-high-risk-multi-layer` | DB migration, contract change, generated artifacts, backend+frontend+docs, 3+ meaningful surfaces, high-risk refactor | required | required | targeted plus full required checks | full validation flow |
| `tier4-agent-tooling-workflow` | `AGENTS.md`, workflow docs, audit scripts, manifest workflow, validation evidence flow, generated operating-model changes | required for broad tooling batches | required | strict tooling and agent-safety validation | full validation flow |

## Tiny Change

Use this tier when all of these are true:

- no business-rule, permission, workflow, or state-transition change
- no DB migration
- no generated artifact change
- no frontend/backend contract change
- no agent/tooling/workflow behavior change

Suggested commands:

- `make codex-context topic=<topic> intent='<intent>'`
- `make recommend-targeted-tests`

Closeout:

- `make audit-todo`
- Final response must state what changed and what was validated.

## Normal Feature

Use this tier for bounded product work that stays inside one area and does not automatically trigger the full workflow.

Suggested commands:

- `make codex-context topic=<topic> intent='<intent>'`
- `make audit-router files=<csv>`
- `make audit-doc-sync-required-surfaces files=<csv>`
- `make audit-manifest-decision files=<csv>`
- `make recommend-validation-preset files=<csv>`

Closeout:

- `make audit-todo`
  - If manifest becomes required:
  `make validation-memory-closeout-card`
  `make audit-validation-memory-drift`
- After validation, update durable memory and backlog items in the same change so lessons and repeated patterns are captured immediately.

## High-Risk Or Multi-Layer Feature

Use this tier for:

- high-risk business logic
- invoice-critical behavior
- DB migrations
- frontend/backend contract changes
- generated artifact changes
- backend + frontend + docs changes
- changes touching 3 or more meaningful surfaces
- broad autonomous implementation

Suggested commands:

- `make codex-context topic=<topic> intent='<intent>'`
- `make audit-router files=<csv>`
- `make audit-doc-sync-required-surfaces files=<csv>`
- `make audit-manifest-decision files=<csv>`
- `make resolve-manifest-path files=<csv>`
- `make recommend-validation-preset files=<csv>`

Validation evidence:

- `make record-validation manifest=<manifest-file> command='<command>'`
- `ruby scripts/audits/record-validation-evidence.rb manifest=<manifest-file> mode=generated_artifact path=<csv> summary='<summary>'`
- `ruby scripts/audits/record-validation-evidence.rb manifest=<manifest-file> mode=skipped_check check='<check>' reason='<reason>'`
- `make clean-text-noise max_lines=80` to normalize raw command output before turning it into a summary.

Closeout:

- `make cleanup-generated-history`
- `make audit-todo`
- `make audit-validation-evidence-quality`
- `make validation-memory-closeout-card`
- `make audit-validation-memory-drift`

## Agent Or Workflow Change

This tier is intentionally strict.

Use it for:

- `AGENTS.md`
- `docs/codex-fast-path.md`
- `docs/feature-delivery-workflow.md`
- `docs/documentation-sync-policy.md`
- `docs/change-completion-checklist.md`
- audit scripts
- validation evidence workflow
- manifest workflow
- generated agent-operating-model artifacts

Expected flow:

- sequenced batch if broad
- durable batch analysis record update if the work changes a program spanning several related batches
- manifest required
- docs sync required
- generated artifacts and validation test required when machine-operational rules change
- full closeout required

## Manifest Decision

Manifest required by default for:

- high-risk business logic
- invoice-critical behavior
- DB migrations
- frontend/backend contract changes
- generated artifact changes
- agent/tooling/workflow changes
- changes touching 3 or more meaningful implementation or documentation surfaces
- broad autonomous changes
- changes where `make audit-manifest-decision` says `required`

Manifest optional by default for:

- single backend service change
- single frontend component change
- small bugfix
- small test-only change
- small docs-only correction
- small internal refactor without behavior, contract, DB, generated-artifact, or agent-safety impact

If the decision is not obvious, treat it as resolver-driven and run:

- `make audit-manifest-decision files=<csv>`
- `make resolve-manifest-path files=<csv>`

## Final Response

Every final response must state:

- what changed
- what was validated
- any remaining risks or not-run checks

Do not claim completion while validation, required docs, or required closeout gates remain open.

## Deeper References

- `docs/feature-delivery-workflow.md`: complete human-readable process
- `docs/feature-delivery-workflow.md`: end-to-end implementation workflow and batch sequencing guidance
- `docs/validation-memory.md`: canonical validator-facing commands, manifest heuristics, and closeout evidence patterns
- `docs/validation-memory.json`: machine-readable validation and closeout cheat sheet for local tools
- `docs/generated/local-tooling/validation-memory-closeout-card-summary.md`: ultra-short closeout command card derived from validation memory
- `docs/documentation-sync-policy.md`: propagation and manifest rules
- `docs/change-completion-checklist.md`: closeout checklist by tier and the preflight gate before broad validation
- `docs/agent-operating-model.md`: human agent-safety contract
- `docs/agent-operating-model.yaml`: machine-operational contract
