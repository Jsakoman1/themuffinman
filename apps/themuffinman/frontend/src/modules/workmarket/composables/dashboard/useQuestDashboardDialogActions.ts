import {isQuestFree} from "../../shared/pricing.ts"
import type {QuestDashboardState} from "../useQuestDashboardState.ts"

export const useQuestDashboardDialogActions = (
  state: QuestDashboardState,
  helpers: {
    loadApplicationsForQuest: (questId: number) => Promise<void>
    loadQuestDetail: (questId: number) => Promise<void>
    loadApplicationDetail: (applicationId: number) => Promise<void>
  }
) => {
  const {loadApplicationsForQuest, loadQuestDetail, loadApplicationDetail} = helpers

  const openQuestDialog = async (questId: number) => {
    state.applicationDialogId.value = null
    state.questDialogId.value = questId
    await loadQuestDetail(questId)
    const quest = state.questDetailsById.value[questId]?.quest ?? state.questForId(questId)
    if (!quest) {
      state.questDialogId.value = null
      return
    }

    if (quest.presentation.canEdit) {
      state.startEditingQuest(quest)
    } else {
      state.cancelEditingQuest()
    }

    if (quest.presentation.canViewApplications) {
      await loadApplicationsForQuest(questId)
    }

    if (quest.presentation.canApply) {
      const suggestedPrice = isQuestFree(quest.awardAmount) ? "" : String(quest.awardAmount ?? "")
      if (!state.proposedPrices.value[quest.id]?.trim() && suggestedPrice) {
        state.proposedPrices.value[quest.id] = suggestedPrice
      }
    }
  }

  const openApplicationDialog = async (applicationId: number) => {
    const application = state.myApplications.value.find((entry) => entry.id === applicationId) ?? null
    if (!application) {
      return
    }

    state.questDialogId.value = null
    state.applicationDialogId.value = applicationId
    await loadApplicationDetail(applicationId)

    if (application.presentation.canEdit) {
      state.startEditingApplication(application)
    } else {
      state.cancelEditingApplication()
    }
  }

  return {
    openQuestDialog,
    openApplicationDialog
  }
}
