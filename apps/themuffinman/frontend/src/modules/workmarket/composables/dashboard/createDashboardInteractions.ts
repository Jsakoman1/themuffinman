import {nextTick, onBeforeUnmount, onMounted, type Ref} from "vue"
import type {RouteLocationRaw} from "vue-router"
import {formatDebugInfo} from "../../../../httpDebug.ts"
import type {Quest, QuestApplication} from "../../api/workmarketApi.ts"
import type {DashboardTab} from "../../domain/workmarketDomain.ts"
import {populateCreateQuestDraft, populateEditQuestDraft} from "./questDraftState.ts"

export const createDashboardInteractions = (state: {
  route: {query: Record<string, unknown>}
  router: {push: (location: RouteLocationRaw) => Promise<unknown>}
  adminModeEnabled: Ref<boolean>
  activeTab: Ref<DashboardTab>
  isNotificationsDialogOpen: Ref<boolean>
  questDialogId: Ref<number | null>
  applicationDialogId: Ref<number | null>
  userProfileDialogId: Ref<number | null>
  isCreateJobDialogOpen: Ref<boolean>
  isFindWorkDialogOpen: Ref<boolean>
  isOpenWorkDialogOpen: Ref<boolean>
  isApplicationsDialogOpen: Ref<boolean>
  applicationMessages: Ref<Record<number, string>>
  proposedPrices: Ref<Record<number, string>>
  questDisclosureRefs: Ref<Record<number, HTMLDetailsElement | null>>
  editingQuestId: Ref<number | null>
  openApplicationsQuestIds: Ref<Record<number, boolean>>
  showAllApplicationsQuestIds: Ref<Record<number, boolean>>
  editingApplicationId: Ref<number | null>
  editApplicationMessage: Ref<string>
  editApplicationPrice: Ref<string>
  editQuestTitle: Ref<string>
  editQuestDescription: Ref<string>
  editQuestAwardAmount: Ref<string>
  editQuestAssigneeTarget: Ref<string>
  editQuestShowApprovedApplicants: Ref<boolean>
  editQuestScheduledAt: Ref<string>
  editQuestEndsAt: Ref<string>
  editQuestTermMode: Ref<"flexible" | "start-only" | "start-end">
  editQuestTermFixed: Ref<boolean>
  editQuestAudience: Ref<Quest["audience"]>
  editQuestSelectedCircleIds: Ref<number[]>
  editQuestLocationSource: Ref<NonNullable<Quest["locationSource"]>>
  editQuestLocationCountry: Ref<string>
  editQuestLocationLocality: Ref<string>
  editQuestLocationPostalCode: Ref<string>
  editQuestLocationStreet: Ref<string>
  editQuestLocationHouseNumber: Ref<string>
  editQuestLocationVisibility: Ref<NonNullable<Quest["locationVisibility"]>>
  editQuestCreatorId: Ref<string>
  editQuestStatus: Ref<Quest["status"]>
  editQuestImages: Ref<string[]>
  questTitle: Ref<string>
  questDescription: Ref<string>
  questAwardAmount: Ref<string>
  questAssigneeTarget: Ref<string>
  questShowApprovedApplicants: Ref<boolean>
  questScheduledAt: Ref<string>
  questEndsAt: Ref<string>
  questTermMode: Ref<"flexible" | "start-only" | "start-end">
  questTermFixed: Ref<boolean>
  questAudience: Ref<Quest["audience"]>
  questSelectedCircleIds: Ref<number[]>
  questLocationSource: Ref<NonNullable<Quest["locationSource"]>>
  questLocationCountry: Ref<string>
  questLocationLocality: Ref<string>
  questLocationPostalCode: Ref<string>
  questLocationStreet: Ref<string>
  questLocationHouseNumber: Ref<string>
  questLocationVisibility: Ref<NonNullable<Quest["locationVisibility"]>>
  questCreatorId: Ref<string>
  questImages: Ref<string[]>
  showFeedback: (message: string, type: "error" | "success") => void
  successPulseTarget: Ref<string>
  copiedDebugBanner: {show: (value: string) => void}
  questForId: (questId: number) => Quest | null
  myApplications: Ref<QuestApplication[]>
}) => {
  let successPulseTimeout: number | undefined

  const setQuestTermMode = (mode: "flexible" | "start-only" | "start-end") => {
    state.questTermMode.value = mode
    state.questTermFixed.value = mode !== "flexible"
    if (mode === "flexible") {
      state.questScheduledAt.value = ""
      state.questEndsAt.value = ""
    }
    if (mode === "start-only") {
      state.questEndsAt.value = ""
    }
  }

  const setEditQuestTermMode = (mode: "flexible" | "start-only" | "start-end") => {
    state.editQuestTermMode.value = mode
    state.editQuestTermFixed.value = mode !== "flexible"
    if (mode === "flexible") {
      state.editQuestScheduledAt.value = ""
      state.editQuestEndsAt.value = ""
    }
    if (mode === "start-only") {
      state.editQuestEndsAt.value = ""
    }
  }

  const triggerSuccessPulse = (target: string) => {
    state.successPulseTarget.value = target

    if (successPulseTimeout !== undefined) {
      window.clearTimeout(successPulseTimeout)
    }

    successPulseTimeout = window.setTimeout(() => {
      state.successPulseTarget.value = ""
    }, 900)
  }

  const setActiveTab = (tabId: DashboardTab) => {
    state.activeTab.value = tabId
  }

  const goToTab = (tabId: DashboardTab) => {
    state.activeTab.value = tabId
    void state.router.push({
      query: {
        ...state.route.query,
        tab: tabId
      }
    })
  }

  const openNotificationsDialog = () => {
    state.isNotificationsDialogOpen.value = true
  }

  const closeNotificationsDialog = () => {
    state.isNotificationsDialogOpen.value = false
  }

  const cancelEditingApplication = () => {
    state.editingApplicationId.value = null
  }

  const closeQuestDisclosure = (questId: number) => {
    const element = state.questDisclosureRefs.value[questId]
    if (element) {
      element.open = false
    }

    if (state.editingQuestId.value === questId) {
      state.editingQuestId.value = null
    }

    state.openApplicationsQuestIds.value[questId] = false
    state.showAllApplicationsQuestIds.value[questId] = false
  }

  const cancelEditingQuest = () => {
    if (state.editingQuestId.value !== null) {
      closeQuestDisclosure(state.editingQuestId.value)
    }

    state.editingQuestId.value = null
  }

  const closeQuestDialog = () => {
    if (state.questDialogId.value !== null) {
      delete state.applicationMessages.value[state.questDialogId.value]
      delete state.proposedPrices.value[state.questDialogId.value]
    }

    state.questDialogId.value = null
    cancelEditingQuest()
  }

  const closeApplicationDialog = () => {
    state.applicationDialogId.value = null
    cancelEditingApplication()
  }

  const openUserProfileDialog = (userId: number) => {
    if (!Number.isFinite(userId)) {
      return
    }

    state.userProfileDialogId.value = userId
  }

  const closeUserProfileDialog = () => {
    state.userProfileDialogId.value = null
  }

  const openCreateJobDialog = () => {
    state.isCreateJobDialogOpen.value = true
  }

  const closeCreateJobDialog = () => {
    state.isCreateJobDialogOpen.value = false
  }

  const openFindWorkDialog = () => {
    state.isFindWorkDialogOpen.value = true
  }

  const closeFindWorkDialog = () => {
    state.isFindWorkDialogOpen.value = false
  }

  const openOpenWorkDialog = () => {
    state.isOpenWorkDialogOpen.value = true
  }

  const closeOpenWorkDialog = () => {
    state.isOpenWorkDialogOpen.value = false
  }

  const openApplicationsDialog = () => {
    state.isApplicationsDialogOpen.value = true
  }

  const closeApplicationsDialog = () => {
    state.isApplicationsDialogOpen.value = false
  }

  const scrollQuestDisclosureIntoView = async (questId: number) => {
    await nextTick()
    const element = state.questDisclosureRefs.value[questId]
    if (!element) {
      return
    }

    element.scrollIntoView({behavior: "smooth", block: "center", inline: "nearest"})
  }

  const registerQuestDisclosure = (questId: number, element: HTMLElement | null) => {
    if (element instanceof HTMLDetailsElement) {
      state.questDisclosureRefs.value[questId] = element
      return
    }

    delete state.questDisclosureRefs.value[questId]
  }

  const openQuestDisclosure = async (questId: number) => {
    const element = state.questDisclosureRefs.value[questId]
    if (element) {
      element.open = true
    }

    if (!state.openApplicationsQuestIds.value[questId]) {
      state.openApplicationsQuestIds.value[questId] = true
      state.showAllApplicationsQuestIds.value[questId] = false
    }

    await scrollQuestDisclosureIntoView(questId)
  }

  const toggleApplicationsForQuest = async (questId: number) => {
    if (state.openApplicationsQuestIds.value[questId]) {
      closeQuestDisclosure(questId)
      return
    }

    await openQuestDisclosure(questId)
  }

  const startEditingQuest = (quest: Quest) => {
    state.editingQuestId.value = quest.id
    populateEditQuestDraft(state, quest)
    state.openApplicationsQuestIds.value[quest.id] = false
    state.showAllApplicationsQuestIds.value[quest.id] = false
    void scrollQuestDisclosureIntoView(quest.id)
  }

  const toggleQuestDisclosure = (questId: number) => {
    const element = state.questDisclosureRefs.value[questId]
    if (!element) {
      return
    }

    if (element.open) {
      closeQuestDisclosure(questId)
      return
    }

    element.open = true
    const currentEditingQuest = state.editingQuestId.value === null ? null : state.questForId(state.editingQuestId.value)

    if (currentEditingQuest && currentEditingQuest.id !== questId) {
      cancelEditingQuest()
    }

    void scrollQuestDisclosureIntoView(questId)
  }

  const handleDocumentClick = (event: MouseEvent) => {
    const target = event.target
    if (!(target instanceof Node)) {
      return
    }

    for (const [questIdString, element] of Object.entries(state.questDisclosureRefs.value)) {
      if (!element || !element.open) {
        continue
      }

      if (element.contains(target)) {
        continue
      }

      closeQuestDisclosure(Number(questIdString))
    }
  }

  onMounted(() => {
    document.addEventListener("click", handleDocumentClick)
  })

  onBeforeUnmount(() => {
    document.removeEventListener("click", handleDocumentClick)
  })

  const startEditingApplication = (application: QuestApplication) => {
    state.editingApplicationId.value = application.id
    state.editApplicationMessage.value = application.message
    state.editApplicationPrice.value = String(application.proposedPrice ?? "")
  }

  const handleApplicationDisclosureToggle = (application: QuestApplication, isOpen: boolean) => {
    if (isOpen) {
      const currentEditingApplication = state.editingApplicationId.value === null
        ? null
        : state.myApplications.value.find((entry) => entry.id === state.editingApplicationId.value) ?? null

      if (currentEditingApplication && currentEditingApplication.id !== application.id) {
        cancelEditingApplication()
      }

      if (application.presentation.canEdit) {
        startEditingApplication(application)
      }
      return
    }

    if (state.editingApplicationId.value === application.id) {
      cancelEditingApplication()
    }
  }

  const reopenQuest = (quest: Quest) => {
    populateCreateQuestDraft(state, quest, state.adminModeEnabled.value)
    state.editingQuestId.value = null
    closeQuestDisclosure(quest.id)
    goToTab("side-job")
  }

  const copyDebugInfo = async (lines: string[]) => {
    if (!lines.length) {
      return
    }

    await navigator.clipboard.writeText(formatDebugInfo(lines))
    state.copiedDebugBanner.show("Copied")
  }

  return {
    setQuestTermMode,
    setEditQuestTermMode,
    showFeedback: state.showFeedback,
    triggerSuccessPulse,
    setActiveTab,
    goToTab,
    openNotificationsDialog,
    closeNotificationsDialog,
    closeQuestDialog,
    closeApplicationDialog,
    openUserProfileDialog,
    closeUserProfileDialog,
    openCreateJobDialog,
    closeCreateJobDialog,
    openFindWorkDialog,
    closeFindWorkDialog,
    openOpenWorkDialog,
    closeOpenWorkDialog,
    openApplicationsDialog,
    closeApplicationsDialog,
    registerQuestDisclosure,
    closeQuestDisclosure,
    openQuestDisclosure,
    toggleApplicationsForQuest,
    toggleQuestDisclosure,
    handleDocumentClick,
    startEditingApplication,
    handleApplicationDisclosureToggle,
    cancelEditingApplication,
    startEditingQuest,
    reopenQuest,
    cancelEditingQuest,
    copyDebugInfo
  }
}
