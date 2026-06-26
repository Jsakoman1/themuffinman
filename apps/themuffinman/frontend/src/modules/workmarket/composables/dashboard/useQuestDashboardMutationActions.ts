import type {QuestDashboardState} from "../useQuestDashboardState.ts"
import {useQuestDashboardApplicationMutations} from "./useQuestDashboardApplicationMutations.ts"
import {useQuestDashboardDialogActions} from "./useQuestDashboardDialogActions.ts"
import {useQuestDashboardQuestMutations} from "./useQuestDashboardQuestMutations.ts"

export const useQuestDashboardMutationActions = (
  state: QuestDashboardState,
  helpers: {
    refreshDashboardData: () => Promise<void>
    loadApplicationsForQuest: (questId: number) => Promise<void>
    loadQuestDetail: (questId: number) => Promise<void>
    loadApplicationDetail: (applicationId: number) => Promise<void>
  }
) => {
  const questMutations = useQuestDashboardQuestMutations(state, helpers)
  const applicationMutations = useQuestDashboardApplicationMutations(state, helpers)
  const dialogActions = useQuestDashboardDialogActions(state, {
    loadApplicationsForQuest: helpers.loadApplicationsForQuest,
    loadQuestDetail: helpers.loadQuestDetail,
    loadApplicationDetail: helpers.loadApplicationDetail
  })

  return {
    ...dialogActions,
    ...questMutations,
    ...applicationMutations
  }
}
