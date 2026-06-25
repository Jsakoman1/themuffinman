import {useRoute, useRouter} from "vue-router"
import {currentUser, isAdmin} from "../../../auth.ts"
import {createQuestDashboardStateModules} from "./dashboard/createQuestDashboardStateModules.ts"

export const useQuestDashboardState = () => {
  const route = useRoute()
  const router = useRouter()
  const {
    tabState,
    dataState,
    optionState,
    profileState,
    questState,
    selectors,
    interactions,
    errorState,
    adminModeEnabled,
    parseInstantFromInput,
    dashboardTabs
  } = createQuestDashboardStateModules(route, router)

  const init = () => undefined

  return {
    ...tabState,
    ...dataState,
    ...profileState,
    currentUser,
    adminModeEnabled,
    isAdmin,
    ...questState,
    ...optionState,
    dashboardTabs,
    ...selectors,
    parseInstantFromInput,
    ...interactions,
    ...errorState,
    init
  }
}

export type QuestDashboardState = ReturnType<typeof useQuestDashboardState>
