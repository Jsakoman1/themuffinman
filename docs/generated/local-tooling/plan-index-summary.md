# Plan Index

- Total entries: 167
- Open entries: 130
- Open master plans: 26
- Open regular plans: 104
- Complete entries: 37

## Open Master Plans

- `.agents/legacy-frontend-decommission-master-plan.md` | `active` | Remove the current multi-route legacy frontend and converge development onto a Vision-first frontend model while the product is still in dev phase and no real users depend on the legacy screens.
- `.agents/legacy-purge-master-plan.md` | `active` | Remove the remaining legacy `workmarket` naming and documentation surface from the product where it is still active, while preserving the current `vision` UX, backend capability model, and chat surface.
- `.agents/vision-context-memory-expansion-master-plan.md` | `active` | Give `/vision` a backend-owned memory layer that separates stable user context, current session context, and turn-level context so the orchestrator can handle multi-topic conversation shifts without flattening everything into one prompt blob.
- `.agents/backend-drift-remediation-master-plan.md` | `unknown` | Keep the source-of-truth, docs, generated audits, and backend code aligned while reducing the remaining oversized or mixed-responsibility backend surfaces.
- `.agents/backend-model-standardization-master-plan.md` | `unknown` | Make backend transport models and service layers look and behave the same across domains unless there is a clear domain-specific reason not to.
- `.agents/codex-context-manifest-standardization-master-plan.md` | `unknown` | Reduce future token spend by making `make codex-context` emit one compact execution manifest that can be read instead of reconstructing plan, audit, and evidence state from several files.
- `.agents/codex-context-optimization-master-plan.md` | `unknown` | Codex Context Optimization Master Plan
- `.agents/codex-context-orchestration-v2-master-plan.md` | `unknown` | Reduce token spend and repeated recomposition cost for repeated Codex sessions by making the local context gateway more deterministic, more selective, and more parallel where the work is independent.
- `.agents/codex-lean-context-master-plan.md` | `unknown` | Codex Lean Context Master Plan
- `.agents/codex-local-context-gateway-master-plan.md` | `unknown` | CODEX-LOCAL-CONTEXT-GATEWAY Master Plan
- `.agents/codex-local-context-orchestrator-master-plan.md` | `unknown` | Turn the existing local context gateway into a more decisive orchestration layer that:
- `.agents/codex-summary-compactification-master-plan.md` | `unknown` | Reduce repeated token spend on broad report review by making the most frequently read generated summaries shorter, more decision-first, and more consistent.
- `.agents/codex-summary-compactification-phase2-master-plan.md` | `unknown` | Reduce the remaining broad-report token cost by tightening the last three noisy summary surfaces and regenerating the stale context manifest.
- `.agents/codex-summary-compactification-phase3-master-plan.md` | `unknown` | Make the remaining broad generated summaries shorter without losing the decision signal or the top evidence samples.
- `.agents/codex-summary-compactification-phase4-master-plan.md` | `unknown` | Reduce token spend on the remaining high-noise report families by compacting them in a fixed sequence of narrow batch plans.
- `.agents/local-tooling-24-tools-master-plan.md` | `unknown` | Local Tooling 24 Tools Master Plan
- `.agents/local-tooling-audits-master-plan.md` | `unknown` | Feature Implementation Plan
- `.agents/local-tooling-remaining-23-master-plan.md` | `unknown` | Implement the remaining `CODEX-LOCAL-*` items from `docs/codex-local-tooling-todo.md` in sequential phases without pausing between items.
- `.agents/master-plan-preference-plan.md` | `unknown` | Record that master-plan orchestration is allowed and preferred for broad, long-running, high-complexity work so agents can reduce unnecessary human interaction and handle larger tasks autonomously.
- `.agents/product-memory-and-vision-master-plan.md` | `unknown` | Separate open work from historical knowledge, then encode what we learn into a reusable memory layer and a clear product-vision model that can guide future UX, network, and interaction choices.
- ... and 6 more

## Open Plans

