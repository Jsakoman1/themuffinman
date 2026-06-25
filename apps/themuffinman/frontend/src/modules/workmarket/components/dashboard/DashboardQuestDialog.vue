<script setup lang="ts">
import {ref, watch} from "vue"
import UiDialog from "../../../../components/ui/UiDialog.vue"
import UiConfirmDialog from "../../../../components/ui/UiConfirmDialog.vue"
import UiStatusBanner from "../../../../components/ui/UiStatusBanner.vue"
import ProfileBio from "../../../../components/profile/ProfileBio.vue"
import {richTextHasContent} from "../../../../shared/richText.ts"
import DashboardQuestApplications from "./DashboardQuestApplications.vue"
import DashboardQuestApplyForm from "./DashboardQuestApplyForm.vue"
import DashboardQuestEditForm from "./DashboardQuestEditForm.vue"
import QuestDetailContent from "../shared/QuestDetailContent.vue"
import {createQuestDialogViewState} from "../../composables/dashboard/createQuestDialogViewState.ts"
import type {DashboardQuestDialogFacade} from "../../composables/dashboard/dashboardFacades.ts"
import {useQuestDialogUiActions} from "../../composables/dashboard/useQuestDialogUiActions.ts"
import {routeForNavigationTarget} from "../../shared/navigationTargets.ts"

const props = defineProps<{
  dashboard: DashboardQuestDialogFacade
}>()

const isApplyFormVisible = ref(false)

const viewState = createQuestDialogViewState(props.dashboard)
const {
  quest,
  applications,
  isDeleting,
  isDeleteConfirmDialogOpen,
  isTermDecisioning,
  showTermChangeDetails,
  actionMessage,
  actionMessageTone,
  canEdit,
  canApply,
  canSubmitApplication,
  myApplication,
  featuredApplication,
  canShowApplications,
  applicationSentVisible,
  canOpenMyApplication,
  executionSection,
  termChangeSection,
  managementSection
} = viewState

const {
  closeQuest,
  cancelDeleteQuest,
  confirmDeleteQuest,
  approveApplication,
  declineApplication,
  confirmTermChange,
  rejectTermChange
} = useQuestDialogUiActions(props.dashboard, viewState)

watch(() => quest.value?.id, () => {
  isApplyFormVisible.value = false
}, {immediate: true})

watch(canApply, (value) => {
  if (!value) {
    isApplyFormVisible.value = false
  }
})
</script>

<template>
  <UiDialog
    :open="!!quest"
    :title="quest?.title ?? 'Quest'"
    subtitle=""
    size="xl"
    :chrome-only-header="true"
    @close="props.dashboard.closeQuestDialog()"
  >
    <UiConfirmDialog
      :open="isDeleteConfirmDialogOpen"
      title="Delete quest"
      message="Are you sure you want to delete this quest? This cannot be undone."
      confirm-label="Delete"
      confirm-tone="danger"
      :busy="isDeleting"
      @close="cancelDeleteQuest"
      @confirm="confirmDeleteQuest"
    />

    <div v-if="quest" class="surface-stack">
      <UiStatusBanner :message="actionMessage" :tone="actionMessageTone" />

      <DashboardQuestEditForm
        v-if="canEdit"
        :dashboard="props.dashboard"
        @discard="props.dashboard.startEditingQuest(quest)"
      >
        <template #main-after>
          <div v-if="quest.status !== 'CANCELLED' && !canApply && !applicationSentVisible && !myApplication" class="surface-stack surface-stack--compact">
            <DashboardQuestApplications
              :dashboard="props.dashboard"
              :quest-id="quest.id"
              :applications="applications"
              :featured-application="featuredApplication"
              :can-show-applications="canShowApplications"
              eyebrow="Applications"
              title="What people offer"
              @approve="approveApplication"
              @decline="declineApplication"
            />
          </div>

          <div v-else class="surface-actions">
            <button class="button button--secondary" type="button" @click="props.dashboard.reopenQuest(quest)">
              Copy to Create work
            </button>
          </div>
        </template>

        <template #side-after>
          <div class="ui-action-stretch">
            <RouterLink class="button button--ghost" :to="routeForNavigationTarget(quest.questNavigation)">
              Open quest page
            </RouterLink>
          </div>
        </template>
      </DashboardQuestEditForm>

      <QuestDetailContent
        v-else
        :quest="quest"
        :my-application="myApplication"
        :show-title="true"
        :show-overview="canShowApplications"
        :show-overview-status="canShowApplications"
        :show-my-application="false"
        :can-open-application="!!myApplication && canOpenMyApplication"
        :application-open-label="'Open my application'"
        :show-term-change-details="showTermChangeDetails"
        :term-change-section="termChangeSection"
        :execution-section="executionSection"
        :management-section="managementSection"
        :is-saving="isDeleting || isTermDecisioning"
        :is-action-in-progress="isDeleting || isTermDecisioning"
        @toggle-term-change="showTermChangeDetails = !showTermChangeDetails"
        @open-application="myApplication && props.dashboard.openApplicationDialog(myApplication.id)"
        @start-work="props.dashboard.updateQuestStatus(quest.id, 'start')"
        @complete-work="props.dashboard.updateQuestStatus(quest.id, 'complete')"
        @delete-quest="closeQuest"
        @confirm-term-change="confirmTermChange"
        @reject-term-change="rejectTermChange"
      >
        <template #side-after>
          <div v-if="canApply || applicationSentVisible || myApplication" class="surface-stack surface-stack--compact">
            <div v-if="canApply && !isApplyFormVisible" class="surface-actions">
              <button class="button button--action button--flat-primary quest-apply-trigger" type="button" @click="isApplyFormVisible = true">
                Apply
              </button>
            </div>

            <DashboardQuestApplyForm
              v-else-if="canApply"
              :dashboard="props.dashboard"
              :quest="quest"
              :can-submit="canSubmitApplication"
            />

            <div v-else-if="myApplication" class="surface-stack surface-stack--compact">
              <div class="surface-inline-spread">
                <strong>Your application</strong>
                <span :class="myApplication.presentation.statusBadgeClass">
                  {{ myApplication.presentation.statusLabel }}
                </span>
              </div>

              <div class="surface-price">$ {{ myApplication.proposedPrice }}</div>

              <ProfileBio
                v-if="richTextHasContent(myApplication.message)"
                class="ui-content-prose ui-content-prose--flat ui-copy-block"
                :text="myApplication.message"
              />

              <div class="surface-actions">
                <button
                  v-if="canOpenMyApplication"
                  class="button button--secondary"
                  type="button"
                  @click="props.dashboard.openApplicationDialog(myApplication.id)"
                >
                  Open my application
                </button>
              </div>
            </div>

            <div v-else class="empty-state">
              Application sent. Check My applications.
            </div>
          </div>

          <div class="ui-action-stretch">
            <RouterLink class="button button--ghost" :to="routeForNavigationTarget(quest.questNavigation)">
              Open quest page
            </RouterLink>
          </div>
        </template>
      </QuestDetailContent>
    </div>
  </UiDialog>
</template>
