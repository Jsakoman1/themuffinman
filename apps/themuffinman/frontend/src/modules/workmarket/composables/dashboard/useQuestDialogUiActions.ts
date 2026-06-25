import {closeAfterDelay} from "../../../../lib/dialogFlow.ts"
import type {DashboardQuestDialogFacade} from "./dashboardFacades.ts"
import type {createQuestDialogViewState} from "./createQuestDialogViewState.ts"

type QuestDialogViewState = ReturnType<typeof createQuestDialogViewState>

export const useQuestDialogUiActions = (
  dashboard: DashboardQuestDialogFacade,
  state: QuestDialogViewState
) => {
  const setActionMessage = (message: string, tone: "success" | "warning" = "success") => {
    state.actionBanner.show(message, tone)
  }

  const beginEditQuest = () => {
    if (!state.quest.value) {
      return
    }

    dashboard.startEditingQuest(state.quest.value)
    state.isEditing.value = true
  }

  const closeQuest = () => {
    if (!state.quest.value) {
      return
    }

    state.isDeleteConfirmDialogOpen.value = true
  }

  const cancelDeleteQuest = () => {
    state.isDeleteConfirmDialogOpen.value = false
  }

  const confirmDeleteQuest = () => {
    if (!state.quest.value) {
      return
    }

    state.isDeleteConfirmDialogOpen.value = false
    state.isDeleting.value = true
    setActionMessage("Deleting quest...", "warning")

    const questId = state.quest.value.id
    void (async () => {
      const deleted = await dashboard.deleteQuest(questId)
      if (!deleted) {
        state.isDeleting.value = false
        return
      }

      setActionMessage("Quest deleted.")
      closeAfterDelay(() => {
        dashboard.closeQuestDialog()
        state.isDeleting.value = false
      })
    })()
  }

  const approveApplication = (applicationId: number) => {
    if (!state.quest.value) {
      return
    }

    const questId = state.quest.value.id
    void (async () => {
      const result = await dashboard.approveApplication(questId, applicationId)
      if (!result) {
        return
      }

      setActionMessage(result.message)
      closeAfterDelay(() => {
        dashboard.closeQuestDialog()
        state.isEditing.value = false
        state.isDeleting.value = false
      })
    })()
  }

  const declineApplication = (applicationId: number) => {
    if (!state.quest.value) {
      return
    }

    const questId = state.quest.value.id
    void (async () => {
      const result = await dashboard.declineApplication(questId, applicationId)
      if (!result) {
        return
      }

      setActionMessage(result.message, "warning")
      closeAfterDelay(() => {
        dashboard.closeQuestDialog()
        state.isEditing.value = false
        state.isDeleting.value = false
      })
    })()
  }

  const confirmTermChange = () => {
    if (!state.quest.value) {
      return
    }

    state.isTermDecisioning.value = true
    setActionMessage("Confirming quest term...", "warning")

    const questId = state.quest.value.id
    void (async () => {
      const confirmed = await dashboard.confirmQuestTermChange(questId)
      if (!confirmed) {
        state.isTermDecisioning.value = false
        return
      }

      setActionMessage("Quest term confirmed.")
      closeAfterDelay(() => {
        dashboard.closeQuestDialog()
        state.isTermDecisioning.value = false
      })
    })()
  }

  const rejectTermChange = () => {
    if (!state.quest.value) {
      return
    }

    state.isTermDecisioning.value = true
    setActionMessage("Rejecting quest term...", "warning")

    const questId = state.quest.value.id
    void (async () => {
      const rejected = await dashboard.rejectQuestTermChange(questId)
      if (!rejected) {
        state.isTermDecisioning.value = false
        return
      }

      setActionMessage("Quest term change rejected.", "warning")
      closeAfterDelay(() => {
        dashboard.closeQuestDialog()
        state.isTermDecisioning.value = false
      })
    })()
  }

  return {
    beginEditQuest,
    closeQuest,
    cancelDeleteQuest,
    confirmDeleteQuest,
    approveApplication,
    declineApplication,
    confirmTermChange,
    rejectTermChange
  }
}
