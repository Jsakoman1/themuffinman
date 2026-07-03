---
machine_kind: plan
machine_status: planning only. no runtime implementation in this plan
machine_title: Vision Preflight Plan
machine_goal: Lock the minimum architecture decisions that should be treated as settled
  before the first real `/vision` execution implementation starts.
---

# Vision Preflight Plan

## Status

Planning only. No runtime implementation in this plan.

## Purpose

Lock the minimum architecture decisions that should be treated as settled before the first real `/vision` execution implementation starts.

This plan exists to reduce repeated re-analysis and to keep backend, API, and frontend work aligned with the long-term blank-canvas direction.

## Decisions To Freeze

### 1. First Executor Scope

The first production mutation capability for `/vision` should be `create_quest`.

Reason:
- high user value
- bounded actor context
- safe stepwise slot collection
- low destructive risk compared with edit/delete or multi-actor flows

Initial execution shape:
- collect missing quest fields one at a time
- build a review state
- require explicit confirmation
- execute through existing quest domain validation and service boundaries

### 2. Conversation Persistence Strategy

Phase 1 should use persisted backend conversation state, not a client-managed opaque state token.

Reason:
- voice and text should share the same turn history
- stepwise clarification needs reliable cross-turn continuity
- adaptive canvas state should stay recoverable across refresh/reconnect cases
- future executor expansion will need auditable turn and review history

The persistence model can stay minimal at first, but the source of truth should be backend-owned conversation records.

### 3. Feature-Flag Rollout Boundary

All real `/vision` mutation execution must ship behind typed backend configuration.

Reason:
- safe staged rollout
- admin-only or developer-only exposure during early validation
- quick disable path if orchestration or executor behavior drifts

Preferred control shape:
- one central `vision.*` configuration object
- explicit read/planning vs execution capability switches
- execution disabled by default outside intended environments

### 4. Architecture Boundary

New `/vision` implementation should proceed from dedicated `vision` orchestration and API layers, not by extending legacy dashboard read-model assumptions.

Reason:
- blank-canvas behavior needs backend-prepared adaptive state
- stepwise conversations do not fit legacy dashboard DTO composition cleanly
- future surface unification depends on reducing coupling to old screen contracts

## Required Follow-On Work Before Runtime Execution

1. Define the persisted conversation model and lifecycle.
2. Define versioned canvas primitives for backend-prepared UI state.
3. Define the `create_quest` slot contract and review state.
4. Add typed `vision.*` operational config for rollout gating.
5. Define executor adapters that call existing quest services without bypassing domain validation.

## Exit Condition

This preflight is complete when these decisions are reflected in the durable `/vision` architecture docs and referenced by the master plan.
