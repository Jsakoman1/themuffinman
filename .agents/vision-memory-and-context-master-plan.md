---
machine_kind: master-plan
machine_status: complete
machine_title: Vision Memory And Context Master Plan
machine_goal: Add a persistent vision-specific memory layer across docs, workflow
  entrypoints, and reusable test fixtures.
---

# Vision Memory And Context Master Plan

## Status

Complete.

This master plan hardens the durable repo context for `/vision` implementation so future sessions do not have to rediscover architecture decisions, generated-artifact rules, repeated failures, or test setup patterns.

## Goal

Add a persistent vision-specific memory layer across docs, workflow entrypoints, and reusable test fixtures.

## Why This Precedes The Larger Vision Master Plan

This is a preflight hardening batch for the larger `/vision` implementation roadmap.

It reduces token burn, repeated explanation, and closeout drift before broader backend orchestration and canvas work expands further.

## Scope

1. Add a stronger local context gateway for vision backend, API, frontend, and docs.
2. Lock immutable or near-immutable vision architecture decisions in one short record.
3. Capture repeat failure classes in a vision-specific failure memory.
4. Add a reusable feature-slice checklist for future vision batches.
5. Add reusable vision test fixtures for conversation, slot, location, and schedule setup.
6. Make generated-artifact expectations explicit for vision work.
7. Add a compact vision status ledger for cross-session continuity.
8. Wire the new artifacts into the compact workflow entrypoint so they are actually reused.

## Execution Order

### 1. Documentation Memory Layer

- [x] Add `docs/vision-context-gateway.md`
- [x] Add `docs/vision-decision-record.md`
- [x] Add `docs/vision-failure-memory.md`
- [x] Add `docs/vision-feature-slice-checklist.md`
- [x] Add `docs/vision-generated-artifact-policy.md`
- [x] Add `docs/vision-status-ledger.md`

### 2. Workflow Integration

- [x] Update `docs/vision-architecture-patterns.md`
- [x] Update `docs/codex-fast-path.md`
- [x] Update `docs/product-memory.md` with durable lessons from this hardening pass

### 3. Test Reuse Layer

- [x] Add reusable vision test fixtures under `apps/themuffinman/src/test/java/com/themuffinman/app/vision/testing/`
- [x] Refactor current vision tests to use the fixtures where it reduces setup drift

### 4. Validation

- [x] Run targeted vision backend tests
- [x] Run `./mvnw test -Dtest=AgentOperatingModelValidationTest`

## Closeout Rule

This plan is complete only when the new artifacts are present, referenced from the default vision read path, used by at least one current vision test surface, and validated.

## Completion Evidence

- Status: complete
- Validation evidence: targeted vision backend tests passed, `./mvnw test` passed, and `./mvnw test -Dtest=AgentOperatingModelValidationTest` passed
- Residual risk: future vision batches should keep reusing the compact context and failure memory surfaces instead of rediscovering them from scratch
