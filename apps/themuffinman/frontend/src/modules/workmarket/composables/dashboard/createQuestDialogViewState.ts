import {computed, ref} from "vue"
import {richTextHasContent} from "../../../../shared/richText.ts"
import {useDialogActionState} from "../../../../composables/useDialogActionState.ts"
import type {DashboardQuestDialogFacade} from "./dashboardFacades.ts"

export const createQuestDialogViewState = (dashboard: DashboardQuestDialogFacade) => {
  const quest = computed(() => dashboard.selectedQuestDialog)
  const applications = computed(() => {
    if (!quest.value) {
      return []
    }

    return dashboard.applicationsForQuest(quest.value.id)
  })

  const isEditing = ref(false)
  const isDeleting = ref(false)
  const isDeleteConfirmDialogOpen = ref(false)
  const isTermDecisioning = ref(false)
  const showTermChangeDetails = ref(false)
  const actionBanner = useDialogActionState(quest, () => {
    isEditing.value = false
    isDeleting.value = false
    isDeleteConfirmDialogOpen.value = false
    isTermDecisioning.value = false
  })
  const actionMessage = actionBanner.message
  const actionMessageTone = actionBanner.tone

  const canEdit = computed(() => quest.value?.presentation.canEdit ?? false)
  const canApply = computed(() => quest.value?.presentation.canApply ?? false)
  const applicationMessage = computed(() => {
    if (!quest.value) {
      return ""
    }

    return dashboard.applicationMessages[quest.value.id] ?? ""
  })
  const applicationPrice = computed(() => {
    if (!quest.value) {
      return ""
    }

    return dashboard.proposedPrices[quest.value.id] ?? ""
  })
  const canSubmitApplication = computed(() => richTextHasContent(applicationMessage.value) && Number(applicationPrice.value) >= 0.01)
  const myApplication = computed(() => {
    const selectedQuest = quest.value
    if (!selectedQuest) {
      return null
    }

    return dashboard.myApplications.find((application) => application.id === selectedQuest.myApplicationId) ?? null
  })
  const featuredApplication = computed(() => quest.value ? dashboard.featuredApplicationForQuest(quest.value.id) : null)
  const canShowApplications = computed(() => quest.value?.presentation.canViewApplications ?? false)
  const canRespondToTermChange = computed(() => quest.value?.presentation.termChangeActionable ?? false)
  const termChangeVisible = computed(() => quest.value?.presentation.termChangeVisible ?? false)
  const applicationSentVisible = computed(() => quest.value?.presentation.applicationSentVisible ?? false)
  const canOpenMyApplication = computed(() => quest.value?.presentation.canOpenMyApplication ?? false)
  const deleteVisible = computed(() => quest.value?.presentation.deleteVisible ?? false)
  const executionPrimaryAction = computed(() => quest.value?.presentation.primaryExecutionAction ?? null)
  const executionHelperText = computed(() => quest.value?.presentation.executionHelperText ?? "")
  const executionSection = computed(() => ({
    visible: !!executionPrimaryAction.value || !!executionHelperText.value,
    primaryAction: executionPrimaryAction.value,
    primaryActionLabel: executionPrimaryAction.value === "START"
      ? "Start work"
      : (executionPrimaryAction.value === "COMPLETE" ? "Mark complete" : null),
    helperText: executionHelperText.value || null,
  }))
  const termChangeSection = computed(() => ({
    visible: termChangeVisible.value,
    actionable: canRespondToTermChange.value,
    summaryLabel: "Term change waiting",
    confirmLabel: "Confirm term change",
    rejectLabel: "Reject term change",
    currentTermLabel: quest.value?.presentation.termLabel ?? "",
    pendingTermLabel: quest.value?.presentation.pendingTermLabel ?? null,
    currentScheduledAt: quest.value?.scheduledAt ?? null,
    currentEndsAt: quest.value?.endsAt ?? null,
    currentTermFixed: quest.value?.termFixed ?? false,
    pendingScheduledAt: quest.value?.pendingScheduledAt ?? null,
    pendingEndsAt: quest.value?.pendingEndsAt ?? null,
    pendingTermFixed: quest.value?.pendingTermFixed ?? null,
  }))
  const managementSection = computed(() => ({
    editVisible: canEdit.value,
    deleteVisible: deleteVisible.value,
    postingSettingsVisible: canEdit.value,
    audienceLabel: quest.value?.presentation.audienceLabel ?? null,
    visibleToCirclesLabel: quest.value?.visibleToCircles?.length
      ? quest.value.visibleToCircles.map((circle) => circle.name).join(", ")
      : null,
  }))

  return {
    quest,
    applications,
    isEditing,
    isDeleting,
    isDeleteConfirmDialogOpen,
    isTermDecisioning,
    showTermChangeDetails,
    actionBanner,
    actionMessage,
    actionMessageTone,
    canEdit,
    canApply,
    applicationMessage,
    canSubmitApplication,
    myApplication,
    featuredApplication,
    canShowApplications,
    canRespondToTermChange,
    termChangeVisible,
    applicationSentVisible,
    canOpenMyApplication,
    deleteVisible,
    executionPrimaryAction,
    executionHelperText,
    executionSection,
    termChangeSection,
    managementSection
  }
}
