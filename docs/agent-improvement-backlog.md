# Agent Improvement Backlog

This file is the persistent forward-looking backlog for agent-safety, backend audit tightening, control-flow hardening, and documentation automation improvements.

It is not the execution source of truth for a single change. Active implementation details should still live in temporary plans under `.agents/`.

Use this file to preserve what remains worth tightening across sessions.

## Current State

- `executor_critical` backend audit tier is fail-hard.
- Strict `automation_relevant` DTO subsets now include:
- admin-agent DTO contracts
- chat DTO contracts
- identity DTO contracts
- location DTO contracts
- Backend audit inventory now assigns every backend file a `domainId` and `ownerId`.
- Current report-only remainder inside `automation_relevant` is still intentionally broad and should be tightened in small slices.

## Next Recommended Tightening

1. Tighten `social/dto` in smaller subsets instead of one large jump.
   Suggested order:
   - circle request and relation DTOs
   - circle overview and member DTOs
   - search and contact list DTOs
   - admin circle DTOs
2. Tighten `workmarket/dto` in selected read-contract subsets instead of the full package.
   Suggested order:
   - dashboard DTOs
   - quest detail DTOs
   - application detail DTOs
   - list/search/options DTOs
3. Keep `automation_relevant` service coverage report-first until DTO contract drift is much lower.

## Future Control-System Work

1. Add more rule-scoped strict slices for high-value automation read models before considering a wider tier promotion.
2. Consider ownership-aware reporting in source-of-truth audit output, not only backend inventory output.
3. Consider separate backlog items for:
   - workflow-aware frontend helper tightening
   - use-case workflow contract harness standardization
   - stronger doc-to-runtime semantic checks
   - source-of-truth registration audits for broader planner-visible DTO surfaces

## Operating Rule

- Prefer small, low-noise strict slices over whole-tier tightening.
- When one slice is promoted, update this backlog to remove or narrow the remaining work.
- Do not treat this file as proof that a change was implemented; it is a continuity aid for future sessions.
