import {computed, ref, watch} from "vue"
import {useDialogActionState} from "../../../../composables/useDialogActionState.ts"
import type {DashboardApplicationEditFacade} from "./dashboardFacades.ts"

export const createApplicationDialogViewState = (dashboard: DashboardApplicationEditFacade) => {
  const application = computed(() => dashboard.selectedApplicationDialog)
  const isEditing = ref(false)
  const isWithdrawing = ref(false)
  const actionBanner = useDialogActionState(application, () => {
    isEditing.value = application.value?.presentation.autoOpenEditForm ?? false
    isWithdrawing.value = false
  })
  const actionMessage = actionBanner.message
  const actionMessageTone = actionBanner.tone

  const canEdit = computed(() => application.value?.presentation.canEdit ?? false)
  const canWithdraw = computed(() => application.value?.presentation.canWithdraw ?? false)
  const quest = computed(() => (application.value ? dashboard.questForId(application.value.questId) : null))

  watch(() => application.value?.id, () => {
    isEditing.value = application.value?.presentation.autoOpenEditForm ?? false
  }, {immediate: true})

  return {
    application,
    isEditing,
    isWithdrawing,
    actionBanner,
    actionMessage,
    actionMessageTone,
    canEdit,
    canWithdraw,
    quest
  }
}
