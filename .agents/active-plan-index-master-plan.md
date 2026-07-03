---
machine_kind: master-plan
machine_status: complete
machine_title: Active Plan Index Master Plan
machine_goal: Make active plans, master plans, and nearby closeout state easier to
  find at a glance through one compact
---

# Active Plan Index Master Plan

## Status

Complete.

## Goal

Make active plans, master plans, and nearby closeout state easier to find at a glance through one compact
machine-readable index and a matching review summary.

## Parent God Plan

- God Plan: Vision God Plan
- Machine-readable path: `.agents/god-plans/vision-god-plan.yaml`

## Scope

- Included:
  - active plan inventory generation
  - master plan and plan discoverability in local tooling
  - planner-facing routing docs that should point at the new index
  - validation and freshness hooks for the new index surface
- Excluded:
  - runtime product behavior changes
  - unrelated backlog or feature cleanup

## Child Plans

1. `.agents/active-plan-index-generator-plan.md`
- Role: generate a compact active-plan index from `.agents` and the plan-completion surfaces.
- Status: complete

2. `.agents/active-plan-index-routing-plan.md`
- Role: route codex fast-path and local-tooling docs toward the new index so it becomes the default plan discovery aid.
- Status: complete

3. `.agents/active-plan-index-validation-plan.md`
- Role: keep the new index, registry, and docs fresh with explicit validation and closeout evidence.
- Status: complete

## Pros

- Makes it much faster to find open plans and master plans.
- Reduces context hunting across `.agents` when starting or resuming broad work.
- Keeps the control system machine-readable instead of relying on memory or manual scanning.

## Cons

- Adds another generated surface that must stay compact.
- Needs careful routing so it does not become a second source of truth.
- Will require future regeneration when planning surfaces change.

## Dependencies

- `docs/program-planning-model.md`
- `docs/codex-fast-path.md`
- `docs/documentation-sync-policy.md`
- `docs/agent-operating-model.md`
- `docs/tooling/codex-local-audits.md`
- `scripts/audits/local_tooling_extended_tools.rb`
- `scripts/audits/audit-plan-completion.rb`

## Validation

- `make audit-doc-canonical-phrases`
- `make audit-todo`
- `make audit-plan-completion plan=<plan-file>`
- `make audit-generated-artifact-freshness`

## Completion Evidence

- Status: complete
- Validation evidence: `make plan-index` passed; `make audit-doc-canonical-phrases` passed; `make audit-todo` passed; `make audit-generated-artifact-freshness` passed after regenerating `make codex-context topic=active-plan-index intent='refresh plan index after local tooling changes'`
- Doc delta summary: added a compact active-plan index generator plus routing references from the planning entrypoints and local-tooling docs
