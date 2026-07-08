# Documentation Sync Policy

This file defines how logical code changes must propagate into living documentation and agent-safety artifacts.

`docs/codex-fast-path.md` is the compact execution entrypoint for most feature work.

Use the full workflow only when the change is high-risk, multi-layer, agent/tooling/workflow-related, or when a resolver requires it.

Manifest usage is tier-driven and conditional instead of being the default for every non-trivial backend change.

## Goal

- prevent silent drift between logic and docs
- preserve strong closeout for high-risk and workflow changes
- avoid forcing every small task through the heaviest workflow
- make machine-readable operational sources the first place to look for active state, with human docs serving as
  canonical meaning layers or curated summaries where appropriate

## Core Rules

- A logic change is not complete when only code and tests are updated.
- When business rules, domain models, permissions, validations, workflows, endpoint contracts, or automation assumptions change, update all affected living docs in the same change.
- No logic-only change is complete until the affected docs, agent artifacts, and validation tests are updated together.
- For active control state, planning state, validation state, and automation-facing rules, treat machine-readable files as
  the primary source of truth and markdown as supporting narrative unless the markdown file is explicitly the canonical
  business or domain reference.
- Generated review artifacts are review aids for the machine-readable source; they should stay compact and should not
  become the place where current control state is maintained.
- For protected documentation-sync phrases, copy the exact canonical sentence verbatim into every required file.
- Do not paraphrase, shorten, reorder, or partially restate protected canonical wording.
- If a change introduces or expands entity capabilities, generation inputs, workflow branches, or edge cases that matter for admin or sandbox data generation, review and extend affected admin or sandbox generation flows in the same change.
- Treat admin-generation or sandbox-generation coverage for entities and workflows as part of the same maintenance surface as backend logic and agent-safety rules.
- synthetic admin-generation flows must be kept current with newly introduced feature logic, validations, and edge cases

## Workflow Entry

- Read `AGENTS.md` first.
- Use `docs/codex-fast-path.md` as the default compact workflow entrypoint for feature implementation.
- When a task touches active behavior or control state, start from the machine-readable source surfaces listed in
  `docs/control-surface-map.md` before broadening into human-readable summaries.
- Use `docs/validation-memory.md` and `docs/validation-memory.json` when manifest-backed validation or closeout evidence is in scope so canonical command and evidence rules are applied consistently.
- Use `make control-start` as the one-shot broad-work snapshot before broad repository search.
- Use `make control-refresh-full` when the broad snapshot must also include the slower generated-artifact freshness pass.
- Use `make implementation-batch topic=<topic>` when you want the deterministic implementation wrapper to drive discovery, docs-sync preflight, manifest routing, validation preset selection, and closeout if a plan exists.
- make implementation-batch topic=<topic> runs the deterministic implementation wrapper for discovery, docs-sync preflight, manifest routing, validation preset selection, recommendations, and closeout hints when a plan exists.
- Use `make codex-context topic=<topic> intent='<intent>'` when the task needs topic-specific file context after the broad snapshot.
- `make control-start`, `make codex-context`, and `make context-pack` should surface the topic's layered-analysis artifact and temp work-product inventory when they exist.
- When `codex-context` changes, keep the workflow docs, `docs/generated/local-tooling/codex-context/latest.execution.json`, and `docs/codex-context-execution-manifest.schema.json` aligned so the machine-readable batch manifest remains discoverable.
- Use `docs/feature-delivery-workflow.md` only when the tier or resolver requires the full workflow.

## Tier Policy

### Tier 1: Tiny change

Default expectations:

- no manifest by default
- no long feature plan by default
- compact context only
- targeted validation only
- final response states what changed and what was validated

Required closeout:

- `make audit-todo`

### Tier 2: Normal feature

Default expectations:

- short plan required
- manifest optional, based on resolver decision
- docs sync only for affected required surfaces
- targeted validation required
- broader validation only if risk requires it

Required closeout:

- `make audit-todo`
- `make audit-plan-completion plan=<plan-file>`

### Tier 3: High-risk or multi-layer feature

Default expectations:

- plan required
- manifest required
- docs sync required according to resolver outputs
- generated artifact sync required when affected
- validation evidence required
- full closeout audit required

### Tier 4: Agent, tooling, or workflow change

Default expectations:

- plan required, master plan if broad
- manifest required
- docs sync required
- generated artifacts required when machine-operational rules change
- validation test required
- full closeout audit required

## Manifest Rule

Manifest required for:

- high-risk business logic
- invoice-critical behavior
- DB migrations
- frontend/backend contract changes
- generated artifact changes
- agent/tooling/workflow changes
- changes touching 3 or more meaningful implementation or documentation surfaces
- broad autonomous changes
- changes where `make audit-manifest-decision` says `required`

Manifest optional for:

- single backend service change
- single frontend component change
- small bugfix
- small test-only change
- small docs-only correction
- small internal refactor without behavior, contract, DB, generated-artifact, or agent-safety impact

If a non-trivial change does not use a manifest, the temporary plan or final closeout must record a one-line skipped-with-reason decision.

## Plan Rule

