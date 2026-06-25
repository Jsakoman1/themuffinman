<script setup lang="ts">
import {computed} from "vue"
import {useQuestDetailView} from "../composables/useQuestDetailView.ts"
import UiDialog from "../../../components/ui/UiDialog.vue"
import UiConfirmDialog from "../../../components/ui/UiConfirmDialog.vue"
import UiStatusBanner from "../../../components/ui/UiStatusBanner.vue"
import UiRequestError from "../../../components/ui/UiRequestError.vue"
import QuestDetailContent from "../components/shared/QuestDetailContent.vue"
import QuestDetailEditForm from "../components/shared/QuestDetailEditForm.vue"
const {
  router,
  quest,
  isLoading,
  error,
  errorDetails,
  copiedDebug,
  isSaving,
  myApplication,
  isActionInProgress,
  isDeleteConfirmDialogOpen,
  showTermChangeDetails,
  reviewStars,
  reviewComment,
  isSubmittingReview,
  reviewSection,
  executionSection,
  termChangeSection,
  managementSection,
  hasSubmittedReview,
  actionMessage,
  actionMessageTone,
  updateStatus,
  copyDebugInfo,
  selectReviewStars,
  handleSubmitReview,
  handleDeleteQuest,
  cancelDeleteQuest,
  confirmDeleteQuest,
  handleConfirmTermChange,
  handleRejectTermChange,
  isEditing,
  editTitle,
  editDescription,
  editAwardAmount,
  editScheduledAt,
  editEndsAt,
  editTermMode,
  editAudience,
  editSelectedCircleIds,
  editImages,
  circleGroups,
  questAudienceOptions,
  canEdit,
  startEditing,
  cancelEditing,
  setEditTermMode,
  toggleEditCircle,
  removeEditImage,
  handleEditImagesChange,
  saveEdits,
  closeQuestDetail
} = useQuestDetailView()

const visibleManagementSection = computed(() => {
  if (!managementSection.value || isEditing.value) {
    return {
      editVisible: false,
      deleteVisible: false,
    }
  }

  return managementSection.value
})
</script>

<template>
  <div class="page">
    <UiConfirmDialog
      :open="isDeleteConfirmDialogOpen"
      title="Delete quest"
      message="Are you sure you want to delete this quest? This cannot be undone."
      confirm-label="Delete"
      confirm-tone="danger"
      :busy="isSaving || isActionInProgress"
      @close="cancelDeleteQuest"
      @confirm="confirmDeleteQuest"
    />

    <UiDialog
      :open="true"
      :title="isEditing ? 'Edit quest' : (quest?.title ?? 'Quest details')"
      size="xl"
      :default-expanded="true"
      @close="closeQuestDetail"
    >
      <UiStatusBanner :message="actionMessage" :tone="actionMessageTone" />

      <UiRequestError :message="error" :details="errorDetails" summary="Debug details" :copied="copiedDebug" @copy="copyDebugInfo" />

      <div v-if="isLoading" class="empty-state">
        Loading quest...
      </div>

      <QuestDetailEditForm
        v-else-if="quest && canEdit && isEditing"
        :title="editTitle"
        :description="editDescription"
        :award-amount="editAwardAmount"
        :scheduled-at="editScheduledAt"
        :ends-at="editEndsAt"
        :term-mode="editTermMode"
        :audience="editAudience"
        :selected-circle-ids="editSelectedCircleIds"
        :images="editImages"
        :circle-groups="circleGroups"
        :quest-audience-options="questAudienceOptions"
        :is-saving="isSaving"
        @update:title="editTitle = $event"
        @update:description="editDescription = $event"
        @update:award-amount="editAwardAmount = $event"
        @update:scheduled-at="editScheduledAt = $event"
        @update:ends-at="editEndsAt = $event"
        @update:term-mode="setEditTermMode($event)"
        @update:audience="editAudience = $event"
        @toggle:circle="toggleEditCircle($event)"
        @change:images="handleEditImagesChange"
        @remove:image="removeEditImage($event)"
        @submit="saveEdits"
        @cancel="cancelEditing"
      />

      <QuestDetailContent
        v-else-if="quest"
        :quest="quest"
        :my-application="myApplication"
        :show-title="false"
        :can-open-application="!!myApplication"
        :show-term-change-details="showTermChangeDetails"
        :execution-section="executionSection"
        :term-change-section="termChangeSection"
        :management-section="visibleManagementSection"
        :review-section="reviewSection"
        :is-saving="isSaving"
        :is-action-in-progress="isActionInProgress || isSubmittingReview"
        :has-submitted-review="hasSubmittedReview"
        :review-stars="reviewStars"
        :review-comment="reviewComment"
        @toggle-term-change="showTermChangeDetails = !showTermChangeDetails"
        @edit-quest="startEditing"
        @open-application="router.push(`/applications/${myApplication?.id}`)"
        @select-review-stars="selectReviewStars"
        @update:review-comment="reviewComment = $event"
        @submit-review="handleSubmitReview"
        @start-work="updateStatus('start')"
        @complete-work="updateStatus('complete')"
        @delete-quest="handleDeleteQuest"
        @confirm-term-change="handleConfirmTermChange"
        @reject-term-change="handleRejectTermChange"
      />
    </UiDialog>
  </div>
</template>
