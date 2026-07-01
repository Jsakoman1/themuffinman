import type {QuestDetail} from "../../api/visionApi.ts"
import type {QuestDetailPageState} from "../useQuestDetailPageState.ts"
import {applyQuestDetailState} from "./applyQuestDetailState.ts"

export const resetQuestDetailState = (state: QuestDetailPageState) => {
  state.detail.value = null
  state.quest.value = null
  state.myApplication.value = null
  state.applications.value = []
  state.applicationsView.value = null
  state.review.value = null
  state.applicationMessage.value = ""
  state.proposedPrice.value = ""
}

export const replaceQuestDetailState = (
  state: QuestDetailPageState,
  detail: QuestDetail
) => {
  applyQuestDetailState(state, detail)

  const suggestedPrice = detail.summary.presentation.suggestedApplicationPrice
  if (detail.summary.presentation.canApply && !state.proposedPrice.value.trim() && suggestedPrice !== null && suggestedPrice !== undefined) {
    state.proposedPrice.value = String(suggestedPrice)
  }
}
