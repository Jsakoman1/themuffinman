# Change Completion Checklist

This file is a compact execution checklist for logical product changes.

It does not replace the source rules in `AGENTS.md`, `docs/documentation-sync-policy.md`, or the agent-operating model.

Use it as the fast review path before considering a feature complete.

For multi-file, multi-layer, or high-risk logical changes, start with a temporary plan in `.agents/<short-feature-topic>-plan.md` before substantial edits.
Prefer `make bootstrap-feature-work topic=<short-feature-topic> [risk=<tier>] [mode=<mode>] [impact=<impact>] [profiles=<csv>]` when you want the plan and matching manifest created together.
For broad, long-running, or high-complexity work, prefer a master plan that coordinates a group of narrower `.agents/*-plan.md` files in explicit sequence instead of treating the entire task as one flat plan.
Use the master-plan pattern when it safely reduces unnecessary human interaction, increases automation, or makes a larger batch auditable through one final closeout pass.

## Checklist

1. Code
- Backend or frontend behavior is implemented.
- Duplicate or stale logic introduced by the change is removed or explicitly justified.

2. Domain tests
- Add or extend tests that cover the changed logic, edge cases, and regressions.
- Keep existing affected test suites passing.
- If the change expands a workflow, prefer both a use-case contract test and a scenario-style workflow test.

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
- Update `docs/feature-completion-manifest.schema.json` if the machine-readable feature manifest structure changes.
- Update `AGENTS.md` only if repository-wide working rules or mandatory maintenance surfaces change.
- If a change touches protected documentation-sync wording, copy the exact canonical phrase verbatim into every required file instead of paraphrasing it.

9. Persistent backlog hygiene
- Add newly discovered deferred implementation work to `docs/implementation-backlog.md` with a stable ID before closing the change.
- Add newly discovered deferred agent/control-system work to `docs/agent-improvement-backlog.md` with a stable ID before closing the change.
- If inline `TODO/FIXME` notes are needed, use `TODO(<ID>):` or `FIXME(<ID>):` and keep the same ID open in one persistent backlog file.
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
- Run `npm run type-check` and `npm run build` for frontend changes.
- Confirm previously existing behavior still matches the updated docs and contracts.

12. Optional feature manifest
- If the change uses the plan-driven workflow, keep a matching machine-readable manifest under `.agents/feature-manifests/` and update it before closing the task.
- Declare the correct `changeMode`, `changeImpact`, and `changeProfiles` so required docs, generators, and validation commands are enforced automatically.
- Keep the manifest `backlog.reviewed`, `backlog.createdIds`, and `backlog.resolvedIds` aligned with the actual open backlog state.
- Keep manifest artifact groups precise and non-overlapping; do not list test files in `codePaths` or repeat the same path across artifact buckets.
- Use `make feature-closeout-audit manifest=<manifest-file>` as a fast close-out check before final validation on plan-driven changes.
- If the task is orchestrated through a master plan plus child plans, update the master plan to reflect the final state of the whole batch before closing the task.

## Temporary Working Notes

- Create temporary scratch files only when they materially reduce risk during a large change.
- Keep them inside the workspace and remove them when the task is complete unless they became useful permanent documentation.
- Temporary notes must never become a second source of truth.
