---
machine_kind: master-plan
machine_status: complete
machine_title: Control System Machine Optimization Master Plan
machine_goal: Make the control system easier to start, easier to refresh, and more
  machine-readable so broad implementation work can
---

# Control System Machine Optimization Master Plan

## Status

Complete.

## Goal

Make the control system easier to start, easier to refresh, and more machine-readable so broad implementation work can
reuse one compact flow instead of rediscovering plan state, registry state, or status parsing rules.

## Scope

- Included:
  - automatic refresh of plan discovery outputs when plan closeout succeeds
  - automatic refresh of audit registry and freshness outputs through one shared control refresh path
  - a shorter one-shot entrypoint for broad work that surfaces the current control state in one place
  - a machine-readable plan status marker that can be parsed before markdown fallback
  - the minimal docs and generated outputs needed to keep those surfaces discoverable
- Excluded:
  - product behavior changes
  - unrelated audit rewrites

## Child Plans

1. `.agents/control-system-refresh-automation-plan.md`
- Role: refresh plan-index, audit registry, summary index, and freshness outputs automatically after successful plan closeout.
- Status: draft

2. `.agents/control-system-start-entrypoint-plan.md`
- Role: add a one-shot control-system entrypoint that prints the current plan and audit discovery state without forcing manual doc hopping.
- Status: complete

3. `.agents/control-system-status-marker-plan.md`
- Role: add a machine-readable status marker to plan files and teach the index/completion parsers to prefer it over plain markdown parsing.
- Status: complete

4. `.agents/control-system-doc-sync-plan.md`
- Role: update the planning and workflow docs so the new refresh and startup path becomes the default guidance.
- Status: complete

## Pros

- Reduces context loss when resuming broad work.
- Keeps plan discovery, registry discovery, and freshness checks on one coordinated path.
- Makes plan status easier to parse without relying only on markdown headings.

## Cons

- Adds more control-system automation that must stay in sync with the docs and generated outputs.
- Requires a migration pass for existing plan files if the new machine status marker becomes the preferred source.
- Needs careful closeout wiring so one refresh path does not accidentally become recursive.

## Dependencies

- `docs/program-planning-model.md`
- `docs/codex-fast-path.md`
- `docs/documentation-sync-policy.md`
- `docs/change-completion-checklist.md`
- `docs/feature-delivery-workflow.md`
- `docs/generated/README.md`
- `scripts/audits/audit-plan-completion.rb`
- `scripts/audits/audit-generated-artifact-freshness.rb`
- `scripts/audits/local_tooling_extended_tools.rb`

## Validation

- `make control-refresh`
- `make control-start`
- `make audit-plan-completion plan=<plan-file>`
- `make audit-generated-artifact-freshness`

## Completion Evidence

- Status: complete
- Validation evidence: `make normalize-plan-metadata` passed; `make control-start` passed; `make audit-generated-artifact-freshness` passed; `make audit-doc-canonical-phrases` passed; `make audit-todo` passed
- Doc delta summary: added a shared control refresh path, a one-shot control-start entrypoint, machine-status frontmatter normalization, and the matching workflow documentation updates
