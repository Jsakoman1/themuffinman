---
machine_kind: plan
machine_status: complete
machine_title: Follow-Up Capture
machine_goal: Make safe follow-up ideas land in a durable backlog during the active batch instead of disappearing.
---

# Follow-Up Capture

## Status

Complete.

## Goal

Make safe follow-up ideas land in a durable backlog during the active batch instead of disappearing.

## Parent Master Plan

- Master plan: `.agents/implementation-system-improvement-master-plan-next.md`

## Scope

- Included: backlog routing, stable IDs, and deferred work capture.
- Excluded: implementing the deferred work itself.

## Validation

- Targeted checks: confirm backlog instructions are explicit in the workflow docs.
- Broader checks: confirm closed plans do not leave important follow-up items only in prose.
- Closeout checks: verify deferred items are recorded with stable IDs.

## Completion Evidence

- Status: complete
- Changed files: AGENTS.md, docs/implementation-backlog.md, docs/agent-improvement-backlog.md, docs/feature-delivery-workflow.md, docs/documentation-sync-policy.md, docs/agent-operating-model.md
- Validation evidence: `make audit-todo`, `make implementation-batch topic=implementation-system`
- Doc delta summary: safe follow-up capture is now tied to persistent backlog surfaces and batch execution rules.
- Deferred work: none
