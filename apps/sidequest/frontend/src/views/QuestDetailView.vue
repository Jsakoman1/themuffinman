<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import {useQuestDetailPage} from "../composables/useQuestDetailPage.ts"
import UiStatusBanner from "../components/ui/UiStatusBanner.vue"
import UiRequestError from "../components/ui/UiRequestError.vue"
import ProfileBio from "../components/profile/ProfileBio.vue"
import {richTextHasContent} from "../shared/richText.ts"
import {formatQuestTerm} from "../shared/questSchedule.ts"
import {useTimedBanner} from "../composables/useTimedBanner.ts"
import {formatApplicationStatus, formatQuestStatus, statusBadgeClass} from "../lib/questDashboardRules.ts"
import {closeAfterDelay} from "../lib/dialogFlow.ts"
import {currentUser} from "../auth.ts"

const {
  router,
  questId,
  quest,
  isLoading,
  error,
  errorDetails,
  copiedDebug,
  isSaving,
  review,
  myApplication,
  applicationsView,
  updateStatus,
  confirmQuestTermChange,
  rejectQuestTermChange,
  deleteQuest,
  submitReview,
  copyDebugInfo,
  init
} = useQuestDetailPage()

const isActionInProgress = ref(false)
const showTermChangeDetails = ref(false)
const reviewStars = ref(5)
const reviewComment = ref("")
const isSubmittingReview = ref(false)
const actionBanner = useTimedBanner()
const actionMessage = actionBanner.message
const actionMessageTone = actionBanner.tone

const isCompletedQuest = computed(() => quest?.status === "COMPLETED")
const reviewTarget = computed(() => {
  if (!quest || !currentUser.value || quest.status !== "COMPLETED") {
    return null
  }

  const featuredApplication = applicationsView?.featuredApplication ?? null

  if (quest.creatorId === currentUser.value.id && featuredApplication?.status === "APPROVED") {
    return {
      userId: featuredApplication.applicantId,
      username: featuredApplication.applicantUsername,
      roleLabel: "worker"
    }
  }

  if (myApplication?.status === "APPROVED" && quest.creatorId !== currentUser.value.id) {
    return {
      userId: quest.creatorId,
      username: quest.creatorUsername,
      roleLabel: "employer"
    }
  }

  return null
})
const canLeaveReview = computed(() => !!reviewTarget.value)
const hasSubmittedReview = computed(() => !!review)
const reviewPlaceholder = computed(() => {
  if (!reviewTarget.value) {
    return "Add a short comment."
  }

  return `Write a short comment about this ${reviewTarget.value.roleLabel}.`
})

const setActionMessage = (message: string, tone: "success" | "warning" = "success") => {
  actionBanner.show(message, tone)
}

const selectReviewStars = (stars: number) => {
  reviewStars.value = stars
}

const handleSubmitReview = () => {
  const target = reviewTarget.value
  if (!target) {
    return
  }

  isSubmittingReview.value = true
  setActionMessage("Saving review...", "warning")

  void (async () => {
    const saved = await submitReview(target.userId, reviewStars.value, reviewComment.value)
    if (!saved) {
      isSubmittingReview.value = false
      return
    }

    setActionMessage("Review saved.")
    reviewComment.value = review?.comment ?? reviewComment.value.trim()
    closeAfterDelay(() => {
      isSubmittingReview.value = false
    }, 900)
  })()
}

const handleDeleteQuest = () => {
  const confirmed = window.confirm("Are you sure you want to delete this quest? This cannot be undone.")
  if (!confirmed) {
    return
  }

  isActionInProgress.value = true
  setActionMessage("Deleting quest...", "warning")

  void (async () => {
    const deleted = await deleteQuest()
    if (!deleted) {
      isActionInProgress.value = false
      return
    }

    setActionMessage("Quest deleted.")
    closeAfterDelay(() => {
      router.push("/quests")
      isActionInProgress.value = false
    })
  })()
}

const handleConfirmTermChange = () => {
  isActionInProgress.value = true
  setActionMessage("Confirming quest term...", "warning")

  void (async () => {
    const confirmed = await confirmQuestTermChange()
    if (!confirmed) {
      isActionInProgress.value = false
      return
    }

    setActionMessage("Quest term confirmed.")
    closeAfterDelay(() => {
      isActionInProgress.value = false
    })
  })()
}

const handleRejectTermChange = () => {
  isActionInProgress.value = true
  setActionMessage("Rejecting quest term...", "warning")

  void (async () => {
    const rejected = await rejectQuestTermChange()
    if (!rejected) {
      isActionInProgress.value = false
      return
    }

    setActionMessage("Quest term change rejected.", "warning")
    closeAfterDelay(() => {
      isActionInProgress.value = false
    })
  })()
}

