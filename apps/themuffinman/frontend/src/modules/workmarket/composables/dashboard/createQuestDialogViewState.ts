import {computed, ref} from "vue"
import {useDialogActionState} from "../../../../composables/useDialogActionState.ts"
import type {DashboardQuestDialogFacade} from "./dashboardFacades.ts"
import {canSubmitQuestApplicationDraft} from "../../shared/applicationDraft.ts"

export const createQuestDialogViewState = (dashboard: DashboardQuestDialogFacade) => {
  const detail = computed(() => dashboard.selectedQuestDetail)
  const quest = computed(() => detail.value?.quest ?? dashboard.selectedQuestDialog)
  const applicationsView = computed(() => {
    const selectedQuest = quest.value
    if (!selectedQuest) {
      return null
    }

    const baseView = detail.value?.applicationsView ?? null
    return dashboard.applicationsForQuest(selectedQuest.id).length || dashboard.approvedApplicationsForQuest(selectedQuest.id).length
      ? {
        featuredApplication: baseView?.featuredApplication ?? null,
        approvedApplications: dashboard.approvedApplicationsForQuest(selectedQuest.id),
        visibleApplications: dashboard.applicationsForQuest(selectedQuest.id),
        pendingApplicationCount: baseView?.pendingApplicationCount ?? 0,
        oldestPendingApplicationId: baseView?.oldestPendingApplicationId ?? null,
        hiddenApplicationsCount: baseView?.hiddenApplicationsCount ?? 0,
        selectedApplicationId: baseView?.selectedApplicationId ?? null,
        canRevealHiddenApplications: baseView?.canRevealHiddenApplications ?? false,
        showingAllApplications: baseView?.showingAllApplications ?? false,
        revealLabel: baseView?.revealLabel ?? ""
      }
      : baseView
  })
  const applications = computed(() => {
    return applicationsView.value?.visibleApplications ?? []
  })
  const approvedApplications = computed(() => {
    return applicationsView.value?.approvedApplications ?? []
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
  const canSubmitApplication = computed(() =>
    canSubmitQuestApplicationDraft(applicationMessage.value, applicationPrice.value, quest.value?.presentation.applicationDraftRules)
  )
  const myApplication = computed(() => {
    return detail.value?.myApplication
      ?? (quest.value
        ? dashboard.myApplications.find((application) => application.id === quest.value?.myApplicationId) ?? null
        : null)
  })
  const canShowApplications = computed(() => quest.value?.presentation.canViewApplications ?? false)
  const canRespondToTermChange = computed(() => detail.value?.sections.termChange?.actionable ?? quest.value?.presentation.termChangeActionable ?? false)
  const termChangeVisible = computed(() => detail.value?.sections.termChange?.visible ?? quest.value?.presentation.termChangeVisible ?? false)
  const applicationSentVisible = computed(() => quest.value?.presentation.applicationSentVisible ?? false)
  const canOpenMyApplication = computed(() => quest.value?.presentation.canOpenMyApplication ?? false)
  const deleteVisible = computed(() => quest.value?.presentation.deleteVisible ?? false)
  const executionSection = computed(() => detail.value?.sections.execution ?? ({
    visible: false,
    primaryAction: null,
    primaryActionLabel: null,
    helperText: null
  }))
  const termChangeSection = computed(() => detail.value?.sections.termChange ?? ({
    visible: termChangeVisible.value,
    actionable: canRespondToTermChange.value,
    summaryLabel: quest.value?.presentation.termChangeSummaryLabel ?? "Term change waiting",
    confirmLabel: quest.value?.presentation.termChangeConfirmLabel ?? "Confirm term change",
    rejectLabel: quest.value?.presentation.termChangeRejectLabel ?? "Reject term change",
    currentScheduledAt: quest.value?.scheduledAt ?? null,
    currentEndsAt: quest.value?.endsAt ?? null,
    currentTermFixed: quest.value?.termFixed ?? false,
    pendingScheduledAt: quest.value?.pendingScheduledAt ?? null,
    pendingEndsAt: quest.value?.pendingEndsAt ?? null,
    pendingTermFixed: quest.value?.pendingTermFixed ?? null,
  }))
  const managementSection = computed(() => detail.value?.sections.management ?? ({
    editVisible: canEdit.value,
    deleteVisible: deleteVisible.value,
    postingSettingsVisible: quest.value?.presentation.postingSettingsVisible ?? canEdit.value,
    audienceLabel: quest.value?.presentation.audienceLabel ?? null,
    visibleToCirclesLabel: quest.value?.presentation.visibleToCirclesLabel ?? null,
  }))
  const executionPrimaryAction = computed(() => executionSection.value.primaryAction)
  const executionHelperText = computed(() => executionSection.value.helperText ?? "")

  return {
    detail,
    quest,
    applicationsView,
    applications,
    approvedApplications,
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
