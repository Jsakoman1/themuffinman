import {useRoute, useRouter} from "vue-router"
import {currentUser} from "../../../auth.ts"
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
    parseInstantFromInput
  } = createQuestDashboardStateModules(route, router)

  return {
    ...tabState,
    ...dataState,
    ...profileState,
    currentUser,
    adminModeEnabled,
    ...questState,
    ...optionState,
    ...selectors,
    parseInstantFromInput,
    ...interactions,
    ...errorState
  }
}

export type QuestDashboardState = ReturnType<typeof useQuestDashboardState>
