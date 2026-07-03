---
machine_kind: plan
machine_status: complete
machine_title: Agent Surface Standardization Phase 2 Plan
machine_goal: Standardize the shared semantic planning boundary used by Vision and
  Admin Playground so both surfaces rely on one backend prompt semantics path for
  normalization and intent classification.
---

# Agent Surface Standardization Phase 2 Plan

## Status

Complete.

## Goal

Standardize the shared semantic planning boundary used by Vision and Admin Playground so both surfaces rely on one backend prompt semantics path for normalization and intent classification.

## Scope

- Extract a shared prompt semantics support component.
- Reuse the same normalized prompt and semantic classification logic in Vision and Admin prompt preparation.
- Keep Vision user-scoped and Admin operator-scoped, but remove duplicated local prompt-classification code.
- Surface the shared semantic result in existing admin planning signals without expanding execution authority.

## Non-Goals

- No new execution capability.
- No frontend workflow redesign.
- No new DB schema.

## Validation

- Backend tests for prompt semantics and affected services.
- Full backend test suite if the refactor touches shared constructors or response assembly.

## Completion Evidence

- Status: complete
- Validation evidence: targeted backend tests passed for shared prompt semantics, Vision prompt understanding, and Admin Playground planning
- Residual risk: additional admin execution capabilities should keep reusing `PromptSemanticsSupport` instead of duplicating new local prompt-classification rules
