# BACKEND-DRIFT-BACKLOG-EXECUTION Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `high`
Master order: 93 of 94

## Backlog Item

Freeze the remaining backend drift into a domain-grouped execution backlog with stable IDs, explicit file references, and a clear order for follow-up implementation.

## Source Findings

- [`.agents/backend-drift-remediation-findings.md`](/Users/jsakoman/Desktop/themuffinman/.agents/backend-drift-remediation-findings.md)
- `docs/generated/local-tooling/architecture-drift-summary.md`
- `docs/generated/local-tooling/doc-sync-preflight-summary.md`
- `docs/generated/local-tooling/test-gap-recommendations-summary.md`
- `docs/generated/source-of-truth-audit.json`

## Implementation Plan

- [x] Group the remaining drift into domain buckets: workmarket, social, identity, location, and docs/control.
- [x] Split each bucket into stable execution items with explicit file references and a single dominant reason for the work.
- [x] Convert the grouped drift into the persistent backlog surface so the next session can execute without re-auditing the whole repository.
- [x] Mark any control-system or documentation follow-up separately from product-code follow-up so the two can be sequenced safely.
- [x] Keep the backlog items narrow enough that each can be executed in one child plan.

## Expected Validation

- `ruby scripts/todo-audit.rb`
- `make audit-architecture-drift`
- `make audit-doc-sync-preflight`
- `make audit-test-gap-recommendations`
- `make generate-agent-artifacts`
- `make audit-generated-artifact-freshness`

## Completion Evidence

- Status: complete
- Changed files: `docs/implementation-backlog.md`, `.agents/backend-drift-remediation-findings.md`, `.agents/backend-drift-remediation-master-plan.md`, `.agents/todo-plans/93-backend-drift-backlog-execution.md`, `.agents/todo-plans/94-backend-hotspot-reduction-workmarket-social.md`
- Validation evidence: `make audit-todo` passed; `make audit-test-gap-recommendations` passed; `make generate-agent-artifacts` passed; `make audit-generated-artifact-freshness` passed
- Backlog update: remaining backend drift is now grouped into stable implementation backlog items
- Residual risk: the new backlog items still need the code-level hotspot reduction pass
