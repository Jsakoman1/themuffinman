---
machine_kind: master-plan
machine_status: complete
machine_title: Control System Improvement Master Plan
machine_goal: Tighten the control system so planning, validation, closeout, and generated artifacts stay compact, current, and easy to audit.
---

# Control System Improvement Master Plan

## Status

Complete.

## Goal

Tighten the control system so planning, validation, closeout, and generated artifacts stay compact, current, and easy to audit.

## Parent God Plan

- God Plan: `Plan System God Plan`
- Machine-readable path: `.agents/god-plans/plan-system-god-plan.yaml`

## Scope

- Included: plan lifecycle, plan index hygiene, control-start and codex-context routing, validation evidence shape, temporary work product cleanup, and closeout discipline.
- Excluded: product feature behavior, domain refactors, and broad repo-wide implementation work.

## Child Plans

1. Plan index hygiene
- Role: keep the active-plan index and routing summaries aligned with the real plan set.
- Status: complete

2. Validation and evidence hygiene
- Role: make validation commands, evidence records, and skipped-check reasons more deterministic.
- Status: complete

3. Temporary work product hygiene
- Role: make `.agents/tmp/` files easier to own, expire, and close out safely.
- Status: complete

4. Closeout and backlog hygiene
- Role: keep completed work out of the active control surfaces and defer only durable follow-ups.
- Status: complete

## Improvement Checklist

- [x] Keep `docs/generated/local-tooling/plan-index-summary.md` and the open-plan mirror synchronized with the actual active planning files.
- [x] Make `machine_status` usage consistent across all plan files so automation can parse status before markdown.
- [x] Reduce duplicated planning prose between the template files and the concrete master plans.
- [x] Add a short, canonical checklist for closing a plan so completion evidence is captured in one predictable shape.
- [x] Standardize temporary work product metadata so ownership and deletion rules are explicit in every scratch file.
- [x] Add a lightweight validation command vocabulary for plan closeout so evidence does not drift into free-form summaries.
- [x] Keep closeout-focused audit commands listed next to the plan files that actually depend on them.
- [x] Make the control snapshot outputs easier to compare by reducing noisy or repeated lines.
- [x] Add clearer rules for when a completed plan should move durable lessons into backlog or memory docs.
- [x] Improve how generated control artifacts point back to the live source of truth so stale mirrors are easier to spot.
- [x] Keep control-system master plans short enough that a future agent can scan them without cross-referencing half the repo.
- [x] Add a stable pattern for plan dependency order so child-plan sequencing is visible without reading historical status notes.

## Pros

- Keeps planning state small enough to audit quickly.
- Makes future control-system changes easier to reason about.
- Reduces the chance of stale mirrors pretending to be current truth.

## Cons

- Requires disciplined maintenance of several small planning surfaces.
- Can drift if generated summaries are not refreshed when plan state changes.

## Dependencies

- `.agents/god-plans/plan-system-god-plan.yaml`
- `docs/program-planning-model.md`
- `docs/codex-fast-path.md`
- `docs/feature-delivery-workflow.md`
- `docs/control-surface-map.md`

## Validation

- Targeted checks: confirm the active-plan mirror matches the real draft plans.
- Broader checks: verify the control workflow still routes cleanly from AGENTS to planning and validation docs.
- Closeout checks: ensure completed work is not lingering in active planning surfaces.

## Completion Evidence

- Status: complete
- Child plan status: complete
- Validation evidence: regenerated `docs/generated/local-tooling/plan-index.*` and `docs/generated/local-tooling/control-start.*` through `make plan-index` and `make control-start`; normalized `machine_status` values; refreshed planning templates; updated `docs/program-planning-model.md`, `docs/change-completion-checklist.md`, `docs/validation-memory.md`, `docs/validation-memory.json`, and `docs/validation-memory.schema.json`.
- Doc delta summary: the control system now has a canonical plan closeout checklist, normalized status vocabulary, explicit temp-work-product metadata, control-system closeout commands, and routing snapshots that point back to the live plan files.
- Deferred work: none
