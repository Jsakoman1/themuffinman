---
machine_kind: plan
machine_status: unknown
machine_title: Validation Memory Phase Two Plan
machine_goal: Add a schema-backed JSON validation-memory contract and integrate its
  canonical command knowledge into local preset recommendations and context routing
  as an additive layer.
---

# Validation Memory Phase Two Plan

Purpose: formalize the machine-readable validation-memory contract and let local validation preset routing reuse it without weakening existing deterministic checks.

## Goal

Add a schema-backed JSON validation-memory contract and integrate its canonical command knowledge into local preset recommendations and context routing as an additive layer.

## Scope

- `docs/validation-memory.json`
- `docs/validation-memory.schema.json`
- `docs/validation-memory.md`
- `docs/documentation-sync-policy.md`
- `docs/feature-delivery-workflow.md`
- `scripts/audits/local_tooling_extended_tools.rb`
- `scripts/audits/codex_local_context_gateway.rb`
- `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`

## Checklist

- [x] Add a JSON schema for validation-memory.
- [x] Validate the JSON contract from the backend docs validation test.
- [x] Reuse validation-memory canonical commands in `recommend-validation-preset` as an additive overlay.
- [x] Keep the overlay advisory-only so deterministic preset logic remains intact.
- [x] Validate docs, gateway syntax, and validation preset generation.

## Validation

- `ruby -c scripts/audits/codex_local_context_gateway.rb`
- `./mvnw test -Dtest=AgentOperatingModelValidationTest`
- `make recommend-validation-preset files='docs/validation-memory.md,docs/validation-memory.json,docs/validation-memory.schema.json,scripts/audits/local_tooling_extended_tools.rb'`
- `make audit-documentation`

## Completion Evidence

- Status: complete
- Execution status: complete
- Validation:
  - `ruby -c scripts/audits/codex_local_context_gateway.rb`
  - `./mvnw test -Dtest=AgentOperatingModelValidationTest`
  - `make recommend-validation-preset files='docs/validation-memory.md,docs/validation-memory.json,docs/validation-memory.schema.json,scripts/audits/local_tooling_extended_tools.rb'`
  - `make audit-documentation`
- Notes:
  - Added `docs/validation-memory.schema.json` and validated `docs/validation-memory.json` through the existing backend docs validation suite.
  - Integrated validation-memory into `recommend-validation-preset` only as an additive canonical-command overlay, so existing deterministic preset selection and stronger validation rules remain the authority.
  - Extended gateway cache invalidation inputs so validation-memory contract changes also refresh codex-context payload composition.
- Persistent backlog item: none
