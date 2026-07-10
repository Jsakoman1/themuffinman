---
machine_kind: master-plan
machine_status: complete
machine_title: Vision UI Simplification Master Plan
machine_goal: Rework the `/vision` UI into a simpler terminal-first experience with
  a stable left-side terminal, a right-side contextual preview rail, and a standardized
  way to present quests, applications, and discovery results for end users.
---

# Vision UI Simplification Master Plan

## Status

Completed.

## Goal

Rework the `/vision` UI into a simpler terminal-first experience with a stable left-side terminal, a right-side contextual preview rail, and a standardized way to present quests, applications, and discovery results for end users.

## Parent God Plan

- God Plan: `Plan System God Plan`
- Machine-readable path: `.agents/god-plans/plan-system-god-plan.yaml`

## Core UX Direction

- The terminal is the primary interaction surface.
- The preview rail is secondary and only appears when it adds useful context.
- Keep one visual language for all terminal messages, prompts, confirmations, and results.
- Reduce explanatory copy to the minimum needed for action clarity.
- Prefer compact, scannable rows over rich card layouts unless the user explicitly needs detail.
- Make the default path easy for end users who want to find work, review work, or act on a single item without reading long descriptions.

## Scope

- Included:
  - a true two-column desktop layout with terminal left and preview right
  - terminal message standardization and copy reduction
  - simplified voice, transcript, and status presentation
  - standardized quest list surfaces for "my quests", "applied quests", and "find work"
  - standardized application list surfaces and result summaries
  - consistent quest/work discovery rows with clear status, reward, location, and action affordances
  - preview rail simplification for draft/review state only
  - mobile fallback behavior that preserves the same information hierarchy in a stacked layout
- Excluded:
  - new backend business rules
  - new entity families outside the supported `/vision` flows
  - unrelated app-shell redesign outside the `/vision` route

## Child Plans

1. `.agents/vision-ui-layout-plan.md`
- Role: convert the current overlay-style shell into a true terminal-left and preview-right layout, with a predictable mobile fallback.
- Status: completed

2. `.agents/vision-ui-copy-plan.md`
- Role: remove redundant explanation text, standardize terminal lines, and keep prompts short and action-oriented.
- Status: completed

3. `.agents/vision-worklists-plan.md`
- Role: design one standardized presentation for all quest/work lists, including my quests, applied quests, discoverable work, and related application states.
- Status: completed

4. `.agents/vision-preview-plan.md`
- Role: keep the right-side preview rail compact, contextual, and draft-focused instead of turning into a second form system.
- Status: completed

5. `.agents/vision-ui-validation-plan.md`
- Role: validate the updated terminal-first layout and list patterns across frontend checks, targeted UI tests, and docs sync.
- Status: completed

## Additional Design Items

- Use a single header rhythm for all terminal states: active flow, current slot, current transcript, current action.
- Keep the input area visually obvious but not oversized.
- Reduce voice controls to one compact action cluster and one short status line.
- Use the preview rail only for:
  - draft forms
  - review-ready summaries
  - resolved item details
  - discovery lists when they are materially useful
- For work discovery, standardize each row to show:
  - title
  - reward or price
  - location or visibility
  - time or schedule
  - one primary action hint
- For "my quests" and "applied quests", standardize each row to show:
  - title
  - current status
  - next action
  - the smallest useful supporting detail
- For "find work", prioritize readable ranked rows over mixed cards and avoid long descriptive blurbs unless the item is opened.
- Keep read-only content visually quieter than draft or review states.
- Prefer one interaction grammar for all list actions: open, apply, edit, withdraw, accept, decline, share, or view detail.

## Pros

- Makes the experience closer to a calm assistant terminal and less like a mixed dashboard.
- Reduces user confusion caused by competing visual systems.
- Gives a clear place for discovery and work-management content without polluting the terminal.
- Keeps the UI aligned with the backend-first adaptive model.

## Cons

- Requires careful refactoring of several visual patterns at once.
- Some existing surfaces may need to be simplified or merged to fit the new standard.
- Worklist standardization may expose inconsistencies in data availability across entity types.

## Dependencies

- `docs/vision-architecture-patterns.md`
- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/product-vision.md`

## Validation

- Targeted checks: frontend type-check, frontend build, and focused `/vision` rendering review.
- Broader checks: list-surface regressions for quests, applications, and discovery flows.
- Closeout checks: docs alignment for the terminal-first layout and the standardized worklist patterns.

## Completion Evidence

- Status: completed
- Completed slices: layout, copy cleanup, worklist standardization, preview rail simplification, validation
- Validation evidence: `npm run type-check`, `npm run build`, `./mvnw test`
- Doc delta summary: terminal-left and preview-right `/vision` shell with standardized work rows and compact preview rail
- Deferred work: any future visual treatment outside the `/vision` route shell and its related worklist surfaces
