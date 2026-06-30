# Vision Failure Memory

This file captures repeatable `/vision` failure classes so future sessions can skip the same mistakes.

Use it as a diagnostic memory, not as an implementation backlog.

## VF-001: Generator Drift

### Symptom

Docs or machine-operational source files changed, but generated artifacts still reflect the old state.

### Typical Triggers

- edits under `docs/agent-operating-model/`
- edits to `docs/agent-operating-model.yaml`
- endpoint inventory or safety coverage changes

### Fix Pattern

- run `make generate-agent-operating-model`
- run `make generate-agent-artifacts`
- verify generated files are included in the same change when the source-of-truth changed

## VF-002: Docs Sync Drift

### Symptom

Vision behavior changed in code, but only one of `business-logic`, `domain-technical`, or vision-specific docs was updated.

### Typical Triggers

- new slot behavior
- changed clarification wording
- changed lifecycle or review loop
- changed executor gating or safety boundary

### Fix Pattern

- update the user-visible rule in `docs/business-logic.md`
- update entities, workflow, validation, or permissions in `docs/domain-technical.md`
- update the relevant vision-specific durable doc in the same batch

## VF-003: Contract Regen Needed

### Symptom

Frontend vision code and backend DTOs compile conceptually, but generated contract files or API docs still expose stale types.

### Typical Triggers

- DTO shape changes
- endpoint response changes
- added lifecycle or list endpoints

### Fix Pattern

- run `npm run generate:contracts`
- run `npm run type-check`
- inspect `apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts`

## VF-004: Vision Flow Test Drift

### Symptom

Tests become noisy because each new conversation case rewrites the same slot setup, location candidates, or spoken-time phrases.

### Typical Triggers

- new create-quest conversation branches
- location confirmation branches
- schedule parsing expansions
- review/edit loops

### Fix Pattern

- use reusable fixtures from `src/test/java/com/themuffinman/app/vision/testing/`
- prefer named presets for slot state, location candidates, and spoken schedule phrases
- keep the test focused on branch intent, not on boilerplate setup

## VF-005: Review Loop Drift

### Symptom

A conversation reaches review, but post-review edits or confirmations bypass the single-next-step rule.

### Typical Triggers

- adding a new editable slot
- mixing correction and confirmation in one turn

### Fix Pattern

- enforce one requested slot or one explicit action per turn
- keep review-ready state transitions explicit in the conversation service
- add a test that proves the edit returns to review instead of skipping forward silently

## VF-006: Lookup Confidence Drift

### Symptom

Location resolution silently replaces typed user input with a backend lookup candidate without a deterministic user choice.

### Typical Triggers

- auto-applying candidate 1
- collapsing multi-candidate results into one label

### Fix Pattern

- keep typed location until the user confirms a candidate
- expose candidate choices explicitly
- fail closed to the same slot when the confirmation answer is ambiguous
