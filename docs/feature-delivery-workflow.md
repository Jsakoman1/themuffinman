# Feature Delivery Workflow

This document is the canonical human-readable workflow for implementing a feature in this repository with Codex and the local tooling stack.

`docs/codex-fast-path.md` is the compact execution entrypoint for most feature work.

Use the full workflow only when the change is high-risk, multi-layer, agent/tooling/workflow-related, or when a resolver requires it.

Manifest usage is tier-driven and conditional instead of being the default for every non-trivial backend change.

## Purpose

- keep one maintained end-to-end process for feature delivery
- preserve strong closeout and documentation guarantees for high-risk work
- let small and normal changes use a smaller startup path
- read machine-readable control and validation sources first when the task touches active behavior, workflow state, or
  automation-facing facts; use human-readable docs as the next layer of context or explanation

## Program Planning Model

Broad work uses a master plan plus one or more narrower plans.

- The master plan holds the shared context, ordering, and final consistency review.
- Each plan covers one executable slice and uses concrete checkboxes for the actual implementation steps.
- Each checkbox should represent one verifiable action, not a vague reminder.
- The first pass should capture analysis, ideas, and local audit findings before the master plan is drafted.
- The second pass should use local audits, docs, vision architecture, and source-of-truth surfaces to split the master plan into concrete plans.
- The third pass should review the master plan and every plan together before implementation starts.
- During safe execution, do not ask for continuation between plans or slices; continue through the full ordered sequence unless a real blocker appears.
- Close each plan only when all its checkboxes are complete and evidence is recorded.
- Close the master plan only when every plan is complete and the final consistency review passes.

For broad, long-running, or high-complexity work, prefer a sequenced batch with explicit slices instead of treating the entire task as one flat edit.
For broad, long-running, or high-complexity work, prefer a sequenced batch that coordinates a group of narrower implementation slices in explicit sequence instead of treating the entire task as one flat block.
Use the sequenced-batch pattern when it safely reduces unnecessary human interaction, increases automation, or makes a larger batch auditable through one final validation pass.
Use the sequenced-batch pattern when it safely reduces unnecessary human interaction, increases automation, or makes a larger batch auditable through one final closeout pass.

## Workflow Tiers

### Tier 1: Tiny change

Use for:

- one-file bugfix
- small rename
- small validation fix
- small test-only change
- small documentation wording correction

Guardrails:

- no business-rule, permission, workflow, or state-transition change
- no generated artifact change
- no frontend/backend contract change
- no DB migration
- no agent/tooling/workflow behavior change

Default flow:

1. Read `AGENTS.md`.
2. Read `docs/codex-fast-path.md`.
3. Run compact context:
   - `make codex-context topic=<topic> intent='<intent>'`
   - `make recommend-targeted-tests`
   - `make audit-summary-index`
   - `make audit-doc-sync-required-surfaces files=<csv>` when doc and generated-artifact scope matters
4. Run only targeted validation.
5. Run `make audit-todo`.
6. Final response states what changed and what was validated.

Manifest:

- not required by default
- if `make audit-manifest-decision files=<csv>` says `required`, switch to the matching heavier tier

### Tier 2: Normal feature

Use for:

- normal backend service or use-case change
- normal frontend component or screen change
- small DTO or API adjustment
- small business-rule change with tests
- small multi-file change inside one bounded area

Default flow:

1. Read `AGENTS.md`.
2. Read `docs/codex-fast-path.md`.
3. If the work is manifest-backed or validator-sensitive, read `docs/validation-memory.md` and `docs/validation-memory.json`.
4. Record a short implementation note or temporary analysis artifact if the slice is broad enough to need one.
5. Run compact context and routing:
   - `make codex-context topic=<topic> intent='<intent>'`
   - `make audit-router files=<csv>`
   - `make audit-doc-sync-required-surfaces files=<csv>`
   - `make audit-manifest-decision files=<csv>`
   - `make recommend-validation-preset files=<csv>`
