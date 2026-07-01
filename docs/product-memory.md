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
- Future `/vision` work should keep a compact repo-persistent memory layer for context, decision records, failure classes, generated-artifact policy, and current delivery status so sessions do not rediscover the same architecture and closeout rules from scratch.
- Reusable vision test fixtures should stay named around conversation builders, slot presets, location candidate presets, and schedule phrase presets, because that keeps conversation-flow tests focused on behavior instead of boilerplate setup.
- When a `/vision` review loop becomes part of the execution path, review corrections should stay on typed backend actions and explicit review targets instead of relying on client-generated natural-language prompts, because that reduces ambiguity and makes future executors reusable.
- `/vision` prompt intake should separate speech transcription from semantic field extraction, because one LLM understanding step can map a single utterance onto multiple explicit slots before deterministic validation decides what sticks.
- `/vision` semantic extraction should carry a focus-slot signal, because broad utterances need a primary target and should not fall back into description or reward by default.
- `/vision` should inherit the current requested slot as the fallback focus when the model does not choose one, because active clarification turns and review edits still need a stable semantic target.
- `/vision` semantic focus should live in a shared mapper, because prompt intake and review-edit follow-ups should use one backend path instead of each service inventing its own fallback rules.
- `/vision` semantic understanding should expose generic planning metadata above capability-specific slots, because candidate intent, confidence, capability id, and planning note can evolve before deterministic backend services allow broader execution.
- `/vision` execution planning should stay read-only and separate from mutation execution, because the backend needs to describe readiness and blockers without creating a second hidden commit path.
- `/vision` confirmed execution should sit behind a dedicated execution service boundary even when the first capability is only `create_quest`, because the orchestration layer should own the review-confirmation gate before it starts adding more executors.
- `/vision` read-only discovery capabilities should stay separate from execution planners, because browse/search surfaces need their own ranked-result payloads and should not inherit create-quest review semantics.
- `/vision` chat-opening capability should resolve the target user through the backend boundary instead of letting the frontend invent counterpart matching rules, because open-chat should obey the same trust and circle checks as the rest of chat.
- Shared prompt semantics should live in one backend support component for Vision and Admin Playground, because normalization and intent classification should stay consistent even when the trust boundary and execution authority are different.
- `/vision` shell should wrap context summary and prompt dock inside one unified adaptive surface, because the blank canvas should feel like one morphing room rather than a stack of separate panels.
- The blank `/vision` canvas should expose the first prompt composer inline and by default, because hiding the only input behind a launcher makes the empty state feel like a dead screen instead of an adaptive surface.
- The blank `/vision` shell should let the inline composer carry the idle entry copy, because a second shell-level hero duplicates the same intent and makes the empty state feel like layered chrome instead of one surface.
- The inline `/vision` composer should autofocus and expand with content, because a static prompt box makes the surface feel dead even when the backend is ready for the next turn.
- `/vision` action chips should appear contextually instead of as a fixed toolbar, because the surface should only show controls that matter in the current state.
- `/vision` should echo the latest voice transcript inline and offer slot-aware suggestion chips, because verification and next-step seeding work better when they live in the same surface as the current turn.
- `/vision` review state should stay compact and decision-first, with one confirm action and a small set of explicit field-edit chips, because nested windows make the review phase feel heavier than the task.
- `/vision` active states should promote a mode-specific hero signal, because review, discovery, blocked, and clarification turns need one strong top-line cue without reopening separate windows.
- `/vision` revealed context should read like a compact state rail, because progress and continuation cues should stay inside the same surface instead of rebuilding a second summary panel.
- `/vision` route shell state should move into reusable composables where practical, because the route should stay thin while adaptive surface rules remain easy to reuse and extend.
- Shared agent abstractions should standardize policy and capability checks without collapsing trust boundaries, because `Vision` must stay user-scoped while admin execution can grow only through separate admin-scoped gates.
- `/vision` transcript feedback should show the active slot target next to the heard text, because users need to see where the backend mapped a spoken turn, not just the raw transcription.
- `/vision` suggestion chips should adapt to the current slot and the latest transcript, because the best follow-up is often a transform of what the user already said rather than a fresh generic command.
- `/vision` review confirmation should stay visually restrained, because the summary should read like a calm decision surface rather than a dominant approval dialog.
- `/vision` composers should show the current slot value when one already exists, because keep/replace decisions are faster when the existing state is visible inline.
- `/vision` responses should expose an explicit applied-slot signal for the current turn, because frontend feedback should reflect actual backend changes rather than reconstructed guesses.
- `/vision` review surfaces should render the current turn's applied slots inline, because the immediate state change is easier to trust than the final summary alone.
- `/vision` recent conversation entries should show the latest applied slot badges inline, because resume affordances work better when they mirror the current turn's evidence.
- `/vision` long-session continuity should come from compact backend summaries rather than raw transcript reconstruction, because the resume rail stays calmer when the surface reads state instead of replaying history.
- `/vision` stale or completed continuation state should be marked explicitly and kept out of the active canvas by default, because unfinished and finished work should not compete for the same visual priority.
- Legacy frontend decommission should happen route-first, because removing redirects and non-essential module entries gives immediate simplification without risking admin or detail-bridge workflows.
