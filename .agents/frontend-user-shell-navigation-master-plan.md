---
machine_kind: master-plan
machine_status: complete
machine_title: Frontend User Shell Navigation Master Plan
machine_goal: Introduce a shared authenticated user shell, stable module navigation,
  and Vision-integrated task entry so TheMuffinMan becomes understandable and navigable
  for end users without losing its adaptive surface model.
---

# Frontend User Shell Navigation Master Plan

## Status

Complete.

## Goal

Introduce a shared authenticated user shell, stable module navigation, and Vision-integrated task entry so TheMuffinMan becomes understandable and navigable for end users without losing its adaptive surface model.

## Parent God Plan

- God Plan: `Plan System God Plan`
- Machine-readable path: `.agents/god-plans/plan-system-god-plan.yaml`

## Scope

- Included:
  - authenticated app shell
  - primary information architecture
  - stable user-facing route model
  - first-level navigation for `Home`, `Work`, `Chat`, `Calendar`, `Business`, `Circles`, and `Profile`
  - `Vision` integration as shared dock and full-route mode
  - standard list, detail, and section-entry interaction patterns
  - mobile and desktop navigation behavior
  - required docs, contracts, and validation for the new frontend structure
- Excluded:
  - unrelated backend domain rewrites
  - major visual polish unrelated to navigation clarity
  - premature promotion of `Things` and `Rides` into first-level nav
  - frontend-owned business logic that duplicates backend permissions, workflow, or state rules

## Reference Analysis

- Primary analysis: `.agents/tmp/frontend-user-shell-navigation-analysis.md`
- Child-plan analysis: `.agents/tmp/frontend-user-shell-child-plan-analysis.md`
- Local tooling map: `.agents/tmp/frontend-user-shell-local-tooling-map.md`

## Product Direction

The next user-facing frontend should combine two valid interaction models:

1. stable navigation for orientation and habit
2. adaptive Vision-driven interaction for speed and task execution

The shell should make the product legible. Vision should make it powerful.

## Alignment With Existing Vision And Architecture

- Aligned with `docs/product-vision.md`: the product still aims for one adaptive shell rather than a pile of unrelated module apps.
- Aligned with `docs/vision-architecture-patterns.md`: backend keeps deciding meaning while frontend renders route state, shell state, and backend-prepared surfaces.
- Aligned with `docs/business-logic.md`: `/vision` remains terminal-first and user-scoped, while route-level module entries improve orientation and continuity.
- Aligned with backend-first repo rules: this plan adds navigation and rendering structure, not frontend-owned workflow logic.
- Aligned with the Her-like direction only if the shell stays calm, sparse, and adaptive instead of becoming a generic admin dashboard.

The main constraint is important:

- the shell must not replace `/vision` as the long-term adaptive product language
- the shell must make `/vision` easier to enter, resume, and trust

## Core Decisions

1. `/vision` remains first-class but stops being the only real authenticated entry point.
2. A shared authenticated shell becomes the default structure for non-auth routes.
3. `Home`, `Work`, `Chat`, `Calendar`, `Business`, `Circles`, and `Profile` become first-level destinations.
4. `Applications` lives under `Work`, not as a top-level destination.
5. `Settings` lives under `Profile`, not as a top-level destination.
6. `Things` and `Rides` stay second-level or deferred until usage and product emphasis justify first-level promotion.
7. Navigation should eventually move from prompt-only redirects toward backend-aligned route targets and real landing surfaces.
8. Vision-native detail continuity should be preserved where it already exists for quest, application, profile, settings, circles, and chat flows; the new shell should route into those surfaces rather than fork a second detail system.
9. Shared backend navigation primitives such as `NavigationTargetDTO` should be the preferred handoff path for cross-surface navigation whenever the existing contract can support it.
10. The shell may aggregate data for orientation, but important business, permission, and workflow state must still come from backend-prepared read models rather than frontend composition heuristics.
11. Promotion of a capability into first-level navigation should require explicit product justification, stable backend read surfaces, and repeated user value across sessions.
12. Canonical surface ownership must be defined before shell implementation so routes, details, and Vision continuity do not drift into parallel stacks.
13. A Vision handoff contract must be defined before shell integration so prompt launches, route launches, resume flows, and deep-work transitions stay typed and predictable.
14. Shared UI contracts for section headers, list rows, detail layouts, action placement, and badge vocabulary must be locked before module entry surfaces broaden.

## Child Plans

1. `.agents/frontend-user-shell-ia-plan.md`
- Role: define the concrete information architecture, route model, nav taxonomy, and screen responsibilities.
- Status: complete

2. `.agents/frontend-shared-shell-foundation-plan.md`
- Role: implement the shared authenticated shell, nav regions, desktop/mobile behavior, and route scaffolding.
- Status: complete

3. `.agents/frontend-home-and-work-entry-plan.md`
- Role: build the first real module landing surfaces for `Home` and `Work`, including work applications and quest entry patterns.
- Status: complete

4. `.agents/frontend-chat-calendar-business-circles-profile-entry-plan.md`
- Role: add the remaining first-level entry surfaces and standardize their list/detail/summary patterns.
- Status: complete

