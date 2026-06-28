# Manifest Process Hardening Plan

Purpose: tighten feature-manifest quality so artifact lists stay precise, non-overlapping, and validator-enforced instead of drifting into broad or ambiguous buckets.

## Scope

- [x] Create this plan before substantial edits because the change affects process artifacts, manifests, and validation rules.
- [x] Remove obvious artifact-list drift from existing manifests.
- [x] Add validator rules for manifest artifact path quality.
- [x] Re-run manifest and operating-model validation.
- [x] Close this plan after process hardening is green.

## Working Notes

- Keep this pass focused on measurable process quality, not on rewriting feature history.
- Prefer rules that prevent common drift patterns such as test files living in `codePaths`.

## Completion Check

- [x] Existing manifests no longer mix test files into `codePaths`.
- [x] Validator enforces the new manifest quality rules.
- [x] Validation passes.
