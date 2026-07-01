# Vision God Plan

## Status

Active.

## Purpose

Coordinate the long-running `/vision` program across multiple master plans while keeping current implementation reality visible.

The machine-readable source is `.agents/god-plans/vision-god-plan.yaml`.

## Current Position

The vision program has a working Phase 1 foundation:

- persisted backend conversation state
- turn history
- create_quest slot collection
- review-ready state
- review edit action
- feature-flagged execution adapter
- adaptive frontend route
- voice transcript and speech feedback
- applied-slot summaries and recent conversation resume state

The program is not yet at the full master vision target:

- intent routing is still mostly `CREATE_QUEST` or `UNSUPPORTED`
- prompt understanding is still create_quest-centric
- no generic planning or execution service exists yet
- the active UI can still feel like several stacked panels instead of one surface

## Immediate Program Priority

1. Reconcile stale master-plan notes with current implementation.
2. Add a generic semantic planning contract before adding more executors.
3. Refactor the frontend canvas so active work feels like one morphing surface.
4. Keep execution rollout behind typed backend flags and explicit review.

## Closeout Rule

This God Plan should remain active until the vision surface can coordinate at least two capabilities through the same backend-governed adaptive surface pattern.
