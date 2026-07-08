---
machine_kind: plan
machine_status: complete
machine_title: Validation Preset Routing
machine_goal: Map changed files to a sensible default validation preset so the same test set is not rediscovered by hand.
---

# Validation Preset Routing

## Status

Complete.

## Goal

Map changed files to a sensible default validation preset so the same test set is not rediscovered by hand.

## Parent Master Plan

- Master plan: `.agents/implementation-system-improvement-master-plan-next.md`

## Scope

- Included: validation preset selection, changed-file routing, and test-set reuse.
- Excluded: actual feature implementation.

## Validation

- Targeted checks: run `make recommend-validation-preset files=<csv>` for a representative implementation batch.
- Broader checks: confirm the preset matches the changed-file categories.
- Closeout checks: ensure preset selection is explained in the plan text.

## Completion Evidence

- Status: complete
- Changed files: scripts/implementation-batch.sh, scripts/audits/local_tooling_extended_tools.rb, docs/generated/local-tooling/validation-preset-summary.md
- Validation evidence: `make implementation-batch topic=implementation-system`, `make audit-generated-artifact-freshness`
- Doc delta summary: the batch wrapper now records validation-preset recommendations as part of the deterministic implementation flow.
- Deferred work: none
