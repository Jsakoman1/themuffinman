---
machine_kind: master-plan
machine_status: complete
machine_title: Vision Backend Standardization Master Plan
machine_goal: Standardize and simplify the Vision backend service layer so new features can be added through smaller reusable components instead of expanding monolithic services.
---

# Vision Backend Standardization Master Plan

## Status

Active.

## Goal

Standardize and simplify the Vision backend service layer so new features can be added through smaller reusable components instead of expanding monolithic services.

## Parent God Plan

- God Plan: `Plan System God Plan`
- Machine-readable path: `.agents/god-plans/plan-system-god-plan.yaml`

## Scope

- Included: preview formatting helpers, conversation orchestration decomposition, intent-routing simplification, and shared prompt/semantic boundaries that reduce service sprawl.
- Excluded: unrelated UI polish, new feature families, and backend rewrites outside the current Vision path.

## Current State

- The backend works, but several Vision services still mix orchestration, formatting, routing, and entity-specific branching in large classes.
- `VisionConversationService`, `VisionCapabilityPreviewService`, `VisionIntentRouter`, and `VisionPromptUnderstandingService` remain the main places where adding a new capability still risks adding more branching instead of a reusable component.
- Shared helper extraction has already started, so this plan should continue that direction rather than introduce another layer of ad hoc logic.

## Desired State

- One shared preview-formatting layer for common item, summary, and presentation helpers.
- One thin conversation coordinator that delegates turn handling to smaller flow-specific components.
- One more declarative intent-routing boundary with less precedence logic buried in long condition chains.
- One prompt-understanding boundary that separates provider orchestration from contract shaping.
- New feature work should mostly mean adding a focused helper or handler, not extending a monolithic service.

## Child Plans

Execution order:
1. `.agents/vision-preview-standardization-plan.md`
2. `.agents/vision-conversation-decomposition-plan.md`

1. `.agents/vision-preview-standardization-plan.md`
- Role: extract shared preview formatting helpers and reduce repeated presentation logic inside `VisionCapabilityPreviewService`.
- Status: complete

2. `.agents/vision-conversation-decomposition-plan.md`
- Role: split conversation orchestration into smaller handler services and keep the coordinator thin.
- Status: complete

## Pros

- Lowers the cost of adding new Vision surfaces.
- Makes unit tests smaller and more targeted.
- Reduces accidental coupling between preview presentation, routing, and execution logic.

## Cons

- The work is incremental, so the main payoff comes after multiple slices.
- A few services will still need to stay large until their shared helpers are in place.

## Dependencies

- `docs/vision-architecture-patterns.md`
- `docs/domain-technical.md`
- `docs/product-memory.md`
- `docs/vision-status-ledger.md`
- `docs/regression-scenario-catalog.md`

## Validation

- Targeted checks: `VisionCapabilityPreviewServiceTest`, `VisionIntentRouterTest`, `VisionPromptUnderstandingServiceTest`, `VisionConversationServiceTest`.
- Broader checks: full backend Maven test suite for `apps/themuffinman`.
- Closeout checks: plan completion audit and generated local-tooling refresh.

## Completion Evidence

- Status: complete
- Child plan status: preview child complete, conversation child complete
- Validation evidence:
  - `./mvnw -q -Dtest=VisionDetailConversationTurnSupportTest,VisionConversationServiceTest test`
  - `./mvnw -q test`
- Doc delta summary:
  - Vision preview and conversation orchestration now delegate repeated work to focused support classes.
  - The backend service layer is thinner and easier to extend with new intent families.
- Deferred work: none in this master plan