6. Update only the docs and generated artifacts that the resolver surface requires.
7. Run targeted validation first and broaden only if risk or profile requires it.
8. Run `make audit-todo` and `make audit-validation-memory-drift`.

Manifest:

- optional by default
- required only when the resolver or the actual change scope triggers the heavier rules

### Tier 3: High-risk or multi-layer feature

Use for:

- high-risk business logic
- invoice-critical behavior
- DB migrations
- frontend/backend contract changes
- generated artifact changes
- backend + frontend + docs changes
- changes touching 3 or more meaningful surfaces
- high-risk refactors
- broad autonomous implementation

Full flow is mandatory:

1. Read `AGENTS.md`, `docs/codex-fast-path.md`, this document, and `docs/validation-memory.md`.
2. Draft or refresh the master plan and the narrower plan files before substantial edits when the work spans multiple slices.
3. Create or resolve the manifest path when the change is manifest-backed.
4. Run compact context and routing:
   - `make codex-context topic=<topic> intent='<intent>'`
   - `make audit-router files=<csv>`
   - `make audit-doc-sync-required-surfaces files=<csv>`
   - `make audit-manifest-decision files=<csv>`
   - `make resolve-manifest-path files=<csv>`
   - `make recommend-validation-preset files=<csv>`
5. Implement in ordered plan files and keep each plan checkbox-driven:
   - first backend plan when backend is in scope
   - first frontend plan when frontend is in scope
   - docs and generated-artifact sync
   - validation and evidence preparation
6. Record validation evidence as commands run.
   - Use `make clean-text-noise max_lines=80` when raw build or audit output is too noisy for a concise evidence summary.
7. Prune archive-only generated history before the final validation pass.
8. Run the required validation and docs-sync audits before the final response.

When `AGENTS.md` records a standing autonomous continuation preference, do not stop only to ask which safe offered follow-up slice should run next; continue with the best sequenced slice unless scope changes, approval is required, or a real blocker appears.
In a safe active batch, do not ask the user whether to continue between slices or follow-up passes; continue automatically through the full planned sequence and only stop for a real blocker, scope change, or required approval.
For broad, long-running, or high-complexity work, prefer a master plan with explicit plan files and checkboxes instead of treating the entire task as one flat edit.
Use the sequenced-batch pattern when it safely reduces unnecessary human interaction, increases automation, or makes a larger batch auditable through one final validation pass.
Never mark a batch complete unless the work it covers is actually implemented, the required validation has passed or been explicitly skipped with a recorded reason, and the completion evidence matches the real state.
When the user asks for a broad safe batch, such as many improvements or an entire workstream, assemble the full safe slice list up front and execute it in order without asking after each slice, unless a real blocker, scope change, or required approval appears.

If `AGENTS.md` records the standing follow-up capture preference, record discovered safe improvements, likely next slices, and repeated failure patterns in the active follow-up or backlog surface during the current slice, then continue with the best sequenced follow-up slice after current validation finishes.

During a safe batch, do not stop after one or two phases just to ask whether to continue; carry the batch through all planned phases in sequence, record any safe follow-up items in the appropriate backlog during the same batch, and close the batch only after the final validation pass.

During broad implementation work, review the product, control-system, and implementation-workflow layers before substantial edits, and capture the review in a temporary analysis artifact when the batch is broad or high-risk.

Typical evidence helpers:

- `make record-validation manifest=<manifest-file> command='<command>'`
- `ruby scripts/audits/record-validation-evidence.rb manifest=<manifest-file> mode=generated_artifact path=<csv> summary='<summary>'`
- `ruby scripts/audits/record-validation-evidence.rb manifest=<manifest-file> mode=skipped_check check='<check>' reason='<reason>'`

Required validation:

