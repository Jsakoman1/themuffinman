---
machine_kind: master-plan
machine_status: complete
machine_title: Implementation System Visibility and Closeout Master Plan
machine_goal: Improve the implementation system so control snapshots, Codex context,
  and plan closeout all surface layered analysis and temporary work-product lifecycle
  state without extra manual rediscovery.
---

# Implementation System Visibility and Closeout Master Plan

## Status

 Complete.

## Goal

Improve the implementation system so control snapshots, Codex context, and plan closeout all surface layered analysis and temporary work-product lifecycle state without extra manual rediscovery.

## Parent God Plan

- God Plan: `Plan System God Plan`
- Machine-readable path: `.agents/god-plans/plan-system-god-plan.yaml`

## Scope

- Included: control-start visibility, Codex context visibility, context-pack layering, and closeout handling for temporary work products.
- Excluded: product feature behavior and unrelated domain code.

## Child Plans

1. `.agents/implementation-system-control-start-visibility-plan.md`
- Role: surface layered-analysis and temporary work-product inventory in the compact control snapshot.
- Status: complete

2. `.agents/implementation-system-codex-context-layering-plan.md`
- Role: surface the same layered-analysis pattern in `codex-context` and the topic context pack so the first read already includes the analysis artifact.
- Status: complete

3. `.agents/implementation-system-closeout-lifecycle-plan.md`
- Role: teach plan-completion and closeout tooling to notice, report, and clean up temporary work products owned by the plan.
- Status: complete

## Pros

- Makes the first control snapshot more useful for broad sessions.
- Keeps the analysis artifact visible in both the compact control surface and the Codex context surface.
- Connects temporary work products to plan completion so they do not linger silently.

## Cons

- Adds more generated surface area that must stay in sync.
- Requires careful handling so temporary analysis stays temporary.

## Dependencies

- `AGENTS.md`
- `docs/program-planning-model.md`
- `docs/codex-fast-path.md`
- `docs/feature-delivery-workflow.md`
- `docs/documentation-sync-policy.md`
- `docs/change-completion-checklist.md`
- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml`
- `scripts/audits/local_tooling_extended_tools.rb`
- `scripts/audits/codex_local_context_gateway.rb`
- `scripts/audits/audit-plan-completion.rb`

## Validation

- Targeted checks: the three touched tooling paths and their generated outputs.
- Broader checks: the generated Codex context and plan-completion audit still work after the new visibility and lifecycle hooks.
- Closeout checks: the plan completion path reports temp work-product state and the control/context surfaces show the layered analysis artifact.

## Completion Evidence

- Status: complete
- Child plan status: complete
- Validation evidence: `npm --prefix apps/themuffinman/frontend run generate:contracts`, `make control-start`, `make codex-context topic=visibility-closeout-check intent='validate layered analysis surfacing'`, `make audit-plan-completion plan=.agents/visibility-closeout-check-plan.md`, `make generate-agent-operating-model`, `./mvnw -Dtest=AgentOperatingModelValidationTest test`, `./mvnw test`
- Doc delta summary: control-start now surfaces layered-analysis artifacts and temp work-product inventory; codex-context and context-pack surface the same topic-matched analysis artifact; plan-completion now reports lingering temp work products and blocks closeout when they remain.
- Deferred work: none
