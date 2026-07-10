---
machine_kind: master-plan
machine_status: complete
machine_title: Vision Ledger TODO Refresh Master Plan
machine_goal: Refresh `docs/vision-status-ledger.md` so the open `In Progress` and
  `Deferred` items reflect the real `/vision` codebase instead of older capability-expansion
  wording.
---

# Vision Ledger TODO Refresh Master Plan

## Status

Complete.

## Goal

Refresh `docs/vision-status-ledger.md` so the open `In Progress` and `Deferred` items reflect the real `/vision` codebase instead of older capability-expansion wording.

## Scope

- Included: ledger wording cleanup for open `/vision` items, stale-item removal, and alignment with the current code and test surface.
- Excluded: new `/vision` feature work, backend logic changes, frontend behavior changes, and unrelated documentation rewrites.

## Child Plans

1. Ledger audit and rewrite
- Role: compare current open ledger items against the implemented code and tests, then rewrite only the outdated open-item wording.
- Status: complete

2. Closeout verification
- Role: re-check the revised ledger wording against the current `/vision` code references and confirm that no stale open-item text remains.
- Status: complete

## Implementation Steps

- [x] Replace stale capability-expansion language in the ledger with wording that reflects already-implemented route and test coverage.
- [x] Keep only genuinely open `/vision` work in `In Progress` and `Deferred`.
- [x] Preserve existing `Done` and `Blocked By Design` sections unless the audit shows wording drift there too.

## Validation

- Primary check: compare revised ledger items against the current `/vision` code and test references already audited in this batch.
- Closeout check: confirm the revised ledger no longer describes broad capability expansion as future work when those capabilities already exist.

## Completion Gate

- Do not mark this master plan complete until the ledger text is updated and the remaining open items are defensible against the current repository state.

## Completion Evidence

- Status: complete
- Updated file: `docs/vision-status-ledger.md`
- Verification summary: the open `/vision` ledger items now keep only the still-active hardening, open-chat, executor-expansion, stale-task, review-edit, and locale/location follow-ups, while the stale broad capability-expansion wording has been removed.
