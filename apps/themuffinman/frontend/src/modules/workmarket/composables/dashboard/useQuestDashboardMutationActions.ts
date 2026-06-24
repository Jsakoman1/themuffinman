import type {QuestDashboardState} from "../useQuestDashboardState.ts"
import {useQuestDashboardApplicationMutations} from "./useQuestDashboardApplicationMutations.ts"
import {useQuestDashboardDialogActions} from "./useQuestDashboardDialogActions.ts"
import {useQuestDashboardProfileMutations} from "./useQuestDashboardProfileMutations.ts"
import {useQuestDashboardQuestMutations} from "./useQuestDashboardQuestMutations.ts"

export const useQuestDashboardMutationActions = (
  state: QuestDashboardState,
  helpers: {
    refreshDashboardData: () => Promise<void>
    loadApplicationsForQuest: (questId: number) => Promise<void>
  }
) => {
  const questMutations = useQuestDashboardQuestMutations(state, helpers)
  const applicationMutations = useQuestDashboardApplicationMutations(state, helpers)
  const profileMutations = useQuestDashboardProfileMutations(state)
  const dialogActions = useQuestDashboardDialogActions(state, {
    loadApplicationsForQuest: helpers.loadApplicationsForQuest
  })

  return {
    ...dialogActions,
    ...questMutations,
    ...applicationMutations,
    ...profileMutations
  }
}
