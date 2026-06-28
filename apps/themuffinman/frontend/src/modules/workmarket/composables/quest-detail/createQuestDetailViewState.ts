import {computed, ref, watch} from "vue"
import type {Quest, QuestApplication, QuestApplicationsView, QuestDetail, UserReview} from "../../api/workmarketApi.ts"
import {canSubmitQuestApplicationDraft} from "../../shared/applicationDraft.ts"

export const createQuestDetailViewState = (state: {
  detail: {value: QuestDetail | null}
  quest: {value: Quest | null}
  myApplication: {value: QuestApplication | null}
  applicationsView: {value: QuestApplicationsView | null}
  review: {value: UserReview | null}
  applicationMessage: {value: string}
  proposedPrice: {value: string}
}) => {
  const isActionInProgress = ref(false)
  const isDeleteConfirmDialogOpen = ref(false)
  const isWithdrawConfirmDialogOpen = ref(false)
  const showTermChangeDetails = ref(false)
  const reviewStars = ref(0)
  const reviewComment = ref("")
  const isSubmittingReview = ref(false)

  watch(() => state.review.value, (review) => {
    if (!review) {
      return
    }

    reviewStars.value = review.stars
    reviewComment.value = review.comment ?? ""
  }, {immediate: true})

  const reviewSection = computed(() => state.detail.value?.sections.review ?? null)
  const executionSection = computed(() => state.detail.value?.sections.execution ?? null)
  const termChangeSection = computed(() => state.detail.value?.sections.termChange ?? null)
  const managementSection = computed(() => state.detail.value?.sections.management ?? null)
  const canApply = computed(() => state.quest.value?.presentation.canApply ?? false)
  const applicationSentVisible = computed(() => state.quest.value?.presentation.applicationSentVisible ?? false)
  const canSubmitApplication = computed(() =>
    canSubmitQuestApplicationDraft(
      state.applicationMessage.value,
      state.proposedPrice.value,
      state.quest.value?.awardAmount
    )
  )

  const hasSubmittedReview = computed(() => !!(state.review.value ?? reviewSection.value?.submittedReview))

  return {
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
    hasSubmittedReview
  }
}
