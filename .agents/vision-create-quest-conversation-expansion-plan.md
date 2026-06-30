# Vision Create Quest Conversation Expansion Plan

## Scope

Expand the current `/vision` `create_quest` conversation so it can collect a minimal real execution-ready schedule and location shape, plus allow one-step post-review corrections without reopening a legacy form flow.

## Goals

- Keep the conversation backend-owned and deterministic.
- Extend the slot sequence beyond title, description, reward, and visibility.
- Add only the smallest useful schedule and location decisions required for safer real quest creation.
- Support a simple review-edit loop where the user can name one field to change after review.
- Preserve one-primary-next-step behavior on every turn.

## Planned Slot Additions

1. `schedule_mode`
2. `scheduled_at` when `schedule_mode` is fixed
3. `location_mode`
4. `location_label` when `location_mode` is custom

## Locked Decisions For This Batch

- `schedule_mode=agreement` maps to `termFixed=false` and no `scheduledAt`.
- `schedule_mode=fixed` requires one explicit `scheduled_at`.
- `location_mode=profile` reuses the authenticated user's profile location.
- `location_mode=off` disables quest location.
- `location_mode=custom` starts with a single free-text location label only.
- Exact structured custom address capture stays deferred until the orchestration pattern is stable.
- Review-edit scope is field-targeted, one field at a time, not arbitrary natural-language diffing.

## Out Of Scope

- End time collection
- Circle selection for circles-only visibility
- Multi-field grouped editing
- Dedicated execute/cancel/reset endpoints
- Rich executor result navigation
- Broader intent expansion

## Validation

- Extend `VisionConversationServiceTest` for:
  - fixed schedule clarification
  - agreement schedule path
  - profile/off/custom location branches
  - review-edit field retargeting
- Re-run `AgentOperatingModelValidationTest`
- Re-run frontend type-check and build after contract/UI updates
