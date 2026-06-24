import {currentUser} from "../../../../auth.ts"
import {getApiErrorMessage} from "../../../../api/apiErrors.ts"
import {workmarketApi} from "../../api/workmarketApi.ts"
import type {QuestDashboardState} from "../useQuestDashboardState.ts"

export const useQuestDashboardApplicationMutations = (
  state: QuestDashboardState,
  helpers: {
    refreshDashboardData: () => Promise<void>
    loadApplicationsForQuest: (questId: number) => Promise<void>
  }
) => {
  const {refreshDashboardData, loadApplicationsForQuest} = helpers

  const applyForQuest = async (questId: number) => {
    if (!currentUser.value) {
      state.showFeedback("You must be signed in to apply.", "error")
      return
    }

    const message = state.applicationMessages.value[questId] ?? ""
    const proposedPriceValue = state.proposedPrices.value[questId]

    try {
      const result = await workmarketApi.applyForQuest(questId, {
        message,
        proposedPrice: Number(proposedPriceValue)
      })

      state.applicationMessages.value[questId] = ""
      state.proposedPrices.value[questId] = ""
      await refreshDashboardData()
      state.showFeedback(result.message, "success")
      state.closeQuestDialog()
      state.goToTab("overview")
    } catch (requestError) {
      state.showFeedback(getApiErrorMessage(requestError, "Could not send application."), "error")
    }
  }

  const approveApplication = async (questId: number, applicationId: number) => {
    try {
      const result = await workmarketApi.approveApplication(questId, applicationId)
      state.triggerSuccessPulse(`quest-${questId}`)
      state.showFeedback(result.message, "success")
      await Promise.all([refreshDashboardData(), loadApplicationsForQuest(questId)])
      return result
    } catch (requestError) {
      state.showFeedback(getApiErrorMessage(requestError, "Could not approve application."), "error")
      return null
    }
  }

  const declineApplication = async (questId: number, applicationId: number) => {
    try {
      const result = await workmarketApi.declineApplication(questId, applicationId)
      state.triggerSuccessPulse(`quest-${questId}`)
      state.showFeedback(result.message, "success")
      await Promise.all([refreshDashboardData(), loadApplicationsForQuest(questId)])
      return result
    } catch (requestError) {
      state.showFeedback(getApiErrorMessage(requestError, "Could not decline application."), "error")
      return null
    }
  }

  const withdrawApplication = async (questId: number) => {
    try {
      const result = await workmarketApi.withdrawMyApplication(questId)
      state.showFeedback(result.message, "success")
      await refreshDashboardData()
      return result
    } catch (requestError) {
      state.showFeedback(getApiErrorMessage(requestError, "Could not withdraw application."), "error")
      return null
    }
  }

  const saveEditedApplication = async (questId: number) => {
    if (state.editingApplicationId.value === null) {
      return
    }

    const applicationId = state.editingApplicationId.value
    const message = state.editApplicationMessage.value

    try {
      const result = await workmarketApi.updateMyApplication(questId, {
        message,
        proposedPrice: Number(state.editApplicationPrice.value)
      })

      state.editingApplicationId.value = null
      state.closeApplicationDialog()
      state.triggerSuccessPulse(`application-${applicationId}`)
      state.showFeedback(result.message, "success")
      await refreshDashboardData()
    } catch (requestError) {
      state.showFeedback(getApiErrorMessage(requestError, "Could not update application."), "error")
    }
  }

  return {
    applyForQuest,
    approveApplication,
    declineApplication,
    withdrawApplication,
    saveEditedApplication
  }
}
