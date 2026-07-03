---
machine_kind: plan
machine_status: complete
machine_title: Control System Status Marker Plan
machine_goal: Add a machine-readable status marker to plan files and teach the plan
  index and closeout checks to prefer it before
---

# Control System Status Marker Plan

## Status

Complete.

## Goal

Add a machine-readable status marker to plan files and teach the plan index and closeout checks to prefer it before
falling back to markdown parsing.

## Scope

- Included:
  - plan metadata frontmatter or equivalent machine-readable status marker
  - parser updates for plan-index and plan-completion
  - migration support for existing plan files
- Excluded:
  - generated registry refresh logic
  - one-shot startup command formatting

## Implementation Slices

1. Define the machine-readable status shape for plan files.
2. Update the shared parser logic to read the machine marker first.
3. Add a migration or normalization step for existing plan files.

## Validation

- `make plan-index`
- `make audit-plan-completion plan=<plan-file>`

## Completion Evidence

- Status: complete
- Validation evidence: `make normalize-plan-metadata` passed; `make plan-index` passed; `make audit-plan-completion plan=.agents/control-system-status-marker-plan.md` passed
- Doc delta summary: added machine-status frontmatter to existing plan files and taught the plan-index and plan-completion parsers to prefer it before markdown fallback
