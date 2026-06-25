import {useTimedBanner} from "../../../../composables/useTimedBanner.ts"
import {closeAfterDelay} from "../../../../lib/dialogFlow.ts"
import type {QuestDetailPageState} from "../useQuestDetailPageState.ts"
import {routeForNavigationTarget} from "../../shared/navigationTargets.ts"

export const useQuestDetailUiActions = (
  state: QuestDetailPageState & {
    isActionInProgress: {value: boolean}
    reviewSection: {value: {target?: {userId: number} | null} | null}
    reviewStars: {value: number}
    reviewComment: {value: string}
    isSubmittingReview: {value: boolean}
  },
  mutations: {
    confirmQuestTermChange: () => Promise<boolean>
    deleteQuest: () => Promise<boolean>
    rejectQuestTermChange: () => Promise<boolean>
    submitReview: (reviewedUserId: number, stars: number, comment: string) => Promise<boolean>
  }
) => {
  const actionBanner = useTimedBanner()
  const actionMessage = actionBanner.message
  const actionMessageTone = actionBanner.tone

  const setActionMessage = (message: string, tone: "success" | "warning" = "success") => {
    actionBanner.show(message, tone)
  }

  const selectReviewStars = (stars: number) => {
    state.reviewStars.value = stars
  }

  const handleSubmitReview = () => {
    const target = state.reviewSection.value?.target ?? null
    if (!target) {
      return
    }

    state.isSubmittingReview.value = true
    setActionMessage("Saving review...", "warning")

    void (async () => {
      const saved = await mutations.submitReview(target.userId, state.reviewStars.value, state.reviewComment.value)
      if (!saved) {
        state.isSubmittingReview.value = false
        return
      }

      setActionMessage("Review saved.")
      state.reviewComment.value = state.review.value?.comment ?? state.reviewComment.value.trim()
      closeAfterDelay(() => {
        state.isSubmittingReview.value = false
      }, 900)
    })()
  }

  const handleDeleteQuest = () => {
    state.isDeleteConfirmDialogOpen.value = true
  }

  const cancelDeleteQuest = () => {
    state.isDeleteConfirmDialogOpen.value = false
  }

  const confirmDeleteQuest = () => {
    state.isDeleteConfirmDialogOpen.value = false

    state.isActionInProgress.value = true
    setActionMessage("Deleting quest...", "warning")

    void (async () => {
      const deleted = await mutations.deleteQuest()
      if (!deleted) {
        state.isActionInProgress.value = false
        return
      }

      setActionMessage("Quest deleted.")
      closeAfterDelay(() => {
        void state.router.push(routeForNavigationTarget(state.detail.value?.sections.navigation?.listNavigation))
        state.isActionInProgress.value = false
      })
    })()
  }

  const handleConfirmTermChange = () => {
    state.isActionInProgress.value = true
    setActionMessage("Confirming quest term...", "warning")

    void (async () => {
      const confirmed = await mutations.confirmQuestTermChange()
      if (!confirmed) {
        state.isActionInProgress.value = false
        return
      }

      setActionMessage("Quest term confirmed.")
      closeAfterDelay(() => {
        state.isActionInProgress.value = false
      })
    })()
  }

  const handleRejectTermChange = () => {
    state.isActionInProgress.value = true
    setActionMessage("Rejecting quest term...", "warning")

    void (async () => {
      const rejected = await mutations.rejectQuestTermChange()
      if (!rejected) {
        state.isActionInProgress.value = false
        return
      }

      setActionMessage("Quest term change rejected.", "warning")
      closeAfterDelay(() => {
        state.isActionInProgress.value = false
      })
    })()
  }

  return {
    actionMessage,
    actionMessageTone,
    selectReviewStars,
    handleSubmitReview,
    handleDeleteQuest,
    cancelDeleteQuest,
    confirmDeleteQuest,
    handleConfirmTermChange,
    handleRejectTermChange
  }
}
