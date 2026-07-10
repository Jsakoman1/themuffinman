---
machine_kind: plan
machine_status: complete
machine_title: Frontend User Shell IA Plan
machine_goal: Define the concrete information architecture, route model, module hierarchy,
  and shared screen responsibilities for the user shell.
---

# Frontend User Shell IA Plan

## Status

Complete.

## Workflow Frame

- Feature tier: tier2-normal-feature, likely escalating toward tier3-high-risk-multi-layer if route contracts or shared navigation DTOs move
- Scope: information architecture, route ownership, promotion policy, and Vision handoff contract
- Out of scope: shell rendering, component styling, and detailed module UI implementation
- Manifest decision: resolver-driven
- Manifest path: TBD if required by routing/doc-sync scope
- Master plan: `.agents/frontend-user-shell-navigation-master-plan.md`

## Routing Snapshot

- Context commands:
  - direct repo inspection of `router.ts`, active Vision view, product docs, and domain docs
  - `make control-start`
  - `make plan-index`
  - `make endpoint-contract-packs`
- Routing commands:
  - `make audit-router files=<csv>` once implementation starts touching route files
  - `make audit-doc-sync-required-surfaces files=<csv>` once route ownership is codified in code/docs
  - `make audit-endpoint-callsite-linker`
- Validation commands:
  - targeted documentation and route-ownership review
  - `make recommend-targeted-tests` after the first concrete route slice lands
- Closeout commands:
  - `make audit-todo`
  - `make audit-plan-completion plan=.agents/frontend-user-shell-ia-plan.md`
- Doc sync commands:
  - update product/domain docs only if IA decisions become durable source of truth
- Generated artifact commands:
  - resolver-driven only

## Preferred Local Tooling

- Tooling map: `.agents/tmp/frontend-user-shell-local-tooling-map.md`
- Most relevant generated surfaces:
  - `docs/generated/local-tooling/control-start-summary.md`
  - `docs/generated/local-tooling/plan-index-summary.md`
  - `docs/generated/local-tooling/endpoint-contract-packs/vision-summary.md`
  - `docs/generated/local-tooling/endpoint-contract-packs/dashboard-summary.md`
  - `docs/generated/local-tooling/endpoint-contract-packs/chat-summary.md`
  - `docs/generated/local-tooling/endpoint-contract-packs/business-summary.md`
  - `docs/generated/local-tooling/endpoint-contract-packs/circles-summary.md`
  - `docs/generated/local-tooling/endpoint-contract-packs/applications-summary.md`
  - `docs/generated/local-tooling/endpoint-contract-packs/app_users-summary.md`

## Goal

Define the concrete information architecture, route model, module hierarchy, and shared screen responsibilities for the user shell.

## Scope

- Included:
  - first-level destinations
  - route model
  - module boundaries
  - navigation taxonomy
  - destination ownership rules
- Excluded:
  - shell implementation
  - detailed component styling
  - unrelated backend logic changes

## Key Outputs

- approved route map
- destination ownership table
- first-level and second-level nav model
- screen responsibility matrix
- Vision handoff rules for route-to-prompt and prompt-to-route transitions
- canonical surface ownership matrix covering:
  - canonical entry route
  - canonical detail route
  - when a flow stays in module space
  - when a flow enters `/vision`
- top-level navigation promotion policy covering:
  - backend read-model readiness
  - repeat user-journey strength
  - cognitive-load reduction
  - mobile-nav fit
- durable in-plan promotion of:
  - `.agents/tmp/frontend-user-shell-canonical-surface-ownership-matrix.md`
  - `.agents/tmp/frontend-user-shell-vision-handoff-contract.md`
  into the final IA plan output so shell implementation does not depend on temporary files

## Required Decisions

- Decide canonical ownership for at least:
  - home
  - work list
  - quest detail
  - applications list
  - application detail
  - chat workspace
  - circles
  - profile
  - settings
  - business landing
  - business bookings
  - calendar
- Decide the first explicit Vision handoff contract for:
  - shell -> Vision quick prompt
  - shell -> Vision contextual prompt
  - Vision -> route target
  - route target -> Vision resume/deep-work
- Decide what qualifies a future module for promotion into top-level navigation.

## Durable Ownership Decisions

### Phase-One Canonical Entry Routes

