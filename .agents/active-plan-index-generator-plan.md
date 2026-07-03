---
machine_kind: plan
machine_status: complete
machine_title: Active Plan Index Generator Plan
machine_goal: Generate a compact active plan index that shows open plans, open master
  plans, and completed planning artifacts in one
---

# Active Plan Index Generator Plan

## Status

Complete.

## Goal

Generate a compact active plan index that shows open plans, open master plans, and completed planning artifacts in one
place for fast Codex discovery.

## Scope

- Included:
  - `.agents/*-plan.md`
  - `.agents/god-plans/*.yaml`
  - `docs/generated/local-tooling/plan-index.*`
  - audit registry entries that should surface the new index
- Excluded:
  - feature manifests and other non-plan control files
  - deep historical archives

## Implementation Slices

1. Build the plan index generator and wrapper command.
2. Teach the local tooling registry and summary index about the new output.
3. Regenerate the new index and verify it is concise and readable.

## Validation

- `make audit-plan-index`
- `make audit-summary-index`
- `make audit-generated-artifact-freshness`

## Completion Evidence

- Status: complete
- Validation evidence: `make plan-index` passed; `make audit-generated-artifact-freshness` passed after refreshing `make codex-context topic=active-plan-index intent='refresh plan index after local tooling changes'`
- Doc delta summary: added `plan-index` generation to the local tooling registry, wrapper, and `Makefile`
