import {ref, watch} from "vue"
import type {RouteLocationNormalizedLoaded} from "vue-router"
import {dashboardTabs, type DashboardTab} from "../../domain/workmarketDomain.ts"

export const createDashboardTabState = (route: RouteLocationNormalizedLoaded) => {
  const activeTab = ref<DashboardTab>("calendar")

  watch(() => route.query.tab, (value) => {
    if (typeof value !== "string") {
      return
    }

    if (dashboardTabs.some((tab) => tab.id === value)) {
      activeTab.value = value as DashboardTab
    }
  }, {immediate: true})

  return {
    activeTab
  }
}
