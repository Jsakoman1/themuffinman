---
machine_kind: master-plan
machine_status: active
machine_title: Legacy Purge Master Plan
machine_goal: Remove the remaining legacy `workmarket` naming and documentation surface
  from the product where it is still active, while preserving the current `vision`
  UX, backend capability model, and chat surface.
---

# Legacy Purge Master Plan

## Status

Active.

## Goal

Remove the remaining legacy `workmarket` naming and documentation surface from the product where it is still active, while preserving the current `vision` UX, backend capability model, and chat surface.

## Current Focus

- Keep backend `workmarket` runtime logic available for Vision.
- Remove stale frontend-era references to workmarket where Vision now owns the surface.
- Keep admin, auth, and Vision runtime behavior stable.

## Order

1. Inventory remaining references and classify them as:
   - frontend leftovers
   - backend/domain naming
   - docs / agent-model source-of-truth
   - generated artifacts / audits
   - historical manifests / backlog notes
2. Clean the remaining frontend-facing leftovers that are still live in the app.
3. Rename or normalize backend/domain source-of-truth names where the change is low risk and the runtime behavior already exists.
4. Regenerate docs, generated artifacts, and validation reports from the updated source of truth.
5. Run validation and finish with a final drift audit.

## Child Work

1. `docs/source-of-truth-inventory.md`
- Remove stale frontend workmarket source-map entries and replace them with current Vision-facing client surfaces.

2. `docs/business-logic.md`
- Rewrite legacy route and API-client descriptions so they reflect the Vision-first frontend state.

3. `docs/domain-technical.md`
- Replace stale frontend source-map entries and keep backend workmarket references only where the runtime still depends on them.

## Constraints

- Do not remove active backend behavior without a replacement path.
- Preserve `vision` and `chat` as the only user-facing frontend surfaces.
- Keep tests and validation green after each slice.
- Treat historical generated reports separately from active source files.
