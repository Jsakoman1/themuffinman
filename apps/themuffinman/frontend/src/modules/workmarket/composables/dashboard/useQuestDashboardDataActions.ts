import {API_BASE_URL} from "../../../../api/httpClient.ts"
import {getApiErrorMessage} from "../../../../api/apiErrors.ts"
import {buildRequestDebugInfo} from "../../../../httpDebug.ts"
import {currentUser} from "../../../../auth.ts"
import {workmarketApi} from "../../api/workmarketApi.ts"
import type {QuestDashboardState} from "../useQuestDashboardState.ts"

export const useQuestDashboardDataActions = (state: QuestDashboardState) => {
  const fetchDashboard = async () => {
    state.isLoadingQuests.value = true
    state.isLoadingApplications.value = true
    state.isLoadingNews.value = true
    state.isLoadingUsers.value = true
    state.questsError.value = ""
    state.questsErrorDetails.value = []
    state.applicationsError.value = ""
    state.applicationsErrorDetails.value = []
    state.newsError.value = ""
    state.newsErrorDetails.value = []
    state.usersError.value = ""
    state.usersErrorDetails.value = []

    try {
      const dashboard = await workmarketApi.getDashboard()
      state.quests.value = dashboard.quests
      state.dashboardMyQuests.value = dashboard.myQuests
      state.dashboardAvailableQuests.value = dashboard.availableQuests
      state.myApplications.value = dashboard.myApplications
      state.newsItems.value = dashboard.recentNews
      state.dashboardOptions.value = dashboard.options
      state.dashboardSummary.value = dashboard.summary
      state.dashboardSections.value = dashboard.sections
      state.unreadNewsCount.value = dashboard.summary.unreadNewsCount
      state.incomingCircleRequests.value = dashboard.incomingCircleRequests
      state.circles.value = dashboard.circles
      state.appUsers.value = dashboard.appUsers
      if (dashboard.summary.adminModeEnabled && currentUser.value && !state.questCreatorId.value) {
        state.questCreatorId.value = String(currentUser.value.id)
      }
    } catch (error) {
      state.questsError.value = getApiErrorMessage(error, "Could not load dashboard.")
      state.questsErrorDetails.value = buildRequestDebugInfo(`${API_BASE_URL}/dashboard/me`, "GET", error)
      state.quests.value = []
      state.dashboardMyQuests.value = []
      state.dashboardAvailableQuests.value = []
      state.myApplications.value = []
      state.newsItems.value = []
      state.dashboardOptions.value = null
      state.dashboardSummary.value = null
      state.dashboardSections.value = null
      state.unreadNewsCount.value = 0
      state.incomingCircleRequests.value = []
      state.circles.value = []
      state.appUsers.value = []
    } finally {
      state.isLoadingQuests.value = false
      state.isLoadingApplications.value = false
      state.isLoadingNews.value = false
      state.isLoadingUsers.value = false
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

  const toggleApplicationRevealForQuest = async (questId: number) => {
    state.showAllApplicationsQuestIds.value[questId] = !state.showAllApplicationsQuestIds.value[questId]
    await loadApplicationsForQuest(questId)
  }

  return {
    fetchDashboard,
    refreshDashboardData,
    markNewsAsRead,
    markNewsItemAsRead,
    loadApplicationsForQuest,
    toggleApplicationRevealForQuest
  }
}
