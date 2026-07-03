---
machine_kind: plan
machine_status: unknown
machine_title: Vision Semantic Slot Guardrails Plan
machine_goal: Reduce create-quest parser leakage so text and voice prompts stop spilling
  into the wrong slots, especially description, reward, and location.
---

# Vision Semantic Slot Guardrails Plan

## Goal

Reduce create-quest parser leakage so text and voice prompts stop spilling into the wrong slots, especially description, reward, and location.

## Scope

1. tighten create-quest slot merge heuristics in `VisionSlotService`
2. make semantic extraction less eager when a prompt contains competing slot signals
3. add regression tests for mixed prompts and low-confidence fallback behavior
4. update vision docs if user-visible parsing behavior changes materially

## Sequence

1. backend parser guardrails
2. regression tests
3. docs sync and validation

