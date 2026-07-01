# Vision God Plan

## Status

Active.

## Purpose

Coordinate the long-running `/vision` program across multiple master plans while keeping current implementation reality visible.

The machine-readable source is `.agents/god-plans/vision-god-plan.yaml`.

## Current Position

The vision program now has a completed Phase 1 foundation and a completed semantic/execution-planning follow-up:

- persisted backend conversation state
- turn history
- create_quest slot collection
- review-ready state
- review edit action
- feature-flagged execution adapter
- adaptive frontend route
- voice transcript and speech feedback
- applied-slot summaries and recent conversation resume state
- read-only execution candidate planning in the review flow
- read-only quest discovery and ranked results in the same adaptive surface
- backend-governed intent switching when the current surface task changes
- open chat routing to the existing chat boundary for explicit contact targets
- inline idle composer, mode-specific active hero states, and compact in-shell context reveal
- reusable route-shell surface state that keeps the view thinner while preserving the same adaptive behavior
- shared prompt semantics across Vision and Admin Playground
- versioned `/vision/conversations/turns` request contract with explicit client capabilities and state version fields
- summary-first long-session continuity that reads compact backend resume state instead of replaying older transcript text
- read-only execution planning above the create_quest review flow
- a shared backend prompt-semantics support component reused by both surfaces
- voice input, transcription, speech output, and visual feedback
- durable memory/context continuity and recent-session recovery support
- dedicated execution boundary service around the first `create_quest` executor
- dedicated chat execution boundary for `open_chat`
- Vision-native quest and application detail routes that can absorb the legacy detail bridges

The program is not yet at the full master vision target:

- intent routing still needs broader capability coverage beyond create_quest and read-only discovery
- prompt understanding now has a shared semantic boundary, but still needs more modern prompt/voice shaping
- no generic planning or execution service exists yet
- the active UI is closer to one surface now, but broader capability coverage still needs to preserve the same calm continuation model
- the repository still carries a broad legacy route-based frontend that should be decommissioned while the product is still in dev phase

## Immediate Program Priority

1. Reconcile stale master-plan notes with current implementation.
2. Expand capability coverage beyond create_quest and read-only discovery without weakening typed planning boundaries.
3. Keep tightening continuation and memory behavior so the surface stays calm as more capabilities arrive.
4. Keep execution rollout behind typed backend flags and explicit review.
5. Continue the rollout and capability slices through the shared semantic boundary.
6. Preserve the new split schedule contract as capability coverage expands, instead of collapsing back into single-slot timestamp heuristics.
7. Decommission the unused legacy route-based frontend so Vision becomes the dominant runtime instead of one surface among many.

## Recent Completed Child Plan

- `.agents/vision-memory-and-context-master-plan.md`
- Goal: harden the durable repo context for `/vision` implementation so future sessions do not have to rediscover architecture decisions, generated-artifact rules, repeated failures, or test setup patterns.
- `.agents/vision-backend-orchestration-plan.md`
- Goal: create a backend-first orchestration layer for the future blank-canvas `/vision` experience.
- `.agents/vision-voice-master-plan.md`
- Goal: add backend-first STT and TTS support for the experimental `/vision` surface while keeping the implementation documented, validated, and aligned with source-of-truth rules.
- `.agents/vision-generic-semantic-planning-plan.md`
- Goal: separate generic turn planning signals from create_quest-specific slot extraction.
- `.agents/vision-execution-planning-plan.md`
- Goal: introduce a read-only execution-planning boundary above the existing create_quest review flow.
- `.agents/vision-canvas-execution-surface-plan.md`
- Goal: surface the execution candidate in the frontend canvas without opening a separate popup.
- `.agents/vision-surface-unification-plan.md`
- Goal: merge the remaining stacked-panel feel into one coherent adaptive surface.
- `.agents/vision-quest-discovery-plan.md`
- Goal: add a read-only quest discovery surface that routes browse/search prompts into ranked quest recommendations inside the same adaptive frame.
- `.agents/vision-modern-prompt-master-plan.md`
- Goal: complete the Phase 1 modern surface so text and voice share one calmer adaptive agent shell.
- `.agents/vision-schedule-date-time-split-master-plan.md`
- Goal: separate day and time collection in `/vision` scheduling so conversation state, review, and execution no longer depend on one overloaded `scheduled_at` slot.
- `.agents/legacy-frontend-decommission-master-plan.md`
- Goal: remove the unused legacy route-based frontend in controlled batches and converge on a Vision-first frontend runtime.
- Status note: all six child plans are now complete, so the next step is implementation of the delete and cleanup batches rather than more decommission planning.

## Active Child Plan

- `.agents/vision-adaptive-architecture-master-plan.md`
- Goal: continue the main adaptive vision architecture program now that the memory and context hardening slice is complete.

## Closeout Rule

This God Plan should remain active until the remaining child master plans are complete, deferred, or replaced and the vision surface can continue adding capabilities through the same backend-governed adaptive surface pattern without reintroducing forked policy logic.
