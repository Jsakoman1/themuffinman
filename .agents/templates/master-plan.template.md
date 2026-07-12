---
machine_kind: master_plan
machine_status: draft
machine_closeout_contract: 2
machine_baseline_ref: TBD
machine_delivery_mode: coordination
machine_title: Master Plan Template
machine_goal: TBD
---

# Master Plan Template

## Status

Draft.

## Master Plan Frame

- Purpose: TBD
- Shared context: TBD
- Plan inventory: list every child plan as an explicit `.agents/<child>-plan.md` path
- Ordering and dependencies: TBD
- Final review gate: TBD

## Initial Analysis

- Problem statement: TBD
- Local audits and docs reviewed: TBD
- Key ideas and tradeoffs: TBD
- Risks or constraints: TBD

## Plan Breakdown

- [ ] Plan 1: TBD
- [ ] Plan 2: TBD
- [ ] Plan 3: TBD
- [ ] Plan 4: TBD

## Child Plan Status

| Plan | Manifest | Depends On | Status | Completion Audit |
| --- | --- | --- | --- | --- |
| Plan 1 label | TBD | none | draft | not run |
| Plan 2 label | TBD | Plan 1 | draft | not run |

Keep child plan paths only in `Plan inventory` or `Plan Breakdown`; the completion audit treats those sections as the authoritative child inventory.

## Cross-Plan Consistency Review

- Shared assumptions: TBD
- Overlaps or conflicts: TBD
- Simplifications or follow-up ideas: TBD

## Closeout Gates

- Required closeout checks: all listed child plan files pass `make audit-plan-completion`, implementation evidence is recorded for every implementation child, validation evidence is recorded, docs are synced, and final consistency review is complete
- Final response evidence: TBD
- Backlog follow-up rule: keep the master plan open until every plan is complete

## Completion Evidence

- Status: draft
- Baseline ref: TBD
- Child plan audit summary: TBD
- Changed files: TBD
- Validation evidence: TBD
- Doc delta summary: TBD
- Backlog update: TBD
- Residual risk: TBD