- `make cleanup-generated-history`
- `make audit-todo`
- `make audit-validation-evidence-quality`
- `make validation-memory-closeout-card`
- `make audit-validation-memory-drift`
- Archive-only paths under `docs/generated/local-tooling/.history/`, `docs/generated/local-tooling/.cache/`, and `.agents/archive/` must not be recorded as live evidence.

### Tier 4: Agent, tooling, or workflow change

Use for:

- `AGENTS.md`
- `docs/codex-fast-path.md`
- `docs/feature-delivery-workflow.md`
- `docs/documentation-sync-policy.md`
- `docs/change-completion-checklist.md`
- audit scripts
- validation evidence workflow changes
- manifest workflow changes
- generated agent-operating-model artifacts
- changes that affect future Codex behavior

This tier is intentionally strict:

- sequenced batch required when broad
- manifest required
- docs sync required
- generated artifacts required when machine-operational rules change
- validation test required
- full validation required

## End-To-End Flow

1. Prompt intake

- read `AGENTS.md`
- route into the smallest safe workflow tier
- treat the prompt as an intent signal, not as sufficient implementation context

2. Compact context first

- `make diff-summary`
- `make audit-summary-index`
- `make codex-context topic=<topic> intent='<intent>'`
- `make audit-doc-sync-required-surfaces files=<csv>` when doc and generated-artifact scope matters
- `make codex-context topic=<topic> intent='<intent>'` for the main context chain.
- `make context-pack topic=<topic>` only when you need a broader topic slice beyond the one-shot context chain
- `make codex-context budget=<tokens> mode=<mode> topic=<topic> intent='<intent>'` keeps the same chain but lets you tune the budget
- `make codex-context` also writes `docs/generated/local-tooling/codex-context/latest.execution.json`, the canonical machine-readable batch manifest for read order, evidence, and next actions, with schema `docs/codex-context-execution-manifest.schema.json`.
- `make cleanup-generated-history` prunes archive-only generated local-tooling history to the configured retention window before closeout-sensitive review.
- `make codex-context` and `make context-pack` surface the topic's layered-analysis artifact and temp work-product inventory when they exist.
- Treat the operator-core local-tooling surfaces as the default session path: `audit-summary-index`, `codex-context/latest.*`, and targeted-tests summaries.
- Treat `docs/generated/local-tooling/.history/` and `docs/generated/local-tooling/.cache/` as archive-only support material instead of live control state.
- When the resolver shape is manifest-backed or closeout-sensitive, `make codex-context` should also surface validation memory so command and evidence expectations are present before the first closeout pass.
- If the task touches active control state, planning state, or automation-facing rules, open the machine-readable source
  surfaces listed in `docs/control-surface-map.md` before broad narrative docs.
- For product-direction, UX, interaction design, or Social Useful Network vision work, start by reading `docs/product-memory.md` and `docs/product-vision.md` before broadening into implementation docs.
- For `/vision` implementation work, read `docs/vision-architecture-patterns.md` before backend orchestration, API, frontend canvas, prompt-handling, or executor decisions.
- For broad work that spans several related slices, capture the sequencing in a temporary analysis artifact before changing code and keep the batch scoped to explicit slices.
- For broad, long-running, or high-complexity work, prefer a master plan that coordinates a group of narrower plans in explicit sequence instead of treating the entire task as one flat block.
- Use the master-plan pattern when it safely reduces unnecessary human interaction, increases automation, or makes a larger program auditable through one final closeout pass.

3. Focused routing and required-surface resolution

- `make audit-router files=<csv>`
- `make audit-doc-sync-preflight files=<csv>`
- `make audit-doc-sync-required-surfaces files=<csv>`
- `make audit-manifest-decision files=<csv>`
- `make resolve-manifest-path files=<csv>`
- `make recommend-validation-preset files=<csv>`

These resolvers answer:

- which living docs are required
- which generated artifacts are expected to move
- whether a manifest is required
- which validation commands are the deterministic baseline
- which closeout gates are required

4. Implementation slices

