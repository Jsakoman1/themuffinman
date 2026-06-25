<script setup lang="ts">
import {computed, ref, watch} from "vue"
import {useQuestDetailView} from "../composables/useQuestDetailView.ts"
import UiDialog from "../../../components/ui/UiDialog.vue"
import UiConfirmDialog from "../../../components/ui/UiConfirmDialog.vue"
import UiStatusBanner from "../../../components/ui/UiStatusBanner.vue"
import UiRequestError from "../../../components/ui/UiRequestError.vue"
import ProfileBio from "../../../components/profile/ProfileBio.vue"
import {richTextHasContent} from "../../../shared/richText.ts"
import QuestApplyForm from "../components/shared/QuestApplyForm.vue"
import QuestDetailContent from "../components/shared/QuestDetailContent.vue"
import QuestDetailEditForm from "../components/shared/QuestDetailEditForm.vue"
import ApplicationManagementCard from "../components/shared/ApplicationManagementCard.vue"
import {routeForNavigationTarget} from "../shared/navigationTargets.ts"
import {formatInstantForInput} from "../../../shared/questSchedule.ts"
const {
  router,
  quest,
  isLoading,
  error,
  errorDetails,
  copiedDebug,
  isSaving,
  myApplication,
  applications,
  applicationsView,
  applicationMessage,
  proposedPrice,
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
  canApply,
  applicationSentVisible,
  canSubmitApplication,
  hasSubmittedReview,
  actionMessage,
  actionMessageTone,
  updateStatus,
  applyForQuest,
  copyDebugInfo,
  selectReviewStars,
  handleSubmitReview,
  handleDeleteQuest,
  cancelDeleteQuest,
  confirmDeleteQuest,
  handleConfirmTermChange,
  handleRejectTermChange,
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
  cancelEditing,
  setEditTermMode,
  toggleEditCircle,
  removeEditImage,
  handleEditImagesChange,
  saveEdits,
  closeQuestDetail
} = useQuestDetailView()

const visibleManagementSection = computed(() => {
  if (!managementSection.value || canEdit.value) {
    return {
      editVisible: false,
      deleteVisible: false,
      postingSettingsVisible: false,
      audienceLabel: null,
      visibleToCirclesLabel: null,
    }
  }

  return managementSection.value
})

const featuredApplication = computed(() => applicationsView.value?.featuredApplication ?? null)

const remainingApplications = computed(() => {
  const selectedFeaturedId = featuredApplication.value?.id ?? null
  return applications.value.filter((application) => application.id !== selectedFeaturedId)
})

const isOwnerView = computed(() => canEdit.value)

const showApplicationsSection = computed(() => isOwnerView.value && !!applicationsView.value)
const showOfferSection = computed(() => !isOwnerView.value && (canApply.value || applicationSentVisible.value || !!myApplication.value))
const showMyApplicationAside = computed(() => isOwnerView.value && !showOfferSection.value)
const showOverview = computed(() => true)
const showOverviewStatus = computed(() => isOwnerView.value)
const ownerQuestHasChanges = computed(() => {
  if (!quest.value) {
    return false
  }

  const normalizedTermMode = quest.value.termFixed
    ? (quest.value.endsAt ? "start-end" : "start-only")
    : "flexible"

  return editTitle.value.trim() !== quest.value.title.trim()
    || editDescription.value.trim() !== quest.value.description.trim()
    || editAwardAmount.value.trim() !== String(quest.value.awardAmount ?? "").trim()
    || editScheduledAt.value !== formatInstantForInput(quest.value.scheduledAt)
    || editEndsAt.value !== formatInstantForInput(quest.value.endsAt)
    || editTermMode.value !== normalizedTermMode
    || editAudience.value !== quest.value.audience
    || editSelectedCircleIds.value.length !== quest.value.visibleToCircles.length
    || editSelectedCircleIds.value.some((id) => !quest.value?.visibleToCircles.some((circle) => circle.id === id))
    || editImages.value.length !== quest.value.images.length
    || editImages.value.some((image, index) => image !== quest.value?.images[index])
})
const isApplyFormVisible = ref(false)

