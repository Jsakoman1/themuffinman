---
machine_kind: plan
machine_status: complete
machine_title: Generated History Pruning
machine_goal: Keep archive-only generated history compact through retention limits and empty-directory cleanup.
---

# Generated History Pruning

## Status

Complete.

## Goal

Keep archive-only generated history compact through retention limits and empty-directory cleanup.

## Parent Master Plan

- Master plan: `.agents/implementation-system-closeout-hardening-master-plan.md`

## Scope

- Included: `.history` and `.cache` retention, pruning, and empty directory cleanup.
- Included: make pruning retention explicit per audit family instead of relying on one global rule for every output.
- Excluded: live evidence semantics and plan state rules.

## Implementation Notes

- Keep pruning deterministic and idempotent so repeated closeout runs do not churn the same files.
- Remove empty archive directories after pruning old snapshots.
- Prefer a small retention limit with an explicit override only when a specific audit needs it.

## Validation

- Targeted checks: pruning removes old snapshots without harming live outputs.
- Broader checks: archive-only surfaces stay compact after repeated batch runs.
- Closeout checks: the generated-history cleanup command is deterministic.
- Verify the cleanup command leaves no dangling empty directories for pruned audit groups.

## Completion Evidence

- Status: complete
- Changed files: `scripts/audits/cleanup-generated-history.rb`, `Makefile`, `scripts/implementation-batch.sh`, `scripts/audits/local_tooling_extended_tools.rb`
- Validation evidence: `make cleanup-generated-history`, `make control-refresh-full`, `make implementation-batch topic=implementation-system`
- Doc delta summary: generated history pruning now runs deterministically before closeout, prunes archive-only noise, and participates in the batch workflow.
- Deferred work: none
