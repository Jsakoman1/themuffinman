---
machine_kind: plan
machine_status: complete
machine_title: Batch Checklist
machine_goal: Define a short implementation-batch checklist for start, slice, validate, widen, and closeout decisions.
---

# Batch Checklist

## Status

Complete.

## Goal

Define a short implementation-batch checklist for start, slice, validate, widen, and closeout decisions.

## Parent Master Plan

- Master plan: `.agents/implementation-system-improvement-master-plan-next.md`

## Scope

- Included: batch checklist wording, decision gates, and slice boundary prompts.
- Excluded: code execution and feature work.

## Validation

- Targeted checks: inspect the checklist wording in the master plan and workflow docs.
- Broader checks: confirm the checklist lines up with validation and closeout steps.
- Closeout checks: confirm the checklist stays short enough to use during live work.

## Completion Evidence

- Status: complete
- Changed files: docs/codex-fast-path.md, docs/feature-delivery-workflow.md, docs/change-completion-checklist.md, scripts/implementation-batch.sh
- Validation evidence: `make implementation-batch topic=implementation-system`, `make audit-generated-artifact-freshness`
- Doc delta summary: the batch checklist is now reflected in the fast path and deterministic batch wrapper.
- Deferred work: none
