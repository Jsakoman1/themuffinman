# Permission Rule Duplication Audit

- Generated at: `2026-06-29T12:47:11Z`
- Backend permission sources: `13`
- Backend presentation flags: `4`
- Frontend passthrough gates: `61`
- Frontend local gates: `11`

## Overlap shortlist

- `APPLY` backend-sources=`3` backend-presentation=`0` frontend-local=`2` frontend-passthrough=`4`
  backend source files: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestViewAssembler.java`
  frontend local files: `apps/themuffinman/frontend/src/modules/workmarket/components/dashboard/DashboardQuestDialog.vue`
  frontend passthrough files: `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/createQuestDialogViewState.ts`, `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/useQuestDashboardDialogActions.ts`, `apps/themuffinman/frontend/src/modules/workmarket/composables/quest-detail/createQuestDetailViewState.ts`, `apps/themuffinman/frontend/src/modules/workmarket/composables/quest-detail/questDetailStateHelpers.ts`
- `WITHDRAW` backend-sources=`0` backend-presentation=`1` frontend-local=`1` frontend-passthrough=`2`
  backend source files: none
  frontend local files: `apps/themuffinman/frontend/src/modules/workmarket/views/QuestDetailView.vue`
  frontend passthrough files: `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/createApplicationDialogViewState.ts`, `apps/themuffinman/frontend/src/modules/workmarket/views/QuestDetailView.vue`

## Canonical backend anchors

- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestViewAssembler.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/SocialRelationActionHelper.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/UserProfileViewService.java`
