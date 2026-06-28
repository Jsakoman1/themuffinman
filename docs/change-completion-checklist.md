# Change Completion Checklist

This file is a compact execution checklist for logical product changes.

It does not replace the source rules in `AGENTS.md`, `docs/documentation-sync-policy.md`, or the agent-operating model.

Use it as the fast review path before considering a feature complete.

For multi-file, multi-layer, or high-risk logical changes, start with a temporary plan in `.agents/<short-feature-topic>-plan.md` before substantial edits.
Prefer `make bootstrap-feature-work topic=<short-feature-topic> [risk=<tier>]` when you want the plan and matching manifest created together.

## Checklist

1. Code
- Backend or frontend behavior is implemented.
- Duplicate or stale logic introduced by the change is removed or explicitly justified.

2. Domain tests
- Add or extend tests that cover the changed logic, edge cases, and regressions.
- Keep existing affected test suites passing.

3. Business meaning
- Update `docs/business-logic.md` if the change affects user-facing behavior, permissions, workflow meaning, or FAQ-level explanation.

4. Technical meaning
- Update `docs/domain-technical.md` if the change affects entities, DTOs, relations, validations, source maps, permissions, workflows, or invariants.

5. Agent-safe human contract
- Update `docs/agent-operating-model.md` if the change affects automation-safe behavior, clarification rules, destructive confirmation, read-before-write resolution, or multi-actor boundaries.

6. Agent-safe machine contract
- Update `docs/agent-operating-model.yaml` if the change affects intents, workflow order, defaults, edge cases, enums, dependencies, endpoint mapping, or source refs.
- Update the automation read-model inventory there if executor-critical DTO fields, producers, or verifier tests changed.
- Update the intent safety catalog there if mutating intent risk, exact-target resolution, destructive confirmation, or multi-actor requirements changed.
- Update capability registry, intent lineage, dry-run simulation contract, and prompt drift metadata there if planner safety dependencies or executor-prep behavior changed.
- Update backend contract snapshots, service workflow inventory, permission matrix, state-transition audit, and request-validation gates there if backend mutation contracts changed.
- Update the documentation coverage manifest there if automation-relevant controllers, mappers, or tracked agent services changed.
- Update generated frontend contracts, frontend contract gates, automation-safe UI safety layers, regression scenarios, and frontend feature expectations there if planner-facing contracts or workflow coverage changed.

7. Schema and validation
- Update `docs/agent-operating-model.schema.json` if the YAML structure itself changes.
- Keep `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java` passing.

8. Propagation rules
- Update `docs/documentation-sync-policy.md` only if the documentation or validation process itself changes.
- Update `docs/feature-completion-manifest.schema.json` if the machine-readable feature manifest structure changes.
- Update `AGENTS.md` only if repository-wide working rules or mandatory maintenance surfaces change.

9. Automation readiness
- Confirm new natural-language management flows use the existing resolution and clarification pattern instead of inventing a feature-specific one.
- Confirm new destructive flows require explicit confirmation.
- Confirm multi-actor flows do not invent another actor's authority or actions.

10. Final gates
- Run `./mvnw test` for backend changes.
- Run `make audit-agent-safety` when agent-safety contracts, multilingual planning behavior, or machine-operating docs changed.
- Run `npm run type-check` and `npm run build` for frontend changes.
- Confirm previously existing behavior still matches the updated docs and contracts.

11. Optional feature manifest
- If the change uses the plan-driven workflow, keep a matching machine-readable manifest under `.agents/feature-manifests/` and update it before closing the task.
- Use `make feature-closeout-audit manifest=<manifest-file>` as a fast close-out check before final validation on plan-driven changes.

## Temporary Working Notes

- Create temporary scratch files only when they materially reduce risk during a large change.
- Keep them inside the workspace and remove them when the task is complete unless they became useful permanent documentation.
- Temporary notes must never become a second source of truth.
