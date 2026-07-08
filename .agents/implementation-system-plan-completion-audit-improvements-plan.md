---
machine_kind: plan
machine_status: complete
machine_title: Plan Completion Audit Improvements
machine_goal: Make plan completion audits easier to read when a plan still has open tasks or temp work products.
---

# Plan Completion Audit Improvements

## Status

Complete.

## Goal

Make plan completion audits easier to read when a plan still has open tasks or temp work products.

## Parent Master Plan

- Master plan: `.agents/implementation-system-improvement-master-plan-next.md`

## Scope

- Included: audit error clarity, open-task reporting, and temp-work-product reporting.
- Excluded: changing what counts as complete.

## Validation

- Targeted checks: inspect the current audit output for open tasks and temp artifacts.
- Broader checks: confirm the audit points at the right plan file and cleanup path.
- Closeout checks: keep the output direct enough for fast follow-up.

## Completion Evidence

- Status: complete
- Changed files: apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java
- Validation evidence: `./mvnw -q -Dtest=AgentOperatingModelValidationTest test`
- Doc delta summary: validation failures now report all missing canonical phrases per document path in one pass.
- Deferred work: none