5. `.agents/frontend-vision-shell-integration-plan.md`
- Role: integrate Vision into the shared shell as docked, contextual, and focused modes without reintroducing frontend-owned semantics.
- Status: complete

6. `.agents/frontend-user-shell-validation-and-docs-plan.md`
- Role: align docs, contracts, validation evidence, and closeout around the new user shell and navigation model.
- Status: complete

## Execution Discipline

- Only one child plan should be treated as the practically active execution slice at a time.
- The other child plans may remain drafted and prepared, but day-to-day implementation should not widen into several concurrent active slices unless a real dependency forces it.
- The first implementation slice should start from `docs/generated/local-tooling/codex-context/latest.review.md` plus the IA durable outputs, not from memory alone.
- Temporary `.agents/tmp/` decision artifacts are useful for preparation, but the IA phase must promote the canonical ownership matrix and the Vision handoff contract into the durable IA plan before shell implementation begins.
- Once practical implementation starts, execute every child plan in the declared order without pausing for routine confirmation.
- Do not stop between child plans unless a real blocker, approval need, or direct conflict with user changes appears.

## Additional Design Guardrails

### Guardrail 1: Keep One Adaptive Product Language

- The shell may add orientation chrome, but the active product language should still feel like one adaptive surface.
- Avoid turning each primary destination into a separate visual product.
- Reuse one shared section-header, list, detail, and action grammar across modules.

### Guardrail 2: Preserve Vision-Native Continuity

- Existing Vision-native detail and continuation routes should remain first-class.
- The shell should deep-link into those routes or host them consistently, not replace them with duplicate page stacks.
- Route-to-Vision and Vision-to-route transitions should be deliberate and typed.

### Guardrail 3: Backend Owns Navigation Meaning

- Frontend may own layout, focus, and presentation state.
- Backend should remain the source for:
  - actionable navigation targets where contracts already exist
  - section labels and descriptions where backend DTOs already expose them
  - resumable task state
  - detail action availability

### Guardrail 4: Home Must Be A Backend-Prepared Orientation Surface

- `Home` should not become a frontend-assembled pile of unrelated widgets.
- Its summary blocks, continuation items, and attention signals should come from explicit backend-prepared sections as the implementation matures.
- Rollout order for `Home` should be:
  - shell-visible skeleton and section contract
  - backend-prepared home summary/read model
  - richer UI rendering on top of that backend contract

### Guardrail 5: Calm By Default

- Desktop shell can have left navigation, but avoid permanent noisy right rails unless the current surface needs them.
- Mobile should prefer a compact header plus bottom navigation plus a restrained Vision trigger.
- The default visual tone should stay quiet enough that the app still reads as one adaptive environment.

### Guardrail 5a: Protect The Blank-Canvas Identity

- `/vision` full route remains the premium deep-work mode.
- The shared shell should stay quiet and utilitarian instead of trying to outshine the adaptive canvas.
- Right-side rails should appear only when the active task benefits from contextual support.
- Avoid permanent extra panels, stacked dashboards, or dense chrome that would collapse the adaptive-environment feeling into a generic workspace.

### Guardrail 6: Promotion Criteria For Future Modules

- `Things` and `Rides` stay out of first-level nav until all of these are true:
  - stable backend read model exists
  - clear repeat user journey exists
  - top-level promotion simplifies navigation more than it adds noise
  - mobile nav still stays compact

### Guardrail 7: Shared UI Contract Before Surface Proliferation

- Before `Work`, `Business`, `Chat`, `Circles`, and `Profile` grow in parallel, define and reuse:
  - section header pattern
  - list row grammar
  - detail split layout
  - action placement rules
  - badge and status vocabulary

### Guardrail 8: Canonical Surface Ownership Before Coding

- Before shell implementation, define:
  - canonical entry route per major surface
  - canonical detail route per major entity
  - when a task stays in module space
  - when a task escalates into `/vision`
- This ownership matrix should prevent duplicate detail stacks and route ambiguity.

### Guardrail 9: Vision Handoff Contract Before Integration

- Before shell-level Vision integration, define:
  - shell -> Vision quick prompt
  - shell -> Vision contextual prompt
  - Vision -> route target handoff
  - route target -> Vision resume/deep-work handoff
- The implementation should use one explicit contract instead of ad hoc prompt wiring.

## Execution Order

1. IA and route contract
- Priority: must-have
- Reason: implementation should not guess navigation structure screen by screen.

2. Shared shell foundation
- Priority: must-have
- Reason: module surfaces need one common frame before detailed entries are built.

3. Home and Work entry surfaces
- Priority: must-have
- Reason: `Work` is the strongest current module, and `Home` is the missing orientation layer.

4. Chat, Calendar, Business, Circles, and Profile entry surfaces
- Priority: must-have
- Reason: first-level destinations should become real and consistent, not placeholder chrome.

5. Vision shell integration
- Priority: must-have
- Reason: Vision should enhance the shell instead of competing with it.

