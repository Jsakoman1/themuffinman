import {workmarketApi} from "../../api/workmarketApi.ts"
import type {QuestMutationContext} from "./questMutationTypes.ts"

export const useQuestDashboardQuestLifecycleMutations = ({
  helpers,
  runMutation
}: QuestMutationContext) => {
  const updateQuestStatus = async (questId: number, action: "start" | "complete") => {
    const result = await runMutation({
      run: () => action === "start" ? workmarketApi.startQuest(questId) : workmarketApi.completeQuest(questId),
      successMessage: (response) => response.message,
      errorMessage: `Could not ${action} quest.`,
      successPulseTarget: `quest-${questId}`,
      afterSuccess: async () => {
        await helpers.refreshDashboardData()
        await helpers.loadQuestDetail(questId)
      }
    })

    return result !== null
  }

  const deleteQuest = async (questId: number) => {
    const result = await runMutation({
      run: () => workmarketApi.deleteQuest(questId),
      successMessage: (response) => response.message,
      errorMessage: "Could not delete quest.",
      successPulseTarget: `quest-${questId}`,
      afterSuccess: helpers.refreshDashboardData
    })

    return result !== null
  }

  return {
    updateQuestStatus,
    deleteQuest
  }
}
