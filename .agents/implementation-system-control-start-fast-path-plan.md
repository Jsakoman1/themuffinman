---
machine_kind: plan
machine_status: complete
machine_title: Implementation System Control Start Fast-Path
machine_goal: Reduce unnecessary blocking in control-start by separating implementation-critical checks from slower global freshness concerns.
---

# Implementation System Control Start Fast-Path

## Status

Complete.

## Goal

Reduce unnecessary blocking in control-start by separating implementation-critical checks from slower global freshness concerns.

## Scope

- Included: `scripts/audits/local_tooling_extended_tools.rb`, `make control-start`, and the generated control snapshot.
- Excluded: codex-context provider fixes, closeout automation, and batch entrypoint work.

## Checklist

- [x] Identify which checks must remain blocking for implementation safety.
- [x] Move non-critical freshness or regeneration checks into warnings or deferred validation where possible.
- [x] Keep the control snapshot compact and readable after the split.

## Validation

- Targeted checks: `make control-start`
- Broader checks: `make audit-generated-artifact-freshness`

## Completion Evidence

- Status: complete
- Validation evidence: `make control-start`, `make control-refresh-full`
- Doc delta summary: control-start now uses a fast core path and the slower freshness pass is available through control-refresh-full.
