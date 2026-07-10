---
machine_kind: plan
machine_status: complete
machine_title: Implementation System Closeout Lifecycle
machine_goal: Teach plan-completion and closeout tooling to notice, report, and clean
  up temporary work products owned by the plan.
---

# Implementation System Closeout Lifecycle

## Status

 Complete.

## Goal

Teach plan-completion and closeout tooling to notice, report, and clean up temporary work products owned by the plan.

## Scope

- Included: `scripts/audits/audit-plan-completion.rb` and any closeout reporting helpers it needs.
- Excluded: control-start visibility and codex-context routing.

## Checklist

- [x] Detect temporary work products owned by the plan during plan-completion audit.
- [x] Report whether temporary work products are still present at closeout.
- [x] Provide a deterministic cleanup or archive signal for lingering temporary work products.

## Validation

- Targeted checks: `make audit-plan-completion plan=<plan-file>`
- Broader checks: `make audit-plan-completion plan=<plan-file> manifest=<manifest-file>` when a manifest exists

## Completion Evidence

- Status: complete
- Validation evidence: `make audit-plan-completion plan=.agents/visibility-closeout-check-plan.md`
- Doc delta summary: plan-completion now reports temp work products owned by the plan and fails closeout when they remain.
