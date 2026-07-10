---
machine_kind: plan
machine_status: complete
machine_title: Closeout Evidence Contract
machine_goal: Define the exact evidence shape for plan completion and manifest closeout so manual drift becomes harder.
---

# Closeout Evidence Contract

## Status

Complete.

## Goal

Define the exact evidence shape for plan completion and manifest closeout so manual drift becomes harder.

## Parent Master Plan

- Master plan: `.agents/implementation-system-closeout-hardening-master-plan.md`

## Scope

- Included: changed files, validation evidence, doc delta summary, child plans, temp work products, and archive-only evidence bans.
- Included: define which fields are derived automatically and which fields stay explicit in plan and manifest closeout.
- Excluded: cleanup implementation and runtime orchestration.

## Implementation Notes

- Make the evidence contract explicit for both temporary plans and feature manifests so one shape can drive both closeout paths.
- Prefer derived `changed files`, `temp work products`, and `generated artifacts` where the repo state already knows the answer.
- Keep explicit text fields only where judgment or summary is still needed.

## Validation

- Targeted checks: completion and closeout audits reject placeholder evidence fields.
- Broader checks: the same evidence shape works for plan completion and manifest closeout.
- Closeout checks: evidence is derived where possible and explicit where required.
- The contract should make it obvious when evidence is missing versus merely deferred.

## Completion Evidence

- Status: complete
- Changed files: `scripts/audits/audit-plan-completion.rb`, `scripts/audits/enforce-feature-closeout.rb`, `scripts/feature-closeout-audit.sh`, `scripts/audits/closeout-driver.rb`, `docs/validation-memory.md`, `docs/validation-memory.json`
- Validation evidence: `make feature-closeout-audit manifest=.agents/feature-manifests/chat-prod-ready-manifest.yaml`, `make closeout-driver plan=.agents/chat-prod-ready-plan.md manifest=.agents/feature-manifests/chat-prod-ready-manifest.yaml`
- Doc delta summary: completion evidence now rejects placeholder fields, archive-only live evidence, and missing closeout-driver proof while allowing derived closeout reports to carry the final boundary.
- Deferred work: none
