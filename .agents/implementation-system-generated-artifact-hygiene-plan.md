---
machine_kind: plan
machine_status: complete
machine_title: Generated Artifact Hygiene
machine_goal: Make generated-artifact hygiene checks easy to target to the exact touched files before widening to global freshness.
---

# Generated Artifact Hygiene

## Status

Complete.

## Goal

Make generated-artifact hygiene checks easy to target to the exact touched files before widening to global freshness.

## Parent Master Plan

- Master plan: `.agents/implementation-system-improvement-master-plan-next.md`

## Scope

- Included: scope-filtered hygiene checks, touched-file targeting, and freshness widening rules.
- Excluded: generation logic itself.

## Validation

- Targeted checks: run `make audit-generated-artifact-hygiene files=<csv>`.
- Broader checks: compare the scope-filtered report with the global freshness audit.
- Closeout checks: make the exact touched files easy to see in the report.

## Completion Evidence

- Status: complete
- Changed files: scripts/implementation-batch.sh, scripts/audits/local_tooling_extended_tools.rb, docs/generated/local-tooling/generated-artifact-freshness.json
- Validation evidence: `make implementation-batch topic=implementation-system`, `make audit-generated-artifact-freshness`
- Doc delta summary: the batch wrapper now records and reports generated-artifact hygiene alongside the broader implementation batch state.
- Deferred work: none
