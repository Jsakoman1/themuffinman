import type {QuestDashboardState} from "../useQuestDashboardState.ts"
import {createDashboardMutationRunner} from "./createDashboardMutationRunner.ts"
import {useQuestDashboardQuestDraftMutations} from "./useQuestDashboardQuestDraftMutations.ts"
import {useQuestDashboardQuestEditMutations} from "./useQuestDashboardQuestEditMutations.ts"
import {useQuestDashboardQuestLifecycleMutations} from "./useQuestDashboardQuestLifecycleMutations.ts"
import type {QuestDashboardMutationHelpers} from "./questMutationTypes.ts"

export const useQuestDashboardQuestMutations = (
  state: QuestDashboardState,
  helpers: QuestDashboardMutationHelpers
) => {
  const {runMutation} = createDashboardMutationRunner(state)
  const context = {state, helpers, runMutation}

  return {
    ...useQuestDashboardQuestDraftMutations(context),
    ...useQuestDashboardQuestLifecycleMutations(context),
    ...useQuestDashboardQuestEditMutations(context)
  }
}
