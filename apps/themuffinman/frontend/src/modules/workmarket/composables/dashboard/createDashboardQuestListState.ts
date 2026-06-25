import {computed} from "vue"
import type {Quest, QuestApplication} from "../../api/workmarketApi.ts"
import type {DashboardQuestListFacade} from "./dashboardFacades.ts"

export const createDashboardQuestListState = (
  dashboard: DashboardQuestListFacade,
  options: {quests?: Quest[]; applications?: QuestApplication[]}
) => {
  const quests = computed(() => options.quests ?? dashboard.dashboardMyQuests)
  const applications = computed(() => options.applications ?? dashboard.myApplications)

  return {
    quests,
    applications
  }
}
