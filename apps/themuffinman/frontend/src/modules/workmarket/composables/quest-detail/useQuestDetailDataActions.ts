import {getApiErrorMessage} from "../../../../api/apiErrors.ts"
import {workmarketApi} from "../../api/workmarketApi.ts"
import type {QuestDetailPageState} from "../useQuestDetailPageState.ts"

export const useQuestDetailDataActions = (state: QuestDetailPageState) => {
  const resetDetailState = () => {
    state.detail.value = null
    state.quest.value = null
    state.myApplication.value = null
    state.applications.value = []
    state.applicationsView.value = null
    state.review.value = null
  }

  const fetchQuestDetail = async () => {
    state.isLoading.value = true
    state.error.value = ""
    state.errorDetails.value = []

    try {
      const detail = await workmarketApi.getQuestDetail(state.questId.value)
      state.detail.value = detail
      state.quest.value = detail.summary
      state.myApplication.value = detail.sections.myApplication
      state.applicationsView.value = detail.sections.applicationsView
      state.applications.value = detail.sections.applicationsView?.visibleApplications ?? []
      state.review.value = detail.sections.review?.submittedReview ?? null
    } catch (error) {
      resetDetailState()
      state.error.value = getApiErrorMessage(error, "Quest not found.")
      state.setNotFoundErrorDetails(error)
    } finally {
      state.isLoading.value = false
    }
  }

  return {
    fetchQuestDetail
  }
}
