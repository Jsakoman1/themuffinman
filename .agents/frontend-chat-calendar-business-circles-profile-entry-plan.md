---
machine_kind: plan
machine_status: complete
machine_title: Frontend Remaining Module Entry Plan
machine_goal: Add real first-level entry surfaces for `Chat`, `Calendar`, `Business`,
  `Circles`, and `Profile` and standardize their entry/list/detail behavior around
  one shell model.
---

# Frontend Remaining Module Entry Plan

## Status

Complete.

## Workflow Frame

- Feature tier: tier2-normal-feature, with potential escalation if several backend read surfaces or contracts need alignment together
- Scope: remaining first-level module entries and reuse of the shared UI contract
- Out of scope: shell foundation and Vision handoff implementation details
- Manifest decision: resolver-driven
- Manifest path: TBD
- Master plan: `.agents/frontend-user-shell-navigation-master-plan.md`

## Routing Snapshot

- Context commands:
  - inspect existing backend read surfaces for chat, business, circles, profile, and scheduling-related data
  - `make endpoint-contract-packs`
- Routing commands:
  - `make audit-router files=<csv>`
  - `make audit-doc-sync-required-surfaces files=<csv>`
  - `make audit-manifest-decision files=<csv>`
- Validation commands:
  - `npm run type-check`
  - `npm run build`
  - targeted backend/frontend checks for any new read surfaces
- Closeout commands:
  - `make audit-todo`
  - `make audit-plan-completion plan=.agents/frontend-chat-calendar-business-circles-profile-entry-plan.md`
- Doc sync commands:
  - resolver-driven
- Generated artifact commands:
  - resolver-driven

## Preferred Local Tooling

- Tooling map: `.agents/tmp/frontend-user-shell-local-tooling-map.md`
- Most relevant generated surfaces:
  - `docs/generated/local-tooling/endpoint-contract-packs/chat-summary.md`
  - `docs/generated/local-tooling/endpoint-contract-packs/business-summary.md`
  - `docs/generated/local-tooling/endpoint-contract-packs/circles-summary.md`
  - `docs/generated/local-tooling/endpoint-contract-packs/app_users-summary.md`
- Most relevant commands:
  - `make audit-frontend-usage-graph`
  - `make audit-frontend-stale-surfaces`
  - `make audit-frontend-state-logic-duplication`

## Execution Discipline

- Do not run this child plan as practical implementation work in parallel with `Home and Work` unless a real dependency requires it.
- When this plan becomes the active slice, treat it as the only practical active child slice unless a real dependency forces overlap.
- Start from `docs/generated/local-tooling/codex-context/latest.review.md`, the finalized IA outputs, and the proven shared UI contract from the earlier slices.
- When this plan opens during the active master-plan batch, complete it and continue automatically into the next declared child plan without pausing for routine confirmation.

## Goal

Add real first-level entry surfaces for `Chat`, `Calendar`, `Business`, `Circles`, and `Profile` and standardize their entry/list/detail behavior around one shell model.

## Scope

- Included:
  - chat entry surface
  - calendar entry surface
  - business entry surface
  - circles entry surface
  - profile entry surface
  - shared section/list/detail conventions across those modules
- Excluded:
  - unrelated deep domain rewrites
  - premature top-level rollout of `Things` and `Rides`

## Key Outputs

- stable route entry points for remaining primary destinations
- standardized list/detail scaffolding
- consistent cross-module action placement
- reuse of the shared UI contract established in `Home` and `Work` instead of module-specific variants

## Standardization Constraint

- `Chat`, `Calendar`, `Business`, `Circles`, and `Profile` should extend the shared UI contract rather than invent:
  - new section header rhythms
  - new row grammars
  - new badge semantics
  - different action placement rules
- Deviations should happen only when the task model genuinely requires them.

## Implementation Slices

- [x] Slice 1: define minimal real entry surfaces for `Chat`, `Calendar`, `Business`, `Circles`, and `Profile`
- [x] Slice 2: map each surface to canonical entry/detail ownership from the IA matrix
- [x] Slice 3: reuse the shared UI contract from `Home` and `Work`
- [x] Slice 4: align each surface with backend-prepared read models and avoid frontend-owned aggregation drift
- [x] Slice 5: validate desktop/mobile behavior and route clarity across all remaining primary destinations

## Docs and Artifacts

- Expected docs:
  - likely `docs/business-logic.md`
  - likely `docs/domain-technical.md`
- Expected generated artifacts:
  - any DTO contract outputs if route contracts shift
- Temporary work products:
  - shared child-plan analysis from `.agents/tmp/frontend-user-shell-child-plan-analysis.md`

## Closeout Gates

- Required closeout checks:
  - remaining primary destinations are real, not fake chrome
  - no module invents its own row/header/badge grammar unnecessarily
  - business, chat, circles, and profile remain aligned with backend read ownership
- Final response evidence:
  - route-entry summary across all remaining primary destinations
- Backlog follow-up rule:
  - any deferred module richness must be recorded explicitly rather than hidden behind placeholder routes

## Validation

- frontend type-check and build
- route rendering review
- spot-check alignment with backend read surfaces and permissions

## First Code Slice Rule

- As soon as this child plan starts real code changes, run:
  - `make audit-manifest-decision files=<csv>`
  - `make audit-doc-sync-required-surfaces files=<csv>`

## Open Questions

- Is `Calendar` initially a cross-domain day/week entry shell or a narrower scheduling summary route?
- Does `Business` start with owner-focused operations only, or also expose public-page continuity from the same entry?
- Which `Chat` route shape is canonical on day one if the shell and Vision both need to open conversation targets?

## Completion Evidence

- Status: complete
- Evidence: `Chat`, `Calendar`, `Business`, `Circles`, and `Profile` now render real entry surfaces from existing backend reads and reuse the same section, row, action, and badge grammar as `Home` and `Work`.
