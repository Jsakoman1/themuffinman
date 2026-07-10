---
machine_kind: plan
machine_status: complete
machine_title: Frontend Shared Shell Foundation Plan
machine_goal: Implement the shared authenticated shell, including primary navigation
  regions, responsive behavior, and route scaffolding.
---

# Frontend Shared Shell Foundation Plan

## Status

Complete.

## Workflow Frame

- Feature tier: tier2-normal-feature, potentially tier3 if shared routing, docs, and multiple frontend surfaces change together
- Scope: authenticated shell structure, responsive navigation regions, and route scaffolding
- Out of scope: full module content and Vision handoff implementation
- Manifest decision: resolver-driven
- Manifest path: TBD
- Master plan: `.agents/frontend-user-shell-navigation-master-plan.md`

## Routing Snapshot

- Context commands:
  - inspect current router, auth flow, and active Vision route shell
  - `make audit-frontend-usage-graph`
- Routing commands:
  - `make audit-router files=<csv>`
  - `make audit-doc-sync-required-surfaces files=<csv>`
- Validation commands:
  - `npm run type-check`
  - `npm run build`
  - `make recommend-targeted-tests`
- Closeout commands:
  - `make audit-todo`
  - `make audit-plan-completion plan=.agents/frontend-shared-shell-foundation-plan.md`
- Doc sync commands:
  - resolver-driven
- Generated artifact commands:
  - resolver-driven

## Preferred Local Tooling

- Tooling map: `.agents/tmp/frontend-user-shell-local-tooling-map.md`
- Most relevant commands:
  - `make audit-frontend-usage-graph`
  - `make audit-style-token-usage`
  - `make audit-architecture-drift`
  - `make audit-frontend-state-logic-duplication`

## Execution Discipline

- Do not start this child plan as practical implementation work until the IA plan has promoted the ownership matrix and Vision handoff contract into durable plan output.
- When this plan becomes the active slice, treat it as the only practical active child slice unless a real dependency forces overlap.
- Start from `docs/generated/local-tooling/codex-context/latest.review.md` and the finalized IA outputs, not from temporary planning memory.
- When this plan opens during the active master-plan batch, complete it and continue automatically into the next declared child plan without pausing for routine confirmation.

## Goal

Implement the shared authenticated shell, including primary navigation regions, responsive behavior, and route scaffolding.

## Scope

- Included:
  - authenticated shell wrapper
  - desktop navigation rail
  - mobile bottom navigation
  - top context bar
  - shared page-stage layout
  - route scaffolding for primary destinations
- Excluded:
  - full module content implementation
  - advanced visual polish
  - frontend-owned business logic

## Key Outputs

- shared shell component structure
- common route layout pattern
- responsive nav behavior
- placeholder but real destination entry routes where needed

## Implementation Slices

- [x] Slice 1: create shell component and route-layout ownership boundaries
- [x] Slice 2: implement desktop navigation rail and top context bar foundation
- [x] Slice 3: implement mobile bottom navigation and compact header behavior
- [x] Slice 4: wire primary destination route scaffolding into the shell
- [x] Slice 5: standardize shell layout tokens, spacing, and calm-by-default behavior

## Docs and Artifacts

- Expected docs:
  - likely route/shell-related living doc updates if structure becomes durable
- Expected generated artifacts:
  - none by default
- Temporary work products:
  - shared child-plan analysis from `.agents/tmp/frontend-user-shell-child-plan-analysis.md`

## Closeout Gates

- Required closeout checks:
  - shell wraps authenticated routes consistently
  - desktop and mobile nav share one destination model
  - shell remains quiet and does not break `/vision`
- Final response evidence:
  - route layout summary and responsive behavior summary
- Backlog follow-up rule:
  - any deferred shell chrome or accessibility work must be recorded explicitly

## Validation

- `npm run type-check`
- `npm run build`
- shell route rendering review on desktop and mobile widths

## Open Questions

- Should the first shell pass host `/vision` inside the shared shell immediately or preserve a transitional standalone route boundary first?
- Which shell affordances need backend-provided labels or counts on day one versus static structural placeholders?
- Is a notification entry present in the first shell pass, or deferred until a backend-ready surface exists?

## First Code Slice Rule

- As soon as this child plan starts real code changes, run:
  - `make audit-manifest-decision files=<csv>`
  - `make audit-doc-sync-required-surfaces files=<csv>`
- Do not postpone those resolver checks until the end of the slice.

## Completion Evidence

- Status: complete
- Evidence: the shared authenticated shell now wraps the route family, provides desktop and mobile navigation, and exposes contextual Vision entry without pulling `/vision` into module chrome.
