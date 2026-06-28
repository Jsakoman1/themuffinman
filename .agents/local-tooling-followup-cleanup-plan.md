# Feature Implementation Plan

Purpose: use the new local-tooling audits to complete one follow-up cleanup pass covering stale generated artifacts, safe read-only transaction coverage improvements, and a compact DTO cleanup shortlist.

## Scope

- [x] Create this plan before substantial edits when the change spans multiple files, layers, or control surfaces.
- [x] Break the feature into small enforceable implementation steps.
- [x] Run relevant gates after each meaningful implementation milestone.
- [x] Close the plan only after code, docs, and validation are all green.

## Feature Tasks

- [x] Refresh stale generated artifacts identified by the freshness audit.
- [x] Add safe `@Transactional(readOnly = true)` coverage to selected read-only or read-mostly services without changing mutation semantics.
- [x] Add or extend backend tests for the transaction configuration changes.
- [x] Produce a compact DTO drift cleanup shortlist from the existing API contract drift report.
- [x] Update docs and rerun local-tooling audits after the cleanup pass.

## Working Notes

- Avoid blanket class-level transaction changes on mixed services unless mutating methods are already explicitly annotated.
- Treat DTO zero-usage output as review-first, not delete-first.

## Completion Check

- [x] Backend validation passes.
- [x] Frontend validation passes if frontend contracts changed.
- [x] Docs and machine-operating artifacts are updated.
- [x] This plan is updated to reflect actual completion state before closing the task.
