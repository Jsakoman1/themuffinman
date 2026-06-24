<script setup lang="ts">
import {onMounted} from "vue"
import AppPageHeader from "../../../components/app/AppPageHeader.vue"
import {useQuestDetailPage} from "../composables/useQuestDetailPage.ts"
import UiConfirmDialog from "../../../components/ui/UiConfirmDialog.vue"
import UiStatusBanner from "../../../components/ui/UiStatusBanner.vue"
import UiRequestError from "../../../components/ui/UiRequestError.vue"
import ProfileBio from "../../../components/profile/ProfileBio.vue"
import {richTextHasContent} from "../../../shared/richText.ts"
import {routeForNavigationTarget} from "../shared/navigationTargets.ts"

const {
  router,
  detail,
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
  isCompletedQuest,
  executionSection,
  termChangeSection,
  managementSection,
  reviewTarget,
  canLeaveReview,
  hasSubmittedReview,
  reviewPlaceholder,
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
  init
} = useQuestDetailPage()

onMounted(init)
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

    <AppPageHeader title="Quest details">
      <template #actions>
        <button class="button button--secondary" type="button" @click="router.push(routeForNavigationTarget(detail?.sections?.navigation?.listNavigation))">
          Back to quests
        </button>
      </template>
    </AppPageHeader>

    <UiStatusBanner :message="actionMessage" :tone="actionMessageTone" />

    <UiRequestError :message="error" :details="errorDetails" summary="Debug details" :copied="copiedDebug" @copy="copyDebugInfo" />

    <div v-if="isLoading" class="empty-state">
      Loading quest...
    </div>

  <div v-if="quest" class="card">
      <div class="card__header u-row-between u-items-start u-gap-12">
        <div class="stack">
          <h2 class="card__title">{{ quest.title }}</h2>
        </div>
      </div>

      <div v-if="myApplication" class="dialog-focus-card dialog-focus-card--application">
        <div class="dialog-focus-card__top u-row-between u-items-center u-wrap u-gap-8">
          <span :class="myApplication.presentation.statusBadgeClass">
            {{ myApplication.presentation.statusLabel }}
          </span>
          <span class="dialog-focus-card__kicker">Your application</span>
        </div>

        <div class="dialog-focus-card__title">
          $ {{ myApplication.proposedPrice }}
        </div>

        <ProfileBio
          v-if="richTextHasContent(myApplication.message)"
          class="dialog-sheet__description dialog-sheet__description--flat"
          :text="myApplication.message"
        />
      </div>

      <div class="dialog-focus-card dialog-focus-card--primary">
        <div class="dialog-focus-card__top u-row-between u-items-center u-wrap u-gap-8">
          <span :class="quest.presentation.statusBadgeClass">
            {{ quest.presentation.statusLabel }}
          </span>
          <span class="dialog-focus-card__kicker">Quest summary</span>
        </div>

        <div class="dialog-focus-card__title">
          $ {{ quest.awardAmount }}
        </div>

        <div class="dialog-focus-card__meta">
          <span>{{ quest.presentation.termLabel }}</span>
          <span>{{ quest.presentation.timeTypeLabel }}</span>
        </div>
      </div>

      <div v-if="quest.images?.length" class="quest-gallery quest-gallery--dialog">
        <div v-for="(image, index) in quest.images" :key="`${quest.id}-${index}`" class="quest-gallery__item quest-gallery__item--dialog">
          <img class="quest-gallery__image" :src="image" alt="Quest image">
        </div>
      </div>

      <ProfileBio
        v-if="richTextHasContent(quest.description)"
        class="dialog-sheet__description"
        :text="quest.description"
      />

      <div class="dialog-focus-grid">
        <div class="field">
          <span class="label">Scheduled time</span>
          <strong>{{ quest.presentation.termLabel }}</strong>
        </div>
        <div class="field">
          <span class="label">Time type</span>
          <strong>{{ quest.presentation.timeTypeLabel }}</strong>
        </div>
        <div v-if="quest.presentation.assigneeTargetVisible" class="field">
          <span class="label">Workers</span>
          <strong>{{ quest.presentation.assigneeTargetLabel }}</strong>
        </div>
      </div>

      <div v-if="termChangeSection?.visible" class="compact-disclosure mt-4">
        <button class="compact-disclosure--launch" type="button" @click="showTermChangeDetails = !showTermChangeDetails">
          Term change waiting
        </button>
        <div v-if="showTermChangeDetails" class="alert alert--warning mt-2">
          <div class="stack">
            <div class="muted">
              Current term: {{ termChangeSection.currentTermLabel }}
            </div>
            <div class="muted">
              Pending term: {{ termChangeSection.pendingTermLabel }}
            </div>
          </div>
        </div>
      </div>

      <section v-if="isCompletedQuest" class="dialog-focus-card dialog-focus-card--soft mt-4">
        <div class="dialog-focus-card__top u-row-between u-items-center u-wrap u-gap-8">
          <div class="dialog-focus-card__section-title">Review</div>
          <span v-if="hasSubmittedReview" class="badge badge--success">Saved</span>
        </div>

        <div v-if="canLeaveReview" class="review-form">
          <div class="stack">
            <strong>Rate {{ reviewTarget?.username }}</strong>
            <div class="muted">This user appears here as {{ reviewTarget?.roleLabel }} on this quest.</div>
          </div>

          <div class="review-stars" role="radiogroup" aria-label="Rating">
            <button
              v-for="stars in 5"
              :key="stars"
              class="review-stars__button"
              :class="{'review-stars__button--active': stars <= reviewStars}"
              type="button"
              :aria-pressed="stars === reviewStars"
              @click="selectReviewStars(stars)"
            >
              ★
            </button>
          </div>

          <label class="field">
            <span class="label">Comment</span>
            <textarea
              v-model="reviewComment"
              class="input review-form__textarea"
              maxlength="500"
              :placeholder="reviewPlaceholder"
            />
          </label>

          <div class="button-row">
            <button class="button" type="button" :disabled="isSaving || isSubmittingReview" @click="handleSubmitReview">
              {{ hasSubmittedReview ? "Update review" : "Submit review" }}
            </button>
            <div class="muted">{{ reviewComment.length }}/500</div>
          </div>
        </div>

        <div v-else class="empty-state empty-state--soft">
          Reviews become available here for the employer and the approved worker after the quest is completed.
        </div>
      </section>

      <div v-if="executionSection?.visible" class="quest-footer">
        <div class="divider"></div>
        <div class="button-row">
          <button
            v-if="executionSection.primaryAction === 'START'"
            class="button"
            type="button"
            :disabled="isSaving"
            @click="updateStatus('start')"
          >
            Start work
          </button>
          <button
            v-if="executionSection.primaryAction === 'COMPLETE'"
            class="button"
            type="button"
            :disabled="isSaving"
            @click="updateStatus('complete')"
          >
            Mark complete
          </button>
          <span v-if="executionSection.helperText" class="muted">{{ executionSection.helperText }}</span>
        </div>
      </div>

      <div v-if="managementSection?.deleteVisible" class="quest-footer">
        <div class="divider"></div>
        <div class="button-row">
          <button class="button button--danger" type="button" :disabled="isSaving || isActionInProgress" @click="handleDeleteQuest">
            Delete
          </button>
        </div>
      </div>

      <div v-if="termChangeSection?.actionable" class="quest-footer">
        <div class="divider"></div>
        <div class="button-row">
          <button class="button button--secondary" type="button" :disabled="isSaving || isActionInProgress" @click="handleConfirmTermChange">
            Confirm term change
          </button>
          <button class="button button--danger" type="button" :disabled="isSaving || isActionInProgress" @click="handleRejectTermChange">
            Reject term change
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
