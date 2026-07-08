---
machine_kind: plan
machine_status: complete
machine_title: Context Pack Routing
machine_goal: Make topic routing choose the smallest useful pack while still surfacing the right context for broad implementation batches.
---

# Context Pack Routing

## Status

Complete.

## Goal

Make topic routing choose the smallest useful pack while still surfacing the right context for broad implementation batches.

## Parent Master Plan

- Master plan: `.agents/implementation-system-improvement-master-plan-next.md`

## Scope

- Included: topic pack selection, layered-analysis read order, and compact routing hints.
- Excluded: code changes and domain feature behavior.

## Validation

- Targeted checks: `make codex-context topic=implementation-system intent='context pack routing'`
- Broader checks: confirm the generated pack includes the analysis artifact before broad repo search.
- Closeout checks: confirm the routing snapshot remains compact.

## Completion Evidence

- Status: complete
- Changed files: scripts/audits/local_tooling_extended_tools.rb, scripts/implementation-batch.sh, docs/generated/local-tooling/codex-context/latest.review.md
- Validation evidence: `make codex-context topic=implementation-system intent='context pack routing'`, `make implementation-batch topic=implementation-system`
- Doc delta summary: context-pack generation and batch context now surface the layered-analysis artifact before broader repository search.
- Deferred work: none
