---
machine_kind: master-plan
machine_status: unknown
machine_title: Codex Lean Context Master Plan
---

# Codex Lean Context Master Plan

Purpose: reduce Codex token waste by cleaning noisy local-tooling output, tightening audit summaries, and making context-gateway outputs smaller and more decision-oriented without weakening validation or closeout rigor.

## Scope

- Add a shared text-noise cleaner for audit and validation output summaries.
- Register the cleaner as a first-class local tooling helper so it is easy to discover and reuse.
- Trim the highest-noise local-tooling summaries so they surface decisions first.
- Reduce context-gateway and validation evidence verbosity where it is redundant.
- Keep the existing safety model, manifests, and closeout checks intact.

## Child Slices

- Slice 1: implement the shared text-noise cleaner and wire it into validation evidence and audit summary generation.
- Slice 2: register the cleaner in local-tooling discovery and tighten the most verbose outputs so they prefer short decisions, counts, and failure anchors.
- Slice 3: inspect remaining context packs and summary generators for avoidable noise and trim them where the cleaner can help.
- Slice 4: validate the cleaned outputs, update evidence, and remove any temporary backlog items created by the batch.

## Working Notes

- Prefer small deterministic helpers over a new framework.
- Preserve raw data when it is the source of truth; clean only the human-facing or summary surfaces.
- Keep the default behavior safe for generated artifacts and closeout evidence.
- If a cleanup rule risks hiding useful diagnostics, keep the raw underlying data and clean only the summarized text.

## Completion Criteria

- Local tooling summaries are shorter and easier to scan.
- Validation evidence summaries no longer carry repetitive build noise.
- The context gateway and audit reports continue to pass their tests after cleanup.
- Todo/backlog audits stay green and no open deferred work is left behind unless intentionally recorded.

## Completion Evidence

- Status: complete
- Validation: passed
- Residual risk: context trimming must not remove failure details needed for debugging or closeout audits.
