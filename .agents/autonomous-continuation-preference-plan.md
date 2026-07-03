---
machine_kind: plan
machine_status: unknown
machine_title: Autonomous Continuation Preference Plan
machine_goal: Future sessions should continue through safe offered follow-up slices
  by default unless the user narrows scope, a real blocker appears, or approval is
  required.
---

# Autonomous Continuation Preference Plan

## Scope

Record the user's standing preference that safe proposed next steps should be implemented automatically instead of pausing for option selection.

## Goal

Future sessions should continue through safe offered follow-up slices by default unless the user narrows scope, a real blocker appears, or approval is required.

## Planned Work

1. Update startup and collaboration instructions in `AGENTS.md`.
2. Update compact workflow guidance in `docs/codex-fast-path.md`.
3. Update workflow and policy docs for autonomous continuation behavior.
4. Update machine-operational source sections if the rule is represented there.
5. Regenerate agent-operating artifacts and validate.

## Validation

- `make generate-agent-operating-model`
- `make generate-agent-artifacts`
- `./mvnw test -Dtest=AgentOperatingModelValidationTest`
