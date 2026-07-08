---
machine_kind: plan
machine_status: complete
machine_title: Validation Evidence Quality
machine_goal: Make evidence records more concrete so later sessions can see exact commands, scopes, and skipped reasons.
---

# Validation Evidence Quality

## Status

Complete.

## Goal

Make evidence records more concrete so later sessions can see exact commands, scopes, and skipped reasons.

## Parent Master Plan

- Master plan: `.agents/implementation-system-improvement-master-plan-next.md`

## Scope

- Included: evidence field quality, exact command capture, scope capture, and skipped-reason capture.
- Excluded: changing the validation policy itself.

## Validation

- Targeted checks: review the validation evidence schema and quality audit.
- Broader checks: confirm the evidence format is usable after a broad batch.
- Closeout checks: ensure skipped checks can be understood later without rereading the whole batch.

## Completion Evidence

- Status: complete
- Changed files: apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java, docs/agent-operating-model.md, docs/validation-memory.md, docs/validation-memory.json
- Validation evidence: `./mvnw -q -Dtest=AgentOperatingModelValidationTest test`
- Doc delta summary: validation failures now report all missing canonical phrases per document path in one pass.
- Deferred work: none