- keep controllers thin
- keep frontend logic minimal
- keep backend rules in services and use cases
- use forward-only migrations
- validate the first meaningful slice before widening scope
- temporary machine-readable work products may live under `.agents/tmp/` only while their owning plan needs them, and must be deleted, promoted, or explicitly archived at plan closeout
- `make temp-work-product-closeout plan=<plan-file>` deletes or archives lingering temp work products owned by a plan before closeout
- `make audit-generated-artifact-hygiene files=<csv>` gives a scope-filtered generated-artifact report before the global freshness check

5. Documentation synchronization

No logic-only change is complete until the affected docs, agent artifacts, and validation tests are updated together.

Use resolver outputs instead of guessing propagation scope from memory.

6. Validation and evidence

- validation should be targeted first and broaden only when the tier, risk, or profile requires it
- record exact commands, scope, and skipped-check reasons when manifests or validation evidence are in scope
- when a validator expects canonical command strings such as `npm run validate:contracts` or `make audit-agent-safety`, record those exact strings in the manifest evidence instead of only path-prefixed equivalents
- the validation-memory JSON overlay should only add canonical command reminders; it must not weaken existing preset or closeout requirements

7. Final closeout

- tiny changes: `make audit-todo`
- normal features: `make audit-todo` and `make audit-validation-evidence-quality`
- `make audit-validation-evidence-quality` reports evidence gaps so closeout can clean them up before completion.
- `make audit-generated-artifact-hygiene files=<csv>` reports only the generated-artifact noise that overlaps the batch scope before the wider freshness pass.
- manifest-backed work: run the full closeout bundle, including `make validation-memory-closeout-card` and the validation-memory drift sub-check inside `make feature-closeout-audit`
- In a safe active batch, do not ask the user whether to continue between slices, phases, or follow-up passes; continue automatically through the full planned sequence and only stop for a real blocker, scope change, or required approval.
- During a safe batch, do not stop after one or two phases just to ask whether to continue; carry the batch through all planned phases in sequence, record any safe follow-up items in the appropriate backlog during the same batch, and close the batch only after the final closeout pass.

8. Post-plan memory update

- If the plan produced a stable lesson, append it to `docs/product-memory.md`.
- If the plan exposed a repeatable failure pattern, refresh `failure-knowledge-base`.
- Run `make post-plan-memory-update plan=<plan-file> [manifest=<manifest-file>] [source=<diagnostic-report>]` to trigger the standard memory and control loop after closeout.

9. Final response

The final response must state:

- what changed
- what was validated
- any remaining risks or not-run checks

## Supporting Local Tools

Compact context:

- `make codex-context`
- `make codex-context-explain`
- `make diff-summary`
- `make audit-summary-index`
- `make context-pack topic=<topic>`

Routing:

- `make audit-router files=<csv>`
- `make audit-doc-sync-required-surfaces files=<csv>`
- `make audit-manifest-decision files=<csv>`
- `make resolve-manifest-path files=<csv>`
- `make recommend-validation-preset files=<csv>`

Closeout and evidence:

- `make record-validation manifest=<manifest-file> command='<command>'`
- `make autofill-feature-closeout manifest=<manifest-file>`
- `make audit-validation-evidence-quality`
- `make audit-validation-evidence-quality`
- `make validation-memory-closeout-card`
- `make feature-closeout-audit manifest=<manifest-file>`
- `make validation-memory-closeout-card`
- `docs/validation-memory.md`
- `docs/validation-memory.json`

## Maintenance Rule

If the implementation workflow, planning workflow, context gateway workflow, evidence capture path, manifest workflow, closeout commands, or tier decision rules change, update:

- `docs/codex-fast-path.md`
- `docs/feature-delivery-workflow.md`
- `docs/documentation-sync-policy.md`
- `docs/change-completion-checklist.md`
- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml` when machine-operational rules changed
- `AGENTS.md` when startup behavior changed
