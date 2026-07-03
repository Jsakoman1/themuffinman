---
machine_kind: plan
machine_status: complete
machine_title: Machine-Readable-First Workflow Plan
machine_goal: Update workflow guidance so Codex starts from machine-readable control
  artifacts, uses human docs as supporting context, and preserves generated summaries
  as non-authoritative outputs.
---

# Machine-Readable-First Workflow Plan

## Status

Complete.

## Goal

Update workflow guidance so Codex starts from machine-readable control artifacts, uses human docs as supporting context, and preserves generated summaries as non-authoritative outputs.

## Scope

- Included:
  - `AGENTS.md`
  - `docs/documentation-sync-policy.md`
  - `docs/codex-fast-path.md`
  - `docs/change-completion-checklist.md`
- Excluded:
  - runtime code
  - generated inventory regeneration unless wording changes require it

## Implementation Slices

1. Update startup guidance to prefer machine-readable inventories and operational files before broad narrative docs.
2. Update the fast path and checklist so they tell contributors where the authoritative state lives.
3. Keep closeout rules aligned with the new hierarchy.

## Validation Plan

- Review wording consistency across workflow docs
- Run the agent-operating-model validation test if protected phrases or machine-operational guidance changes

## Completion Evidence

- Status: complete
- Validation evidence:
  - `make audit-todo`
  - `make generate-agent-operating-model`
  - `./mvnw -Dtest=AgentOperatingModelValidationTest test`
- Doc delta summary:
  - `AGENTS.md`
  - `docs/documentation-sync-policy.md`
  - `docs/codex-fast-path.md`
  - `docs/change-completion-checklist.md`
  - `docs/feature-delivery-workflow.md`
  - `docs/agent-operating-model.md`
