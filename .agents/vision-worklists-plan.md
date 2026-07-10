---
machine_kind: plan
machine_status: complete
machine_title: Vision Worklists Plan
machine_goal: Standardize how `/vision` presents quest-related work so "find work",
  "my quests", and "applied quests" all read as one simple, scannable system instead
  of separate ad hoc list designs.
---

# Vision Worklists Plan

## Status

Completed.

## Goal

Standardize how `/vision` presents quest-related work so "find work", "my quests", and "applied quests" all read as one simple, scannable system instead of separate ad hoc list designs.

## Scope

- Included: quest discovery rows, applied-quest rows, owned-quest rows, and detail drill-in cues.
- Excluded: non-quest modules and backend logic changes.

## Steps

1. Define one row pattern for discovery and management lists.
2. Make status, reward, location, and next action the primary data points.
3. Reduce list copy to the smallest useful amount and avoid mixed visual styles.

## Validation

- `npm run type-check`
- `npm run build`

## Completion

- Quest discovery and search discovery both render as standardized terminal rows.
- The same row grammar is now the default for work-oriented lists.
