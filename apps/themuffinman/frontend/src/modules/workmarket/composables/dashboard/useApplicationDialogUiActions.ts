import {useRouter} from "vue-router"
import {closeAfterDelay} from "../../../../lib/dialogFlow.ts"
import type {DashboardApplicationEditFacade} from "./dashboardFacades.ts"
import type {createApplicationDialogViewState} from "./createApplicationDialogViewState.ts"
import {routeForNavigationTarget} from "../../shared/navigationTargets.ts"

type ApplicationDialogViewState = ReturnType<typeof createApplicationDialogViewState>

export const useApplicationDialogUiActions = (
  dashboard: DashboardApplicationEditFacade,
  state: ApplicationDialogViewState
) => {
  const router = useRouter()

  const setActionMessage = (message: string, tone: "success" | "warning" = "success") => {
    state.actionBanner.show(message, tone)
  }

  const startEditing = () => {
    state.isEditing.value = true
  }

  const discardEditing = () => {
    state.isEditing.value = false
  }

  const openQuest = () => {
    void router.push(routeForNavigationTarget(state.navigationSection.value?.questNavigation))
  }

  const openPostedBy = () => {
    void router.push(routeForNavigationTarget(state.navigationSection.value?.postedByNavigation))
  }

  const withdrawApplication = () => {
    if (!state.application.value) {
      return
    }

    state.isWithdrawing.value = true
    setActionMessage("Withdrawing application...", "warning")

    const questId = state.application.value.questId
    void (async () => {
      const result = await dashboard.withdrawApplication(questId)
      if (!result) {
        state.isWithdrawing.value = false
        return
      }

      setActionMessage(result.message)
      closeAfterDelay(() => {
        dashboard.closeApplicationDialog()
        state.isWithdrawing.value = false
      })
    })()
  }

  return {
    startEditing,
    discardEditing,
    openQuest,
    openPostedBy,
    withdrawApplication
  }
}
