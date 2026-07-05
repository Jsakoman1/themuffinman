# Change Completion Checklist

This file is the fast operational completion checklist for logical product changes.

`docs/codex-fast-path.md` is the compact execution entrypoint for most feature work.

Use the full workflow only when the change is high-risk, multi-layer, agent/tooling/workflow-related, or when a resolver requires it.

For `codex-context` and related workflow changes, confirm that `docs/generated/local-tooling/codex-context/latest.execution.json` exists, matches the current batch state, and still conforms to `docs/codex-context-execution-manifest.schema.json` before closeout.

`make control-start` now stays on the fast path, while `make control-refresh-full` regenerates plan-index, audit registry, codex-context, audit summary index, control-start, and freshness outputs after successful plan completion.
Use `make temp-work-product-closeout plan=<plan-file>` when you need to delete or archive lingering temp work products owned by a plan before closeout.
Use `make audit-generated-artifact-hygiene files=<csv>` when a batch needs generated-artifact noise filtered to the implementation scope before consulting the global freshness audit.

Manifest usage is tier-driven and conditional instead of being the default for every non-trivial backend change.

It does not replace `AGENTS.md`, `docs/documentation-sync-policy.md`, or the agent-operating model.

For broad, long-running, or high-complexity work, prefer a master plan that coordinates a group of narrower `.agents/*-plan.md` files in explicit sequence instead of treating the entire task as one flat plan.

Use the master-plan pattern when it safely reduces unnecessary human interaction, increases automation, or makes a larger batch auditable through one final closeout pass.

For work that spans several master plans, use `docs/program-planning-model.md` and keep the relevant `.agents/god-plans/*.yaml` file aligned with the active Master Plans, Plans, pros, cons, decisions, and risks.

## Pick The Tier First

### Tier 1: Tiny change

Use for:

- one-file bugfix
- small rename
- small validation fix
- small test-only change
- small docs wording correction

Default path:

- no manifest by default
- no long plan by default
- compact context only
- targeted validation only
- `make audit-todo`

### Tier 2: Normal feature

Use for:

- normal backend service or use-case change
- normal frontend component or screen change
- small DTO or API adjustment
- small business-rule change with tests
- small multi-file change inside one bounded area

Default path:

- short plan required
- manifest optional, based on resolver decision
- targeted validation required
- `make audit-todo`
- `make audit-plan-completion plan=<plan-file>`

### Tier 3: High-risk or multi-layer feature

Use for:

- high-risk business logic
- invoice-critical behavior
- DB migration
- frontend/backend contract change
- generated artifact change
- backend + frontend + docs change
- changes touching 3 or more meaningful surfaces
- broad autonomous implementation

Default path:

- plan required
- manifest required
- docs and generated artifact sync required when affected
- validation evidence required
- full closeout required

### Tier 4: Agent, tooling, or workflow change

Use for:

- `AGENTS.md`
- `docs/codex-fast-path.md`
- `docs/feature-delivery-workflow.md`
- `docs/documentation-sync-policy.md`
- `docs/change-completion-checklist.md`
- audit scripts
- validation evidence workflow
- manifest workflow
- generated agent-operating-model artifacts

Default path:

- plan required, master plan if broad
- manifest required
- docs sync required
- machine-operational validation required when affected
- full closeout required

## Checklist

1. Code
- Backend or frontend behavior is implemented.
- Duplicate or stale logic introduced by the change is removed or explicitly justified.

2. Preflight
- The scope is narrowed to the smallest affected semantic path before broad validation starts.
- The semantic envelope, resolver layer, and DTO mapping path are all updated together when the change affects input understanding.
- Any required docs or generated artifacts are identified before the first closeout pass.
- If the change touches entity resolution, capture the canonical aliases, ambiguity cases, and confidence thresholds up front.
- If the change touches workflow or agent behavior, confirm the machine-readable source of truth before editing human-readable summaries.

3. Plan
- Tier 2, Tier 3, and Tier 4 work has a current plan in `.agents/`.
- Broad work uses a master plan plus child plans when that keeps the batch auditable.
- Program-level work that spans several master plans has a God Plan under `.agents/god-plans/`.
- Temporary machine-readable work products under `.agents/tmp/` name their owning plan and have a closeout deletion, promotion, or archive decision.
- `make audit-plan-completion` should surface lingering temp work products owned by the plan so closeout can clear them explicitly.
- When `AGENTS.md` records a standing autonomous continuation preference, do not stop only to ask which safe offered follow-up slice should run next; continue with the best sequenced slice unless scope changes, approval is required, or a real blocker appears.
- During a safe master-plan or plan batch, do not stop after one or two phases just to ask whether to continue; carry the batch through all planned phases in sequence, record any safe follow-up items in the appropriate backlog during the same batch, and close the plan only after the final closeout pass.
- During broad implementation work, review the product, control-system, and implementation-workflow layers before substantial edits, and capture the review in a temporary analysis artifact when the batch is broad or high-risk.
- Use `make codex-context topic=<topic> intent='<intent>'` before broad discovery so the plan starts from the diff summary, audit summary index, and the most relevant audit.
- If a manifest is in scope, read `docs/validation-memory.md` and `docs/validation-memory.json` before finalizing evidence so canonical command strings and manifest bucket rules are explicit.

