import {currentUser} from "../../../../auth.ts"
import {workmarketApi} from "../../api/workmarketApi.ts"
import type {QuestDashboardState} from "../useQuestDashboardState.ts"
import {createDashboardMutationRunner} from "./createDashboardMutationRunner.ts"

export const useQuestDashboardApplicationMutations = (
  state: QuestDashboardState,
  helpers: {
    refreshDashboardData: () => Promise<void>
    loadApplicationsForQuest: (questId: number) => Promise<void>
  }
) => {
  const {refreshDashboardData, loadApplicationsForQuest} = helpers
  const {runMutation} = createDashboardMutationRunner(state)

  const applyForQuest = async (questId: number) => {
    if (!currentUser.value) {
      state.showFeedback("You must be signed in to apply.", "error")
      return
    }

    const message = state.applicationMessages.value[questId] ?? ""
    const proposedPriceValue = state.proposedPrices.value[questId]

    const result = await runMutation({
      run: () => workmarketApi.applyForQuest(questId, {
        message,
        proposedPrice: Number(proposedPriceValue)
      }),
      successMessage: (response) => response.message,
      errorMessage: "Could not send application.",
      afterSuccess: async () => {
      state.applicationMessages.value[questId] = ""
      state.proposedPrices.value[questId] = ""
      await refreshDashboardData()
      state.closeQuestDialog()
      state.goToTab("calendar")
      }
    })

    return result !== null
  }

  const approveApplication = async (questId: number, applicationId: number) => {
    return runMutation({
      run: () => workmarketApi.approveApplication(questId, applicationId),
      successMessage: (response) => response.message,
      errorMessage: "Could not approve application.",
      successPulseTarget: `quest-${questId}`,
      afterSuccess: async () => {
        await refreshDashboardData()
        await loadApplicationsForQuest(questId)
      }
    })
  }

  const declineApplication = async (questId: number, applicationId: number) => {
    return runMutation({
      run: () => workmarketApi.declineApplication(questId, applicationId),
      successMessage: (response) => response.message,
      errorMessage: "Could not decline application.",
      successPulseTarget: `quest-${questId}`,
      afterSuccess: async () => {
        await refreshDashboardData()
        await loadApplicationsForQuest(questId)
      }
    })
  }

  const withdrawApplication = async (questId: number) => {
    return runMutation({
      run: () => workmarketApi.withdrawMyApplication(questId),
      successMessage: (response) => response.message,
      errorMessage: "Could not withdraw application.",
      afterSuccess: refreshDashboardData
    })
  }

  const saveEditedApplication = async (questId: number) => {
    if (state.editingApplicationId.value === null) {
      return
    }

    const applicationId = state.editingApplicationId.value
    const message = state.editApplicationMessage.value

    const result = await runMutation({
      run: () => workmarketApi.updateMyApplication(questId, {
        message,
        proposedPrice: Number(state.editApplicationPrice.value)
      }),
      successMessage: (response) => response.message,
      errorMessage: "Could not update application.",
      successPulseTarget: `application-${applicationId}`,
      afterSuccess: async () => {
        state.editingApplicationId.value = null
        state.closeApplicationDialog()
        await refreshDashboardData()
      }
    })

    return result !== null
  }

  return {
    applyForQuest,
    approveApplication,
    declineApplication,
    withdrawApplication,
    saveEditedApplication
  }
}
