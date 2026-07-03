---
machine_kind: plan
machine_status: complete
machine_title: Control System Start Entrypoint Plan
machine_goal: Add a shorter one-shot entrypoint for broad work that shows the current
  plan discovery state and the current audit
---

# Control System Start Entrypoint Plan

## Status

Complete.

## Goal

Add a shorter one-shot entrypoint for broad work that shows the current plan discovery state and the current audit
discovery state in one compact step.

## Scope

- Included:
  - a single command that prepares and prints the control-system starting view
  - compact terminal output that points at the relevant generated summaries
  - generated output registration if a new review artifact is introduced
- Excluded:
  - product behavior changes
  - plan status migration

## Implementation Slices

1. Define the control-start command shape and its generated output contract.
2. Implement the command so it surfaces plan and audit discovery in one place.
3. Wire the command into the fast-path docs as the recommended broad-work starting point.

## Validation

- `make control-start`
- `make audit-doc-canonical-phrases`
- `make audit-generated-artifact-freshness`

## Completion Evidence

- Status: complete
- Validation evidence: `make control-start` passed; `make audit-generated-artifact-freshness` passed
- Doc delta summary: added `make control-start` plus the compact `docs/generated/local-tooling/control-start.json` and `control-start-summary.md` review aid
