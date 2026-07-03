---
machine_kind: master-plan
machine_status: complete
machine_title: Vision Schedule Date Time Split Master Plan
machine_goal: Replace the current single-slot `scheduled_at` conversation model in
  `/vision` with a layered scheduling model that treats date and time as separate
  user-facing concepts while still deriving one absolute execution timestamp only
  when the backend has enough deterministic information.
---

# Vision Schedule Date Time Split Master Plan

## Status

Complete.

## Goal

Replace the current single-slot `scheduled_at` conversation model in `/vision` with a layered scheduling model that treats date and time as separate user-facing concepts while still deriving one absolute execution timestamp only when the backend has enough deterministic information.

The immediate product goal is to let users say things like `next Tuesday` without forcing an artificial timestamp decision too early, then ask only for the missing time when needed.

## Parent God Plan

- God Plan: `.agents/god-plans/vision-god-plan.md`
- Machine-readable path: `.agents/god-plans/vision-god-plan.yaml`

## Scope

- Included: Vision slot model, prompt understanding, deterministic schedule parsing, clarification flow, review summary, execution planning, create_quest execution adapter inputs, frontend canvas contract, frontend review rendering, docs, and regression tests.
- Excluded: non-Vision scheduling models outside the current `/vision` conversation flow, broad legacy module scheduling redesign, and speculative support for fuzzy recurring schedules.

## Why This Workstream Exists

The current `scheduled_at` shape is efficient for execution but unnatural for conversation:

- users naturally provide date and time in separate turns
- date-only phrases like `next Tuesday` are valid intent but incomplete execution state
- the current model encourages fallback defaults or repeated retries on one overloaded slot
- review and canvas copy cannot clearly distinguish `day known, time missing` from `nothing known yet`

This workstream should move `/vision` toward a truer conversational contract:

- `schedule_mode`
- `scheduled_date`
- `scheduled_time`
- derived `scheduled_at` only when both date and time are execution-ready

## Proposed Target Model

User-facing scheduling state:

- `schedule_mode`: `fixed` or `agreement`
- `scheduled_date`: normalized calendar date when known
- `scheduled_time`: normalized local time when known
- optional follow-up shape for later consideration: `scheduled_time_precision` or `scheduled_time_window`

Execution-facing state:

- `scheduled_at`: derived backend value used only when the schedule is fixed and both date and time are present

Conversation rule:

- date-only answers should satisfy the date slot
- time-only answers should satisfy the time slot when a date already exists
- fixed schedule execution remains blocked until both date and time are valid

## Child Plans

1. `.agents/vision-schedule-domain-contract-plan.md`
- Role: redesign the backend slot vocabulary, parser behavior, semantic mapping, clarification order, and execution-planning state transitions around separate date/time concepts.
- Status: complete

2. `.agents/vision-schedule-api-canvas-plan.md`
- Role: update DTOs, canvas blocks, review summaries, and frontend rendering so the surface can show partial schedule state without pretending an absolute timestamp already exists.
- Status: complete

3. `.agents/vision-schedule-execution-migration-plan.md`
- Role: keep create_quest execution deterministic by deriving `scheduled_at` only at the execution boundary, then remove temporary defaults and align validation/runtime behavior.
- Status: complete

4. `.agents/vision-schedule-validation-docs-plan.md`
- Role: reconcile product docs, technical docs, agent-operating rules, and regression coverage with the new scheduling contract.
- Status: complete

## Implementation Sequence

1. Lock the vocabulary and slot order in backend/domain docs before code changes spread.
2. Change backend conversation and parser behavior so date and time can be collected independently.
3. Update API and canvas rendering so partial schedule state is visible and calm.
4. Move absolute timestamp derivation to the execution boundary.
5. Remove temporary default-time behavior once the split model is fully active.
6. Reconcile docs, tests, and God Plan/master-plan state.

## Pros

- Matches how users naturally speak.
- Removes pressure to invent default times for date-only input.
- Makes clarification prompts narrower and more precise.
- Lets review and canvas surfaces show partial schedule progress honestly.
- Reduces schedule-specific hacks inside semantic mapping and retry flows.

## Cons

- Touches backend slots, API contract, frontend rendering, docs, and tests in one workstream.
- Introduces a short migration period where derived `scheduled_at` and split fields must coexist safely.
- Will likely require updating current fixtures, presets, and review assumptions across several test layers.

## Risks

- If slot naming drifts between backend, DTOs, and frontend, `/vision` can regress into partial-state confusion.
- If execution adapters start reading partial schedule fields directly, mutation safety can weaken.
- If the split model is implemented only in Vision but not documented in the shared agent-operating rules, future automation can reintroduce `scheduled_at`-only assumptions.

## Dependencies

- `docs/vision-architecture-patterns.md`
- `.agents/vision-adaptive-architecture-master-plan.md`
- existing `create_quest` execution-planning and review flow
- Vision shared prompt semantics support and parser services

## Validation

- Targeted checks: backend unit tests for parser/slot/clarification flow, conversation tests for split collection, frontend type-check/build after DTO changes
- Broader checks: documentation sync for `docs/business-logic.md`, `docs/domain-technical.md`, `docs/vision-architecture-patterns.md`, and agent-operating artifacts if scheduling rules or slot names change
- Closeout checks: `make audit-todo`, `make audit-plan-completion plan=.agents/vision-schedule-date-time-split-master-plan.md`

## Completion Evidence

- Status: complete
- Child plan status: all listed child plans completed in the same implementation batch
- Validation evidence: `./mvnw test -Dtest=VisionScheduleParserServiceTest,VisionConversationServiceTest,VisionExecutionPlannerTest`, `npm run type-check`, `npm run build`
- Doc delta summary: `/vision` fixed scheduling now collects day and time separately, renders partial schedule state honestly, and derives `scheduled_at` only at execution time
- Deferred work: recurring/fuzzy schedule modeling remains out of scope unless explicitly promoted later
