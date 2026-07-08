---
machine_kind: plan
machine_status: complete
machine_title: Continuation Rule Consolidation
machine_goal: Reduce repeated continuation wording by consolidating the safe-batch rule across workflow docs.
---

# Continuation Rule Consolidation

## Status

Complete.

## Goal

Reduce repeated continuation wording by consolidating the safe-batch rule across workflow docs.

## Parent Master Plan

- Master plan: `.agents/implementation-system-improvement-master-plan-next.md`

## Scope

- Included: canonical continuation wording, doc duplication reduction, and safe batch execution guidance.
- Excluded: changing blocker or approval semantics.

## Validation

- Targeted checks: compare the canonical wording across workflow docs.
- Broader checks: verify no doc file becomes the only place where the rule lives.
- Closeout checks: keep the canonical wording exact and stable.

## Completion Evidence

- Status: complete
- Changed files: AGENTS.md, docs/codex-fast-path.md, docs/feature-delivery-workflow.md, docs/documentation-sync-policy.md, docs/change-completion-checklist.md, docs/agent-operating-model.md
- Validation evidence: `make generate-agent-operating-model`, `./mvnw -q -Dtest=AgentOperatingModelValidationTest test`
- Doc delta summary: the broad-batch continuation rule is centralized across the workflow docs and the machine-operational contract.
- Deferred work: none
