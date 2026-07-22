# Round 37: Generated System-Map Change Impact Report

Status: implemented control. Reviewed: 2026-07-22.

## Result

`make system-map-impact` generates a disposable report under `docs/audit-output/`
for the current changed-file set, or for paths supplied to the script. It links each
changed path to likely system-map registries, canonical documentation, and runtime
evidence sources using repository domain/category mapping.

## Boundary

The report is advisory. It does not replace the work-plan verifier, decide capability
status, infer runtime proof, grant permissions, or prove that every suggested source
needs a change. It makes the required human review surface explicit before closeout.
