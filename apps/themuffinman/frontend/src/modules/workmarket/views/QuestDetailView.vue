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
import {formatApplicationPrice, isQuestFree} from "../shared/pricing.ts"
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
  isWithdrawConfirmDialogOpen,
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
  handleWithdrawApplication,
  cancelWithdrawApplication,
  confirmWithdrawApplication,
  handleConfirmTermChange,
  handleRejectTermChange,
  editTitle,
  editDescription,
  editAwardAmount,
  editAssigneeTarget,
  editShowApprovedApplicants,
  editScheduledAt,
  editEndsAt,
  editTermMode,
  editAudience,
  editSelectedCircleIds,
  editLocationVisibility,
  editLocationSource,
  editLocationCountry,
  editLocationLocality,
  editLocationPostalCode,
  editLocationStreet,
  editLocationHouseNumber,
  editImages,
  isEditing,
  circleGroups,
  questAudienceOptions,
  questLocationVisibilityOptions,
  canEdit,
  cancelEditing,
  setEditTermMode,
  toggleEditCircle,
  removeEditImage,
  handleEditImagesChange,
  saveEdits,
  assignQuestNow,
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

const approvedApplications = computed(() => applicationsView.value?.approvedApplications ?? [])
const remainingApplications = computed(() => applications.value)

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
    || editDescription.value !== quest.value.description
    || editAwardAmount.value.trim() !== String(quest.value.awardAmount ?? "").trim()
    || editAssigneeTarget.value.trim() !== String(quest.value.assigneeTarget ?? 1).trim()
    || editShowApprovedApplicants.value !== quest.value.showApprovedApplicants
    || editScheduledAt.value !== formatInstantForInput(quest.value.scheduledAt)
    || editEndsAt.value !== formatInstantForInput(quest.value.endsAt)
    || editTermMode.value !== normalizedTermMode
    || editAudience.value !== quest.value.audience
    || editLocationSource.value !== (quest.value.locationSource ?? "PROFILE")
    || editLocationCountry.value !== (quest.value.locationCountry ?? "")
    || editLocationLocality.value !== (quest.value.locationLocality ?? "")
    || editLocationPostalCode.value !== (quest.value.locationPostalCode ?? "")
    || editLocationStreet.value !== (quest.value.locationStreet ?? "")
    || editLocationHouseNumber.value !== (quest.value.locationHouseNumber ?? "")
    || editLocationVisibility.value !== quest.value.locationVisibility
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
    <UiConfirmDialog
      :open="isWithdrawConfirmDialogOpen"
      title="Withdraw application"
      message="Are you sure you want to withdraw this application?"
      confirm-label="Withdraw"
      confirm-tone="danger"
      :busy="isSaving || isActionInProgress"
      @close="cancelWithdrawApplication"
      @confirm="confirmWithdrawApplication"
    />

    <UiDialog
      :open="true"
      :title="canEdit ? 'Preview Quest' : (quest?.title ?? 'Quest details')"
      size="xl"
      :chrome-only-header="false"
      @close="closeQuestDetail"
    >
      <UiStatusBanner :message="actionMessage" :tone="actionMessageTone" />

      <UiRequestError :message="error" :details="errorDetails" summary="Debug details" :copied="copiedDebug" @copy="copyDebugInfo" />

      <div v-if="isLoading" class="empty-state">
        Loading quest...
      </div>

      <QuestDetailEditForm
        v-else-if="quest && canEdit && isEditing"
        :key="[
          quest.id,
          quest.title,
          quest.description,
          quest.awardAmount,
          quest.scheduledAt ?? '',
          quest.endsAt ?? '',
          quest.audience,
          quest.locationSource ?? '',
          quest.locationCountry ?? '',
          quest.locationLocality ?? '',
          quest.locationPostalCode ?? '',
          quest.locationStreet ?? '',
          quest.locationHouseNumber ?? '',
          quest.locationVisibility,
          quest.images.join('|'),
          quest.visibleToCircles.map((circle) => circle.id).join(',')
        ].join(':')"
        :title="editTitle"
        :description="editDescription"
        :award-amount="editAwardAmount"
        :assignee-target="editAssigneeTarget"
        :show-approved-applicants="editShowApprovedApplicants"
        :scheduled-at="editScheduledAt"
        :ends-at="editEndsAt"
        :term-mode="editTermMode"
        :audience="editAudience"
        :selected-circle-ids="editSelectedCircleIds"
        :location-source="editLocationSource"
        :location-country="editLocationCountry"
        :location-locality="editLocationLocality"
        :location-postal-code="editLocationPostalCode"
        :location-street="editLocationStreet"
        :location-house-number="editLocationHouseNumber"
        :location-visibility="editLocationVisibility"
        :images="editImages"
        :circle-groups="circleGroups"
        :quest-audience-options="questAudienceOptions"
        :quest-location-visibility-options="questLocationVisibilityOptions"
        :is-saving="isSaving"
        :has-changes="ownerQuestHasChanges"
        @update:title="editTitle = $event"
        @update:description="editDescription = $event"
        @update:award-amount="editAwardAmount = $event"
        @update:assignee-target="editAssigneeTarget = $event"
        @update:show-approved-applicants="editShowApprovedApplicants = $event"
        @update:scheduled-at="editScheduledAt = $event"
        @update:ends-at="editEndsAt = $event"
        @update:term-mode="setEditTermMode($event)"
        @update:audience="editAudience = $event"
        @toggle:circle="toggleEditCircle($event)"
        @update:location-source="editLocationSource = $event"
        @update:location-country="editLocationCountry = $event"
        @update:location-locality="editLocationLocality = $event"
        @update:location-postal-code="editLocationPostalCode = $event"
        @update:location-street="editLocationStreet = $event"
        @update:location-house-number="editLocationHouseNumber = $event"
        @update:location-visibility="editLocationVisibility = $event"
        @change:images="handleEditImagesChange"
        @remove:image="removeEditImage($event)"
        @save="saveEdits"
        @cancel="cancelEditing"
      >
        <template #main-after>
          <section v-if="showApplicationsSection" class="surface-stack surface-stack--compact">
            <div class="surface-stack surface-stack--compact">
              <ApplicationManagementCard
                v-for="application in approvedApplications"
                :key="`approved-${application.id}`"
                :application="application"
                :selected="true"
                :show-status="false"
                @open-applicant="openApplicantProfile(application.id)"
              />

              <ApplicationManagementCard
                v-for="application in remainingApplications"
                :key="application.id"
                :application="application"
                @open-applicant="openApplicantProfile(application.id)"
              />

              <div v-if="!approvedApplications.length && !remainingApplications.length" class="empty-state">
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
        :applications-view="applicationsView"
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
        @assign-now="assignQuestNow"
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
              :quickfill-label="isQuestFree(quest.awardAmount) ? undefined : 'Use suggested'"
              :show-price="!isQuestFree(quest.awardAmount)"
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

              <div v-if="myApplication.proposedPrice !== null && myApplication.proposedPrice !== undefined" class="surface-price">
                {{ formatApplicationPrice(myApplication.proposedPrice) }}
              </div>

              <ProfileBio
                v-if="richTextHasContent(myApplication.message)"
                class="ui-content-prose ui-content-prose--flat ui-copy-block"
                :text="myApplication.message"
              />

              <div class="surface-actions">
                <button class="button button--secondary" type="button" @click="router.push(`/applications/${myApplication.id}`)">
                  Open my application
                </button>
                <button
                  v-if="myApplication.presentation.canWithdraw"
                  class="button button--danger"
                  type="button"
                  :disabled="isSaving || isActionInProgress"
                  @click="handleWithdrawApplication"
                >
                  Withdraw application
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
