import type {QuestDashboardState} from "../useQuestDashboardState.ts"
import type {createDashboardMutationRunner} from "./createDashboardMutationRunner.ts"

export type QuestDashboardMutationHelpers = {
  refreshDashboardData: () => Promise<void>
  loadApplicationsForQuest: (questId: number) => Promise<void>
  loadQuestDetail: (questId: number) => Promise<void>
  loadApplicationDetail: (applicationId: number) => Promise<void>
}

export type DashboardMutationRunner = ReturnType<typeof createDashboardMutationRunner>["runMutation"]

export type QuestMutationContext = {
  state: QuestDashboardState
  helpers: QuestDashboardMutationHelpers
  runMutation: DashboardMutationRunner
}
