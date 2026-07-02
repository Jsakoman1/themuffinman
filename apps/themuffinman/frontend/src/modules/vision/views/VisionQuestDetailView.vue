<script setup lang="ts">
import {ref, watch} from "vue"
import {useQuestDetailView} from "../composables/useQuestDetailView.ts"
import {richTextHasContent} from "../../../shared/richText.ts"
import {renderProfileText} from "../../../shared/profileFormatting.ts"
import QuestApplyForm from "../components/shared/QuestApplyForm.vue"
import QuestDetailContent from "../components/shared/QuestDetailContent.vue"
import QuestDetailEditForm from "../components/shared/QuestDetailEditForm.vue"
import ApplicationManagementCard from "../components/shared/ApplicationManagementCard.vue"
import {routeForNavigationTarget} from "../shared/navigationTargets.ts"
import {formatApplicationPrice} from "../shared/pricing.ts"
import VisionDetailSurface from "../components/VisionDetailSurface.vue"

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
  visibleManagementSection,
  approvedApplications,
  remainingApplications,
  canApply,
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
  hasChanges: ownerQuestHasChanges,
  showApplicationsSection,
  showOfferSection,
  showMyApplicationAside,
  showOverview,
  showOverviewStatus,
  cancelEditing,
  setEditTermMode,
  toggleEditCircle,
  removeEditImage,
  handleEditImagesChange,
  saveEdits,
  assignQuestNow,
  closeQuestDetail
} = useQuestDetailView()
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
  <VisionDetailSurface
    :title="canEdit ? 'Preview Quest' : (quest?.title ?? 'Quest details')"
    @close="closeQuestDetail"
  >
    <div class="vision-terminal-feed">
      <p v-if="actionMessage" :class="['vision-terminal-feed__line', `vision-terminal-feed__line--${actionMessageTone}`]">{{ actionMessage }}</p>
      <p class="vision-terminal-feed__line">> quest</p>
      <p v-if="isLoading" class="vision-terminal-feed__line vision-terminal-feed__line--soft">Loading quest...</p>
      <p v-else-if="error" class="vision-terminal-feed__line vision-terminal-feed__line--error">{{ error }}</p>

      <section v-if="errorDetails.length" class="vision-terminal-feed__block">
        <p class="vision-terminal-feed__block-title">debug</p>
        <p class="vision-terminal-feed__line vision-terminal-feed__line--soft">{{ errorDetails.join(" · ") }}</p>
        <button class="vision-terminal-feed__link-button" type="button" @click="copyDebugInfo">
          {{ copiedDebug ? "Copied debug details" : "Copy debug details" }}
        </button>
      </section>

      <section v-if="isDeleteConfirmDialogOpen" class="vision-terminal-feed__block">
        <p class="vision-terminal-feed__block-title">confirm delete</p>
        <p class="vision-terminal-feed__line">Are you sure you want to delete this quest? This cannot be undone.</p>
        <div class="vision-terminal-feed__action-row">
          <button class="vision-terminal-feed__link-button" type="button" :disabled="isSaving || isActionInProgress" @click="confirmDeleteQuest">Delete quest</button>
          <button class="vision-terminal-feed__link-button" type="button" :disabled="isSaving || isActionInProgress" @click="cancelDeleteQuest">Cancel</button>
        </div>
      </section>

      <section v-if="isWithdrawConfirmDialogOpen" class="vision-terminal-feed__block">
        <p class="vision-terminal-feed__block-title">confirm withdraw</p>
        <p class="vision-terminal-feed__line">Are you sure you want to withdraw this application?</p>
        <div class="vision-terminal-feed__action-row">
          <button class="vision-terminal-feed__link-button" type="button" :disabled="isSaving || isActionInProgress" @click="confirmWithdrawApplication">Withdraw</button>
          <button class="vision-terminal-feed__link-button" type="button" :disabled="isSaving || isActionInProgress" @click="cancelWithdrawApplication">Cancel</button>
        </div>
      </section>

      <QuestDetailEditForm
        v-if="quest && canEdit && isEditing"
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
          <section v-if="showApplicationsSection" class="vision-terminal-feed__block">
            <p class="vision-terminal-feed__block-title">applications</p>
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
            <p v-if="!approvedApplications.length && !remainingApplications.length" class="vision-terminal-feed__line vision-terminal-feed__line--soft">No applications yet.</p>
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
        @open-application="router.push(`/vision/applications/${myApplication?.id}`)"
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
          <section v-if="showOfferSection" class="vision-terminal-feed__block">
            <p class="vision-terminal-feed__block-title">offer</p>
            <button v-if="canApply && !isApplyFormVisible" class="vision-terminal-feed__link-button" type="button" @click="isApplyFormVisible = true">
              Apply
            </button>

            <QuestApplyForm
              v-else-if="canApply"
              :message="applicationMessage"
              :price="proposedPrice"
              :price-placeholder="String(quest.presentation.suggestedApplicationPrice ?? '')"
              :quickfill-label="quest.presentation.suggestedApplicationPrice != null ? 'Use suggested' : undefined"
              :show-price="quest.presentation.suggestedApplicationPrice != null"
              :can-submit="canSubmitApplication"
              @update:message="applicationMessage = $event"
              @update:price="proposedPrice = $event"
              @quickfill="proposedPrice = String(quest.presentation.suggestedApplicationPrice ?? '')"
              @submit="applyForQuest"
            />

            <div v-else-if="myApplication" class="vision-terminal-feed__block">
              <p class="vision-terminal-feed__line">{{ myApplication.presentation.statusLabel }}</p>
              <p v-if="myApplication.proposedPrice !== null && myApplication.proposedPrice !== undefined" class="vision-terminal-feed__line">{{ formatApplicationPrice(myApplication.proposedPrice) }}</p>
              <div
                v-if="richTextHasContent(myApplication.message)"
                class="profile-bio ui-content-prose ui-content-prose--flat ui-copy-block"
                v-html="renderProfileText(myApplication.message)"
              />
              <div class="vision-terminal-feed__action-row">
                <button class="vision-terminal-feed__link-button" type="button" @click="router.push(`/vision/applications/${myApplication.id}`)">
                  Open my application
                </button>
                <button
                  v-if="myApplication.presentation.canWithdraw"
                  class="vision-terminal-feed__link-button"
                  type="button"
                  :disabled="isSaving || isActionInProgress"
                  @click="handleWithdrawApplication"
                >
                  Withdraw application
                </button>
              </div>
            </div>

            <p v-else class="vision-terminal-feed__line vision-terminal-feed__line--soft">Application sent. Open your application for the full details.</p>
          </section>
        </template>
      </QuestDetailContent>
    </div>
  </VisionDetailSurface>
</template>
