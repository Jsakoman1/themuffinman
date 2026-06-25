import type {QuestDetailPageState} from "../useQuestDetailPageState.ts"
import type {QuestDetail} from "../../api/workmarketApi.ts"

export const applyQuestDetailState = (
  state: Pick<QuestDetailPageState, "detail" | "quest" | "myApplication" | "applicationsView" | "applications" | "review">,
  detail: QuestDetail
) => {
  state.detail.value = detail
  state.quest.value = detail.summary
  state.myApplication.value = detail.sections.myApplication
  state.applicationsView.value = detail.sections.applicationsView
  state.applications.value = detail.sections.applicationsView?.visibleApplications ?? []
  state.review.value = detail.sections.review?.submittedReview ?? null
}
