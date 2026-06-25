import {computed, ref, watch} from "vue"
import {richTextHasContent} from "../../../../shared/richText.ts"
import type {Quest, QuestApplication, QuestApplicationsView, QuestDetail, UserReview} from "../../api/workmarketApi.ts"

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
  const canSubmitApplication = computed(() => richTextHasContent(state.applicationMessage.value) && Number(state.proposedPrice.value) >= 0.01)

  const hasSubmittedReview = computed(() => !!(state.review.value ?? reviewSection.value?.submittedReview))

  return {
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
    hasSubmittedReview
  }
}
