import {reactive} from "vue"
import {useQuestDetailPageState} from "./useQuestDetailPageState.ts"
import {useQuestDetailPageActions} from "./useQuestDetailPageActions.ts"

export const useQuestDetailPage = () => {
  const state = useQuestDetailPageState()
  const actions = useQuestDetailPageActions(state)

  return reactive({
    ...state,
    ...actions
  })
}
