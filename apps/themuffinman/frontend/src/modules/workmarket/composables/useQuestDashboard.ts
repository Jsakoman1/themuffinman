import {reactive} from "vue"
import {useQuestDashboardState} from "./useQuestDashboardState.ts"
import {useQuestDashboardActions} from "./useQuestDashboardActions.ts"

export const useQuestDashboard = () => {
  const state = useQuestDashboardState()
  const actions = useQuestDashboardActions(state)
  const init = async () => {
    await actions.refreshDashboardData()

    if (state.adminModeEnabled.value && state.currentUser.value) {
      state.questCreatorId.value = String(state.currentUser.value.id)
    }
  }

  return reactive({
    ...state,
    ...actions,
    init
  })
}

export type QuestDashboard = ReturnType<typeof useQuestDashboard>
