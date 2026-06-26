import {API_BASE_URL} from "../../../../api/httpClient.ts"
import {getApiErrorMessage} from "../../../../api/apiErrors.ts"
import {buildRequestDebugInfo} from "../../../../httpDebug.ts"
import {workmarketApi} from "../../api/workmarketApi.ts"
import type {QuestDashboardState} from "../useQuestDashboardState.ts"
import {
  applyDashboardResponse,
  clearDashboardResponseState,
  resetDashboardRequestErrors,
  setDashboardLoadingState
} from "./applyDashboardResponse.ts"

export const useQuestDashboardDataActions = (state: QuestDashboardState) => {
  const fetchDashboard = async () => {
    setDashboardLoadingState(state, true)
    resetDashboardRequestErrors(state)

    try {
      applyDashboardResponse(state, await workmarketApi.getDashboard())
    } catch (error) {
      state.questsError.value = getApiErrorMessage(error, "Could not load dashboard.")
      state.questsErrorDetails.value = buildRequestDebugInfo(`${API_BASE_URL}/dashboard/me`, "GET", error)
      clearDashboardResponseState(state)
    } finally {
      setDashboardLoadingState(state, false)
    }
  }

  const refreshDashboardData = async () => {
    state.resetErrorState()
    await fetchDashboard()
  }

  const markNewsAsRead = async () => {
    try {
      const result = await workmarketApi.markMyNewsAsRead()
      await fetchDashboard()
      state.showFeedback(result.message, "success")
    } catch (requestError) {
      state.showFeedback(getApiErrorMessage(requestError, "Could not mark updates as read."), "error")
    }
  }

  const markNewsItemAsRead = async (newsId: number) => {
    try {
      await workmarketApi.markMyNewsItemAsRead(newsId)
      await fetchDashboard()
    } catch (requestError) {
      state.showFeedback(getApiErrorMessage(requestError, "Could not mark update as read."), "error")
    }
  }

  const loadApplicationsForQuest = async (questId: number) => {
    try {
      state.applicationsByQuestId.value[questId] = await workmarketApi.getQuestApplicationsView(
        questId,
        !!state.showAllApplicationsQuestIds.value[questId]
      )
    } catch (requestError) {
      delete state.applicationsByQuestId.value[questId]
      state.showFeedback(getApiErrorMessage(requestError, "Could not load applications."), "error")
    }
  }

  const loadQuestDetail = async (questId: number) => {
    try {
      state.questDetailsById.value[questId] = await workmarketApi.getQuestDetail(questId)
    } catch (requestError) {
      delete state.questDetailsById.value[questId]
      state.showFeedback(getApiErrorMessage(requestError, "Could not load quest."), "error")
    }
  }

  const loadApplicationDetail = async (applicationId: number) => {
    try {
      state.applicationDetailsById.value[applicationId] = await workmarketApi.getApplicationDetail(applicationId)
    } catch (requestError) {
      delete state.applicationDetailsById.value[applicationId]
      state.showFeedback(getApiErrorMessage(requestError, "Could not load application."), "error")
    }
  }

  const toggleApplicationRevealForQuest = async (questId: number) => {
    state.showAllApplicationsQuestIds.value[questId] = !state.showAllApplicationsQuestIds.value[questId]
    await loadApplicationsForQuest(questId)
  }

  return {
    fetchDashboard,
    refreshDashboardData,
    markNewsAsRead,
    markNewsItemAsRead,
    loadQuestDetail,
    loadApplicationDetail,
    loadApplicationsForQuest,
    toggleApplicationRevealForQuest
  }
}
