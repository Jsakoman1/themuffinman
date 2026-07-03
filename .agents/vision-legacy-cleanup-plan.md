---
machine_kind: plan
machine_status: complete
machine_title: Vision Legacy Cleanup Plan
machine_goal: Finish the frontend decommission by removing the remaining legacy route-era
  surface while keeping Vision, admin, and auth stable.
---

# Vision Legacy Cleanup Plan

## Status

Completed.

## Goal

Finish the frontend decommission by removing the remaining legacy route-era surface while keeping Vision, admin, and auth stable.

## What Is Already Done

- Legacy user-facing routes now redirect into Vision.
- Legacy detail routes now redirect into Vision-native detail views.
- The old route entry files for user profile, settings, circles, chat, quest detail, and application detail have been removed.

## Layer 1: Can Be Removed After Validation

These are legacy route-entry concerns that should not come back:

- `modules/social/views/*` user-facing route views
- `modules/chat/views/ChatWorkspaceView.vue`
- legacy redirects for user-facing routes once no code depends on them
- old route-linked navigation targets that still point to route-era paths

## Layer 2: Keep As Shared Logic

These still support Vision and should remain shared until duplicated into a Vision-specific module:

- `modules/social/composables/useUserProfileView.ts`
- `modules/social/composables/useCirclesView.ts`
- `modules/social/components/profile/*`
- `modules/social/components/circles/*`
- `modules/chat/api/chatApi.ts`
- `modules/chat/composables/useAppChat.ts`
- `modules/workmarket/composables/useQuestDetailView.ts`
- shared detail and surface components used by Vision

## Layer 3: Keep Out Of Vision Cleanup

These are not legacy frontend cleanup targets for this pass:

- `modules/identity/views/LoginView.vue`
- `modules/identity/views/RegisterView.vue`
- `modules/workmarket/pages/Admin*.vue`
- `modules/social/pages/AdminCirclesPage.vue`

## Next Cleanup Candidates

1. Move any remaining user-facing route navigation hard-codes to Vision paths only.
2. Reduce or rename the remaining shared social/chat/workmarket modules so they read as shared infrastructure, not legacy module screens.
3. Decide whether `modules/social/views` and `modules/chat/views` can be deleted entirely once all references are removed.
4. Audit `README.md` and docs that still name deleted legacy views.

## Result

- Legacy route redirects were removed from the router.
- Route-facing documentation now points at Vision entrypoints instead of deleted legacy views.

## Validation

- `npm run type-check`
- `npm run build`
- `git diff --check`
