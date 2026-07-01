import type {QuestDashboardState} from "./useQuestDashboardState.ts"
import {useQuestDashboardDataActions} from "./dashboard/useQuestDashboardDataActions.ts"
import {useQuestDashboardMutationActions} from "./dashboard/useQuestDashboardMutationActions.ts"

export const useQuestDashboardActions = (state: QuestDashboardState) => {
  const dataActions = useQuestDashboardDataActions(state)
  const mutationActions = useQuestDashboardMutationActions(state, {
    refreshDashboardData: dataActions.refreshDashboardData,
    loadApplicationsForQuest: dataActions.loadApplicationsForQuest,
    loadQuestDetail: dataActions.loadQuestDetail,
    loadApplicationDetail: dataActions.loadApplicationDetail
  })

  return {
    ...dataActions,
    ...mutationActions
  }
}
