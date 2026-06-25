import {workmarketApi} from "../../api/workmarketApi.ts"
import type {QuestDetailPageState} from "../useQuestDetailPageState.ts"
import {createQuestDetailMutationRunner} from "./createQuestDetailMutationRunner.ts"
import {replaceQuestDetailState} from "./questDetailStateHelpers.ts"

export const useQuestDetailMutationActions = (state: QuestDetailPageState) => {
  const {runQuestMutation} = createQuestDetailMutationRunner(state)

  const updateStatus = async (action: "start" | "complete") => {
    const result = await runQuestMutation(
      () => action === "start"
        ? workmarketApi.startQuest(state.questId.value)
        : workmarketApi.completeQuest(state.questId.value),
      "Could not update quest."
    )

    if (!result) {
      return null
    }

    const detail = await runQuestMutation(
      () => workmarketApi.getQuestDetail(state.questId.value),
      "Could not refresh quest."
    )

    if (!detail) {
      return null
    }

    replaceQuestDetailState(state, detail)

    return result
  }

  const applyForQuest = async () => {
    const applied = await runQuestMutation(
      () => workmarketApi.applyForQuest(state.questId.value, {
        message: state.applicationMessage.value,
        proposedPrice: Number(state.proposedPrice.value)
      }),
      "Could not send application."
    )

    if (!applied) {
      return false
    }

    state.applicationMessage.value = ""
    const detail = await runQuestMutation(
      () => workmarketApi.getQuestDetail(state.questId.value),
      "Could not refresh quest."
    )

    if (!detail) {
      return false
    }

    replaceQuestDetailState(state, detail)
    return true
  }

  const confirmQuestTermChange = async () => {
    const updatedQuest = await runQuestMutation(
      () => workmarketApi.confirmQuestTermChange(state.questId.value),
      "Could not confirm quest term."
    )

    if (!updatedQuest) {
      return false
    }

    state.quest.value = updatedQuest
    return true
  }

  const rejectQuestTermChange = async () => {
    const updatedQuest = await runQuestMutation(
      () => workmarketApi.rejectQuestTermChange(state.questId.value),
      "Could not reject quest term."
    )

    if (!updatedQuest) {
      return false
    }

    state.quest.value = updatedQuest
    return true
  }

  const deleteQuest = async (): Promise<boolean> => {
    const deleted = await runQuestMutation(
      () => workmarketApi.deleteQuest(state.questId.value),
      "Could not delete quest."
    )

    return deleted !== null
  }

  const submitReview = async (reviewedUserId: number, stars: number, comment: string) => {
    const savedReview = await runQuestMutation(
      () => workmarketApi.createQuestReview(state.questId.value, {
        reviewedUserId,
        stars,
        comment
      }),
      "Could not save review."
    )

    if (!savedReview) {
      return false
    }

    state.review.value = savedReview
    return true
  }

  return {
    applyForQuest,
    updateStatus,
    confirmQuestTermChange,
    rejectQuestTermChange,
    deleteQuest,
    submitReview
  }
}
