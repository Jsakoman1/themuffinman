---
machine_kind: plan
machine_status: unknown
machine_title: Vision Detail Parity Plan
machine_goal: Finish the current Vision terminal-first parity slice by routing remaining
  read-only settings and detail entry points through the shared conversation flow
  instead of dedicated route views.
---

# Vision Detail Parity Plan

## Goal

Finish the current Vision terminal-first parity slice by routing remaining read-only settings and detail entry points through the shared conversation flow instead of dedicated route views.

## Steps

1. Extend backend Vision capability preview and resolution services for:
   - settings snapshot
   - circle detail snapshot
   - application detail snapshot
2. Extend `VisionConversationService` to support:
   - `VIEW_SETTINGS`
   - `VIEW_CIRCLE_DETAIL`
   - `VIEW_APPLICATION_DETAIL`
3. Update canvas labels and placeholders for any new slot ids surfaced by the read-only detail flows.
4. Redirect remaining frontend settings/application detail routes into `/vision?prompt=...&autorun=1`.
5. Extend the same terminal-first read-only pattern to quest detail and user-profile-by-id routes.
6. Run targeted backend/frontend validation and then update Vision status docs if behavior changed materially.
