import {computed, ref, watch} from "vue"
import {useDialogActionState} from "../../../../composables/useDialogActionState.ts"
import type {DashboardApplicationEditFacade} from "./dashboardFacades.ts"

export const createApplicationDialogViewState = (dashboard: DashboardApplicationEditFacade) => {
  const detail = computed(() => dashboard.selectedApplicationDetail)
  const application = computed(() => detail.value?.application ?? dashboard.selectedApplicationDialog)
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
  const quest = computed(() => detail.value?.sections.quest ?? (application.value ? dashboard.questForId(application.value.questId) : null))
  const navigationSection = computed(() => detail.value?.sections.navigation ?? null)
  const contextSection = computed(() => detail.value?.sections.context ?? null)

  watch(() => application.value?.id, () => {
    isEditing.value = application.value?.presentation.autoOpenEditForm ?? false
  }, {immediate: true})

  return {
    detail,
    application,
    isEditing,
    isWithdrawing,
    actionBanner,
    actionMessage,
    actionMessageTone,
    canEdit,
    canWithdraw,
    quest,
    navigationSection,
    contextSection
  }
}
