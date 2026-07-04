# Vision Master Plan

## Status

In progress.

## Goal

Track the current `/vision` implementation state and drive the remaining semantic, executor, and surface work to maturity.

## Parent God Plan

- God Plan: `Plan System God Plan`
- Machine-readable path: `.agents/god-plans/plan-system-god-plan.yaml`

## Scope

- Included: semantic envelope hardening, entity resolution, route catalog growth, executor expansion, and surface simplification.
- Excluded: broad backend rewrites outside `/vision`, full historical planning reconstruction, and frontend-only workflow inference.

## Current State

What already exists:
- persisted backend conversation state
- first executor scope is `create_quest`
- semantic envelope with normalized prompt, intent metadata, entity family, target entity query, slot candidates, confidence, and ambiguity data
- resolver layer for quest, circle, user, and application entities
- OpenAI-backed prompt understanding plus English-only emergency fallback
- route catalog and read-only discovery support
- review-ready execution planning and explicit confirmation flow
- terminal-first, backend-driven canvas and route shell
- multiple mutating pilots already in place for quest, profile, application, circle request, and circle operations
- `vision` docs, memory, architecture patterns, and status ledger are already established

What this means:
- the foundation is real, not hypothetical
- the remaining work is mostly hardening, broadening, and simplifying
- the main risk now is semantic drift across capabilities and entity families, not lack of a foundation

## Desired State

- one stable semantic boundary that can understand any supported language or noisy grammar and still emit a predictable English backend envelope
- one canonical entity-resolution path per entity family
- one intent-to-DTO/use-case registry rather than ad hoc service branching
- one clean execution boundary per capability with explicit confidence thresholds
- a calm adaptive surface that reveals only the next useful thing
- a smaller set of carefully named capability executors rather than many loosely wired heuristics

## Child Plans

Execution order:
1. `.agents/vision-capability-expansion-plan.md`
2. `.agents/vision-surface-simplification-plan.md`
3. `.agents/vision-validation-reintegration-plan.md`

1. `.agents/vision-semantic-hardening-plan.md`
- Role: tighten prompt understanding, ambiguity handling, alias resolution, and semantic envelope quality.
- Status: in progress

2. `.agents/vision-capability-expansion-plan.md`
- Role: add the next capability slices only after the semantic boundary and entity resolution are stable.
- Status: completed

3. `.agents/vision-surface-simplification-plan.md`
- Role: keep the frontend calm, backend-governed, and thin as capabilities expand.
- Status: completed

4. `.agents/vision-validation-reintegration-plan.md`
- Role: restore the minimum validation and generated-artifact checks needed to keep `/vision` honest.
- Status: completed

## Pros

- Mirrors the real state of implementation instead of the old roadmap fantasy.
- Makes the next few `/vision` slices easier to prioritize.
- Keeps the semantic work separate from UI polish and from capability expansion.

## Cons

- Still broad enough that it needs disciplined slicing.
- The surface is already advanced, so the remaining work is more subtle than the initial foundation build.

## Dependencies

- `docs/vision-status-ledger.md`
- `docs/vision-architecture-patterns.md`
- `docs/vision-decision-record.md`
- `docs/vision-feature-slice-checklist.md`

## Validation

- Targeted checks: semantic-envelope tests, resolver tests, and route-catalog tests.
- Broader checks: capability-specific integration tests as each executor family expands.
- Closeout checks: keep the ledger aligned with the actual implemented surface.

## Completion Evidence

- Status: draft
- Child plan status: draft
- Validation evidence: not run
- Doc delta summary: sets the remaining vision roadmap
- Deferred work: broader executor expansion and surface reduction remain intentionally staged
