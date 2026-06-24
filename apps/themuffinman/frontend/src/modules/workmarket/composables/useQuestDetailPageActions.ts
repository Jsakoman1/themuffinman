import type {QuestDetailPageState} from "./useQuestDetailPageState.ts"
import {useQuestDetailDataActions} from "./quest-detail/useQuestDetailDataActions.ts"
import {useQuestDetailMutationActions} from "./quest-detail/useQuestDetailMutationActions.ts"
import {useQuestDetailUiActions} from "./quest-detail/useQuestDetailUiActions.ts"

export const useQuestDetailPageActions = (state: QuestDetailPageState) => {
  const dataActions = useQuestDetailDataActions(state)
  const mutationActions = useQuestDetailMutationActions(state)
  const uiActions = useQuestDetailUiActions(state, mutationActions)

  const init = async () => {
    await dataActions.fetchQuestDetail()
  }

  return {
    ...dataActions,
    ...mutationActions,
    ...uiActions,
    init
  }
}
