---
machine_kind: plan
machine_status: unknown
machine_title: Permission Rule Duplication Audit Plan
machine_goal: Implement `CODEX-LOCAL-PERMISSION-RULE-DUPLICATION-AUDIT` so Codex can
  inspect permission gates and allowed-action duplication across backend sources,
  DTO assembly, and frontend gating code without broad manual repo scans.
---

# Permission Rule Duplication Audit Plan

## Goal

Implement `CODEX-LOCAL-PERMISSION-RULE-DUPLICATION-AUDIT` so Codex can inspect permission gates and allowed-action duplication across backend sources, DTO assembly, and frontend gating code without broad manual repo scans.

## Scope

- inventory backend permission-source methods and action-resolution helpers
- inventory backend presentation-flag derivation from allowed actions or relation/status checks
- inventory frontend permission passthrough gates based on backend `presentation` or `primaryAction`
- inventory frontend local permission gates that combine statuses, ownership, relation state, or local workflow state
- produce a cross-layer shortlist for actions that appear in multiple places

## Steps

- [x] identify backend and frontend permission anchor files
- [x] implement `scripts/audits/audit-permission-rule-duplication.rb`
- [x] add `Makefile` target and include it in `audit-local-tooling`
- [x] register the audit in `docs/codex-local-tooling-todo.md`
- [x] document generated outputs in `docs/domain-technical.md`
- [x] run `make audit-permission-rule-duplication`
- [x] run `make audit-local-tooling`
- [x] run `make audit-todo`
