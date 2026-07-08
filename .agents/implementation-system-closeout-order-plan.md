---
machine_kind: plan
machine_status: complete
machine_title: Closeout Order
machine_goal: Make the closeout sequence stable so control refresh, cleanup, evidence, audits, and memory updates happen predictably.
---

# Closeout Order

## Status

Complete.

## Goal

Make the closeout sequence stable so control refresh, cleanup, evidence, audits, and memory updates happen predictably.

## Parent Master Plan

- Master plan: `.agents/implementation-system-improvement-master-plan-next.md`

## Scope

- Included: closeout ordering, control refresh, temp cleanup, evidence capture, and memory update sequence.
- Excluded: code changes outside the workflow layer.

## Validation

- Targeted checks: compare closeout order in the workflow docs and checklist.
- Broader checks: verify the same order is reflected in the master plan.
- Closeout checks: confirm no step in the sequence is implied only by memory.

## Completion Evidence

- Status: complete
- Changed files: scripts/implementation-batch.sh, docs/change-completion-checklist.md, docs/feature-delivery-workflow.md, docs/documentation-sync-policy.md
- Validation evidence: `make implementation-batch topic=implementation-system`, `make audit-generated-artifact-freshness`
- Doc delta summary: closeout order is now explicit in the batch wrapper and workflow docs.
- Deferred work: none
