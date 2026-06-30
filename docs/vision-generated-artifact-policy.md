# Vision Generated Artifact Policy

This file makes the generated-artifact rules for `/vision` work explicit at the start of a batch instead of only during closeout.

## Core Rule

If the source of truth changed, refresh the generated artifact in the same batch.

## Command Matrix

### Run `make generate-agent-operating-model` when:

- `docs/agent-operating-model.yaml` changed
- files under `docs/agent-operating-model/sections/` changed
- machine-operational workflow, intent, permission, endpoint, or safety rules changed

### Run `make generate-agent-artifacts` when:

- the operating-model source docs changed and generated inventories or summaries depend on them
- backend audit, endpoint inventory, automation read-model inventory, or source-of-truth audit output should reflect the new state

### Run `npm run generate:contracts` when:

- backend DTO shape changed for vision endpoints
- vision controller responses changed
- endpoint request contracts changed
- frontend vision code depends on generated contract types that the backend change affects

## Vision Default Heuristic

Use this shortcut before implementation:

- backend-only parser or pure service logic: usually no generated artifact refresh
- backend DTO or endpoint change: contract regeneration likely required
- agent-operating docs or machine-safety doc change: operating-model generators required
- mixed backend, frontend, and agent-doc batch: assume all three command families may be required until proven otherwise

## Closeout Recording Rule

When a generator is required, record both:

- the command that refreshed the artifact
- the artifact files that changed because of it

## Common Vision Cases

### Conversation DTO Changed

Usually run:

- `npm run generate:contracts`
- `npm run type-check`

### Vision Endpoint Inventory Changed

Usually run:

- `make generate-agent-operating-model`
- `make generate-agent-artifacts`
- `./mvnw test -Dtest=AgentOperatingModelValidationTest`

### New Vision Durable Process Rule Added

Usually update:

- `docs/vision-architecture-patterns.md`
- `docs/codex-fast-path.md`

Then decide whether it also changed machine-operational docs. If yes, run the generator pair above.