- `Home` -> `/home`
- `Work` -> `/work`
- `Chat` -> `/chat`
- `Calendar` -> `/calendar`
- `Business` -> `/business`
- `Circles` -> `/circles`
- `Profile` -> `/profile`
- `Settings` -> `/profile/settings`
- `Vision deep-work` -> `/vision`

### Phase-One Canonical Detail Ownership

- quest detail -> `/vision/quests/:id`
- application detail -> `/vision/applications/:id`
- chat thread detail -> `/chat/:conversationId`
- business owner detail stays inside business route family
- calendar never becomes the source-of-truth detail owner for quest, application, or booking records

### Phase-One Locked Route Decision

- Do not introduce `/work/quests/:id` or `/work/applications/:id` in the first shell pass.
- `Work` entry lists should route directly to canonical Vision-native detail ownership for those entities.

### Module Surface Versus Vision Escalation

- Stay in module space when the user is browsing, scanning, filtering, or reviewing stable owner/workspace information.
- Escalate into `/vision` when the task becomes guided, semantic, review-gated, or cross-module.
- `Home` is orientation-only and must not absorb detail ownership.
- `Calendar` is a coordination lens and must route to the owning surface for details.

## Durable Vision Handoff Contract

### Shell -> Vision Quick Prompt

- Trigger: global Vision action with no strong local entity context
- Behavior: open `/vision`, seed the prompt, let backend route meaning

### Shell -> Vision Contextual Prompt

- Trigger: user launches Vision from a known route or entity context
- Behavior: pass route and entity context as launch hints without inventing business meaning on the client

### Vision -> Route Target

- Trigger: Vision resolves a stable read/navigation outcome owned by a module route
- Behavior: hand off to canonical module route when stable workspace browsing is the right next step

### Route Target -> Vision Resume / Deep Work

- Trigger: module task becomes guided, semantic, review-gated, or cross-module
- Behavior: reopen or seed `/vision` with stable context and keep backend conversation continuity authoritative

## Promotion Policy For Future Top-Level Navigation

A module should only become top-level nav when all of these are true:

- stable backend read model exists
- repeat user journey is strong enough to justify direct entry
- top-level promotion reduces cognitive load more than it adds noise
- mobile navigation still stays compact and legible

## Implementation Slices

- [x] Slice 1: inventory the current authenticated route model and existing Vision-native continuity surfaces
- [x] Slice 2: define the canonical surface ownership matrix
- [x] Slice 3: define the Vision handoff contract
- [x] Slice 4: define the top-level navigation promotion policy
- [x] Slice 5: record the final IA and ownership decisions in the durable plan output

## Docs and Artifacts

- Expected docs:
  - likely none at draft stage unless the IA decisions are promoted into living docs immediately
- Expected generated artifacts:
  - none by default
- Temporary work products:
  - `.agents/tmp/frontend-user-shell-child-plan-analysis.md`
  - `.agents/tmp/frontend-user-shell-canonical-surface-ownership-matrix.md`
  - `.agents/tmp/frontend-user-shell-vision-handoff-contract.md`

## Execution Discipline

- This child plan should be the first practical active execution slice for the master plan.
- Before implementation moves to shell foundation, promote the ownership matrix and Vision handoff contract into this durable IA plan as final outputs.
- Use `docs/generated/local-tooling/codex-context/latest.review.md` as the first compact execution context for this slice.
- Once the durable IA outputs are complete, continue automatically into the next declared child plan without pausing for routine confirmation.

## Closeout Gates

- Required closeout checks:
  - ownership matrix complete
  - handoff contract complete
  - promotion policy complete
  - no unresolved ambiguity left for shell implementation
- Final response evidence:
  - explicit route ownership table and handoff rules
- Backlog follow-up rule:
  - any route or module deferred from top-level nav must be recorded explicitly instead of left implicit

## Validation

- architecture review against `docs/product-vision.md`
- route and ownership review against current frontend and backend navigation contracts

## Open Questions

- Do any existing backend navigation DTOs need extension, or can the first shell pass stay on current route conventions?
- Which currently documented Vision-native detail routes still need concrete frontend route surfaces versus thin hosted shells?
- Does `Calendar` start as a true route with partial content or as a narrow entry summary linked to existing scheduling surfaces?

## Completion Evidence

- Status: complete
- Evidence: durable ownership matrix, promotion policy, and Vision handoff contract are recorded in this plan and referenced by the implemented shell routes.
