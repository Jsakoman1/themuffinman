---
machine_kind: plan
machine_status: complete
machine_title: Human Versus Machine Truth Boundary
machine_goal: Make it easier to tell which surfaces are human-review guidance and which surfaces are machine-operational truth.
---

# Human Versus Machine Truth Boundary

## Status

Complete.

## Goal

Make it easier to tell which surfaces are human-review guidance and which surfaces are machine-operational truth.

## Parent Master Plan

- Master plan: `.agents/implementation-system-improvement-master-plan-next.md`

## Scope

- Included: boundary wording, source-of-truth labels, and review-versus-contract separation.
- Excluded: changing the actual machine contract.

## Validation

- Targeted checks: review the operating-model and workflow docs for clear labels.
- Broader checks: confirm readers can tell which file is authoritative without guessing.
- Closeout checks: keep the boundary consistent across docs and generated YAML.

## Completion Evidence

- Status: complete
- Changed files: docs/agent-operating-model.md, docs/agent-operating-model/sections/documentation_sync.yaml, AGENTS.md
- Validation evidence: `make generate-agent-operating-model`, `./mvnw -q -Dtest=AgentOperatingModelValidationTest test`
- Doc delta summary: human-review docs and machine-operational truth are now called out separately in the operating model.
- Deferred work: none
