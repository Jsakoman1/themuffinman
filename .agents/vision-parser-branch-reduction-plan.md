---
machine_kind: plan
machine_status: complete
machine_title: Vision Parser Branch Reduction
machine_goal: Reduce Vision parser special cases by moving shared behavior into a common contract and shared helpers.
---

# Vision Parser Branch Reduction

## Status

Completed.

## Goal

Reduce Vision parser special cases by moving shared behavior into a common contract and shared helpers.

## Parent Master Plan

- Master Plan: `Vision Parser Standardization Master Plan`
- Machine-readable path: `.agents/vision-parser-standardization-master-plan.md`

## Scope

- Included:
  - shared prompt-understanding behavior
  - unified slot merge and normalization rules where they are truly family-agnostic
  - reduction of ad hoc parser branches and one-off fallbacks
  - explicit handling of active slot, draft snapshot, and recent correction context
- Excluded:
  - new entity families
  - UI work
  - unrelated workflow rewrites

## Checklist

- [x] Move family-agnostic parser logic into shared helpers or shared contract objects.
- [x] Reduce one-off branch handling where the route catalog can express the same rule.
- [x] Keep necessary family-specific logic explicit and minimal.
- [x] Verify the parser output still matches the backend execution boundary contract.

## Validation

- Targeted checks: parser unit tests and route-catalog tests that exercise shared branches.
- Broader checks: follow-up turns across multiple entity families.
- Closeout checks: no accidental regressions in active draft handling or slot filling.

## Completion Evidence

- Status: completed
- Validation evidence: `./mvnw -Dtest=VisionPromptUnderstandingServiceTest,VisionSemanticRouteCatalogServiceTest,VisionSemanticAuditMatrixTest test`
- Doc delta summary: shared parser behavior now replaces the remaining family-specific drift in target resolution and slot normalization
- Deferred work: regression coverage and docs sync remain open
