# Vision Executor Rollout Plan

## Status

Planning only. Do not implement until explicitly requested.

## Purpose

Introduce real backend execution from `/vision` safely and incrementally.

The executor should turn a reviewed, fully resolved conversation into a real domain action by calling existing backend services and use cases.

## Current State

The agent can plan and summarize. It cannot yet execute natural-language mutations.

The agent operating docs explicitly require future mutation paths to resolve targets through documented read workflows and fail closed on ambiguity.

## Executor Principles

- Execution is a separate step from prompt interpretation.
- Execution requires structured intent, collected slots, validation, and explicit user confirmation.
- Execution calls existing use cases or domain services.
- LLM output cannot bypass validation, permission checks, or state transitions.
- Destructive and multi-actor flows are delayed until safe patterns exist.

## Recommended First Capability

`create_quest`.

Why this first:
- It is core product value.
- It is owned by the authenticated user.
- It exercises structured slot collection.
- It can be reviewed before commit.
- It avoids exact target resolution against existing records.

Flow:
1. User: "Create a quest for help moving boxes tomorrow for 30 euros."
2. Backend detects `create_quest`.
3. Backend extracts slots.
4. Backend asks one missing field at a time.
5. Backend assembles review.
6. User confirms.
7. Executor calls quest creation use case.
8. Canvas returns compact success state.

## Capability Rollout Order

1. Create quest draft/review.
2. Create quest after explicit confirmation.
3. Find/rank quests.
4. Apply to quest.
5. Edit own quest.
6. Update own application.
7. Update own profile fields.
8. Open or send chat message to accepted contact.
9. Create thing listing.
10. Create ride offer.
11. Create business profile update.

Later:
- Delete own quest.
- Approve/decline applications.
- Circle requests.
- Circle-only scheduled automation.
- Admin flows.

## Execution Adapter Pattern

For each capability, create an adapter:
- `VisionCreateQuestExecutionAdapter`
- `VisionApplyToQuestExecutionAdapter`
- `VisionUpdateProfileExecutionAdapter`

Adapter responsibilities:
- accept a typed execution candidate
- validate actor authority
- map slots into existing request DTOs
- call the domain use case/service
- map domain response into a compact canvas result

Adapter must not:
- parse natural language
- ask clarification questions
- own frontend display decisions
- skip existing domain validation

## Review Pattern

Before mutation, the backend should return:
- action name
- actor
- key fields
- warnings
- missing optional fields if relevant
- confirmation requirement

Frontend should show a compact review and ask for confirmation.

## Stop Conditions

Stop instead of executing when:
- target is ambiguous
- required slot missing
- translation unreliable and meaning affects execution
- action is destructive and not explicitly confirmed
- action requires another actor's consent
- action requires current location and no trusted coordinates exist
- current user lacks permission
- domain validation fails

## Testing

For every executor adapter:
- happy path
- missing required slot
- invalid slot value
- unauthorized actor
- domain validation failure
- confirmation required
- idempotency or duplicate-submit behavior where relevant

## Metrics To Add Later

- turn count before completion
- most common missing slots
- clarification success rate
- abandoned flows
- blocked execution reasons
- OpenAI fallback rate
