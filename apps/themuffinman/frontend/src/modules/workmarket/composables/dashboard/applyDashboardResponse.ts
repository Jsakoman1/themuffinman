import {currentUser} from "../../../../auth.ts"
import type {DashboardResponse} from "../../api/workmarketApi.ts"
import type {QuestDashboardState} from "../useQuestDashboardState.ts"

export const setDashboardLoadingState = (state: QuestDashboardState, isLoading: boolean) => {
  state.isLoadingQuests.value = isLoading
  state.isLoadingApplications.value = isLoading
  state.isLoadingNews.value = isLoading
  state.isLoadingUsers.value = isLoading
}

export const resetDashboardRequestErrors = (state: QuestDashboardState) => {
  state.questsError.value = ""
  state.questsErrorDetails.value = []
  state.applicationsError.value = ""
  state.applicationsErrorDetails.value = []
  state.newsError.value = ""
  state.newsErrorDetails.value = []
  state.usersError.value = ""
  state.usersErrorDetails.value = []
}

export const clearDashboardResponseState = (state: QuestDashboardState) => {
  state.quests.value = []
  state.myApplications.value = []
  state.dashboardOptions.value = null
  state.dashboardSummary.value = null
  state.circles.value = []
  state.appUsers.value = []
  state.questDetailsById.value = {}
  state.applicationDetailsById.value = {}
}

export const applyDashboardResponse = (state: QuestDashboardState, dashboard: DashboardResponse) => {
  state.quests.value = dashboard.quests
  state.myApplications.value = dashboard.myApplications
  state.dashboardOptions.value = dashboard.options
  state.dashboardSummary.value = dashboard.summary
  state.circles.value = dashboard.circles
  state.appUsers.value = dashboard.appUsers

  if (dashboard.summary.adminModeEnabled && currentUser.value && !state.questCreatorId.value) {
    state.questCreatorId.value = String(currentUser.value.id)
  }
}
