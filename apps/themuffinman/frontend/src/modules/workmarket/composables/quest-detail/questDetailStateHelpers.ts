import type {QuestDetail} from "../../api/workmarketApi.ts"
import type {QuestDetailPageState} from "../useQuestDetailPageState.ts"
import {applyQuestDetailState} from "./applyQuestDetailState.ts"

export const resetQuestDetailState = (state: QuestDetailPageState) => {
  state.detail.value = null
  state.quest.value = null
  state.myApplication.value = null
  state.applications.value = []
  state.applicationsView.value = null
  state.review.value = null
}

export const replaceQuestDetailState = (
  state: QuestDetailPageState,
  detail: QuestDetail
) => {
  applyQuestDetailState(state, detail)
}