- For multi-file, multi-layer, or high-risk logical changes, create a temporary implementation plan in `.agents/` before substantial edits.
- Prefer `make bootstrap-feature-work` when the change is large enough to justify the plan-driven workflow.
- Use `docs/program-planning-model.md` when work spans several master plans or durable program directions.
- God Plans live under `.agents/god-plans/` and coordinate related Master Plans without replacing child plan completion evidence.
- For broad, long-running, or high-complexity work, prefer a master plan that coordinates a group of narrower `.agents/*-plan.md` files in explicit sequence instead of treating the entire task as one flat plan.
- Use the master-plan pattern when it safely reduces unnecessary human interaction, increases automation, or makes a larger batch auditable through one final closeout pass.
- Never mark a plan, child plan, or master plan `complete` unless the work it covers is actually implemented, the required validation has passed or been explicitly skipped with a recorded reason, and the completion evidence matches the real state.
- Temporary machine-readable work products live under `.agents/tmp/`, must name their owning plan, and must be deleted, promoted into durable docs, or explicitly archived when the owning plan closes.
- Plan closeout should also fail if the owning plan still has undeleted temp work products at completion time.
- Use `make temp-work-product-closeout plan=<plan-file>` to deterministically delete or archive temp work products owned by a plan before closeout.
- Use `make audit-generated-artifact-hygiene files=<csv>` for batch-scoped generated-artifact noise checks before widening to the global freshness audit.
- When `AGENTS.md` records a standing autonomous continuation preference, do not stop only to ask which safe offered follow-up slice should run next; continue with the best sequenced slice unless scope changes, approval is required, or a real blocker appears.
- In a safe active master plan, do not ask the user whether to continue between child slices, phases, or follow-up passes; continue automatically through the full planned sequence and only stop for a real blocker, scope change, or required approval.
- When the user asks for a broad safe batch, such as many improvements or an entire workstream, assemble the full safe slice list up front and execute it in order without asking after each slice, unless a real blocker, scope change, or required approval appears.
- When `AGENTS.md` records the standing follow-up capture preference, record discovered safe improvements and repeated failure patterns in the appropriate follow-up or backlog surface during the active slice and continue with the best sequenced follow-up slice after the current slice closes.
- During a safe master-plan or plan batch, do not stop after one or two phases just to ask whether to continue; carry the batch through all planned phases in sequence, record any safe follow-up items in the appropriate backlog during the same batch, and close the plan only after the final closeout pass.
- During broad implementation work, review the product, control-system, and implementation-workflow layers before substantial edits, and capture the review in a temporary analysis artifact when the batch is broad or high-risk.

## Backlog Rule

- Record new deferred implementation or control-system work in the appropriate persistent backlog with a stable ID before closing the change that discovered it.
- When a backlog item is implemented, remove it from the open backlog and clear matching inline `TODO(<ID>):` or `FIXME(<ID>):` references in the same change.

## Validation And Closeout

- Tiny changes usually stop at `make audit-todo`.
- Normal features must close the short plan through `make audit-plan-completion`.
- Manifest-backed work must pass `make feature-closeout-audit manifest=<manifest-file>`.
- Completed manifest-backed changes must keep structured validation evidence for every required profile check, completed plan evidence, and a ready closeout decision.
- Validation evidence must name exact commands, scopes, `ranAt`, generated-artifact actions, and concrete skipped-check reasons, and pass `make audit-validation-evidence-quality`.
- Manifest-backed closeout should refresh `make validation-memory-closeout-card`, and `make feature-closeout-audit` should also rerun the validation-memory drift sub-check.
- Manifest-backed closeout summaries can be generated with `make closeout-report manifest=<manifest-file>`.
- Completed master plans must not keep child rows marked `pending`, `draft`, or `in_progress`; `make audit-plan-completion` treats that as a closeout failure.

## Maintenance Propagation

If the implementation workflow, planning workflow, context gateway workflow, evidence capture path, manifest workflow, closeout commands, or tier decision rules change, update:

- `docs/codex-fast-path.md`
- `docs/feature-delivery-workflow.md`
- `docs/validation-memory.md`
- `docs/validation-memory.json`
- `docs/validation-memory.schema.json`
- `docs/program-planning-model.md`
- `docs/documentation-sync-policy.md`
- `docs/change-completion-checklist.md`
- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml` when machine-operational rules changed
- `AGENTS.md` when startup behavior changed

## Mandatory Files

For workflow, manifest, closeout, or startup-routing changes, review and update as needed:

- `AGENTS.md`
- `docs/codex-fast-path.md`
- `docs/feature-delivery-workflow.md`
- `docs/documentation-sync-policy.md`
- `docs/change-completion-checklist.md`
- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml`

For logical product changes, continue reviewing the normal living docs as needed:

- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/product-vision.md` when long-term product direction or interaction principles change
- `docs/vision-architecture-patterns.md` when `/vision` backend orchestration, API contract, frontend canvas, prompt-handling, or executor patterns change
- `docs/product-memory.md` when product-direction sessions discover a durable lesson, recurring pattern, or stable product principle
- `docs/implementation-backlog.md`
- `docs/agent-improvement-backlog.md` when deferred control-system work changes
- `docs/feature-completion-manifest.schema.json`
- `docs/validation-evidence.schema.json`
- `docs/regression-scenario-catalog.md`
- `docs/regression-scenario-catalog.yaml`
- `docs/docs-as-contract-slices.md`
- `docs/docs-as-contract-slices.yaml`

## Mandatory Tests

For logical product changes, keep passing:

- `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`
- domain tests that cover the changed behavior

Use `docs/change-completion-checklist.md` as the fast review layer, but keep this policy and `AGENTS.md` as the stronger source rules.

## Current Protected Rules

- Pending circle requests do not create chat eligibility.
- Chat workspace only includes current accepted circle contacts.
- Existing conversation history does not preserve chat access after the accepted relation is lost.
