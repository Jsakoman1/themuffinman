import {computed} from "vue"
import type {DashboardOpenWorkDialogFacade} from "./dashboardFacades.ts"

export const createDashboardOpenWorkState = (dashboard: DashboardOpenWorkDialogFacade) => {
  const openQuests = computed(() => dashboard.dashboardSections?.openWork?.openQuests ?? [])
  const waitingQuests = computed(() => dashboard.dashboardSections?.openWork?.waitingQuests ?? [])

  return {
    openQuests,
    waitingQuests
  }
}
