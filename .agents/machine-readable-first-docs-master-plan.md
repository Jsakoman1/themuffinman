---
machine_kind: master-plan
machine_status: complete
machine_title: Machine-Readable-First Docs Master Plan
machine_goal: Make machine-readable operational and control files the primary source
  of truth for active behavior, planning state, and validation state, while keeping
  human-readable docs as derived summaries or curated narrative where needed.
---

# Machine-Readable-First Docs Master Plan

## Status

Complete.

## Goal

Make machine-readable operational and control files the primary source of truth for active behavior, planning state, and validation state, while keeping human-readable docs as derived summaries or curated narrative where needed.

## Scope

- Included:
  - `AGENTS.md`
  - `docs/documentation-sync-policy.md`
  - `docs/codex-fast-path.md`
  - `docs/change-completion-checklist.md`
  - `docs/control-surface-map.md`
  - `docs/program-planning-model.md`
  - adjacent doc references that must stay consistent with the new source hierarchy
- Excluded:
  - feature code changes
  - product behavior changes
  - backend or frontend runtime changes

## Child Plans

1. `.agents/machine-readable-first-policy-plan.md`
- Role: tighten the source-of-truth hierarchy, archive boundary, and generated-control distinction.
- Status: complete

2. `.agents/machine-readable-first-workflow-plan.md`
- Role: update startup, fast-path, and closeout guidance so workflows prefer machine-readable sources first and treat human docs as derived outputs.
- Status: complete

## Pros

- Reduces docs drift by making the authoritative layer explicit.
- Gives future automation a clear hierarchy for reading and writing state.
- Keeps narrative docs useful without letting them become accidental control inputs.

## Cons

- Requires careful wording so we do not accidentally demote genuinely authoritative human docs.
- May require follow-up updates to generated inventories or validation helpers if they currently imply a different hierarchy.

## Dependencies

- Existing `docs/agent-operating-model.yaml` documentation-sync rules
- Existing local-tooling generated inventories and audit summaries
- Current planning hierarchy in `docs/program-planning-model.md`

## Validation

- Targeted docs consistency review
- `./mvnw -Dtest=AgentOperatingModelValidationTest test` if protected workflow wording changes affect the agent operating model

## Completion Evidence

- Status: complete
- Child plan status: complete
- Validation evidence:
  - `make audit-todo`
  - `make generate-agent-operating-model`
  - `./mvnw -Dtest=AgentOperatingModelValidationTest test`
- Doc delta summary:
  - `AGENTS.md`
  - `docs/control-surface-map.md`
  - `docs/documentation-sync-policy.md`
  - `docs/codex-fast-path.md`
  - `docs/change-completion-checklist.md`
  - `docs/feature-delivery-workflow.md`
  - `docs/program-planning-model.md`
  - `docs/source-of-truth-inventory.md`
  - `docs/agent-operating-model.md`
  - `docs/agent-operating-model/sections/metadata.yaml`
  - `docs/agent-operating-model.yaml`
- Deferred work: none for this policy pass