onMounted(init)
</script>

<template>
  <div class="page">
    <div class="page-header u-row-between u-items-end u-wrap u-gap-16">
      <div>
        <h1 class="page-title">Quest details</h1>
      </div>

      <div class="button-row">
        <button class="button button--secondary" type="button" @click="router.push('/quests')">
          Back to quests
        </button>
      </div>
    </div>

    <UiStatusBanner :message="actionMessage" :tone="actionMessageTone" />

    <UiRequestError :message="error" :details="errorDetails" summary="Debug details" :copied="copiedDebug" @copy="copyDebugInfo" />

    <div v-if="isLoading" class="empty-state">
      Loading quest...
      <div class="debug-inline mt-2">GET http://localhost:8080/quests/{{ questId }}/detail</div>
    </div>

  <div v-if="quest" class="card">
      <div class="card__header u-row-between u-items-start u-gap-12">
        <div class="stack">
          <h2 class="card__title">{{ quest.title }}</h2>
        </div>
      </div>

      <div v-if="myApplication" class="dialog-focus-card dialog-focus-card--application">
        <div class="dialog-focus-card__top u-row-between u-items-center u-wrap u-gap-8">
          <span :class="['badge', statusBadgeClass(myApplication.status)]">
            {{ formatApplicationStatus(myApplication.status) }}
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
          <span :class="['badge', statusBadgeClass(quest.status)]">
            {{ formatQuestStatus(quest.status) }}
          </span>
          <span class="dialog-focus-card__kicker">Quest summary</span>
        </div>

        <div class="dialog-focus-card__title">
          $ {{ quest.awardAmount }}
        </div>

        <div class="dialog-focus-card__meta">
          <span>{{ formatQuestTerm(quest.scheduledAt, quest.endsAt, quest.termFixed) }}</span>
          <span>{{ quest.termFixed ? "Fixed" : "Negotiable" }}</span>
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
          <strong>{{ formatQuestTerm(quest.scheduledAt, quest.endsAt, quest.termFixed) }}</strong>
        </div>
        <div class="field">
          <span class="label">Time type</span>
          <strong>{{ quest.termFixed ? "Fixed time" : "By agreement" }}</strong>
        </div>
        <div v-if="quest.assigneeTarget === null || quest.assigneeTarget > 1" class="field">
          <span class="label">Workers</span>
          <strong>{{ quest.assigneeTarget === null ? "Unlimited" : quest.assigneeTarget }}</strong>
        </div>
      </div>

      <div v-if="quest.status === 'WAITING_CONFIRMATION'" class="compact-disclosure mt-4">
        <button class="compact-disclosure--launch" type="button" @click="showTermChangeDetails = !showTermChangeDetails">
          Term change waiting
        </button>
        <div v-if="showTermChangeDetails" class="alert alert--warning mt-2">
          <div class="stack">
            <div class="muted">
              Current term: {{ formatQuestTerm(quest.scheduledAt, quest.endsAt, quest.termFixed) }}
            </div>
            <div class="muted">
              Pending term: {{ formatQuestTerm(quest.pendingScheduledAt, quest.pendingEndsAt, quest.pendingTermFixed ?? quest.termFixed) }}
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

      <div v-if="quest.allowedActions.includes('START') || quest.allowedActions.includes('COMPLETE')" class="quest-footer">
        <div class="divider"></div>
        <div class="button-row">
          <button
            v-if="quest.status === 'ASSIGNED'"
            class="button"
            type="button"
            :disabled="isSaving"
            @click="updateStatus('start')"
          >
            Start work
          </button>
          <button
            v-if="quest.status === 'IN_PROGRESS'"
            class="button"
            type="button"
            :disabled="isSaving"
            @click="updateStatus('complete')"
          >
            Mark complete
          </button>
          <span v-if="quest.viewerRelation === 'APPROVED_APPLICANT'" class="muted">You are the approved applicant for this quest.</span>
        </div>
      </div>

      <div v-if="quest.allowedActions.includes('DELETE')" class="quest-footer">
        <div class="divider"></div>
        <div class="button-row">
          <button class="button button--danger" type="button" :disabled="isSaving || isActionInProgress" @click="handleDeleteQuest">
            Delete
          </button>
        </div>
      </div>

      <div v-if="quest.allowedActions.includes('CONFIRM_TERM_CHANGE') || quest.allowedActions.includes('REJECT_TERM_CHANGE')" class="quest-footer">
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
