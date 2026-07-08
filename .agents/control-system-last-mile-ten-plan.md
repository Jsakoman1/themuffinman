---
machine_kind: plan
machine_status: complete
machine_title: Control System Last Mile Ten Plan
machine_goal: Remove the last operator-core heaviness so the control system stays strict, low-noise, and easier to maintain at scale.
---

# Feature Implementation Plan

## Status

Complete.

## Workflow Frame

- Feature tier: control-system hardening
- Scope: reduce generated-history noise, narrow inventory-only machine rule friction, and simplify operator-core maintenance without weakening enforcement
- Out of scope: product behavior changes and any weakening of plan/doc/validation closeout rules
- Manifest decision: not required unless the batch changes manifest workflow rules directly
- Manifest path: none
- Master plan: `.agents/platform-last-mile-ten-master-plan.md`
- Use one durable Plan instead of introducing a Master Plan when the work still fits one bounded implementation surface and one validation story.

## Routing Snapshot

- Context commands:
  - `make control-start`
  - `make codex-context topic=control-system intent='identify the final operator-core heaviness keeping the control system below ten out of ten'`
- Routing commands:
  - `make implementation-batch topic=control-system`
- Validation commands:
  - `./mvnw -Dtest=AgentOperatingModelValidationTest,WorkflowStateMachineCatalogTest test`
- Closeout commands:
  - `ruby scripts/audits/audit-plan-completion.rb plan=.agents/control-system-last-mile-ten-plan.md`
- Doc sync commands:
  - `make generate-agent-operating-model`
  - `make generate-agent-artifacts`
- Generated artifact commands:
  - `make control-start`
  - `make audit-generated-artifact-freshness`

## Implementation Slices

- [x] Slice 1: reduce unnecessary generated history and secondary summaries that still add operational weight without improving closeout clarity
- [x] Slice 2: tighten the machine-readable distinction between direct workflow sources and inventory-only helper sources so validators stay strict without repeated manual allowlist churn
- [x] Slice 3: simplify operator-core documentation and closeout routing where duplicated reminders still survive across the control stack
- [x] Slice 4: extend validation around the lighter control surface so simplification does not silently remove useful enforcement
- [x] Slice 5: refresh control docs and inventories so the last-mile control model is easier for future agents to execute consistently

## Validation Plan

- Targeted checks:
  - `./mvnw -Dtest=AgentOperatingModelValidationTest,WorkflowStateMachineCatalogTest test`
  - `make control-start`
  - `make audit-generated-artifact-freshness`
- Broader checks:
  - `./mvnw test`
- Skipped checks or reasons: frontend checks only if this slice touches frontend-facing generated control outputs
- Validation preset: control-system

## Docs and Artifacts

- Expected docs:
  - `docs/codex-fast-path.md`
  - `docs/feature-delivery-workflow.md`
  - `docs/agent-operating-model.md`
  - `docs/agent-operating-model.yaml`
  - `docs/tooling/codex-local-audits.md`
- Expected generated artifacts:
  - control start
  - audit summary index
  - generated artifact freshness audit
- Temporary work products: temporary control inventories only while the plan is active

## Closeout Gates

- Required closeout checks:
  - operator-core surfaces stay compact and current
  - validator strictness remains intact after any simplification
  - generated artifact freshness must stay green
- Final response evidence: include concrete before and after examples of lighter control flow with unchanged enforcement quality
- Backlog follow-up rule: any deferred control work must be recorded in `docs/agent-improvement-backlog.md`
- Do not mark the plan complete until the scope is actually implemented, the required validation has passed or been explicitly skipped with a reason, and the completion evidence matches reality.

## Open Questions

- Resolver outputs still needed: which control artifacts are truly operator-core versus merely historical
- Risks or approvals: low code risk, medium process-design risk if simplification hides useful audit signals

## Completion Evidence

- Status: complete
- Changed files: `docs/agent-operating-model/sections/dead_path_tracker.yaml`, `docs/agent-operating-model/sections/source_of_truth.yaml`, `docs/agent-operating-model.schema.json`, `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`, `docs/agent-operating-model.yaml`
- Validation evidence: `make generate-agent-operating-model`; `./mvnw -Dtest=AgentOperatingModelValidationTest,WorkflowStateMachineCatalogTest test`; `make control-start`; `make audit-generated-artifact-freshness`
- Doc delta summary: the dead-path tracker now groups inventory-only helper source refs by role and reason instead of maintaining one flat allowlist, and the schema plus validator now enforce that grouped structure.
- Backlog update: none
- Residual risk: low; the grouped model is clearer and easier to maintain, but future helper additions still require explicit classification.
