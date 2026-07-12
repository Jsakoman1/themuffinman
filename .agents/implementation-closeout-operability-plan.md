---
machine_kind: plan
machine_status: complete
machine_closeout_contract: 2
machine_baseline_ref: 0cf598d
machine_delivery_mode: implementation
machine_title: Implementation Closeout Operability Plan
machine_goal: Make implementation and closeout execution location-aware, run-auditable, regression-tested, and predictable for master plans.
---

# Implementation Closeout Operability Plan

## Status

Complete.

## Workflow Frame

- Feature tier: tier4-agent-tooling-workflow
- Scope: root target ownership, run-scoped batch ledgers, fixture tests for plan audit, explicit master child inventory parsing, read-only closeout preflight, and a master-plan status table.
- Out of scope: product application behavior, legacy plan migration, and changing closeout evidence requirements.
- Manifest decision: required
- Manifest path: .agents/feature-manifests/implementation-closeout-operability-manifest.yaml
- Master plan: none; this is a bounded tooling workflow improvement slice.

## Implementation Checkboxes

- [x] Document root versus application target ownership in human and machine-operational workflow surfaces.
- [x] Add a unique run identifier and run-scoped ledger output to implementation batches.
- [x] Add executable fixture coverage for completion-audit positive and negative paths.
- [x] Restrict master child discovery to explicit inventory and breakdown sections, and add a status table to the template.
- [x] Add read-only closeout preflight, validate every path, and record final evidence.

## Completion Evidence

- Status: complete
- Baseline ref: `0cf598d`
- Implemented code paths: `Makefile`, `scripts/audits/audit-plan-completion.rb`, `scripts/audits/test-audit-plan-completion.rb`, `scripts/feature-closeout-audit.sh`, and `scripts/implementation-batch.sh`
- Changed files: root closeout targets, batch ledger generation, plan audit, fixture test, master-plan template, agent validator, and workflow documentation.
- Validation evidence: Ruby and Zsh syntax checks; `make test-plan-completion-audit`; `make closeout-preflight`; preparation batch run with verified `runId`; `make generate-agent-operating-model`; `make validation-memory-closeout-card`; `make audit-validation-memory-drift`; and `make audit-agent-safety` passed.
- Doc delta summary: recorded root target ownership, read-only preflight behavior, run-scoped ledger behavior, and explicit master child-inventory semantics.
- Backlog update: no backlog item is expected unless validation discovers a distinct issue.
- Residual risk: the append-only JSONL ledger remains intentionally shared history; consumers must use the current `runId` or the per-run JSON file for one invocation.
