# Legacy Purge Master Plan

Goal: remove the remaining legacy `workmarket` surface from the product where it is still active, while preserving the current `vision` UX and chat surface.

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

## Constraints

- Do not remove active backend behavior without a replacement path.
- Preserve `vision` and `chat` as the only user-facing frontend surfaces.
- Keep tests and validation green after each slice.
- Treat historical generated reports separately from active source files.
