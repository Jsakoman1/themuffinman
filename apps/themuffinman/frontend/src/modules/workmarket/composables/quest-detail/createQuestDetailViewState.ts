import {computed, ref, watch} from "vue"
import type {Quest, QuestApplication, QuestApplicationsView, QuestDetail, UserReview} from "../../api/workmarketApi.ts"

export const createQuestDetailViewState = (state: {
  detail: {value: QuestDetail | null}
  quest: {value: Quest | null}
  myApplication: {value: QuestApplication | null}
  applicationsView: {value: QuestApplicationsView | null}
  review: {value: UserReview | null}
}) => {
  const isActionInProgress = ref(false)
  const isDeleteConfirmDialogOpen = ref(false)
  const showTermChangeDetails = ref(false)
  const reviewStars = ref(5)
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

  const isCompletedQuest = computed(() => reviewSection.value?.visible ?? false)
  const reviewTarget = computed(() => reviewSection.value?.target ?? null)
  const canLeaveReview = computed(() => reviewSection.value?.canSubmit ?? false)
  const hasSubmittedReview = computed(() => !!(state.review.value ?? reviewSection.value?.submittedReview))
  const reviewPlaceholder = computed(() => {
    return reviewSection.value?.placeholder ?? "Add a short comment."
  })

  return {
    isActionInProgress,
    isDeleteConfirmDialogOpen,
    showTermChangeDetails,
    reviewStars,
    reviewComment,
    isSubmittingReview,
    isCompletedQuest,
    reviewSection,
    executionSection,
    termChangeSection,
    managementSection,
    reviewTarget,
    canLeaveReview,
    hasSubmittedReview,
    reviewPlaceholder
  }
}
