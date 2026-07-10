---
machine_kind: plan
machine_status: complete
machine_title: Closeout Preflight
machine_goal: Add a preflight that reports closeout blockers before the first final audit runs.
---

# Closeout Preflight

## Status

Complete.

## Goal

Add a preflight that reports closeout blockers before the first final audit runs.

## Parent Master Plan

- Master plan: `.agents/implementation-system-closeout-hardening-master-plan.md`

## Scope

- Included: git diff inspection, generated-artifact freshness, temp work products, archive-only evidence, and plan state.
- Included: produce a blocker summary artifact that the driver can read before it starts final audits.
- Excluded: state-machine rules and driver execution order.

## Implementation Notes

- The preflight should be cheap enough to run on every broad closeout path.
- Prefer one compact blocker report over multiple scattered warnings.
- If possible, derive blockers from existing generated audits instead of re-implementing the underlying checks.

## Validation

- Targeted checks: preflight output shows the correct blockers for a changed closeout scope.
- Broader checks: preflight integrates cleanly with the existing closeout path.
- Closeout checks: the first final audit starts only after blockers are known.
- The blocker report should distinguish hard failures from cleanup-only noise.

## Completion Evidence

- Status: complete
- Changed files: `scripts/audits/closeout-driver.rb`, `docs/generated/local-tooling/closeout-driver/*`, `docs/generated/local-tooling/generated-artifact-freshness.json`
- Validation evidence: `make closeout-driver plan=.agents/chat-prod-ready-plan.md manifest=.agents/feature-manifests/chat-prod-ready-manifest.yaml`
- Doc delta summary: preflight now checks generated freshness, plan status, temp work products, and archive-only evidence before final closeout begins.
- Deferred work: none
