import {getApiErrorMessage} from "../../../../api/apiErrors.ts"
import type {QuestDetailPageState} from "../useQuestDetailPageState.ts"

export const createQuestDetailMutationRunner = (state: QuestDetailPageState) => {
  const runQuestMutation = async <T>(mutation: () => Promise<T>, errorMessage: string) => {
    state.isSaving.value = true
    state.error.value = ""

    try {
      return await mutation()
    } catch (requestError) {
      state.error.value = getApiErrorMessage(requestError, errorMessage)
      return null
    } finally {
      state.isSaving.value = false
    }
  }

  return {
    runQuestMutation
  }
}