6. Validation and docs closeout
- Priority: must-have
- Reason: navigation, route ownership, and UI responsibilities need durable documentation and proof.

## Improvement Checklist

- [x] Define one ordered child plan per concrete improvement slice.
- [x] Confirm that a Master Plan is really needed and that one durable Plan would not be clearer.
- [x] Keep the child-plan order stable unless the implementation sequence changes.
- [x] Include a short validation and closeout path for the whole batch.
- [x] Record any deferred follow-up items in a persistent backlog.
- [x] Do not mark the master plan complete until every child plan row is complete or explicitly deferred.

## Dependencies

- `docs/product-memory.md`
- `docs/product-vision.md`
- `docs/domain-technical.md`
- `docs/business-logic.md`
- `docs/vision-architecture-patterns.md`
- `apps/themuffinman/frontend/src/router.ts`
- `apps/themuffinman/frontend/src/modules/vision/views/VisionSurfaceModernView.vue`
- `apps/themuffinman/frontend/src/modules/vision/components/VisionCanvasRenderer.vue`

## Cross-Phase Dependency Map

### IA -> Shared Shell

- The shell should implement a deliberate destination model, not invent one while coding layout.
- The IA phase should also define which current Vision-native routes stay canonical for detail continuity versus which surfaces need true standalone module entries.
- The IA phase must also produce the canonical surface ownership matrix and the first version of the Vision handoff contract.

### Shared Shell -> Module Entry Surfaces

- Module entries should inherit one shared page frame, action placement model, and responsive navigation system.
- Shared shell work should also standardize app-level tokens and layout regions so module entries do not reinvent spacing, density, or chrome rules.
- Shared shell work should not force `Home` into a frontend-only aggregation pattern; it should leave room for a later backend-prepared home summary contract.

### Home And Work -> Remaining Module Entries

- `Home` and `Work` should establish the standard header, summary, list, and detail patterns before the rest of the modules copy weaker patterns.
- `Home` should also establish the cross-module orientation contract before later modules add their own badge, summary, or "continue" variants.
- `Home` should launch first as a shell-visible section structure with explicit placeholders or narrow backend sections, then deepen only when backend-prepared orientation surfaces exist.

### Shared Shell And Module Entries -> Vision Integration

- Vision integration should attach to a stable shell and real surfaces, not to temporary placeholders.
- Vision integration should also preserve existing backend conversation continuity, recent-summary behavior, and typed review/edit flows rather than wrapping them in a separate client-owned task layer.
- Vision integration should consume the predefined handoff contract instead of inventing per-surface prompt-launch exceptions.

### All Previous Phases -> Validation And Docs

- Documentation and validation should close against implemented route ownership, shell behavior, and Vision handoff semantics.

## UX Acceptance Criteria

- An authenticated user can understand the main product areas without writing a prompt.
- A user can enter `Work`, `Chat`, `Business`, `Calendar`, `Circles`, and `Profile` through stable navigation.
- `Home` gives cross-module orientation and next actions.
- `Home` does not depend on frontend-assembled business logic to determine priorities, resumability, or action safety.
- `Applications` is reachable naturally within `Work`.
- `Vision` remains visible and useful from inside the shell without being the only path.
- Vision-native detail continuity remains intact for supported quest, application, profile, settings, circles, and chat flows.
- Canonical entry and detail ownership are documented and consistently applied across `Work`, `Chat`, `Business`, `Circles`, `Profile`, and `/vision`.
- Desktop and mobile navigation use the same destination model with different layout density.
- The frontend does not recreate backend business rules to make navigation work.
- Primary navigation can stay compact on mobile without forcing overflow into an unreadable menu stack.

## Validation

- Targeted checks:
  - `npm run type-check`
  - `npm run build`
  - targeted backend/frontend checks for any new route or DTO surface used by the shell
- Broader checks:
  - authenticated-route regression review
  - mobile and desktop shell behavior review
  - `/vision` handoff and deep-link regression review
  - route ownership review against existing Vision-native continuation surfaces
  - backend-navigation-contract review for any changed `NavigationTargetDTO` or route-target handoff behavior
  - shared UI contract review across section headers, list rows, detail layouts, actions, and badges
- Closeout checks:
  - `make audit-todo`
  - `make audit-plan-completion plan=<plan-file>`
  - additional manifest and validation-evidence checks if resolver scope requires them

## Completion Evidence

- Status: complete
- Child plan status: complete
- Validation evidence: `npm run type-check`, `npm run build`, `make audit-manifest-decision`, `make audit-doc-sync-required-surfaces`, `make audit-router`, and `make audit-frontend-route-surfaces` passed for the implemented scope
- Doc delta summary: business logic, domain technical notes, and vision architecture patterns now reflect shared shell ownership, backend-first entry surfaces, and the typed Vision handoff contract
- Deferred work: no mandatory follow-up blocker remains for this shell-navigation batch

## Execution Continuation Rule

- After one child plan closes, continue automatically into the next child plan in the declared order without pausing for routine confirmation.
- Stop only for a real blocker, conflicting user changes, required approval, or a genuine scope change.
