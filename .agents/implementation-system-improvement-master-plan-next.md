---
machine_kind: master-plan
machine_status: draft
machine_title: Implementation System Improvement Master Plan
machine_goal: Make broad implementation work easier to start, slice, validate, and close out without losing the layered-analysis discipline.
---

# Implementation System Improvement Master Plan

## Status

Draft.

## Goal

Make broad implementation work easier to start, slice, validate, and close out without losing the layered-analysis discipline.

## Parent God Plan

- God Plan: `Plan System God Plan`
- Machine-readable path: `.agents/god-plans/plan-system-god-plan.yaml`

## Scope

- Included: layered-analysis entrypoints, workflow guidance, closeout discipline, evidence capture, and docs-sync routing for implementation batches.
- Excluded: domain business rules, UI redesign, and control-system rebuild tasks.

## Child Plans

1. Layered analysis and routing
- Role: make broad batches start with the right context pack, snapshot, and routing docs.
- Status: pending

2. Batch execution discipline
- Role: keep implementation batches moving through the planned slices without unnecessary interruptions.
- Status: pending

3. Validation and evidence capture
- Role: keep validation commands, evidence records, and closeout gates explicit and repeatable.
- Status: pending

4. Documentation sync discipline
- Role: make docs and generated artifacts follow the code changes instead of drifting behind them.
- Status: pending

## Improvement Checklist

- [ ] Keep `make control-start`, `make codex-context`, and `make context-pack` aligned so they produce comparable planning context.
- [ ] Make the layered-analysis step the default entrypoint for broad batches instead of an optional extra.
- [ ] Reduce duplicated advice between `docs/codex-fast-path.md` and `docs/feature-delivery-workflow.md`.
- [ ] Add a short implementation-batch checklist that tells agents when to stop, validate, or widen scope.
- [ ] Improve how the workflow docs explain the boundary between tiny, normal, high-risk, and agent/tooling work.
- [ ] Make plan creation templates more explicit about scope, risk, dependencies, and validation before any code changes start.
- [ ] Tighten the handoff between implementation planning and docs-sync requirements so change propagation is harder to forget.
- [ ] Keep temporary work products tied to one owning plan and make their cleanup path obvious in the plan text.
- [ ] Add a simple rule for when to record deferred follow-ups in a persistent backlog during the current batch.
- [ ] Improve the closeout sequence so validation, evidence, and plan completion use one stable order.
- [ ] Make generated-artifact hygiene checks easier to invoke on the exact files touched by the batch.
- [ ] Add a cleaner route from implementation plans to the relevant validation preset so teams do not rediscover the same test set.
- [ ] Keep the implementation system docs focused on operator flow instead of repeating broad philosophical guidance.
- [ ] Make it easier to tell which parts of the workflow are human review context and which parts are machine-operational truth.

## Pros

- Makes large batches easier to start safely.
- Keeps validation and closeout from becoming ad hoc.
- Gives future implementation work a clearer execution lane.

## Cons

- Adds more process surfaces that need the same discipline as the code.
- Can become noisy if the same workflow rule is repeated in too many places.

## Dependencies

- `AGENTS.md`
- `docs/codex-fast-path.md`
- `docs/feature-delivery-workflow.md`
- `docs/documentation-sync-policy.md`
- `docs/change-completion-checklist.md`
- `docs/program-planning-model.md`

## Validation

- Targeted checks: confirm the workflow docs still point at the right fast-path entrypoints.
- Broader checks: verify the batch guidance is consistent with planning, validation, and closeout expectations.
- Closeout checks: ensure docs-sync and evidence capture steps are still explicit enough for broad work.

## Completion Evidence

- Status: draft
- Child plan status: pending
- Validation evidence: not run
- Doc delta summary: new implementation-system improvement plan created to tighten layered analysis, batch execution, and closeout discipline.
- Deferred work: none
