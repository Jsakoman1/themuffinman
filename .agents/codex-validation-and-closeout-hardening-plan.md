# Codex Validation And Closeout Hardening Plan

## Status

Complete.

## Goal

Make validation, freshness, closeout, and plan completion checks more deterministic so Codex can trust the current repository state without re-discovering the same issues.

## Scope

- Included:
  - validation-memory docs and generated card outputs
  - plan completion audits
  - freshness audits for generated source-of-truth artifacts
  - closeout guidance in workflow docs
- Excluded:
  - runtime feature implementation
  - broad archive cleanup unless it directly affects validation noise

## Implementation Slices

1. Tighten the validation-memory and closeout-card sources.
2. Ensure generated freshness audits point to the real source of drift instead of vague summaries.
3. Make plan completion and backlog hygiene easier to verify from a single glance.

## Validation

- `make audit-todo`
- `make audit-generated-artifact-freshness`
- `make audit-plan-completion plan=<plan-file>`
- `make audit-doc-canonical-phrases`

## Completion Evidence

- Status: complete
- Validation evidence:
  - `make validation-memory-closeout-card`
  - `make audit-doc-canonical-phrases`
  - `make audit-generated-artifact-freshness`
  - `make audit-todo`
- Doc delta summary:
  - `docs/validation-memory.json`
  - `docs/validation-memory.md`
  - `scripts/audits/generate-validation-memory-closeout-card.rb`
  - `docs/generated/local-tooling/validation-memory-closeout-card-summary.md`
  - `docs/generated/local-tooling/validation-memory-closeout-card.json`
