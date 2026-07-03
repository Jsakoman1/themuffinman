---
machine_kind: plan
machine_status: complete
machine_title: Control System Refresh Automation Plan
machine_goal: Refresh plan-index, registry, summary, and freshness outputs automatically
  after a plan closeout so the control system
---

# Control System Refresh Automation Plan

## Status

Complete.

## Goal

Refresh plan-index, registry, summary, and freshness outputs automatically after a plan closeout so the control system
stays current without manual regeneration.

## Scope

- Included:
  - closeout-triggered refresh wiring
  - shared control refresh command
  - freshness output regeneration after control surfaces change
- Excluded:
  - one-shot broad-work summary formatting
  - plan status metadata migration

## Implementation Slices

1. Add a shared control refresh target or script that regenerates the plan and audit discovery outputs.
2. Wire successful plan closeout through that refresh path.
3. Confirm the refresh path stays non-recursive and fast enough for normal use.

## Validation

- `make control-refresh`
- `make audit-plan-completion plan=<plan-file>`
- `make audit-generated-artifact-freshness`

## Completion Evidence

- Status: complete
- Validation evidence: `make control-start` passed; `make audit-generated-artifact-freshness` passed
- Doc delta summary: the shared control refresh path now regenerates plan-index, registry, codex-context, audit summary index, control-start, and freshness outputs after successful plan closeout
