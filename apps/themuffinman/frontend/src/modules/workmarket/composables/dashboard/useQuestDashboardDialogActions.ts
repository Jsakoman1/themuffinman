import type {QuestDashboardState} from "../useQuestDashboardState.ts"

export const useQuestDashboardDialogActions = (
  state: QuestDashboardState,
  helpers: {
    loadApplicationsForQuest: (questId: number) => Promise<void>
  }
) => {
  const {loadApplicationsForQuest} = helpers

  const openQuestDialog = async (questId: number) => {
    const quest = state.questForId(questId)
    if (!quest) {
      return
    }

    state.applicationDialogId.value = null
    state.questDialogId.value = questId

    if (quest.presentation.autoOpenEditForm) {
      state.startEditingQuest(quest)
    } else {
      state.cancelEditingQuest()
    }

    if (quest.presentation.canViewApplications) {
      await loadApplicationsForQuest(questId)
    }

    if (quest.presentation.canApply) {
      const suggestedPrice = quest.awardAmount ? String(quest.awardAmount) : ""
      if (!state.proposedPrices.value[quest.id]?.trim()) {
        state.proposedPrices.value[quest.id] = suggestedPrice
      }
    }
  }

  const openApplicationDialog = (applicationId: number) => {
    const application = state.myApplications.value.find((entry) => entry.id === applicationId) ?? null
    if (!application) {
      return
    }

    state.questDialogId.value = null
    state.applicationDialogId.value = applicationId

    if (application.presentation.autoOpenEditForm) {
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
