import {computed} from "vue"
import type {Quest, QuestApplication} from "../../api/workmarketApi.ts"
import type {QuestDashboard} from "../useQuestDashboard.ts"

export const createDashboardQuestListState = (
  dashboard: QuestDashboard,
  options: {quests?: Quest[]; applications?: QuestApplication[]}
) => {
  const quests = computed(() => options.quests ?? dashboard.dashboardMyQuests)
  const applications = computed(() => options.applications ?? dashboard.myApplications)

  return {
    quests,
    applications
  }
}
