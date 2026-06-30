# Vision Lifecycle And Ambiguity Plan

## Scope

Finish the next hardening batch for the persisted `/vision` conversation flow:

1. split reset and cancel lifecycle actions into dedicated backend endpoints
2. improve ambiguity handling for location candidate confirmation
3. improve ambiguity handling for spoken schedule phrases before larger voice rollout

## Locked Decisions

- `POST /vision/conversations/{conversationId}/reset` replaces turn-action reset for existing conversations.
- `POST /vision/conversations/{conversationId}/cancel` replaces turn-action cancel for existing conversations.
- `POST /vision/conversations/turns` remains the only prompt-bearing endpoint for user text or voice turns.
- Location candidate confirmation remains one-decision-at-a-time and must not fall through into review on ambiguous answers.
- Schedule parsing may accept a few more common spoken phrases, but unresolved or ambiguous phrases must stay in clarification mode.

## Validation

- Extend backend tests for dedicated lifecycle endpoints or service entrypoints.
- Extend backend tests for location candidate confirmation retry behavior.
- Extend backend tests for broader schedule voice phrasing and ambiguity retry behavior.
- Re-run `AgentOperatingModelValidationTest`.
- Re-run frontend `type-check` and `build`.