- `.agents/god-plans/vision-god-plan.yaml` | `active` | Coordinate the long-running `/vision` program across multiple master plans while keeping current implementation reality visible.
- `.agents/vision-context-memory-user-session-plan.md` | `active` | Add a backend-owned memory context to `/vision` that carries stable user context, current session context, and recent turn context into semantic understanding.
- `.agents/agent-control-phase-two-plan.md` | `unknown` | Agent Control Phase Two Plan
- `.agents/agent-model-feature-coverage-audit-plan.md` | `unknown` | Implement `CODEX-LOCAL-AGENT-MODEL-FEATURE-COVERAGE-AUDIT` to report code features not yet linked to the agent operating model or simulation surface.
- `.agents/agent-operating-refactor-plan.md` | `unknown` | Agent Operating Refactor Plan
- `.agents/agent-safety-enforcement-round2-plan.md` | `unknown` | Agent Safety Enforcement Round 2 Plan
- `.agents/agent-safety-upgrade-plan.md` | `unknown` | Agent Safety Upgrade Plan
- `.agents/agent-surface-standardization-phase1-plan.md` | `unknown` | Agent Surface Standardization Phase 1 Plan
- `.agents/aggregate-dead-code-audit-plan.md` | `unknown` | Implement `CODEX-LOCAL-AGGREGATE-DEAD-CODE-AUDIT` as one wrapper that runs dead-code audits and emits a compact merged summary.
- `.agents/api-contract-drift-audit-plan.md` | `unknown` | Feature Implementation Plan
- `.agents/async-mutation-flow-audit-plan.md` | `unknown` | Implement `CODEX-LOCAL-ASYNC-MUTATION-FLOW-AUDIT` to inventory frontend mutation flows, refresh paths, banners, and error handling.
- `.agents/audit-documentation-plan.md` | `unknown` | Implement `CODEX-LOCAL-AUDIT-DOCUMENTATION` as one short page describing local audits, outputs, and recommended usage order.
- `.agents/automation-readiness-gap-audit-plan.md` | `unknown` | Implement `CODEX-LOCAL-AUTOMATION-READINESS-GAP-AUDIT` to check minimum automation-relevant coverage for each feature/workflow.
- `.agents/autonomous-continuation-preference-plan.md` | `unknown` | Future sessions should continue through safe offered follow-up slices by default unless the user narrows scope, a real blocker appears, or approval is required.
- `.agents/backend-audit-domain-tagging-plan.md` | `unknown` | Backend Audit Domain Tagging Plan
- `.agents/backend-audit-identity-dto-tightening-plan.md` | `unknown` | Backend Audit Identity DTO Tightening Plan
- `.agents/backend-audit-location-dto-tightening-plan.md` | `unknown` | Backend Audit Location DTO Tightening Plan
- `.agents/backend-audit-manifest-cleanup-plan.md` | `unknown` | Backend Audit Manifest Cleanup Plan
- `.agents/backend-audit-tiering-plan.md` | `unknown` | Backend Audit Tiering Plan
- `.agents/backend-audit-tightening-plan.md` | `unknown` | Backend Audit Tightening Plan
- ... and 84 more

## Recent Complete Plans

- `.agents/active-plan-index-master-plan.md` | `complete` | Make active plans, master plans, and nearby closeout state easier to find at a glance through one compact
- `.agents/agent-surface-standardization-master-plan.md` | `complete` | Standardize the shared orchestration model behind:
- `.agents/codex-implementation-reliability-master-plan.md` | `complete` | Clean up and standardize the Codex control surface so implementation work becomes faster, more precise, more accurate, and more reliable.
- `.agents/control-system-machine-optimization-master-plan.md` | `complete` | Make the control system easier to start, easier to refresh, and more machine-readable so broad implementation work can
- `.agents/machine-readable-first-docs-master-plan.md` | `complete` | Make machine-readable operational and control files the primary source of truth for active behavior, planning state, and validation state, while keeping human-readable docs as derived summaries or curated narrative where needed.
- `.agents/vision-memory-and-context-master-plan.md` | `complete` | Add a persistent vision-specific memory layer across docs, workflow entrypoints, and reusable test fixtures.
- `.agents/vision-schedule-date-time-split-master-plan.md` | `complete` | Replace the current single-slot `scheduled_at` conversation model in `/vision` with a layered scheduling model that treats date and time as separate user-facing concepts while still deriving one absolute execution timestamp only when the backend has enough deterministic information.
- `.agents/active-plan-index-generator-plan.md` | `complete` | Generate a compact active plan index that shows open plans, open master plans, and completed planning artifacts in one
- `.agents/active-plan-index-routing-plan.md` | `complete` | Make the planning docs point at the active-plan index first so broad work can find open plan state quickly without
- `.agents/active-plan-index-validation-plan.md` | `complete` | Make the new active-plan index deterministic, auditable, and easy to keep fresh when plans are added, closed, or
- `.agents/agent-surface-standardization-phase2-plan.md` | `complete` | Standardize the shared semantic planning boundary used by Vision and Admin Playground so both surfaces rely on one backend prompt semantics path for normalization and intent classification.
- `.agents/codex-context-review-nomenclature-plan.md` | `complete` | Rename the `codex-context` human-facing output and its references to `latest.review.md`, while keeping `latest.machine.json`
- `.agents/codex-control-surface-hierarchy-plan.md` | `complete` | Standardize how the repo describes live truth, generated control, and historical archive material so Codex always knows which files are authoritative for current work.
- `.agents/codex-local-tooling-compactification-plan.md` | `complete` | Make local-tooling outputs compact, reviewer-useful, and consistent with the new review/machine/explain naming model so Codex can consume them faster.
- `.agents/codex-validation-and-closeout-hardening-plan.md` | `complete` | Make validation, freshness, closeout, and plan completion checks more deterministic so Codex can trust the current repository state without re-discovering the same issues.
- `.agents/control-system-doc-sync-plan.md` | `complete` | Update the planning and workflow docs so the new control refresh path, control-start entrypoint, and machine status
- `.agents/control-system-refresh-automation-plan.md` | `complete` | Refresh plan-index, registry, summary, and freshness outputs automatically after a plan closeout so the control system
- `.agents/control-system-start-entrypoint-plan.md` | `complete` | Add a shorter one-shot entrypoint for broad work that shows the current plan discovery state and the current audit
- `.agents/control-system-status-marker-plan.md` | `complete` | Add a machine-readable status marker to plan files and teach the plan index and closeout checks to prefer it before
- `.agents/legacy-backend-surface-cleanup-plan.md` | `complete` | Define how backend and contract cleanup should follow the frontend decommission so the repository stops carrying read models, endpoints, and docs that existed only to support removed legacy screens.

_Routing aid only. Use the underlying plan file or plan-completion report for final status._