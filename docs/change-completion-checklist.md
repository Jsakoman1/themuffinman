# Change Completion Checklist

This file is a compact execution checklist for logical product changes.

It does not replace the source rules in `AGENTS.md`, `docs/documentation-sync-policy.md`, or the agent-operating model.

Use it as the fast review path before considering a feature complete.

For multi-file, multi-layer, or high-risk logical changes, start with a temporary plan in `.agents/<short-feature-topic>-plan.md` before substantial edits.
Prefer `make bootstrap-feature-work topic=<short-feature-topic> [risk=<tier>] [mode=<mode>] [impact=<impact>] [profiles=<csv>]` when you want the plan and matching manifest created together.
For broad, long-running, or high-complexity work, prefer a master plan that coordinates a group of narrower `.agents/*-plan.md` files in explicit sequence instead of treating the entire task as one flat plan.
Use the master-plan pattern when it safely reduces unnecessary human interaction, increases automation, or makes a larger batch auditable through one final closeout pass.
For broad, long-running, high-complexity, multi-layer, high-risk, or master-plan-driven changes, use the implementation checkpoints in `docs/agent-operating-model.md` and `docs/agent-operating-model.yaml`: plan, first backend slice, first frontend slice, docs/artifacts sync, validation, and commit boundary.

## Checklist

1. Code
- Backend or frontend behavior is implemented.
- Duplicate or stale logic introduced by the change is removed or explicitly justified.

2. Domain tests
- Add or extend tests that cover the changed logic, edge cases, and regressions.
- Keep existing affected test suites passing.
- If the change expands a workflow, prefer both a use-case contract test and a scenario-style workflow test.
- Use `docs/regression-scenario-catalog.yaml` to choose focused regression commands for the affected domain before widening validation.
- If a changed living-doc section is listed in `docs/docs-as-contract-slices.yaml`, run the slice command or update the slice with the new verification in the same change.

3. Business meaning
- Update `docs/business-logic.md` if the change affects user-facing behavior, permissions, workflow meaning, or FAQ-level explanation.

4. Technical meaning
- Update `docs/domain-technical.md` if the change affects entities, DTOs, relations, validations, source maps, permissions, workflows, or invariants.

5. Agent-safe human contract
- Update `docs/agent-operating-model.md` if the change affects automation-safe behavior, clarification rules, destructive confirmation, read-before-write resolution, or multi-actor boundaries.

6. Agent-safe machine contract
- Update `docs/agent-operating-model.yaml` if the change affects intents, workflow order, defaults, edge cases, enums, dependencies, endpoint mapping, or source refs.
- If the machine contract is maintained through `docs/agent-operating-model/sections/*.yaml`, regenerate the combined YAML before validation.
- Update the automation read-model inventory there if executor-critical DTO fields, producers, or verifier tests changed.
- Update the intent safety catalog there if mutating intent risk, exact-target resolution, destructive confirmation, or multi-actor requirements changed.
- Update capability registry, intent lineage, dry-run simulation contract, and prompt drift metadata there if planner safety dependencies or executor-prep behavior changed.
- Update backend contract snapshots, service workflow inventory, permission matrix, state-transition audit, and request-validation gates there if backend mutation contracts changed.
- Update the documentation coverage manifest there if automation-relevant controllers, mappers, or tracked agent services changed.
- Update generated frontend contracts, frontend contract gates, automation-safe UI safety layers, regression scenarios, and frontend feature expectations there if planner-facing contracts or workflow coverage changed.
- Update backend audit tier rules there if strict-vs-report-only backend coverage changed.
- Update backend audit domain ownership there if repo structure, domain routing, or control ownership changed.
- When tightening backend audit, prefer rule-scoped subsets with clear product value before raising an entire broad tier at once.

7. Schema and validation
- Update `docs/agent-operating-model.schema.json` if the YAML structure itself changes.
- Regenerate `docs/generated/agent-endpoint-inventory.json` and `docs/generated/automation-read-model-inventory.json` if controller or automation DTO surfaces changed.
- Regenerate `docs/generated/source-of-truth-audit.json` if tracked controllers, services, mappers, or workflow tests changed.
- Regenerate `docs/generated/backend-audit-inventory.json` if backend classification rules or broad backend package coverage changed.
- Keep `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java` passing.

8. Propagation rules
- Update `docs/documentation-sync-policy.md` only if the documentation or validation process itself changes.
- Update `docs/feature-delivery-workflow.md` if the end-to-end feature delivery process, context gateway usage, validation evidence path, manifest autofill path, or closeout flow changed.
- Update `docs/feature-completion-manifest.schema.json` if the machine-readable feature manifest structure changes.
- Update `AGENTS.md` only if repository-wide working rules or mandatory maintenance surfaces change.
- Use `documentation_ownership` in `docs/agent-operating-model.yaml` as the machine-readable starting point for required living docs, generated artifacts, and validation checks by domain and change category.
- For new workflows, endpoints, DTO contracts, modules, permission rules, or schema migrations, start from the matching `.agents/templates/docs/` documentation template and keep only the sections that apply to the concrete change.
- Use `docs/example-scenario-library.md` as the short canonical pattern reference for adding endpoints, changing workflow transitions, adding DTO contracts, adding migrations, and updating docs.
- Keep `docs/regression-scenario-catalog.yaml` and `docs/regression-scenario-catalog.md` current when a critical domain workflow, permission rule, validation rule, or automation-safe behavior gains a new regression scenario.
- Keep `docs/docs-as-contract-slices.yaml` and `docs/docs-as-contract-slices.md` current when a living-doc section becomes contract-like or its runtime/audit proof changes.
- For logic-drift changes, record a doc delta summary: behavior changed, docs updated, and related surfaces intentionally left unchanged.
- Use `make audit-doc-staleness-scoring` when a broad code change needs a ranked report of likely stale living-doc sections.
- Use `make audit-architecture-drift` when a broad change adds or heavily edits services, controllers, Vue views, or long living-doc sections.
- If a change touches protected documentation-sync wording, copy the exact canonical phrase verbatim into every required file instead of paraphrasing it.

