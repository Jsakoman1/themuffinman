# Vision Feature Slice Checklist

Use this checklist for every meaningful `/vision` batch.

The goal is to force the same implementation slice every time so backend, API, frontend, docs, generators, and tests do not drift apart.

## Slice Checklist

### 1. Backend State

- intent or conversation state identified
- slot additions or changes identified
- clarification path identified
- review and confirmation path identified
- execution gate identified
- fail-closed conditions identified

### 2. Endpoint Contract

- request shape updated if needed
- response blocks and next-action shape updated if needed
- lifecycle or list endpoints updated if needed
- generated contract impact checked

### 3. Frontend Composable Or Renderer

- composable state updated for the new backend response shape
- canvas rendering updated for any new block or mode
- prompt dock or action wiring updated only if backend contract requires it
- no frontend-only business logic introduced

### 4. Docs Sync

- `docs/business-logic.md` updated when user-visible behavior changed
- `docs/domain-technical.md` updated when workflow, validation, entity, or permission logic changed
- vision durable docs updated when architecture, memory, or process rules changed

### 5. Generated Artifacts

- `make generate-agent-operating-model` if machine-operational source docs changed
- `make generate-agent-artifacts` if generated inventories or summaries depend on the changed source docs
- `npm run generate:contracts` if DTO or endpoint contracts changed

### 6. Targeted Tests

- conversation flow tests for stepwise state changes
- parser or resolver tests for deterministic normalization logic
- frontend type check and build when frontend files or contracts changed
- agent-operating validation test when machine-operational docs changed

## Vision Batch Start Rule

Before implementation starts, explicitly lock:

1. first real executor in scope
2. whether the batch is planning-only or execution-capable
3. persisted conversation assumptions
4. feature flags involved
5. generated-artifact commands likely required at closeout

## Vision Batch Closeout Rule

Do not close a vision batch while any of these are still implicit:

- the next-step behavior
- the execution gate
- the contract refresh requirement
- the docs sync surface
- the targeted tests for the changed slice
