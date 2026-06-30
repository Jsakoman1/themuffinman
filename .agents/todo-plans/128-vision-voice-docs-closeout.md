# Vision Voice Docs And Closeout

Purpose: synchronize the new voice surface across living docs, agent-operating docs, and closeout artifacts.

## Goal

Document the backend-first voice contract, the `/vision` speech behavior, and the agent/documentation implications of the new endpoint and UX layer.

## Scope

- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/product-memory.md`
- `docs/product-vision.md`
- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml`
- `docs/agent-operating-model/sections/api.yaml`
- `docs/agent-operating-model/sections/intents.yaml`
- `docs/codex-fast-path.md`
- `docs/feature-delivery-workflow.md`
- `docs/documentation-sync-policy.md`
- `docs/change-completion-checklist.md`

## Checklist

- [x] Document the backend voice-config endpoint and `/vision` speech behavior.
- [x] Add the new endpoint and read intent to the agent-operating model.
- [x] Regenerate and validate agent-operating artifacts if needed.
- [x] Close out the master plan and refresh post-plan memory artifacts.

## Validation

- `./mvnw test -Dtest=AgentOperatingModelValidationTest`
- `make audit-documentation`
- `make audit-doc-canonical-phrases`
- `make audit-summary-index`

## Completion Evidence

- Status: complete
- Execution status: complete
- Validation:
  - `make generate-agent-operating-model`
  - `make generate-agent-artifacts`
  - `./mvnw test -Dtest=AgentOperatingModelValidationTest`
  - `make audit-documentation`
  - `make audit-doc-canonical-phrases`
  - `make audit-summary-index`
- Notes:
  - Voice-config source-of-truth coverage is present in the agent-operating section files and generated YAML.
  - Living docs now describe backend-governed speech defaults for the adaptive `/vision` surface.
  - Product memory and product vision keep the parallel audio-visual interaction rule explicit for future sessions.
- Persistent backlog item: none
