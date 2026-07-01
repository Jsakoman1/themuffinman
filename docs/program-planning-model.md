# Program Planning Model

This document defines the planning hierarchy used for broad TheMuffinMan work that is too large for one master plan.

It extends the existing `.agents/*-plan.md`, master-plan, feature-manifest, validation-evidence, backlog, and living-document workflow without replacing them.

## Available Planning Concepts

- `God Plan`: durable program map for a large product or control-system direction that spans multiple master plans.
- `Master Plan`: durable coordination plan for one major workstream with ordered child plans.
- `Plan`: executable implementation or analysis slice under `.agents/`.
- `Temporary work product`: short-lived machine-readable file used by one plan for inventories, comparisons, extracted facts, decision matrices, or validation scratch state.
- `Feature manifest`: machine-readable closeout and evidence record when the workflow tier or resolver requires one.
- `Validation evidence`: command-level evidence for checks, skipped checks, generated artifacts, and closeout status.
- `Persistent backlog`: durable open-work registry for deferred implementation or control-system follow-ups.
- `Living docs`: stable product, domain, workflow, and architecture references under `docs/`.

## Hierarchy

The hierarchy is:

```text
God Plan
Master Plan
Plan
Temporary work product
```

A God Plan may contain several Master Plans. A Master Plan may contain several Plans. A Plan may create temporary work products, but those temporary files must not become the source of truth after the plan closes.

## God Plan Contract

Use a God Plan when a direction spans multiple master plans, high-complexity implementation phases, or several product surfaces.

God Plans should track:

- objective and current status
- included and excluded scope
- child master plans
- implementation state
- strengths already achieved
- known gaps or minuses
- risks and dependencies
- active decisions
- validation and closeout expectations

God Plans live under `.agents/god-plans/` and should have a machine-readable `.yaml` file. A companion `.md` file is optional when human review needs more narrative.

The schema for the machine-readable shape is `docs/god-plan.schema.json`.

## Master Plan Contract

Use a Master Plan when a workstream is broad enough to require ordered child plans and a final closeout pass.

Master Plans should track:

- goal and status
- child plans in execution order
- preflight decisions
- phase boundaries
- dependencies
- validation expectations
- completion evidence

Existing `.agents/*-master-plan.md` files already satisfy this role when they name child plans and final closeout expectations.

## Plan Contract

Use a Plan for an implementation or analysis slice that can be executed and validated directly.

Plans should track:

- tier and scope
- changed surfaces
- implementation slices
- validation plan
- docs and artifacts
- completion evidence
- explicit deferred work, if any

The default template is `.agents/templates/feature-implementation-plan.template.md`.

## Temporary Work Product Contract

Temporary work products are allowed under `.agents/tmp/` when they make implementation or analysis safer.

Allowed uses:

- codebase inventories
- API or DTO comparison tables
- semantic mapping matrices
- generated analysis snapshots
- temporary execution manifests for one plan
- parser or audit scratch output

Required fields for machine-readable temporary work products:

- `id`
- `ownerPlan`
- `purpose`
- `createdAt`
- `deleteWhen`
- `sourceFiles`
- `status`

Temporary work products must be deleted, promoted into a durable doc, or explicitly marked `archived` when the owning plan closes.

## Closeout Rule

God Plans and Master Plans are durable. Plans are temporary but may remain as completed evidence. Temporary work products are not durable by default.

When a completed plan leaves useful knowledge behind, move that knowledge into the right durable surface:

- product direction into `docs/product-memory.md` or `docs/product-vision.md`
- backend/domain truth into `docs/domain-technical.md`
- user-facing rules into `docs/business-logic.md`
- workflow rules into agent-operating docs
- deferred work into `docs/implementation-backlog.md` or `docs/agent-improvement-backlog.md`

