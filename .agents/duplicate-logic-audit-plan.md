# Duplicate Logic Audit Plan

## Goal

Implement `CODEX-LOCAL-DUPLICATE-LOGIC-AUDIT` so Codex can inspect duplicated status, permission, and action-label logic locally instead of rediscovering cross-layer drift by hand.

## Scope

- scan active frontend route-backed files plus shared workmarket/social helpers
- detect frontend-local status label and status badge mapping logic
- detect frontend action/permission visibility gates that are not simple backend `presentation` passthroughs
- detect frontend transition eligibility helpers that encode workflow rules locally
- link findings to likely backend canonical files where the same concern already exists
- generate compact JSON and Markdown outputs under `docs/generated/local-tooling/`

## Steps

- [x] review current local tooling patterns and identify canonical backend/frontend signal sources
- [x] implement `scripts/audits/audit-duplicate-logic.rb`
- [x] wire the new audit into `Makefile`
- [x] register the audit in `docs/codex-local-tooling-todo.md`
- [x] document the generated output in `docs/domain-technical.md`
- [x] run `make audit-duplicate-logic`
- [x] run `make audit-local-tooling`
- [x] run `make audit-todo`
