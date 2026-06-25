<script setup lang="ts">
import UiDialog from "../../../../components/ui/UiDialog.vue"
import UiConfirmDialog from "../../../../components/ui/UiConfirmDialog.vue"
import UiStatusBanner from "../../../../components/ui/UiStatusBanner.vue"
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

const viewState = createQuestDialogViewState(props.dashboard)
const {
  quest,
  applications,
  isEditing,
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
  canRespondToTermChange,
  termChangeVisible,
  applicationSentVisible,
  canOpenMyApplication,
  deleteVisible,
  executionPrimaryAction,
  executionHelperText
} = viewState

const {
  beginEditQuest,
  closeQuest,
  cancelDeleteQuest,
  confirmDeleteQuest,
  approveApplication,
  declineApplication,
  confirmTermChange,
  rejectTermChange
} = useQuestDialogUiActions(props.dashboard, viewState)
</script>

<template>
  <UiDialog
    :open="!!quest"
    :title="isEditing ? 'Edit quest' : (quest?.title ?? 'Quest')"
    subtitle=""
    :size="isEditing ? 'xl' : 'lg'"
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
        v-if="canEdit && isEditing"
        :dashboard="props.dashboard"
        @discard="props.dashboard.cancelEditingQuest(); isEditing = false"
      />

      <QuestDetailContent
        v-else
        :quest="quest"
        :my-application="myApplication"
        :show-title="false"
        :can-open-application="!!myApplication && canOpenMyApplication"
        :application-open-label="'Open my application'"
        :show-term-change-details="showTermChangeDetails"
        :term-change-visible="termChangeVisible"
        :term-change-actionable="canRespondToTermChange"
        :term-change-current-label="quest.presentation.termLabel"
        :term-change-pending-label="quest.presentation.pendingTermLabel ?? ''"
        :execution-primary-action="executionPrimaryAction"
        :execution-helper-text="executionHelperText"
        :edit-visible="canEdit"
        :delete-visible="deleteVisible"
        :is-saving="isDeleting || isTermDecisioning"
        :is-action-in-progress="isDeleting || isTermDecisioning"
        @toggle-term-change="showTermChangeDetails = !showTermChangeDetails"
        @edit-quest="beginEditQuest"
        @open-application="myApplication && props.dashboard.openApplicationDialog(myApplication.id)"
        @start-work="props.dashboard.updateQuestStatus(quest.id, 'start')"
        @complete-work="props.dashboard.updateQuestStatus(quest.id, 'complete')"
        @delete-quest="closeQuest"
        @confirm-term-change="confirmTermChange"
        @reject-term-change="rejectTermChange"
      >
        <template #main-after>
          <DashboardQuestApplyForm
            v-if="canApply"
            :dashboard="props.dashboard"
            :quest="quest"
            :can-submit="canSubmitApplication"
          />

          <div v-else-if="applicationSentVisible" class="empty-state">
            Application sent. Check My applications.
          </div>

          <div v-else-if="!applicationSentVisible && quest.status !== 'CANCELLED'" class="surface-stack surface-stack--compact">
            <DashboardQuestApplications
              :dashboard="props.dashboard"
              :quest-id="quest.id"
              :applications="applications"
              :featured-application="featuredApplication"
              :can-show-applications="canShowApplications"
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
      </QuestDetailContent>
    </div>
  </UiDialog>
</template>
