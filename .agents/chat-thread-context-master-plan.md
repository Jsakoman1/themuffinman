---
machine_kind: master-plan
machine_status: complete
machine_title: Chat Thread Context Master Plan
machine_goal: Evolve chat from fixed direct conversations into a thread-based backend with multi-participant membership, role assignment, and context-owned rooms for circles, quests, and applications.
---

# Chat Thread Context Master Plan

## Status

Complete.

## Goal

Evolve chat from fixed direct conversations into a thread-based backend with multi-participant membership, role assignment, and context-owned rooms for circles, quests, and applications.

## Scope Rules

- Focus on backend schema, entities, repositories, services, controllers, tests, contracts, and docs.
- Keep frontend implementation out of scope, but allow generated contracts to refresh when backend DTOs or endpoints change.
- Preserve the existing direct-chat API surface where practical while extending it to thread-aware summaries and messages.
- Use additive schema changes so existing direct conversations remain readable and writable after migration.

## Slice Order

1. `.agents/chat-thread-context-schema-plan.md`
- Role: additive thread schema, participant membership model, context ownership fields, service-level access rules, and direct-chat compatibility baseline
- Status: complete

2. `.agents/chat-thread-context-closeout-plan.md`
- Role: docs sync, generated contract refresh, validation evidence, and final closeout across the chat-v2 backend slice
- Status: complete

## Continuation Rule

Continue through the child plans in order without pausing between slices unless a real blocker, scope conflict, or approval need appears.

## Dependencies

- `docs/codex-fast-path.md`
- `docs/feature-delivery-workflow.md`
- `docs/business-logic.md`
- `docs/domain-technical.md`

## Validation

- Targeted checks: chat service and controller tests for direct, group, circle, quest, and application thread flows.
- Broader checks: `./mvnw test`, contract validation, and required closeout audits after doc sync.

## Completion Evidence

- Status: complete
- Child plan status: all child plans complete
- Validation evidence: `cd apps/themuffinman && ./mvnw -q test` passed after chat thread-context code, docs, and manifest sync
- Doc delta summary: chat now supports manual group threads plus canonical circle, quest, and application rooms with explicit participant membership, roles, and context ownership metadata
- Deferred work: none recorded during this slice