watch(() => quest.value?.id, () => {
  isApplyFormVisible.value = false
}, {immediate: true})

watch(canApply, (value) => {
  if (!value) {
    isApplyFormVisible.value = false
  }
})

const openApplicantProfile = (applicationId: number) => {
  const application = applications.value.find((entry) => entry.id === applicationId)
  if (!application) {
    return
  }

  void router.push(routeForNavigationTarget(application.applicantNavigation))
}
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
      :title="quest?.title ?? 'Quest details'"
      size="xl"
      :chrome-only-header="true"
      @close="closeQuestDetail"
    >
      <UiStatusBanner :message="actionMessage" :tone="actionMessageTone" />

      <UiRequestError :message="error" :details="errorDetails" summary="Debug details" :copied="copiedDebug" @copy="copyDebugInfo" />

      <div v-if="isLoading" class="empty-state">
        Loading quest...
      </div>

      <QuestDetailEditForm
        v-else-if="quest && canEdit"
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
        :has-changes="ownerQuestHasChanges"
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
      >
        <template #main-after>
          <section v-if="showApplicationsSection" class="surface-stack surface-stack--compact">
            <div class="surface-stack surface-stack--compact">
              <ApplicationManagementCard
                v-if="featuredApplication"
                :application="featuredApplication"
                :selected="true"
                :show-status="false"
                @open-applicant="openApplicantProfile(featuredApplication.id)"
              />

              <ApplicationManagementCard
                v-for="application in remainingApplications"
                :key="application.id"
                :application="application"
                @open-applicant="openApplicantProfile(application.id)"
              />

              <div v-if="!featuredApplication && !remainingApplications.length" class="empty-state">
                No applications yet.
              </div>
            </div>
          </section>
        </template>
      </QuestDetailEditForm>

      <QuestDetailContent
        v-else-if="quest"
        :quest="quest"
        :my-application="myApplication"
        :show-title="true"
        :show-overview="showOverview"
        :show-overview-status="showOverviewStatus"
        :show-my-application="showMyApplicationAside"
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
        @open-application="router.push(`/applications/${myApplication?.id}`)"
        @select-review-stars="selectReviewStars"
        @update:review-comment="reviewComment = $event"
        @submit-review="handleSubmitReview"
        @start-work="updateStatus('start')"
        @complete-work="updateStatus('complete')"
        @delete-quest="handleDeleteQuest"
        @confirm-term-change="handleConfirmTermChange"
        @reject-term-change="handleRejectTermChange"
      >
        <template #side-after>
          <div v-if="showOfferSection" class="surface-stack surface-stack--compact">
            <div v-if="canApply && !isApplyFormVisible" class="surface-actions">
              <button class="button button--action button--flat-primary quest-apply-trigger" type="button" @click="isApplyFormVisible = true">
                Apply
              </button>
            </div>

            <QuestApplyForm
              v-else-if="canApply"
              :message="applicationMessage"
              :price="proposedPrice"
              :price-placeholder="String(quest.awardAmount ?? '')"
              quickfill-label="Use suggested"
              :can-submit="canSubmitApplication"
              @update:message="applicationMessage = $event"
              @update:price="proposedPrice = $event"
              @quickfill="proposedPrice = String(quest.awardAmount ?? '')"
              @submit="applyForQuest"
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
                <button class="button button--secondary" type="button" @click="router.push(`/applications/${myApplication.id}`)">
                  Open my application
                </button>
              </div>
            </div>

            <div v-else class="empty-state">
              Application sent. Open your application for the full details.
            </div>
          </div>
        </template>
      </QuestDetailContent>
    </UiDialog>
  </div>
</template>
