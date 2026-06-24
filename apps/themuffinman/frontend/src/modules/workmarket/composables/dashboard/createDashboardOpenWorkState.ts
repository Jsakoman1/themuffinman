import {computed} from "vue"
import type {QuestDashboard} from "../useQuestDashboard.ts"

export const createDashboardOpenWorkState = (dashboard: QuestDashboard) => {
  const openQuests = computed(() => dashboard.dashboardSections?.openWork?.openQuests ?? [])
  const waitingQuests = computed(() => dashboard.dashboardSections?.openWork?.waitingQuests ?? [])

  return {
    openQuests,
    waitingQuests
  }
}
