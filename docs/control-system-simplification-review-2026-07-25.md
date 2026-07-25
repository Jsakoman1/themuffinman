# Control-system simplification review

Reviewed: 2026-07-25

## Retained authorities

- `docs/system-map.md` and its machine-readable registries remain the architecture and impact map.
- `docs/capability-inventory.yaml` remains the current implementation/evidence inventory.
- `docs/target-capability-catalog.yaml` remains the planned/target capability inventory.
- `docs/runtime-acceptance-matrix.yaml` remains the runtime status authority.
- `docs/implementation-control.md`, `scripts/verify-work.rb`, and atomic-task hardening remain the implementation control path.
- Chromium/Playwright remains the browser runtime path; runtime evidence is retained only when referenced by a canonical source.

## Simplifications applied

- Removed five orphaned plan-specific audit scripts: Apple polish atomization/leaf expansion, Chromium queue validation, structural-redesign validation, and unused text-noise cleanup.
- Removed seven advisory/duplicated audits for changeset scoring, contract-gap reporting, fixture duplication, and repeated frontend logic heuristics.
- Removed eight unreferenced frontend runtime scripts; the four referenced Chromium/Playwright scenario scripts remain.
- Reduced the local audit inventory to 31 scripts and the frontend runtime inventory to 4 referenced scripts.
- Removed 40 unreferenced review, analysis, and preflight Markdown documents; retained the System Map mapping set and the active simplification report.
- Removed the separate broad test-gap/changeset-impact audit path from `make audit-all`; the retained broad suite now covers backend, frontend, and documentation integrity.
- Removed 116 unreferenced runtime/screenshot artifacts. Referenced runtime evidence was preserved.
- Removed 10 additional runtime evidence files orphaned by the review-document cleanup.
- Added one explicit cleanup command: `make clean-generated` removes disposable audit output, frontend `dist`, and temporary files.
- Added one compact control preflight: `make control-check` validates preserved sources, YAML, plan coverage, docs, capability/runtime state, and core control registries, then cleans generated output.
- Updated the control documentation to make generated-output cleanup part of closeout instead of an informal expectation.

## Deliberate non-removals

The System Map, current and target capability inventories, runtime matrix, Chromium/Playwright path, verifier, atomic-task hardening, backend/frontend audits, and canonical contract/mapping registries were not removed.

Historical runtime evidence without a current reference is disposable. If a future trace must remain durable, it must be linked from a canonical runtime or capability registry before closeout.

Current retained runtime evidence: 92 files, all referenced by a canonical registry, regression catalog, or evidence document. Disposable audit output: 0 files after closeout.
