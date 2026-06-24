import {createQuestDetailDataState} from "./quest-detail/createQuestDetailDataState.ts"
import {createQuestDetailDebugState} from "./quest-detail/createQuestDetailDebugState.ts"
import {createQuestDetailViewState} from "./quest-detail/createQuestDetailViewState.ts"

export const useQuestDetailPageState = () => {
  const dataState = createQuestDetailDataState()
  const debugState = createQuestDetailDebugState(dataState)
  const viewState = createQuestDetailViewState({
    ...dataState,
    ...debugState
  })

  return {
    ...dataState,
    ...debugState,
    ...viewState
  }
}

export type QuestDetailPageState = ReturnType<typeof useQuestDetailPageState>
