import {getApiErrorMessage} from "../../../../api/apiErrors.ts"
import {workmarketApi} from "../../api/workmarketApi.ts"
import type {QuestDetailPageState} from "../useQuestDetailPageState.ts"
import {replaceQuestDetailState, resetQuestDetailState} from "./questDetailStateHelpers.ts"

export const useQuestDetailDataActions = (state: QuestDetailPageState) => {
  const fetchQuestDetail = async () => {
    state.isLoading.value = true
    state.error.value = ""
    state.errorDetails.value = []

    try {
      replaceQuestDetailState(state, await workmarketApi.getQuestDetail(state.questId.value))
    } catch (error) {
      resetQuestDetailState(state)
      state.error.value = getApiErrorMessage(error, "Quest not found.")
      state.setNotFoundErrorDetails(error)
    } finally {
      state.isLoading.value = false
    }
  }

  return {
    fetchQuestDetail
  }
}
