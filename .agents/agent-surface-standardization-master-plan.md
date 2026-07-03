---
machine_kind: master-plan
machine_status: complete
machine_title: Agent Surface Standardization Master Plan
machine_goal: 'Standardize the shared orchestration model behind:'
---

# Agent Surface Standardization Master Plan

## Status

Complete.

## Goal

Standardize the shared orchestration model behind:

- `Vision` as the strict user-facing adaptive runtime
- `Admin Playground` as the privileged admin operator runtime

while preserving a hard authority boundary between user-scoped execution and admin-scoped execution.

## Why This Plan Exists

The codebase already has two agent-like surfaces:

- `/vision` owns persisted conversations, clarification, review, and typed execution
- `/admin/agent/*` owns prompt planning and dry-run analysis for admins

They currently share some concepts informally, but they do not yet share one explicit runtime policy model or one explicit phased execution standard. That makes future capability growth harder and increases the risk of duplicated prompt logic and inconsistent safety rules.

## Locked Decisions

1. `Vision` remains the user runtime and must continue to obey product-domain permissions, state transitions, and explicit review confirmation.
2. `Admin Playground` may gain direct execution, but only through typed backend capabilities, explicit confirmation, audit-friendly responses, and admin-only policy gates.
3. The first direct admin execution slice is narrow on purpose: synthetic quest batch generation for one exact target user.
4. Shared standardization starts with policy, planning, and capability contracts before any attempt at broad generic execution.

## Child Plans

1. `.agents/agent-surface-standardization-phase1-plan.md`
- Introduce a shared policy model for user-scoped versus admin-scoped execution.
- Standardize the first reusable admin execution planning contract.
- Wire the first admin direct execution capability for synthetic batch quest generation.
- Status: complete

2. `.agents/agent-surface-standardization-phase2-plan.md`
- Expand shared planning contracts between Vision prompt understanding and Admin execution planning.
- Reduce duplicated prompt/semantic logic across surfaces.

3. `.agents/agent-surface-standardization-phase3-plan.md`
- Add more typed admin execution capabilities with the same policy and audit pattern.
- Reconcile frontend affordances and docs once multiple admin execution capabilities exist.

## Validation Expectations

- Backend targeted tests for new policy and admin execution slices
- Frontend type-check and build after admin UI/API changes
- Agent-operating-model validation after endpoint, DTO, or workflow rule changes

## Completion Rule

This master plan can close only when:

- shared runtime policy boundaries are explicit in code
- the first admin execution capability is implemented and validated
- living docs and agent-operating docs reflect the new separation between planning-only and gated direct execution

## Completion Evidence

- Status: complete
- Validation evidence: targeted backend tests passed for the shared semantic planning refactor
- Residual risk: future admin execution capabilities should reuse the shared semantics boundary rather than fork new prompt-classification logic
