import {reactive} from "vue"
import {useQuestDashboardState} from "./useQuestDashboardState.ts"
import {useQuestDashboardActions} from "./useQuestDashboardActions.ts"

export const useQuestDashboard = () => {
  const state = useQuestDashboardState()
  const actions = useQuestDashboardActions(state)

  return reactive({
    ...state,
    ...actions
  })
}

export type QuestDashboard = ReturnType<typeof useQuestDashboard>
