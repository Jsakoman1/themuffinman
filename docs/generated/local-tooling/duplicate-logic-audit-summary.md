# Duplicate Logic Audit

- Generated at: `2026-06-29T12:47:11Z`
- Frontend files scanned: `140`
- Active route-backed files: `34`
- Review candidates: `8`
- Status mapping hits: `2`
- Permission gate hits: `9`
- Transition eligibility hits: `5`

## Review shortlist

- `apps/themuffinman/frontend/src/modules/workmarket/composables/useQuestDetailView.ts` score=`10` active=`true` status-maps=`0` permission-gates=`3` transition-helpers=`2`
  line 33: `const isOwnerView = computed(() => edit.canEdit.value)`
  line 34: `const showApplicationsSection = computed(() => isOwnerView.value && !!page.applicationsView.value)`
  line 35: `const showOfferSection = computed(() => !isOwnerView.value && (`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestViewAssembler.java`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/WorkmarketPresentationHelper.java`
- `apps/themuffinman/frontend/src/modules/workmarket/components/shared/QuestComposerForm.vue` score=`9` active=`false` status-maps=`1` permission-gates=`3` transition-helpers=`0`
  line 205: `const statusLabel = computed(() =>`
  line 102: `props.audience === "CIRCLES" && props.selectedCircleIds.length === 0`
  line 751: `v-if="inlineEditable && audience === 'CIRCLES'"`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestViewAssembler.java`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/WorkmarketPresentationHelper.java`
- `apps/themuffinman/frontend/src/modules/workmarket/components/shared/QuestEditFields.vue` score=`5` active=`false` status-maps=`1` permission-gates=`1` transition-helpers=`0`
  line 80: `const statusLabel = computed(() =>`
  line 251: `v-if="showAudience !== false && audience === 'CIRCLES'"`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestViewAssembler.java`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/WorkmarketPresentationHelper.java`
- `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/createQuestDialogViewState.ts` score=`3` active=`true` status-maps=`0` permission-gates=`0` transition-helpers=`1`
  line 68: `const canSubmitApplication = computed(() =>`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestViewAssembler.java`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/WorkmarketPresentationHelper.java`
- `apps/themuffinman/frontend/src/modules/workmarket/components/dashboard/DashboardQuestDialog.vue` score=`2` active=`false` status-maps=`0` permission-gates=`1` transition-helpers=`0`
  line 119: `<div v-if="quest.status !== 'CANCELLED' && !canApply && !applicationSentVisible && !myApplication" class="surface-stack surface-stack--compact">`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestViewAssembler.java`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/WorkmarketPresentationHelper.java`
- `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/useDashboardPostWorkState.ts` score=`2` active=`false` status-maps=`0` permission-gates=`1` transition-helpers=`0`
  line 16: `if (audience === "CIRCLES") {`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestViewAssembler.java`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/WorkmarketPresentationHelper.java`
- `apps/themuffinman/frontend/src/modules/workmarket/composables/quest-detail/createQuestDetailViewState.ts` score=`1` active=`false` status-maps=`0` permission-gates=`0` transition-helpers=`1`
  line 37: `const canSubmitApplication = computed(() =>`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestViewAssembler.java`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/WorkmarketPresentationHelper.java`
- `apps/themuffinman/frontend/src/modules/workmarket/shared/applicationDraft.ts` score=`1` active=`false` status-maps=`0` permission-gates=`0` transition-helpers=`1`
  line 4: `export const canSubmitQuestApplicationDraft = (message: string, proposedPrice: string, questAwardAmount: number | null | undefined) => {`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestViewAssembler.java`
  backend: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/WorkmarketPresentationHelper.java`

## Canonical backend anchors

- `workmarket` -> `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestViewAssembler.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/WorkmarketPresentationHelper.java`
- `social` -> `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleViewAssembler.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/SocialPresentationHelper.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/SocialRelationActionHelper.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/UserProfileViewService.java`
- `identity` -> `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/UserProfileViewService.java`
