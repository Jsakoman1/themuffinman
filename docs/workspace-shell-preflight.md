# Workspace Shell Preflight Inventory

Reviewed: 2026-07-19

This is an implementation preflight, not a work plan. It records the current product, code, contract, and cleanup
baseline before `docs/work/workspace-app-shell-first-layer.yaml` starts.

## Canonical sources

- `docs/capability-inventory.yaml` is the canonical current capability inventory.
- Planned and partial capabilities are capability records in that file; there is no separate planned-inventory file.
- `docs/capability-inventory-audit.yaml` is the current route/read-model/action integrity snapshot.
- `docs/target-capability-catalog.yaml` is the desired production capability catalog.
- `docs/work/workspace-app-shell-first-layer.yaml` is the only open implementation plan.
- `docs/runtime-evidence/*.json` is retained runtime evidence when referenced by the inventory or acceptance matrix.

## Current implementation shape

- The inventory contains 152 capabilities: 143 implemented, 5 verified, 2 partial, and 2 planned.
- The backend is organized into domain packages including Work, Business, Chat, Vision, Identity, Things, Rides,
  Social/Circles, Location, Notification, Activity, Native Handoff, and shared support.
- The frontend currently has one dominant shared `app-shell` module with 75 source files, plus small Identity, Rides,
  Things, and Vision modules.
- The authenticated router exposes 45 frontend routes, including module collections, detail surfaces, settings,
  notifications, activity, saved searches, and Vision.
- The backend exposes domain controllers and read/action APIs; domain permissions, validation, state transitions, and
  allowed actions remain backend-owned.
- The existing shell already owns shared navigation definitions, route registry, surface data, account controls, Vision
  entry, create entry, and shared surface primitives. The planned shell work should extend these contracts rather than
  create a second navigation grammar.

## Pre-shell findings

1. The capability inventory is sufficient as the product capability source of truth. A separate planned-inventory file
   would duplicate status and gap data.
2. The inventory previously had a stale plan block containing completed plans and one deleted account
   recovery plan. It now uses `open_plans` and lists only the workspace shell plan.
3. The deferred capability-coverage master was historical control state, not current work. It and its stale master links
   were removed; verified child evidence remains where it is still referenced.
4. `AppCard.vue` had no runtime importer and was only retained by an old validator assertion. It was removed together
   with that assertion and stale path references.
5. `run-rides-vision-probe.mjs` was not referenced by package scripts or the repository workflow. It was removed as an
   obsolete probe.
6. No tracked screenshot, smoke-output, or browser-report files were found. Retained runtime evidence is JSON and is
   referenced by the canonical acceptance/inventory documents.
7. The remaining cleanup candidate is one frontend-local permission/action duplication. It requires a targeted code
   review before removal and is not safe to delete based on naming alone.

## Required shell decisions before implementation

- Define a dedicated backend `workspace-navigation-v1` DTO/read service and route allowlist.
- Define explicit relevance semantics for module children, counts, unread markers, and attention metadata.
- Add a thin frontend API/composable and one shared rail component; do not derive navigation from loaded collections.
- Keep Vision adaptive and separate from persistent module chrome.
- Add one desktop and one narrow-layout runtime evidence path, then remove temporary traces during closeout.

## Validation baseline

The preflight must keep these checks passing before shell implementation begins:

- `make audit-inventory-freshness`
- `make audit-docs`
- `make audit-plan-coverage`
- `make audit-target-capability-catalog`
- `make audit-target-capability-coverage`
- `git diff --check`
