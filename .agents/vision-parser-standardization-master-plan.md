---
machine_kind: master-plan
machine_status: complete
machine_title: Vision Parser Standardization Master Plan
machine_goal: Standardize the Vision backend parsing contract across all supported entity families so route metadata, active-draft context, slot extraction, and follow-up handling stay consistent without family-specific drift.
---

# Vision Parser Standardization Master Plan

## Status

Completed.

## Goal

Standardize the Vision backend parsing contract across all supported entity families so route metadata, active-draft context, slot extraction, and follow-up handling stay consistent without family-specific drift.

## Parent God Plan

- God Plan: `Plan System God Plan`
- Machine-readable path: `.agents/god-plans/plan-system-god-plan.yaml`

## Scope

- Included:
  - shared parser contract normalization across quests, circles, applications, profiles, chat, discovery, and settings
  - route metadata and slot metadata consistency checks
  - follow-up context handling for active slot, draft snapshot, and recent correction signals
  - backend parser special-case reduction where a shared family-agnostic path is now possible
  - regression coverage for actual entity-family utterances and parser edge cases
  - docs and contract synchronization for the parser shape
- Excluded:
  - UI layout changes
  - new capability families outside the current Vision scope
  - unrelated backend domain rewrites outside `/vision`

## Current State

- The Vision backend already has a semantic envelope, route catalog, slot aliases, route examples, anti-examples, active-draft context, and repair-pass handling.
- The current parser is good enough to support real usage, but it still carries family-specific branches and route-specific fallback behavior that make future expansion harder to keep uniform.
- The frontend has already been simplified to a terminal-first shell, so the next meaningful gain is backend parser consistency rather than additional UI polishing.

## Desired State

- One stable parsing contract for all supported entity families.
- One predictable route metadata shape that the model can consume without per-family special casing.
- One follow-up context model for active slot, current draft, and correction memory.
- One shared normalization layer for parser output before it reaches conversation state or execution planning.
- A regression suite that catches drift when a new entity family or route is added.

## Child Plans

Execution order:
1. `.agents/vision-parser-contract-inventory-plan.md`
2. `.agents/vision-parser-branch-reduction-plan.md`
3. `.agents/vision-parser-regression-and-docs-plan.md`

1. `.agents/vision-parser-contract-inventory-plan.md`
- Role: inventory current route metadata, parser entrypoints, and family-specific branches so the remaining standardization work is explicit and bounded.
- Status: completed

2. `.agents/vision-parser-branch-reduction-plan.md`
- Role: reduce special-case parsing and normalization branches by moving shared behavior into a common parser contract and shared helpers.
- Status: completed

3. `.agents/vision-parser-regression-and-docs-plan.md`
- Role: validate the standardized parser shape with regression tests and keep the living docs and machine-readable contract surfaces aligned.
- Status: completed

## Pros

- Makes future entity-family expansion less brittle.
- Reduces the number of parser-specific edge cases hidden inside prompt understanding.
- Keeps the semantic contract easier to reason about for both humans and automation.

## Cons

- It is a backend-wide surface, so the work must stay sliced to avoid a broad risky rewrite.
- Some existing family-specific logic may still be intentionally useful and should only be removed when the shared path is clearly better.

## Dependencies

- `docs/vision-architecture-patterns.md`
- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/product-vision.md`
- `docs/regression-scenario-catalog.yaml`
- `docs/regression-scenario-catalog.md`

## Validation

- Targeted checks: route catalog tests, prompt-understanding tests, semantic audit tests, and parser-specific regression tests.
- Broader checks: entity-family flow regressions across create/view/update/delete and follow-up turns.
- Closeout checks: docs sync, contract freshness, and no remaining family-specific parser drift in the hot path.

## Completion Evidence

- Status: completed
- Child plan status: all child plans completed
- Validation evidence: `./mvnw -Dtest=VisionPromptUnderstandingServiceTest,VisionSemanticRouteCatalogServiceTest,VisionSemanticAuditMatrixTest test`
- Doc delta summary: parser standardization is complete across all supported Vision entity families
- Deferred work: none
