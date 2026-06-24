import {computed, ref} from "vue"
import {useDialogActionState} from "../../../../composables/useDialogActionState.ts"
import type {QuestDashboard} from "../useQuestDashboard.ts"

export const createApplicationDialogViewState = (dashboard: QuestDashboard) => {
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
