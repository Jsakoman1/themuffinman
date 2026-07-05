---
machine_kind: master-plan
machine_status: complete
machine_title: Implementation System Fast-Path and Batch Automation Master Plan
machine_goal: Reduce fast-path blocking, harden Codex context routing, automate temporary work-product lifecycle handling, and add a deterministic implementation batch entrypoint with cleaner generated-artifact hygiene.
---

# Implementation System Fast-Path and Batch Automation Master Plan

## Status

Complete.

## Goal

Reduce fast-path blocking, harden Codex context routing, automate temporary work-product lifecycle handling, and add a deterministic implementation batch entrypoint with cleaner generated-artifact hygiene.

## Parent God Plan

- God Plan: `Plan System God Plan`
- Machine-readable path: `.agents/god-plans/plan-system-god-plan.yaml`

## Scope

- Included: control-start fast-path gating, codex-context provider symmetry, temporary work-product lifecycle automation, unified batch execution entrypoints, batch-manifest consolidation, and generated-artifact noise reduction.
- Excluded: product feature behavior, unrelated domain logic, and broad control-plane redesign beyond the listed implementation-system slices.

## Current State

- `control-start` is useful, but it can still feel too coupled to global freshness checks for a fast-path command.
- `codex-context` now surfaces layered analysis, but provider registration and concrete implementation coverage still need symmetry checks.
- Temporary work products are visible at closeout, but the lifecycle is still mostly detect-and-report rather than fully automated.
- Broad implementation batches still require multiple coordinated commands rather than one deterministic end-to-end entrypoint.
- Context packs still depend on several separate generated artifacts instead of one compact batch-level manifest.
- Generated-artifact noise outside the subsystem can weaken audit signal and make the implementation state harder to read.

## Desired State

- `control-start` gives a compact, low-friction snapshot without becoming overly blocked by unrelated freshness drift.
- `codex-context` and its provider registry stay aligned so the first read is reliable and complete.
- Temporary work products either auto-promote, auto-clean, or fail closeout with clear ownership and deterministic rules.
- One batch command can drive discovery, layered analysis, implementation, docs sync, validation, and closeout in sequence.
- A batch manifest can summarize the current implementation state without requiring a reader to consult many separate generated files.
- Generated-artifact hygiene keeps the control surface clean and reduces false noise in audits.

## Child Plans

1. `.agents/implementation-system-control-start-fast-path-plan.md`
- Role: reduce unnecessary blocking in `control-start` by separating true implementation-critical checks from slower global freshness concerns.
- Status: complete

2. `.agents/implementation-system-codex-context-provider-symmetry-plan.md`
- Role: make the `codex-context` provider registry and actual audit providers symmetrical so the generated context surface stays predictable.
- Status: complete

3. `.agents/implementation-system-temp-work-product-automation-plan.md`
- Role: automate the lifecycle of temporary work products so closeout can promote, clean, or fail with explicit ownership rules.
- Status: complete

4. `.agents/implementation-system-batch-entrypoint-manifest-plan.md`
- Role: add a deterministic end-to-end implementation batch entrypoint and consolidate the batch state into a compact manifest.
- Status: complete

5. `.agents/implementation-system-generated-artifact-hygiene-plan.md`
- Role: reduce generated-artifact noise outside the subsystem so audits and control snapshots stay readable and trustworthy.
- Status: complete

## Execution Order

1. Fix `control-start` fast-path friction first so implementation sessions stop getting blocked on unrelated drift.
2. Harden `codex-context` provider symmetry next so the context layer becomes a reliable read surface.
3. Automate temporary work-product lifecycle handling so closeout can enforce ownership deterministically.
4. Add the batch entrypoint and compact batch manifest once the read surfaces are stable.
5. Finish with generated-artifact hygiene so the new flow stays clean over time.

## Pros

- Removes the biggest friction from the current implementation flow first.
- Improves trust in the context and closeout surfaces before adding a bigger batch runner.
- Keeps the work split into slices that can be validated independently.
- Makes future automation easier because the lifecycle and manifest rules are explicit.

## Cons

- Adds more tooling and generated-state surface area that must stay synchronized.
- Requires disciplined sequencing so the new batch entrypoint does not hide underlying lifecycle bugs.
- Hygiene work can be slippery if stale artifacts are not tied to explicit ownership rules.

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

- Targeted checks: the touched tooling path for each child plan and the generated output it owns.
- Broader checks: run the relevant control snapshot, Codex context, and closeout audit commands together after each slice.
- Closeout checks: verify the batch flow is deterministic, temporary work products are either cleaned up or promoted, and generated-artifact noise is not masking real state.

## Completion Evidence

- Status: complete
- Child plan status: complete
- Validation evidence: `make control-start`, `make control-refresh-full`, `make implementation-batch topic=implementation-system files=Makefile,scripts/audits/audit-generated-artifact-freshness.rb,scripts/implementation-batch.sh,scripts/audits/local_tooling_extended_tools.rb,scripts/audits/audit-plan-completion.rb`, `make temp-work-product-closeout plan=.agents/control-system-maintenance-plan.md action=delete`, `make audit-plan-completion plan=.agents/control-system-maintenance-plan.md`, `make audit-plan-completion plan=.agents/implementation-system-fast-path-batch-automation-master-plan.md`, `make audit-todo`, `make generate-agent-operating-model`, `make audit-documentation`, `make audit-doc-canonical-phrases`, `./mvnw -Dtest=AgentOperatingModelValidationTest test`
- Doc delta summary: control-start now stays on the fast path while control-refresh-full keeps the slower freshness pass; codex-context provider failures were removed; implementation batches now have a deterministic wrapper, a temp work-product cleanup helper, and a scope-filtered generated-artifact hygiene report.
- Deferred work: none
