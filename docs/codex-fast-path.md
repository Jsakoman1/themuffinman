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
   - `make control-start` when you want the current plan and audit discovery state in one compact snapshot before broader search
   - `make control-refresh-full` when you want the same compact snapshot plus the slower generated-artifact freshness pass
   - `make implementation-batch topic=<topic>` when you want the deterministic implementation wrapper to run discovery, context, targeted recommendations, and closeout if a plan exists
   - `make codex-context topic=<topic> intent='<intent>'`
   - `make recommend-targeted-tests`
   - `make clean-text-noise max_lines=80` when you need to strip Maven, audit, or generated log noise before summarizing evidence.
   - `make codex-context` now uses the diff summary, audit summary index, the most relevant audit, targeted tests, and a concise evidence bundle as its default read chain, writes a canonical execution manifest at `docs/generated/local-tooling/codex-context/latest.execution.json` with schema `docs/codex-context-execution-manifest.schema.json`, and auto-includes validation memory when the batch is manifest-backed or closeout-sensitive.
   - `make control-start`, `make codex-context`, and `make context-pack` surface the topic's layered-analysis artifact and temp work-product inventory when they exist.
7. Load deeper workflow docs only if the chosen tier or a resolver requires them.

For `/vision` work, the compact context should usually be opened after the vision memory set so the repo search starts from the right backend, API, frontend, test, and doc surfaces.

When the task spans several existing master plans or long-running program directions, read `docs/program-planning-model.md`, `docs/generated/local-tooling/plan-index-summary.md`, and the relevant `.agents/god-plans/*.yaml` file before creating or modifying child master plans.

When `AGENTS.md` records a standing autonomous continuation preference, do not stop to ask the user which safe offered follow-up slice to pick; choose the best sequenced next slice and continue until scope is narrowed, approval is needed, or a real blocker appears.

When `AGENTS.md` records the standing follow-up capture preference, record safe discovered improvements or repeated failure patterns in the appropriate follow-up or backlog surface during the active slice, then continue with the best sequenced follow-up slice after the current slice closes.

During a safe master-plan or plan batch, do not stop after one or two phases just to ask whether to continue; carry the batch through all planned phases in sequence, record any safe follow-up items in the appropriate backlog during the same batch, and close the plan only after the final closeout pass.

During broad implementation work, review the product, control-system, and implementation-workflow layers before substantial edits, and capture the review in a temporary analysis artifact when the batch is broad or high-risk.

## Tier Decision

| Tier | Use When | Plan | Manifest | Validation | Closeout |
| --- | --- | --- | --- | --- | --- |
| `tier1-tiny-change` | one-file bugfix, rename, test-only tweak, docs wording, no business-rule or contract change | not required by default | not required by default | targeted only | `make audit-todo` |
| `tier2-normal-feature` | normal backend/frontend change in one bounded area, small DTO/API change, small business-rule update | short plan required | resolver-driven | targeted, broaden only if risk requires | `make audit-todo`, `make audit-plan-completion` |
| `tier3-high-risk-multi-layer` | DB migration, contract change, generated artifacts, backend+frontend+docs, 3+ meaningful surfaces, high-risk refactor | required | required | targeted plus full required checks | full closeout flow |
| `tier4-agent-tooling-workflow` | `AGENTS.md`, workflow docs, audit scripts, manifest workflow, validation evidence flow, generated operating-model changes | required, master plan if broad | required | strict tooling and agent-safety validation | full closeout flow |

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

- `make bootstrap-feature-work topic=<short-topic> mode=normal`
- `make codex-context topic=<topic> intent='<intent>'`
- `make audit-router files=<csv>`
- `make audit-doc-sync-required-surfaces files=<csv>`
- `make audit-manifest-decision files=<csv>`
- `make recommend-validation-preset files=<csv>`

Closeout:

- `make audit-todo`
- `make audit-plan-completion plan=<plan-file>`
- If manifest becomes required:
  `make autofill-feature-closeout manifest=<manifest-file> files=<csv>`
  `make validation-memory-closeout-card`
  `make feature-closeout-audit manifest=<manifest-file>`
- After closeout, run `make post-plan-memory-update plan=<plan-file> [manifest=<manifest-file>] [source=<diagnostic-report>]` so durable lessons and failure patterns are captured immediately.

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

- `make bootstrap-feature-work topic=<short-topic> risk=high mode=feature`
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

- `make autofill-feature-closeout manifest=<manifest-file> files=<csv> generated=<csv> docs=<csv>`
- `make audit-todo`
- `make audit-plan-completion plan=<plan-file> manifest=<manifest-file>`
- `make audit-validation-evidence-quality`
- `make validation-memory-closeout-card`
- `make feature-closeout-audit manifest=<manifest-file>`
- `make closeout-report manifest=<manifest-file>`

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

- master plan if broad
- God Plan update if the work changes a program spanning several master plans
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
- `docs/program-planning-model.md`: God Plan, Master Plan, Plan, and temporary work product hierarchy
- `docs/validation-memory.md`: canonical validator-facing commands, manifest heuristics, and closeout evidence patterns
- `docs/validation-memory.json`: machine-readable validation and closeout cheat sheet for local tools
- `docs/generated/local-tooling/validation-memory-closeout-card-summary.md`: ultra-short closeout command card derived from validation memory
- `docs/documentation-sync-policy.md`: propagation and manifest rules
- `docs/change-completion-checklist.md`: closeout checklist by tier and the preflight gate before broad validation
- `docs/agent-operating-model.md`: human agent-safety contract
- `docs/agent-operating-model.yaml`: machine-operational contract
