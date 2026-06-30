# Backend Drift Remediation Master Plan

Purpose: convert the remaining backend standardisation drift into an executable backlog, then shrink the highest-value workmarket and social hotspots.

## Goal

Keep the source-of-truth, docs, generated audits, and backend code aligned while reducing the remaining oversized or mixed-responsibility backend surfaces.

This master plan is a continuation of the repository-wide standardisation effort, but it narrows execution to the current unresolved drift.

## Execution Model

- Run the child plans in the order listed below.
- Do not start hotspot reduction until the drift backlog is frozen into concrete, domain-grouped items.
- If a child plan grows beyond one safe pass, split the remainder into a new stable child plan and keep the master plan moving.
- Keep the final closeout pass open until the code, docs, generated artifacts, and validation evidence agree.

## Current Findings

- [`.agents/backend-drift-remediation-findings.md`](/Users/jsakoman/Desktop/themuffinman/.agents/backend-drift-remediation-findings.md)
- [`docs/generated/local-tooling/architecture-drift-summary.md`](/Users/jsakoman/Desktop/themuffinman/docs/generated/local-tooling/architecture-drift-summary.md)
- [`docs/generated/local-tooling/doc-sync-preflight-summary.md`](/Users/jsakoman/Desktop/themuffinman/docs/generated/local-tooling/doc-sync-preflight-summary.md)
- [`docs/generated/local-tooling/test-gap-recommendations-summary.md`](/Users/jsakoman/Desktop/themuffinman/docs/generated/local-tooling/test-gap-recommendations-summary.md)
- [`docs/generated/source-of-truth-audit.json`](/Users/jsakoman/Desktop/themuffinman/docs/generated/source-of-truth-audit.json)

## Child Plans

- [x] `BACKEND-DRIFT-BACKLOG-EXECUTION` - `.agents/todo-plans/93-backend-drift-backlog-execution.md`
- [x] `BACKEND-HOTSPOT-REDUCTION-WORKMARKET-SOCIAL` - `.agents/todo-plans/94-backend-hotspot-reduction-workmarket-social.md`

## Execution Phases

1. Freeze the remaining drift inventory into domain-grouped backlog items.
2. Reduce workmarket hotspots first because it is the densest backend surface.
3. Reduce social hotspots next because it still has the largest controller/service fanout.
4. Re-run backend validation, docs sync, and generated artifact refreshes after each slice.
5. Leave identity and location follow-ups as the next queued wave unless they block the current work.

## Closeout Rules

- The `BACKEND-MODEL-STANDARDIZATION-001` umbrella item was completed by the upstream standardization passes and removed from the open backlog.
- Update living docs whenever behavior, permissions, workflows, or contracts change.
- Update generated artifacts whenever source-of-truth or machine-operational docs change.
- Record any newly deferred remainder as a new stable backlog item before closing the current slice.

## Completion Evidence

- Status: complete
- Execution status: complete
- Persistent backlog source: `docs/implementation-backlog.md`
- Residual risk: later identity and location cleanup remains queued in the persistent backlog
