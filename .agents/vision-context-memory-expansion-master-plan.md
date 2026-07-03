---
machine_kind: master-plan
machine_status: active
machine_title: Vision Context Memory Expansion Master Plan
machine_goal: Give `/vision` a backend-owned memory layer that separates stable user
  context, current session context, and turn-level context so the orchestrator can
  handle multi-topic conversation shifts without flattening everything into one prompt
  blob.
---

# Vision Context Memory Expansion Master Plan

## Status

Draft.

## Goal

Give `/vision` a backend-owned memory layer that separates stable user context, current session context, and turn-level context so the orchestrator can handle multi-topic conversation shifts without flattening everything into one prompt blob.

## Parent God Plan

- God Plan: `vision`
- Machine-readable path: `.agents/god-plans/vision-god-plan.yaml`

## Scope

- Included: semantic request context expansion, backend memory pack assembly, targeted tests, and docs updates for user/session memory behavior.
- Excluded: new client-local storage, filesystem persistence as runtime source of truth, and broad executor expansion outside the memory boundary.

## Child Plans

1. `.agents/vision-context-memory-user-session-plan.md`
- Role: implement backend memory packs for user and session context and feed them into semantic orchestration.
- Status: draft

## Pros

- Gives the orchestrator a stable place to look for repeated user preferences and current thread state.
- Keeps multi-topic turns from collapsing into one undifferentiated prompt.
- Reuses existing persisted conversations and turns instead of inventing a second state system.

## Cons

- Adds another semantic contract layer that must stay small and deterministic.
- Can become noisy if memory snapshots grow without a strict compactness rule.

## Dependencies

- Existing persisted vision conversations and turns.
- Semantic orchestration request contract.
- Prompt understanding tests.

## Validation

- Targeted vision backend tests.
- `./mvnw -q -Dtest=VisionSemanticOrchestrationContextServiceTest,VisionPromptUnderstandingServiceTest test`
- `./mvnw -q -Dtest=VisionConversationServiceTest,VisionSemanticOrchestrationContextServiceTest,VisionPromptUnderstandingServiceTest test`

## Completion Evidence

- Status: draft
- Child plan status: draft
- Validation evidence: not run yet
- Doc delta summary: pending
- Deferred work: promote any durable lessons into `docs/product-memory.md`, `docs/domain-technical.md`, and `docs/vision-status-ledger.md`
