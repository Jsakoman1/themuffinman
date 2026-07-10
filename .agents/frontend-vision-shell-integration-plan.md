---
machine_kind: plan
machine_status: complete
machine_title: Frontend Vision Shell Integration Plan
machine_goal: Integrate Vision into the shared shell as a docked, contextual, and
  focused interaction layer while preserving backend-owned semantics.
---

# Frontend Vision Shell Integration Plan

## Status

Complete.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer because this slice directly affects Vision continuity, route ownership, and shell interaction semantics
- Scope: shell-level Vision entry and typed handoff behavior
- Out of scope: new semantic capabilities unrelated to shell integration
- Manifest decision: likely required if contracts, docs, and several frontend surfaces change together
- Manifest path: TBD
- Master plan: `.agents/frontend-user-shell-navigation-master-plan.md`

## Routing Snapshot

- Context commands:
  - inspect current `useVisionConversation`, route autorun behavior, and existing `/vision` conversation lifecycle expectations
  - `make endpoint-contract-packs`
- Routing commands:
  - `make audit-router files=<csv>`
  - `make audit-doc-sync-required-surfaces files=<csv>`
  - `make audit-manifest-decision files=<csv>`
  - `make resolve-manifest-path files=<csv>` if needed
- Validation commands:
  - `npm run type-check`
  - `npm run build`
  - targeted backend/frontend checks for Vision route and handoff behavior
- Closeout commands:
  - `make audit-todo`
  - `make audit-plan-completion plan=.agents/frontend-vision-shell-integration-plan.md`
- Doc sync commands:
  - likely `docs/business-logic.md`
  - likely `docs/domain-technical.md`
  - possibly `docs/product-vision.md` or `docs/vision-architecture-patterns.md` if the integration semantics become canonical
- Generated artifact commands:
  - resolver-driven if contracts move

## Preferred Local Tooling

- Tooling map: `.agents/tmp/frontend-user-shell-local-tooling-map.md`
- Most relevant generated surfaces:
  - `docs/generated/local-tooling/endpoint-contract-packs/vision-summary.md`
  - `docs/generated/local-tooling/endpoint-contract-packs/chat-summary.md`
  - `docs/generated/local-tooling/frontend-usage-graph-summary.md`
- Most relevant commands:
  - `make audit-endpoint-callsite-linker`
  - `make audit-architecture-drift`
  - `make audit-frontend-state-logic-duplication`

## Execution Discipline

- Do not start practical Vision shell integration until the IA outputs are durable and the shell foundation plus canonical module entries already exist.
- When this plan becomes the active slice, treat it as the only practical active child slice unless a real dependency forces overlap.
- Start from `docs/generated/local-tooling/codex-context/latest.review.md` plus the finalized Vision handoff contract, not from ad hoc prompt-launch ideas.
- When this plan opens during the active master-plan batch, complete it and continue automatically into the next declared child plan without pausing for routine confirmation.

## Goal

Integrate Vision into the shared shell as a docked, contextual, and focused interaction layer while preserving backend-owned semantics.

## Scope

- Included:
  - shell-level Vision entry
  - contextual launch points
  - full-route Vision continuity
  - route-to-Vision and Vision-to-route handoff behavior
- Excluded:
  - new semantic backend capabilities unrelated to shell integration
  - frontend inference logic that duplicates backend route or task meaning

## Key Outputs

- persistent Vision entry pattern
- shell-to-Vision handoff rules
- Vision-to-surface navigation rules
- shared UX behavior for quick prompt, contextual prompt, and focused Vision mode
- explicit Vision handoff contract covering:
  - shell -> Vision quick prompt
  - shell -> Vision contextual prompt
  - Vision -> route target
  - route target -> Vision resume/deep-work

## Identity Protection Constraint

- `/vision` full route remains the premium deep-work mode.
- Shell integration must not reduce `/vision` to a small helper widget or secondary utility.
- Shell-level Vision entry should improve access and continuity while preserving the blank-canvas identity and backend-owned conversation model.

## Integration Constraint

- Do not implement ad hoc per-surface prompt launch behavior.
- Use the predefined handoff contract from the IA phase.
- Preserve existing recent-summary, resume, typed review/edit, and user-scoped continuity behavior from the backend conversation flow.

## Implementation Slices

- [x] Slice 1: map current `/vision` autorun and route-prompt behavior against the new handoff contract
- [x] Slice 2: implement shell-level quick Vision entry
- [x] Slice 3: implement contextual module-to-Vision launches
- [x] Slice 4: implement Vision-to-route target handoff and route-to-Vision resume/deep-work behavior
- [x] Slice 5: validate that `/vision` remains premium deep-work mode and not a downgraded helper widget

## Docs and Artifacts

- Expected docs:
  - likely `docs/business-logic.md`
  - likely `docs/domain-technical.md`
  - possibly `docs/vision-architecture-patterns.md` if route/shell interaction becomes an explicit durable pattern
- Expected generated artifacts:
  - contract artifacts if request/response or navigation primitives move
- Temporary work products:
  - shared child-plan analysis from `.agents/tmp/frontend-user-shell-child-plan-analysis.md`
  - `.agents/tmp/frontend-user-shell-vision-handoff-contract.md`

## Closeout Gates

- Required closeout checks:
  - handoff contract is implemented consistently
  - no ad hoc per-surface prompt wiring remains
  - `/vision` still feels like the adaptive deep-work route
  - backend continuity semantics remain intact
- Final response evidence:
  - implemented handoff map and continuity summary
- Backlog follow-up rule:
  - any deferred advanced Vision shell affordances must be recorded explicitly

## Validation

- frontend type-check and build
- `/vision` route regression review
- handoff behavior review across work, chat, and profile contexts

## First Code Slice Rule

- As soon as this child plan starts real code changes, run:
  - `make audit-manifest-decision files=<csv>`
  - `make audit-doc-sync-required-surfaces files=<csv>`
- Because this slice is likely tier3, do not wait until closeout to resolve manifest scope.

## Open Questions

- Can the first handoff pass stay within existing prompt-query conventions, or should it introduce a more typed route-state handoff immediately?
- Which shell contexts merit a direct Vision launch on day one versus later expansion?
- How much of the current route autorun behavior should survive once stable module entries exist?

## Completion Evidence

- Status: complete
- Evidence: shell-level quick and contextual Vision launches now use one typed handoff helper, and `/vision` preserves context and return targets without inheriting module chrome.
