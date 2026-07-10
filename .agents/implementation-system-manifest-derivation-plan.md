---
machine_kind: plan
machine_status: complete
machine_title: Manifest Evidence Derivation
machine_goal: Derive more closeout evidence from repo state so manifests need less manual copying.
---

# Manifest Evidence Derivation

## Status

Complete.

## Goal

Derive more closeout evidence from repo state so manifests need less manual copying.

## Parent Master Plan

- Master plan: `.agents/implementation-system-closeout-hardening-master-plan.md`

## Scope

- Included: derived changed files, generated artifacts, temp work products, and closeout evidence derivation.
- Included: reduce repeated manifest inputs where `git status`, existing generated reports, or plan files already provide the same truth.
- Excluded: driver sequencing and plan status enforcement.

## Implementation Notes

- Treat derivation as a way to remove manual duplication, not as a way to hide evidence.
- Prefer derived evidence only when it is reproducible from the current repository state.
- Leave explicit fields in place when human judgment or summary is still required.

## Validation

- Targeted checks: derived fields match the actual repo state for the active batch.
- Broader checks: manifest closeout still passes when the derived state is used.
- Closeout checks: manual fields shrink without losing evidence quality.
- Check that the reduced manual surface still satisfies closeout audits without extra shims.

## Completion Evidence

- Status: complete
- Changed files: `scripts/feature-closeout-audit.sh`, `scripts/audits/closeout-driver.rb`, `scripts/audits/local_tooling_extended_tools.rb`, `scripts/local_tooling_common.rb`
- Validation evidence: `make feature-closeout-audit manifest=.agents/feature-manifests/chat-prod-ready-manifest.yaml`, `make closeout-driver plan=.agents/chat-prod-ready-plan.md manifest=.agents/feature-manifests/chat-prod-ready-manifest.yaml`
- Doc delta summary: closeout can now derive final evidence from generated driver reports and archive-only live evidence is removed from the manifest-backed closeout surface.
- Deferred work: none
