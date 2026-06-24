import {getApiErrorMessage} from "../../../../api/apiErrors.ts"
import {workmarketApi} from "../../api/workmarketApi.ts"
import type {QuestDetailPageState} from "../useQuestDetailPageState.ts"

export const useQuestDetailMutationActions = (state: QuestDetailPageState) => {
  const runQuestMutation = async <T>(mutation: () => Promise<T>, errorMessage: string) => {
    state.isSaving.value = true
    state.error.value = ""

    try {
      return await mutation()
    } catch (requestError) {
      state.error.value = getApiErrorMessage(requestError, errorMessage)
      return null
    } finally {
      state.isSaving.value = false
    }
  }

  const updateStatus = async (action: "start" | "complete") => {
    const result = await runQuestMutation(
      () => action === "start"
        ? workmarketApi.startQuest(state.questId.value)
        : workmarketApi.completeQuest(state.questId.value),
      "Could not update quest."
    )

    if (!result) {
      return null
    }

    const detail = await runQuestMutation(
      () => workmarketApi.getQuestDetail(state.questId.value),
      "Could not refresh quest."
    )

    if (!detail) {
      return null
    }

    state.detail.value = detail
    state.quest.value = detail.summary
    state.myApplication.value = detail.sections.myApplication
    state.applicationsView.value = detail.sections.applicationsView
    state.applications.value = detail.sections.applicationsView?.visibleApplications ?? []
    state.review.value = detail.sections.review?.submittedReview ?? null

    return result
  }

  const confirmQuestTermChange = async () => {
    const updatedQuest = await runQuestMutation(
      () => workmarketApi.confirmQuestTermChange(state.questId.value),
      "Could not confirm quest term."
    )

    if (!updatedQuest) {
      return false
    }

    state.quest.value = updatedQuest
    return true
  }

  const rejectQuestTermChange = async () => {
    const updatedQuest = await runQuestMutation(
      () => workmarketApi.rejectQuestTermChange(state.questId.value),
      "Could not reject quest term."
    )

    if (!updatedQuest) {
      return false
    }

    state.quest.value = updatedQuest
    return true
  }

  const deleteQuest = async (): Promise<boolean> => {
    const deleted = await runQuestMutation(
      () => workmarketApi.deleteQuest(state.questId.value),
      "Could not delete quest."
    )

    return deleted !== null
  }

  const submitReview = async (reviewedUserId: number, stars: number, comment: string) => {
    const savedReview = await runQuestMutation(
      () => workmarketApi.createQuestReview(state.questId.value, {
        reviewedUserId,
        stars,
        comment
      }),
      "Could not save review."
    )

    if (!savedReview) {
      return false
    }

    state.review.value = savedReview
    return true
  }

  return {
    updateStatus,
    confirmQuestTermChange,
    rejectQuestTermChange,
    deleteQuest,
    submitReview
  }
}
