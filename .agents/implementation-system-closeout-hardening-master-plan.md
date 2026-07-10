---
machine_kind: master-plan
machine_status: complete
machine_title: Implementation System Closeout Hardening
machine_goal: Build a deterministic closeout driver, stricter plan state machine, proactive preflight, stronger evidence contracts, generated-history pruning, a master batch ledger, and more derived manifest evidence so closeout becomes simpler, faster, and harder to fake.
---

# Implementation System Closeout Hardening

## Status

Complete.

## Goal

Build a deterministic closeout driver, stricter plan state machine, proactive preflight, stronger evidence contracts, generated-history pruning, a master batch ledger, and more derived manifest evidence so closeout becomes simpler, faster, and harder to fake.

## Parent God Plan

- God Plan: `Plan System God Plan`
- Machine-readable path: `.agents/god-plans/plan-system-god-plan.yaml`

## Scope

- Included: closeout orchestration, plan-state enforcement, preflight gating, evidence shape hardening, archive-only cleanup, batch ledgering, and manifest derivation.
- Excluded: product UI changes, domain behavior changes, and unrelated workflow rewrites.

## Analysis Notes

- The closeout driver must be a first-class executable entrypoint, not just a doc-level sequence.
- Preflight should emit a readable blocker report before final audits begin.
- Evidence contracts need to cover both plan completion and manifest closeout so drift cannot split across two shapes.
- Generated-history pruning should own retention and empty-directory cleanup, not just snapshot deletion.
- The batch ledger should be machine-readable and appended by the batch wrapper so execution state is durable.
- Manifest derivation should stay bounded to repo-state-derived fields instead of becoming a full manifest rewrite.

## Child Plans

1. `.agents/implementation-system-closeout-driver-plan.md`
- Role: create one deterministic closeout driver that runs `cleanup-generated-history`, `temp-work-product-closeout`, `autofill-feature-closeout`, `audit-plan-completion`, `feature-closeout-audit`, and `closeout-report` in order and fails immediately on the first drift.
- Status: complete

2. `.agents/implementation-system-plan-state-machine-plan.md`
- Role: tighten plan status transitions to `draft -> active -> blocked/deferred -> complete` and make `complete` impossible without real evidence for code, tests, docs, and changed files.
- Status: complete

3. `.agents/implementation-system-closeout-preflight-plan.md`
- Role: add an automatic preflight that checks `git diff`, plan status, generated-artifact freshness, temp work products, and archive-only evidence before closeout starts.
- Status: complete

4. `.agents/implementation-system-closeout-evidence-contract-plan.md`
- Role: define the evidence contract for plan completion and manifest closeout, including changed files, validation evidence, doc delta summary, child plans, temp work products, and archive-only bans.
- Status: complete

5. `.agents/implementation-system-generated-history-pruning-plan.md`
- Role: make `.history` and `.cache` auto-pruning deterministic with retention limits by audit type and cleanup of empty directories.
- Status: complete

6. `.agents/implementation-system-master-batch-ledger-plan.md`
- Role: record each child slice in a durable ledger as executed, deferred, or blocked so batch progress does not have to be reconstructed from scattered plan files.
- Status: complete

7. `.agents/implementation-system-manifest-derivation-plan.md`
- Role: reduce manual manifest fields where repo state can derive the same truth, especially for closeout evidence and artifact lists.
- Status: complete

## Execution Order

1. Closeout driver
2. Plan state machine
3. Closeout preflight
4. Evidence contract
5. Generated-history pruning
6. Master batch ledger
7. Manifest derivation

## Design Constraints

- Closeout must fail on the first real blocker instead of aggregating only at the end.
- Completed plans must not be inferred from status alone.
- Archive-only paths must never appear as live closeout evidence.
- Derived evidence should replace repeated manual fields wherever repo state can already answer the question.
- The implementation should keep the normal closeout path compact enough that operators can still run it without reading the entire repo.

## Preconditions

- The current workflow docs, validation memory, and agent-operating model must stay in sync with any new control behavior.
- Any durable follow-up surfaced during execution must be recorded in the appropriate backlog before the batch closes.
- The master plan should not be marked complete until every child plan is complete or explicitly deferred with a stable backlog reference.

## Dependencies

- `AGENTS.md`
- `docs/program-planning-model.md`
- `docs/codex-fast-path.md`
- `docs/feature-delivery-workflow.md`
- `docs/change-completion-checklist.md`
- `docs/documentation-sync-policy.md`
- `docs/validation-memory.md`
- `docs/validation-memory.json`
- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml`

## Validation

- Targeted checks: plan and workflow doc review against the requested closeout improvements.
- Broader checks: any closeout driver or state-machine implementation should validate against plan completion, feature closeout, and validation-memory drift.
- Closeout checks: the master plan should only close after each child plan has proof of actual implementation, not just status updates.
- Batch validation should include at least one driver-path run once the child plans become active.

## Completion Evidence

- Status: complete
- Changed files: `.agents/implementation-system-closeout-hardening-master-plan.md`, `.agents/implementation-system-closeout-driver-plan.md`, `.agents/implementation-system-plan-state-machine-plan.md`, `.agents/implementation-system-closeout-preflight-plan.md`, `.agents/implementation-system-closeout-evidence-contract-plan.md`, `.agents/implementation-system-generated-history-pruning-plan.md`, `.agents/implementation-system-master-batch-ledger-plan.md`, `.agents/implementation-system-manifest-derivation-plan.md`, `scripts/audits/closeout-driver.rb`, `scripts/feature-closeout-audit.sh`, `scripts/implementation-batch.sh`
- Child plan status: complete
- Validation evidence: `make closeout-driver plan=.agents/chat-prod-ready-plan.md manifest=.agents/feature-manifests/chat-prod-ready-manifest.yaml`, `make feature-closeout-audit manifest=.agents/feature-manifests/chat-prod-ready-manifest.yaml`, `make implementation-batch topic=implementation-system`, `make control-refresh-full`
- Doc delta summary: closeout orchestration, plan-state enforcement, preflight gating, evidence contracts, generated-history pruning, master batch ledgering, and derived manifest evidence are now wired into the operational workflow.
- Deferred work: none