9. Persistent backlog hygiene
- Add newly discovered deferred implementation work to `docs/implementation-backlog.md` with a stable ID before closing the change.
- Add newly discovered deferred agent/control-system work to `docs/agent-improvement-backlog.md` with a stable ID before closing the change.
- If inline `TODO/FIXME` notes are needed, use `TODO(<ID>):` or `FIXME(<ID>):` and keep the same ID open in one persistent backlog file.
- Keep every open persistent backlog ID traceable to at least one plan, feature manifest, doc, code surface, or inline backlog reference outside the backlog file itself.
- When implementing a backlog item, remove it from the open backlog and clear matching inline references in the same change.
- Run `make audit-todo` after backlog or inline TODO changes.

10. Automation readiness
- Confirm new natural-language management flows use the existing resolution and clarification pattern instead of inventing a feature-specific one.
- Confirm new destructive flows require explicit confirmation.
- Confirm multi-actor flows do not invent another actor's authority or actions.
- For every newly introduced workflow or self-service action, explicitly check whether the feature needs:
  `docs/agent-operating-model.md`
  `docs/agent-operating-model.yaml`
  generated agent inventories or contract snapshots
  frontend safety wiring or generated contracts
  synthetic admin-generation or sandbox-generation updates
- If a feature reuses an existing intent, still review whether backend read DTOs, resolution fields, and UI action surfaces changed in a way that future automation depends on.
- If a feature adds a new backend DTO field or new action visibility rule, review sibling read surfaces that expose the same entity so automation does not inherit partial coverage.

11. Final gates
- Run `./mvnw test` for backend changes.
- Run `make audit-agent-safety` when agent-safety contracts, multilingual planning behavior, or machine-operating docs changed.
- Run `make audit-todo` when backlog state or inline TODO references changed.
- Run `npm run validate:contracts`, `npm run type-check`, and `npm run build` for frontend contract changes; run type-check and build for other frontend changes.
- Confirm previously existing behavior still matches the updated docs and contracts.

12. Feature manifest decision
- Manifest required: use a machine-readable manifest under `.agents/feature-manifests/` for multi-file, multi-layer, high-risk, executor-critical, workflow-expansion, agent-contract, frontend-contract, backend-logic, generated-artifact, or master-plan-driven changes.
- Manifest optional: cosmetic and single-file contract-neutral refactors may skip the manifest workflow when they do not alter behavior, contracts, generated artifacts, validation scope, or documentation meaning.
- Manifest skipped with reason: if a non-trivial change does not use a manifest, record a one-line reason in the temporary plan or final closeout that names why the required category does not apply.
- If the change uses the plan-driven workflow, keep the matching machine-readable manifest updated before closing the task.
- Declare the correct `changeMode`, `changeImpact`, and `changeProfiles` so required docs, generators, and validation commands are enforced automatically.
- Use `policies.self_test_matrix` in `docs/agent-operating-model.yaml` to pick the validation tier set for the change risk and profiles.
- Keep the manifest `backlog.reviewed`, `backlog.createdIds`, and `backlog.resolvedIds` aligned with the actual open backlog state.
- Keep manifest artifact groups precise and non-overlapping; do not list test files in `codePaths` or repeat the same path across artifact buckets.
- For changes that need a compact validation trail, keep a validation evidence record under `.agents/validation-evidence/` using `docs/validation-evidence.schema.json`.
- Record exact command results, `ranAt`, generated artifact refreshes, skipped checks, and skipped-check reasons in the validation evidence record.
- Run `make audit-validation-evidence-quality` when validation evidence records are added or changed; vague summaries such as "tests not run" or "build passed" are not enough without exact command, scope, and skipped-check reason details.
- Use `make feature-closeout-audit manifest=<manifest-file>` as a hard-fail close-out check before final validation on plan-driven changes. Completed manifests must include structured validation evidence for required checks, a completed plan state, and a ready closeout decision.
- Use `make audit-plan-completion plan=<plan-file> [manifest=<manifest-file>]` when closing a temporary plan, master plan, or completed manifest-backed change; the audit fails open task checkboxes that are not explicitly deferred to a stable backlog ID.
- Use `make closeout-report manifest=<manifest-file>` when a manifest-backed change needs a compact final review summary from changed files, artifact groups, validation commands, docs delta, generated artifacts, backlog delta, and residual risks.
- After a large change or master-plan batch, run `make post-merge-retrospective topic=<short-topic>` to capture failure points, missing tools, docs gaps, and reusable patterns for the next session.
- If the task is orchestrated through a master plan plus child plans, update the master plan to reflect the final state of the whole batch before closing the task.

13. Implementation checkpoints
- For broad or high-risk work, record the plan checkpoint before substantial edits.
- If backend is in scope, finish and validate the first backend slice before widening backend changes.
- If frontend is in scope, finish and validate the first frontend slice before widening UI changes.
- Sync affected living docs and generated artifacts before final validation.
- Record exact validation commands, scopes, and skipped-check reasons before closing the slice.
- Treat commit or push as outside the normal checkpoint unless the user explicitly requested it.

## Temporary Working Notes

- Create temporary scratch files only when they materially reduce risk during a large change.
- Keep them inside the workspace and remove them when the task is complete unless they became useful permanent documentation.
- Temporary notes must never become a second source of truth.
