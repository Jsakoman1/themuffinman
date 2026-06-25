import type {Ref} from "vue"
import {currentUser} from "../../../../auth.ts"
import {formatInstantForInput} from "../../../../shared/questSchedule.ts"
import type {Quest} from "../../api/workmarketApi.ts"

type QuestDraftState = {
  questTitle: Ref<string>
  questDescription: Ref<string>
  questAwardAmount: Ref<string>
  questScheduledAt: Ref<string>
  questEndsAt: Ref<string>
  questTermMode: Ref<"flexible" | "start-only" | "start-end">
  questTermFixed: Ref<boolean>
  questAudience: Ref<Quest["audience"]>
  questSelectedCircleIds: Ref<number[]>
  questCreatorId: Ref<string>
  questImages: Ref<string[]>
}

type EditQuestDraftState = {
  editQuestTitle: Ref<string>
  editQuestDescription: Ref<string>
  editQuestAwardAmount: Ref<string>
  editQuestScheduledAt: Ref<string>
  editQuestEndsAt: Ref<string>
  editQuestTermMode: Ref<"flexible" | "start-only" | "start-end">
  editQuestTermFixed: Ref<boolean>
  editQuestAudience: Ref<Quest["audience"]>
  editQuestSelectedCircleIds: Ref<number[]>
  editQuestCreatorId: Ref<string>
  editQuestStatus: Ref<Quest["status"]>
  editQuestImages: Ref<string[]>
}

const resolveQuestTermMode = (quest: Quest) => (
  quest.termFixed
    ? (quest.endsAt ? "start-end" : "start-only")
    : "flexible"
)

export const populateCreateQuestDraft = (
  state: QuestDraftState,
  quest: Quest,
  adminModeEnabled: boolean
) => {
  state.questTitle.value = quest.title
  state.questDescription.value = quest.description
  state.questAwardAmount.value = String(quest.awardAmount ?? "")
  state.questScheduledAt.value = formatInstantForInput(quest.scheduledAt)
  state.questEndsAt.value = formatInstantForInput(quest.endsAt)
  state.questTermMode.value = resolveQuestTermMode(quest)
  state.questTermFixed.value = quest.termFixed
  state.questAudience.value = quest.audience
  state.questSelectedCircleIds.value = quest.visibleToCircles.map((circle) => circle.id)
  state.questCreatorId.value = adminModeEnabled ? String(quest.creatorId) : ""
}

export const resetCreateQuestDraft = (
  state: QuestDraftState,
  adminModeEnabled: boolean
) => {
  state.questTitle.value = ""
  state.questDescription.value = ""
  state.questAwardAmount.value = ""
  state.questScheduledAt.value = ""
  state.questEndsAt.value = ""
  state.questTermMode.value = "flexible"
  state.questTermFixed.value = false
  state.questAudience.value = "CIRCLES"
  state.questSelectedCircleIds.value = []
  state.questImages.value = []
  state.questCreatorId.value = adminModeEnabled && currentUser.value ? String(currentUser.value.id) : ""
}

export const populateEditQuestDraft = (state: EditQuestDraftState, quest: Quest) => {
  state.editQuestTitle.value = quest.title
  state.editQuestDescription.value = quest.description
  state.editQuestAwardAmount.value = String(quest.awardAmount ?? "")
  state.editQuestScheduledAt.value = formatInstantForInput(quest.scheduledAt)
  state.editQuestEndsAt.value = formatInstantForInput(quest.endsAt)
  state.editQuestTermMode.value = resolveQuestTermMode(quest)
  state.editQuestTermFixed.value = quest.termFixed
  state.editQuestAudience.value = quest.audience
  state.editQuestSelectedCircleIds.value = quest.visibleToCircles.map((circle) => circle.id)
  state.editQuestCreatorId.value = String(quest.creatorId)
  state.editQuestStatus.value = quest.status
  state.editQuestImages.value = [...quest.images]
}
