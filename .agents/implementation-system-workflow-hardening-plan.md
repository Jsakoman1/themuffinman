---
machine_kind: plan
machine_status: complete
machine_title: Implementation System Workflow Hardening
machine_goal: Update workflow docs and machine-operational rules so broad batches start with layered review and then execute continuously to final closeout.
---

# Implementation System Workflow Hardening

## Status

Complete.

## Goal

Update workflow docs and machine-operational rules so broad batches start with layered review and then execute continuously to final closeout.

## Scope

- Included: `AGENTS.md`, workflow docs, agent-operating-model docs, generated operating-model YAML, and section source.
- Excluded: product-domain code changes.

## Checklist

- [x] Add a canonical layered-review rule for broad implementation work.
- [x] Keep the batch-continuation rule synchronized across all workflow docs.
- [x] Regenerate the combined agent-operating model from the section source.

## Validation

- Targeted checks: `make generate-agent-operating-model`.
- Broader checks: `./mvnw -Dtest=AgentOperatingModelValidationTest test`.

## Completion Evidence

- Status: complete
- Validation evidence: `make generate-agent-operating-model`, `./mvnw -Dtest=AgentOperatingModelValidationTest test`
- Doc delta summary: the workflow docs, AGENTS, and agent-operating model now share the same batch-continuation and layered-review guidance.