4. Manifest decision
- Manifest is required for high-risk business logic, invoice-critical behavior, DB migrations, frontend/backend contract changes, generated artifact changes, agent/tooling/workflow changes, 3+ meaningful surfaces, broad autonomous changes, and resolver-required changes.
- Manifest is optional for single backend service changes, single frontend component changes, small bugfixes, small test-only changes, small docs-only corrections, and small internal refactors without behavior, contract, DB, generated-artifact, or agent-safety impact.
- If a non-trivial change skips the manifest, the plan or final closeout records a one-line reason.

5. Business meaning
- Update `docs/business-logic.md` if user-facing behavior, permissions, or workflow meaning changed.
- Update `docs/product-vision.md` if the change clarifies the long-term interaction model, app feel, or Social Useful Network direction.
- Update `docs/vision-architecture-patterns.md` if the change alters `/vision` backend orchestration, API contracts, frontend canvas rendering, prompt handling, or executor patterns.
- If the change introduces or clarifies authoritative operational state, update the machine-readable source first and then
  mirror the explanation into the relevant human-readable docs.

6. Technical meaning
- Update `docs/domain-technical.md` if entities, DTOs, relations, validations, permissions, workflows, or invariants changed.

7. Agent and workflow meaning
- Update `docs/agent-operating-model.md` when automation-safe behavior, clarification rules, destructive confirmation, read-before-write resolution, startup routing, tier rules, or closeout expectations changed.
- Update `docs/agent-operating-model.yaml` when machine-operational rules changed.

8. Workflow docs
- Update `AGENTS.md`, `docs/codex-fast-path.md`, `docs/feature-delivery-workflow.md`, `docs/documentation-sync-policy.md`, and this file when startup routing, manifest policy, tier policy, or closeout flow changed.
- Keep `docs/control-surface-map.md` aligned with the active hierarchy when a file moves between live truth, generated control, or archive.

9. Generated artifacts and schema
- Regenerate affected generated artifacts when source-of-truth or machine-operational docs changed.
- Update `docs/feature-completion-manifest.schema.json` only if the manifest structure changes.
- Keep `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java` passing.

10. Validation
- Tiny changes run only the cheapest relevant targeted checks.
- Normal features run targeted validation first and broaden only if risk requires it.
- High-risk and workflow changes record exact validation commands and skipped-check reasons.
- Frontend contract changes run `npm run validate:contracts`, `npm run type-check`, and `npm run build`.
- Backend changes keep `./mvnw test` or the required targeted backend suite passing.
- Agent-contract changes record `make generate-agent-operating-model`, `make generate-agent-artifacts`, and `make audit-agent-safety` when the machine-operational surface changed.
- Workflow-expansion manifests include at least one `*ScenarioTest` in `testPaths` plus passed scenario or use-case contract evidence.

11. Closeout
- Tier 1: run `make audit-todo`.
- Tier 2: run `make audit-todo` and `make audit-plan-completion plan=<plan-file>`.
- `make audit-plan-completion` now triggers the shared control refresh path automatically after a successful closeout.
- Tier 3 and Tier 4 manifest-backed work:
  `make autofill-feature-closeout manifest=<manifest-file> ...`
  `make audit-todo`
  `make audit-plan-completion plan=<plan-file> manifest=<manifest-file>`
  `make audit-validation-evidence-quality`
  `make validation-memory-closeout-card`
  `make feature-closeout-audit manifest=<manifest-file>`
  `make closeout-report manifest=<manifest-file>` when a compact final review summary helps
- After each completed plan or master plan, run `make post-plan-memory-update plan=<plan-file> [manifest=<manifest-file>] [source=<diagnostic-report>]` to refresh durable lessons, failure memory, and the standard control loop.
- For product-direction work, confirm that `docs/product-memory.md` and `docs/product-vision.md` were the first canonical reference points before broader business or technical docs were expanded.
- For `/vision` implementation work, confirm that `docs/vision-architecture-patterns.md` was read before implementation decisions were made.

11. Final response
- State what changed.
- State what was validated.
- State any remaining risks or not-run checks.
- Do not claim completion while required validation, docs, or closeout gates remain open.

## Working Notes

- Record new deferred implementation or control-system work in the appropriate persistent backlog with a stable ID before closing the change that discovered it.
- When a backlog item is implemented, remove it from the open backlog and clear matching inline `TODO(<ID>):` or `FIXME(<ID>):` references in the same change.
- For protected documentation-sync phrases, copy the exact canonical sentence verbatim into every required file.
- Do not paraphrase, shorten, reorder, or partially restate protected canonical wording.
