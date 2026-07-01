# Vision Decision Record

This file holds the compact set of `/vision` decisions that should not be re-argued every implementation session.

If one of these changes, update this file and the dependent architecture docs in the same change.

## Locked Decisions

### VDR-001: Backend Owns Meaning

- OpenAI may suggest.
- Backend decides.
- Frontend renders.

Natural-language interpretation may use OpenAI, but execution-critical meaning, permissions, validations, and final action routing must remain deterministic backend decisions.

### VDR-002: One Primary Next Step Only

Every vision turn should yield one primary next step only:

- ask for one slot
- ask for one choice
- show one review
- ask for one confirmation
- execute one confirmed action
- stop in one clear blocked state

### VDR-003: Execution Gating Is Deterministic

Real mutation execution is never enabled implicitly by prompt confidence or frontend state.

Execution remains controlled by typed backend feature flags and explicit confirmation boundaries.

### VDR-004: Persisted Conversation Is The Default

Vision continuity is backend-persisted conversation state, not client-owned state tokens.

This is required for:

- resume and recent-session recovery
- stepwise clarification
- review/edit loops
- future auditing
- voice and text continuity

### VDR-005: Clarification Is Progressive

The system should not ask a twenty-field questionnaire up front.

It should collect the minimum next missing or ambiguous field and advance step by step.

### VDR-006: Domain Services Stay Authoritative

Vision adapters coordinate domain work.

They do not replace quest, location, social, or chat business services.

### VDR-007: Planning Can Be Flexible, Execution Cannot

Read-only planning and candidate generation may be permissive.

Mutation execution must fail closed on ambiguity, missing data, permission uncertainty, or untrusted normalization.

### VDR-008: Vision Work Uses A Stable Slice Contract

Every meaningful vision batch should be reasoned through the same slice:

- backend conversation or orchestration state
- API or contract impact
- frontend composable or renderer impact
- docs sync
- generated artifacts
- targeted tests

### VDR-010: Review Edits Are Typed

Review corrections should use explicit backend action plus explicit review target.

Free-text correction prompts must not be reinterpreted as review-edit commands inside the review-ready backend state.

### VDR-011: Continuity Is Summary-First

The shell should treat compact backend summaries as the source of long-session continuity.

- Resume and recent-task surfaces should read the summary first.
- Raw turn history is for detail, audit, and drill-down, not for reconstructing the active state from scratch.
- Frontend state may cache display hints, but it should not become the authoritative memory layer.

### VDR-009: Legacy Surfaces Are Not The Design Baseline

Legacy pages may remain temporarily.

New `/vision` behavior should be designed from the blank-canvas adaptive surface target, not by copying form-page-window patterns forward.
