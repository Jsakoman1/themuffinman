---
machine_kind: master-plan
machine_status: complete
machine_title: Implementation System Improvement Master Plan
machine_goal: Coordinate concrete implementation/control improvements in explicit child plans so broad batches start cleanly, run to completion, and close out without losing layered-analysis discipline.
---

# Implementation System Improvement Master Plan

## Status

Complete.

## Goal

Coordinate concrete implementation/control improvements in explicit child plans so broad batches start cleanly, run to completion, and close out without losing layered-analysis discipline.

## Parent God Plan

- God Plan: `Plan System God Plan`
- Machine-readable path: `.agents/god-plans/plan-system-god-plan.yaml`

## Scope

- Included: layered-analysis entrypoints, batch routing, execution discipline, validation/evidence capture, closeout ordering, docs-sync propagation, and control-surface clarity for implementation batches.
- Excluded: domain business rules, UI redesign, and control-system rebuild tasks.

## Child Plans

1. `.agents/implementation-system-layered-analysis-snapshot-parity-plan.md`
- Role: keep `make control-start`, `make codex-context`, and `make context-pack` aligned on layered-analysis and temporary work-product visibility.
 - Status: complete

2. `.agents/implementation-system-context-pack-routing-plan.md`
- Role: make topic routing choose the smallest useful pack while still surfacing the right context for broad implementation batches.
 - Status: complete

3. `.agents/implementation-system-batch-slice-sequencing-plan.md`
- Role: give broad batches an explicit ordered slice list so execution continues without needless “what next” pauses.
 - Status: complete

4. `.agents/implementation-system-batch-checklist-plan.md`
- Role: define a short batch checklist for start, slice, validate, widen, and closeout decisions.
 - Status: complete

5. `.agents/implementation-system-continuation-rule-consolidation-plan.md`
- Role: reduce repeated continuation wording by consolidating the safe-batch rule across workflow docs.
 - Status: complete

6. `.agents/implementation-system-follow-up-capture-plan.md`
- Role: make safe follow-up ideas land in backlog or deferred work surfaces during the current batch instead of disappearing.
- Status: complete

7. `.agents/implementation-system-temp-work-product-lifecycle-plan.md`
- Role: keep temporary analysis and inventory files tied to one owning plan with an obvious cleanup path.
- Status: complete

8. `.agents/implementation-system-validation-preset-routing-plan.md`
- Role: map changed files to a sensible default validation preset so the same test set is not rediscovered by hand.
- Status: complete

9. `.agents/implementation-system-validation-evidence-quality-plan.md`
- Role: make evidence records more concrete so later sessions can see exact commands, scopes, and skipped reasons.
- Status: complete

10. `.agents/implementation-system-closeout-order-plan.md`
- Role: make the closeout sequence stable so snapshots, cleanup, evidence, audits, and memory updates happen in one predictable order.
- Status: complete

11. `.agents/implementation-system-generated-artifact-hygiene-plan.md`
- Role: make generated-artifact hygiene checks easy to target to the exact touched files before widening to global freshness.
- Status: complete

12. `.agents/implementation-system-doc-sync-preflight-plan.md`
- Role: surface docs-sync requirements earlier so the affected docs are obvious before the first closeout pass.
- Status: complete

13. `.agents/implementation-system-canonical-phrase-registry-plan.md`
- Role: centralize protected canonical wording so repeated phrases are easier to keep in sync across docs.
- Status: complete

14. `.agents/implementation-system-workflow-duplication-reduction-plan.md`
- Role: trim duplicated startup and workflow guidance while keeping the canonical meaning layer intact.
- Status: complete

15. `.agents/implementation-system-human-vs-machine-truth-boundary-plan.md`
- Role: make it easier to tell which surfaces are human-review guidance and which surfaces are machine-operational truth.
- Status: complete

16. `.agents/implementation-system-master-plan-template-hardening-plan.md`
- Role: improve the master-plan template so scope, phases, and validation expectations are explicit from the start.
- Status: complete

17. `.agents/implementation-system-plan-template-hardening-plan.md`
- Role: improve the per-plan template so each child slice captures the same minimum implementation contract.
- Status: complete

18. `.agents/implementation-system-god-plan-template-hardening-plan.md`
- Role: make the God Plan template better at tracking included scope, exclusions, child master plans, and active decisions.
- Status: complete

19. `.agents/implementation-system-plan-completion-audit-improvements-plan.md`
- Role: make plan completion audits easier to read when a plan still has open tasks or temp work products.
- Status: complete

20. `.agents/implementation-system-post-plan-memory-update-plan.md`
- Role: make post-plan memory updates a standard closeout step so durable lessons are captured right away.
- Status: complete

## Improvement Checklist

- [x] Keep the compact control snapshot, Codex context, and topic pack aligned on the same layered-analysis artifact.
- [x] Make broad batches start from the layered-analysis artifact instead of falling back to broad repo search.
- [x] Give broad batches a stable ordered slice sequence before implementation starts.
- [x] Add a short implementation-batch checklist for stop, validate, widen, and closeout decisions.
- [x] Consolidate the safe continuation rule so the same instruction does not need to be restated in slightly different words everywhere.
- [x] Make safe follow-up ideas land in a durable backlog during the active batch.
- [x] Keep temporary work products owned by one plan and easy to clean up at closeout.
- [x] Route file sets to a sensible validation preset instead of rediscovering the same tests each time.
- [x] Record validation evidence in a way that later sessions can replay the exact command and scope.
- [x] Stabilize closeout order so control refresh, cleanup, evidence, audits, and memory updates happen predictably.
- [x] Make generated-artifact hygiene checks easy to aim at the touched scope first.
- [x] Surface docs-sync requirements before closeout so the needed docs are obvious.
- [x] Reduce duplicate workflow prose without losing the canonical rule.
- [x] Make human-review docs and machine-operational docs easier to distinguish at a glance.
- [x] Harden the master-plan template for scope, phase boundaries, and dependencies.
- [x] Harden the plan template for per-slice validation and artifact expectations.
- [x] Harden the God Plan template for multi-master-plan coordination.
- [x] Improve plan-completion audits so open tasks and temp artifacts stand out immediately.
- [x] Standardize post-plan memory updates so durable lessons are captured as part of closeout.
- [x] Keep the implementation system focused on operator flow instead of broad philosophy.

## Pros

- Makes large batches easier to start safely.
- Keeps validation and closeout from becoming ad hoc.
- Gives future implementation work a clearer execution lane.

## Cons

- Adds more process surfaces that need the same discipline as the code.
- Can become noisy if the same workflow rule is repeated in too many places.

## Dependencies

- `AGENTS.md`
- `docs/codex-fast-path.md`
- `docs/feature-delivery-workflow.md`
- `docs/documentation-sync-policy.md`
- `docs/change-completion-checklist.md`
- `docs/program-planning-model.md`

## Validation

- Targeted checks: confirm each child plan file exists and names one concrete improvement.
- Broader checks: verify the master plan covers layered analysis, batch execution, validation, docs-sync, and closeout in one ordered chain.
- Closeout checks: refresh the plan index and control snapshot when this batch is ready to become active.

## Completion Evidence

- Status: complete
- Child plan status: complete
- Validation evidence: `make generate-agent-operating-model`, `./mvnw -q -Dtest=AgentOperatingModelValidationTest test`, `make implementation-batch topic=implementation-system`, `make audit-generated-artifact-freshness`, `make audit-todo`
- Doc delta summary: the implementation-system improvement batch now has synchronized workflow docs, machine-operational rules, generated batch outputs, and reusable templates.
- Deferred work: none
