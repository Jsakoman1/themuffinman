---
machine_kind: plan
machine_status: complete
machine_title: Control System Final Polish Plan
machine_goal: Improve plan-closeout diagnostics so incomplete child-plan failures are explicit in the audit output.
---

# Control System Final Polish Plan

## Status

Complete.

## Goal

Improve plan-closeout diagnostics so incomplete child-plan failures are explicit in the audit output.

## Scope

- Included: `audit-plan-completion` diagnostics, machine-readable payload improvements, summary output improvements, and control doc sync if workflow wording changes.
- Excluded: broader workflow redesign, manifest-rule expansion, and generated-artifact policy changes unrelated to plan closeout.

## Implementation Slices

- [x] Extend child-plan audit payloads to expose linked child issues directly instead of only collapsing to `incomplete`.
- [x] Surface the richer child failure detail in the generated markdown summary.
- [x] Sync any affected tooling docs or operating-model wording.

## Validation

- `ruby scripts/audits/audit-plan-completion.rb plan=.agents/control-system-final-polish-plan.md`

## Completion Evidence

- Status: complete
- Validation evidence: `make generate-agent-operating-model`; `./mvnw -Dtest=AgentOperatingModelValidationTest test`
- Doc delta summary: `audit-plan-completion` now writes child-issue rollups into the generated payload and summary so completed master plans expose linked child-plan failure reasons directly.
- Deferred work: none
