---
machine_kind: plan
machine_status: complete
machine_title: Frontend Home And Work Entry Plan
machine_goal: Build `Home` and `Work` as the first true module landing surfaces and
  establish the reusable interaction grammar for summaries, lists, and detail entry.
---

# Frontend Home And Work Entry Plan

## Status

Complete.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer if `Home` requires new backend-prepared read surfaces; otherwise tier2 for initial shell-visible surfaces
- Scope: `Home`, `Work`, and first real shared UI contract proof
- Out of scope: remaining module entries and full Vision integration
- Manifest decision: likely resolver-driven, with elevated chance of required manifest if backend read-model work joins the slice
- Manifest path: TBD
- Master plan: `.agents/frontend-user-shell-navigation-master-plan.md`

## Routing Snapshot

- Context commands:
  - inspect workmarket DTOs, navigation DTOs, current `/vision` discovery patterns, and any reusable summary sections
  - `make endpoint-contract-packs`
- Routing commands:
  - `make audit-router files=<csv>`
  - `make audit-doc-sync-required-surfaces files=<csv>`
  - `make audit-manifest-decision files=<csv>`
- Validation commands:
  - `npm run type-check`
  - `npm run build`
  - targeted backend checks if new home/work read surfaces are added
- Closeout commands:
  - `make audit-todo`
  - `make audit-plan-completion plan=.agents/frontend-home-and-work-entry-plan.md`
- Doc sync commands:
  - resolver-driven business/domain/product docs
- Generated artifact commands:
  - resolver-driven if contracts move

## Preferred Local Tooling

- Tooling map: `.agents/tmp/frontend-user-shell-local-tooling-map.md`
- Most relevant generated surfaces:
  - `docs/generated/local-tooling/endpoint-contract-packs/dashboard-summary.md`
  - `docs/generated/local-tooling/endpoint-contract-packs/applications-summary.md`
  - `docs/generated/local-tooling/frontend-usage-graph-summary.md`
- Most relevant commands:
  - `make audit-frontend-state-logic-duplication`
  - `make recommend-targeted-tests`
  - `make audit-architecture-drift`

## Execution Discipline

- Do not start this child plan as practical implementation work until the IA outputs and shell foundation are stable enough to supply route ownership, section ownership, and shared shell behavior.
- When this plan becomes the active slice, treat it as the only practical active child slice unless a real dependency forces overlap.
- Use `docs/generated/local-tooling/codex-context/latest.review.md` as the first compact context checkpoint before broadening the touched-file scope.
- When this plan opens during the active master-plan batch, complete it and continue automatically into the next declared child plan without pausing for routine confirmation.

## Goal

Build `Home` and `Work` as the first true module landing surfaces and establish the reusable interaction grammar for summaries, lists, and detail entry.

## Scope

- Included:
  - Home orientation surface
  - Work landing surface
  - Work sectioning for discover / quests / applications
  - initial summary, list, and detail-entry standards
- Excluded:
  - full remaining module rollout
  - unrelated business or chat surface redesign

## Key Outputs

- `Home` surface
- `Work` landing surface
- standard section header pattern
- standard work list and application entry pattern
- initial shared UI contract for:
  - section header pattern
  - list row grammar
  - detail split layout
  - action placement
  - badge/status vocabulary

## Home Rollout Constraint

- `Home` must not start as a frontend mashup of unrelated APIs.
- Roll it out in this order:
  - shell-visible section skeleton
  - explicit section contract
  - backend-prepared orientation/read-model support where needed
  - richer UI rendering only after that contract exists

## Phase-One Locked Decision

- Backend-prepared `Home` summary does not need to land in the very first shell code slice.
- Phase one should start with:
  - shell-visible `Home` skeleton
  - explicit section ownership
  - narrow backend-safe data only where already available
- The first richer cross-module `Home` summary/read model should be treated as the next step after shell skeleton and route ownership are stable.

## Implementation Slices

- [x] Slice 1: define `Home` section skeleton and minimal section ownership
- [x] Slice 2: define the first shared UI contract primitives on `Home` and `Work`
- [x] Slice 3: implement `Work` entry surface structure for discover, quests, applications, and activity
- [x] Slice 4: identify and implement any backend-prepared `Home` or `Work` summary/read-model support needed for safe UX
- [x] Slice 5: align the resulting surfaces against the canonical ownership matrix

## Docs and Artifacts

- Expected docs:
  - likely `docs/business-logic.md`
  - likely `docs/domain-technical.md`
  - possibly `docs/product-vision.md` only if the shell meaning materially changes the stated UX model
- Expected generated artifacts:
  - DTO contract artifacts if frontend/backend contracts change
- Temporary work products:
  - shared child-plan analysis from `.agents/tmp/frontend-user-shell-child-plan-analysis.md`

## Closeout Gates

- Required closeout checks:
  - `Home` is not frontend-owned business logic
  - `Work` establishes the reusable UI contract instead of a one-off module style
  - `Applications` remains naturally nested under `Work`
- Final response evidence:
  - `Home` rollout shape and `Work` UI-contract summary
- Backlog follow-up rule:
  - any deferred backend `Home` summary/read-model work must be recorded explicitly

## Validation

- frontend type-check and build
- targeted route behavior review
- backend DTO compatibility review for work sections

## First Code Slice Rule

- As soon as this child plan starts real code changes, run:
  - `make audit-manifest-decision files=<csv>`
  - `make audit-doc-sync-required-surfaces files=<csv>`
- Re-run them when the changed-file scope grows meaningfully.

## Open Questions

- What is the thinnest acceptable backend-prepared `Home` section contract for the second `Home` slice after the shell skeleton lands?
- Can `Work` reuse existing dashboard/grouping DTOs safely, or should it wait for a thinner work-entry contract?
- Does `Activity` start as a real section or remain deferred until the source read model is stable?

## Completion Evidence

- Status: complete
- Evidence: `Home` now stays thin on dashboard summary data, while `Work` renders discover, owned quests, and applications from existing backend read models under the shared UI contract.
