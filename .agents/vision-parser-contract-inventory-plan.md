---
machine_kind: plan
machine_status: complete
machine_title: Vision Parser Contract Inventory
machine_goal: Inventory the current Vision parser contract, route metadata, and family-specific branches so the remaining standardization work is explicit and bounded.
---

# Vision Parser Contract Inventory

## Status

Completed.

## Goal

Inventory the current Vision parser contract, route metadata, and family-specific branches so the remaining standardization work is explicit and bounded.

## Parent Master Plan

- Master Plan: `Vision Parser Standardization Master Plan`
- Machine-readable path: `.agents/vision-parser-standardization-master-plan.md`

## Scope

- Included:
  - parser entrypoints and semantic envelope assembly
  - route catalog metadata, examples, aliases, and anti-examples
  - active draft and follow-up context inputs
  - family-specific branches and fallback heuristics in the prompt-understanding path
- Excluded:
  - behavior changes in this slice
  - frontend changes
  - unrelated backend domain rewrites

## Checklist

- [x] Inventory all Vision parser entrypoints and supporting helpers.
- [x] Inventory route catalog metadata shape for every supported entity family.
- [x] Identify family-specific branches, special cases, and normalization fallbacks.
- [x] Record the remaining parser gaps in a temporary machine-readable artifact owned by this plan.

## Validation

- Targeted checks: source inventory of parser and route metadata paths.
- Broader checks: confirm the inventory covers create, view, update, delete, and follow-up flows.
- Closeout checks: the next slice should be able to read this inventory without reopening the discovery step.

## Completion Evidence

- Status: completed
- Validation evidence: `.agents/tmp/vision-parser-contract-inventory.yaml`
- Doc delta summary: parser contract inventory captured for the supported Vision entity families
- Deferred work: branch reduction and regression/doc synchronization remain open
