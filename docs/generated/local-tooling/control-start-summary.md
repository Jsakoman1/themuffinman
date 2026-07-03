# Control Start

- Plan index: `docs/generated/local-tooling/plan-index-summary.md`
- Audit summary index: `docs/generated/local-tooling/audit-summary-index.md`
- Plan count: `167`
- Open count: `130`
- Open master plans: `5`
- Open plans: `10`
- Codex context review: `docs/generated/local-tooling/codex-context/latest.review.md`

## Open Master Plans

- `.agents/legacy-frontend-decommission-master-plan.md` | `active` | Remove the current multi-route legacy frontend and converge development onto a Vision-first frontend model while the product is still in dev phase and no real users depend on the legacy screens.
- `.agents/legacy-purge-master-plan.md` | `active` | Remove the remaining legacy `workmarket` naming and documentation surface from the product where it is still active, while preserving the current `vision` UX, backend capability model, and chat surface.
- `.agents/vision-context-memory-expansion-master-plan.md` | `active` | Give `/vision` a backend-owned memory layer that separates stable user context, current session context, and turn-level context so the orchestrator can handle multi-topic conversation shifts without flattening everything into one prompt blob.
- `.agents/backend-drift-remediation-master-plan.md` | `unknown` | Keep the source-of-truth, docs, generated audits, and backend code aligned while reducing the remaining oversized or mixed-responsibility backend surfaces.
- `.agents/backend-model-standardization-master-plan.md` | `unknown` | Make backend transport models and service layers look and behave the same across domains unless there is a clear domain-specific reason not to.

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

## Next Action

- Use `make codex-context topic=<topic> intent='<intent>'` for topic-specific broad work once this control snapshot is fresh.
