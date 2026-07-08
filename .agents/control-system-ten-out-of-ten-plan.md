---
machine_kind: plan
machine_status: complete
machine_title: Control System Ten Out Of Ten Plan
machine_goal: Tighten the control and implementation system until the operator-core path is compact, deterministic, low-noise, and resistant to false green closeouts.
---

# Feature Implementation Plan

## Status

Complete.

## Workflow Frame

- Feature tier: control-system hardening
- Scope: remove the last operational friction, noisy artifacts, weak closeout edges, and duplicated control routing from the implementation system
- Out of scope: new product behavior and broad UI work
- Manifest decision: not required unless a later slice introduces manifest-gated workflow changes
- Manifest path: none
- Master plan: `.agents/platform-ten-out-of-ten-master-plan.md`
- Use one durable Plan instead of introducing a Master Plan when the work still fits one bounded implementation surface and one validation story.

## Routing Snapshot

- Context commands:
  - `make control-start`
  - `make codex-context topic=control-system intent='identify remaining friction to reach ten out of ten control quality'`
- Routing commands:
  - `make implementation-batch topic=control-system`
- Validation commands:
  - `./mvnw -Dtest=AgentOperatingModelValidationTest,WorkflowStateMachineCatalogTest test`
- Closeout commands:
  - `ruby scripts/audits/audit-plan-completion.rb plan=.agents/control-system-ten-out-of-ten-plan.md`
- Doc sync commands:
  - `make generate-agent-operating-model`
  - `make generate-agent-artifacts`
- Generated artifact commands:
  - `make control-start`
  - `make audit-generated-artifact-freshness`

## Implementation Slices

- [x] Slice 1: reduce operator-core surface noise further by shrinking non-essential generated summaries and duplicate routing outputs
- [x] Slice 2: harden plan lifecycle enforcement so machine status, markdown status, completion evidence, and audit interpretation cannot silently drift apart
- [x] Slice 3: simplify doc-sync and closeout expectations where the current system is correct but overly heavy for common safe batches
- [x] Slice 4: add or tighten validation around workflow metadata, protected wording, and generated-artifact freshness assumptions
- [x] Slice 5: refresh control docs so the fast path and the strict path are both easier for future agents to execute consistently

## Validation Plan

- Targeted checks:
  - `./mvnw -Dtest=AgentOperatingModelValidationTest,WorkflowStateMachineCatalogTest test`
  - `make control-start`
  - `make audit-generated-artifact-freshness`
- Broader checks:
  - `./mvnw test`
- Skipped checks or reasons: frontend checks only if this slice touches frontend contract generation or frontend-facing workflow outputs
- Validation preset: control-system

## Docs and Artifacts

- Expected docs:
  - `docs/codex-fast-path.md`
  - `docs/feature-delivery-workflow.md`
  - `docs/change-completion-checklist.md`
  - `docs/documentation-sync-policy.md`
  - `docs/agent-operating-model.md`
  - `docs/agent-operating-model.yaml`
- Expected generated artifacts:
  - control start
  - plan index
  - audit summary index
  - generated artifact freshness audit
- Temporary work products: temporary control-surface inventories only while the plan is active

## Closeout Gates

- Required closeout checks:
  - operator-core surfaces stay compact and current
  - no plan can be marked complete while still materially open
  - generated artifact freshness must stay green
- Final response evidence: include before and after examples of removed control noise and hardened closeout behavior
- Backlog follow-up rule: any deferred control-system tightening must be recorded in `docs/agent-improvement-backlog.md`
- Do not mark the plan complete until the scope is actually implemented, the required validation has passed or been explicitly skipped with a reason, and the completion evidence matches reality.

## Open Questions

- Resolver outputs still needed: which operator-core outputs are still valuable versus merely historical
- Risks or approvals: low implementation risk, medium process-design risk if the simplification accidentally removes useful auditability

## Completion Evidence

- Status: complete
- Changed files: `scripts/audits/local_tooling_extended_tools.rb`, `Makefile`, `docs/codex-fast-path.md`, `docs/feature-delivery-workflow.md`, `docs/change-completion-checklist.md`, `docs/documentation-sync-policy.md`, `docs/tooling/codex-local-audits.md`, `docs/agent-operating-model.md`
- Validation evidence: `ruby -c scripts/audits/local_tooling_extended_tools.rb`; `make control-start`; `make audit-generated-artifact-freshness`; `./mvnw -Dtest=AgentOperatingModelValidationTest,WorkflowStateMachineCatalogTest test`
- Doc delta summary: audit summary index now separates on-demand template outputs from truly missing static outputs; `control-refresh-core` now always refreshes `diff-summary`; fast-path and workflow docs no longer repeat duplicate implementation-batch and autonomy guidance.
- Backlog update: none
- Residual risk: low; control surfaces are cleaner, but the broader workflow stack is still intentionally strict and should not be widened further without strong evidence.
