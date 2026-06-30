# Product Memory

This document is the canonical memory layer for stable lessons, repeated patterns, and proven product logic that should survive across Codex sessions without staying in the active backlog.

It is separate from:
- `docs/implementation-backlog.md`, which tracks open product work
- `docs/agent-improvement-backlog.md`, which tracks open control-system work
- `docs/codex-local-tooling-todo.md`, which tracks local tooling ideas and follow-up work
- `docs/generated/local-tooling/failure-knowledge-base.json` and `docs/generated/local-tooling/failure-knowledge-base-summary.md`, which track recurring validation failures and fixes

## What Belongs Here

- Stable product lessons that have been verified in code, docs, or generated audits.
- Repeated implementation patterns that we want future work to reuse.
- Product rules that are broader than one feature but not broad enough to become a separate technical spec.
- Durable observations about how TheMuffinMan should grow as a Social Useful Network.
- Design principles that are useful across modules and do not belong in a backlog item.

## What Does Not Belong Here

- Open implementation work.
- Deferred work that still needs execution.
- Raw diagnostic logs.
- One-off bug reports that only matter until they are fixed.
- Aspirational ideas that are not yet framed as product principles.

## Canonical Capture Format

When a lesson is added, record it in a short, factual form:

- Lesson ID
- Source of the lesson
- What was learned
- Where it applies
- What to do next time
- Related docs, tests, or audits

## Maintenance Rules

- Add lessons only after the behavior, workflow, or implementation pattern is proven.
- Keep entries short and decision-first.
- Remove or narrow lessons if later product evidence shows the assumption was too broad.
- Do not duplicate active backlog items here.
- Keep this file aligned with `docs/business-logic.md` and `docs/domain-technical.md`.

## After-Plan Update Protocol

After every completed plan or master plan:

1. Check whether the change produced a durable lesson, repeated pattern, or product principle.
2. Append the lesson here if it is stable and reusable.
3. Update `docs/business-logic.md` or `docs/domain-technical.md` if the lesson changes the source of truth.
4. Refresh the failure knowledge base when the plan revealed a repeatable failure or fix pattern.
5. Run `make post-plan-memory-update plan=<plan-file> [manifest=<manifest-file>] [source=<diagnostic-report>]` to trigger the standard post-plan control loop, including the make-target index, documentation index, and staleness checks.

## Current Stable Lessons

- Completed work should leave the active backlog and remain only in plan-completion or retrospective artifacts.
- The repository benefits from canonical docs that explain repeated product behavior instead of rediscovering the same logic from code each session.
- Failure knowledge should stay compact and diagnostic, while product memory should stay about durable lessons and design patterns.
- The Social Useful Network vision should stay separate from implementation backlog items so the team can reason about product direction without polluting open work.
- When a workflow or interaction pattern proves repeatable, it should be written down once and then referenced from future plans and docs.
- Voice-first interaction should default to parallel audio and visual feedback because voice-only interaction is still too fragile for precision, noisy environments, and complex information structures.
- Speech features should still be backend-governed through a typed capability contract even when the actual STT/TTS runtime is browser-native.
- Manifest-backed validation is easiest to keep stable when canonical validator-facing command strings are recorded exactly as expected instead of only as equivalent path-prefixed variants.
- Workflow-expansion and agent-contract closeout should be treated as evidence-shape problems as much as code problems, because missing scenario coverage or canonical audit commands can block otherwise-correct implementations.
- Repeated validation and closeout rules are worth keeping in one machine-readable cheat sheet and auto-including in local context when manifest-backed work is detected, because that reduces rediscovery and validator churn.
- Future `/vision` implementation work should start from `docs/product-vision.md` for product direction and `docs/vision-architecture-patterns.md` for backend, API, frontend canvas, prompt-handling, and executor patterns before borrowing from legacy module screens.
- Future `/vision` execution work should treat `create_quest` as the first mutation scope, keep conversation continuity backend-persisted across text and voice turns, and gate real execution behind typed backend `vision.*` feature flags until the new orchestration layer proves stable.
