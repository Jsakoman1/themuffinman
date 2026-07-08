---
machine_kind: plan
machine_status: complete
machine_title: Canonical Phrase Registry
machine_goal: Centralize protected canonical wording so repeated phrases are easier to keep in sync across docs.
---

# Canonical Phrase Registry

## Status

Complete.

## Goal

Centralize protected canonical wording so repeated phrases are easier to keep in sync across docs.

## Parent Master Plan

- Master plan: `.agents/implementation-system-improvement-master-plan-next.md`

## Scope

- Included: canonical phrase inventory, protected wording reuse, and registry-style source-of-truth handling.
- Excluded: changing the meaning of protected phrases.

## Validation

- Targeted checks: compare protected wording across docs.
- Broader checks: confirm one source can drive repeated canonical text.
- Closeout checks: ensure the registry does not drift from the machine-operational source.

## Completion Evidence

- Status: complete
- Changed files: docs/agent-operating-model/sections/documentation_sync.yaml, docs/codex-fast-path.md, docs/feature-delivery-workflow.md, docs/documentation-sync-policy.md, docs/change-completion-checklist.md, docs/agent-operating-model.md
- Validation evidence: `make generate-agent-operating-model`, `./mvnw -q -Dtest=AgentOperatingModelValidationTest test`
- Doc delta summary: protected plan-completion wording is now machine-checkable through the documentation-sync rules.
- Deferred work: none
