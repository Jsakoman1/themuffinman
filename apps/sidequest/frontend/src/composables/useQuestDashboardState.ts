import {computed, nextTick, onBeforeUnmount, onMounted, ref, watch} from "vue"
import {useRoute, useRouter} from "vue-router"
import {currentUser, isAdmin} from "../auth.ts"
import {formatDebugInfo} from "../httpDebug.ts"
import {type AppUser, type CircleRequest, type CircleGroup, type DashboardSummary, type Quest, type QuestApplication, type QuestApplicationsView, type QuestNewsItem} from "../api/sidequestApi.ts"
import {
  formatApplicationStatus,
  formatQuestStatus,
  statusBadgeClass,
  statusSurfaceClass
} from "../lib/questDashboardRules.ts"
import {
  formatInstantForDisplay,
  formatInstantForInput,
  formatQuestTerm,
  parseInstantFromInput
} from "../shared/questSchedule.ts"
import {useTimedBanner} from "./useTimedBanner.ts"
import {
  dashboardTabs,
  applicationStatusSortOrder,
  questStatusSortOrder,
  questAudienceOptions,
  questStatusOptions,
  type DashboardTab,
  type OverviewFocus,
  type QuestAudience,
  type QuestStatus,
  type QuestStatusFilter
} from "../shared/sidequestDomain.ts"

export const useQuestDashboardState = () => {
  const route = useRoute()
  const router = useRouter()

  const activeTab = ref<DashboardTab>("overview")
  const quests = ref<Quest[]>([])
  const dashboardMyQuests = ref<Quest[]>([])
  const dashboardAvailableQuests = ref<Quest[]>([])
  const myApplications = ref<QuestApplication[]>([])
  const newsItems = ref<QuestNewsItem[]>([])
  const unreadNewsCount = ref(0)
  const dashboardSummary = ref<DashboardSummary | null>(null)
  const incomingCircleRequests = ref<CircleRequest[]>([])
  const circles = ref<CircleGroup[]>([])
  const appUsers = ref<AppUser[]>([])

  const isLoadingQuests = ref(false)
  const isLoadingApplications = ref(false)
  const isLoadingNews = ref(false)
  const isLoadingUsers = ref(false)

  const questsError = ref("")
  const questsErrorDetails = ref<string[]>([])
  const applicationsError = ref("")
  const applicationsErrorDetails = ref<string[]>([])
  const newsError = ref("")
  const newsErrorDetails = ref<string[]>([])
  const usersError = ref("")
  const usersErrorDetails = ref<string[]>([])
  const copiedDebugBanner = useTimedBanner(1500)
  const copiedDebug = computed(() => !!copiedDebugBanner.message.value)

  const feedback = ref("")
  const feedbackType = ref<"error" | "success">("success")
  const isProfileEditDialogOpen = ref(false)
  const isNotificationsDialogOpen = ref(false)
  const successPulseTarget = ref("")
  const feedbackTimeout = ref<number | null>(null)
  let successPulseTimeout: number | undefined

  const questTitle = ref("")
  const questDescription = ref("")
  const questAwardAmount = ref("")
  const questScheduledAt = ref("")
  const questEndsAt = ref("")
  const questTermMode = ref<"flexible" | "start-only" | "start-end">("flexible")
  const questTermFixed = ref(false)
  const questAudience = ref<QuestAudience>("CIRCLES")
  const questSelectedCircleIds = ref<number[]>([])
  const questCreatorId = ref("")
  const questImages = ref<string[]>([])

  const profileUsername = ref("")
  const profileDescription = ref(currentUser.value?.profileDescription ?? "")
  const profileAvatarDataUrl = ref(currentUser.value?.profileAvatarDataUrl ?? "")
  const accountCreatedAt = computed(() => currentUser.value?.createdAt ?? new Date().toISOString())

  const myQuestStatusFilter = ref<QuestStatusFilter>("ALL")
  const adminQuestStatusFilter = ref<QuestStatusFilter>("ALL")

  const applicationMessages = ref<Record<number, string>>({})
  const proposedPrices = ref<Record<number, string>>({})
  const applicationsByQuestId = ref<Record<number, QuestApplicationsView>>({})
  const openApplicationsQuestIds = ref<Record<number, boolean>>({})
  const showAllApplicationsQuestIds = ref<Record<number, boolean>>({})
  const questDisclosureRefs = ref<Record<number, HTMLDetailsElement | null>>({})

  const editingQuestId = ref<number | null>(null)
  const editQuestTitle = ref("")
  const editQuestDescription = ref("")
  const editQuestAwardAmount = ref("")
  const editQuestScheduledAt = ref("")
  const editQuestEndsAt = ref("")
  const editQuestTermMode = ref<"flexible" | "start-only" | "start-end">("flexible")
  const editQuestTermFixed = ref(false)
  const editQuestAudience = ref<QuestAudience>("CIRCLES")
  const editQuestSelectedCircleIds = ref<number[]>([])
  const editQuestCreatorId = ref("")
  const editQuestStatus = ref<QuestStatus>("OPEN")
  const editingApplicationId = ref<number | null>(null)
  const editApplicationMessage = ref("")
  const editApplicationPrice = ref("")
  const overviewFocus = ref<OverviewFocus | null>(null)
  const questDialogId = ref<number | null>(null)
  const applicationDialogId = ref<number | null>(null)
  const userProfileDialogId = ref<number | null>(null)
  const isCreateJobDialogOpen = ref(false)
  const isFindWorkDialogOpen = ref(false)
  const isOpenWorkDialogOpen = ref(false)
  const isApplicationsDialogOpen = ref(false)

  const sectionTitle = computed(() => {
    return dashboardTabs.find((tab) => tab.id === activeTab.value)?.title ?? "Overview"
  })

  const sectionSubtitle = computed(() => {
    return dashboardTabs.find((tab) => tab.id === activeTab.value)?.description ?? ""
  })

  watch(() => route.query.tab, (value) => {
    if (typeof value !== "string") {
      return
    }

    if (dashboardTabs.some((tab) => tab.id === value)) {
      activeTab.value = value as DashboardTab
    }
  }, {immediate: true})

  const sortedQuests = computed(() => {
    return [...quests.value].sort((left, right) => {
      const leftRank = questStatusSortOrder[left.status] ?? Number.POSITIVE_INFINITY
      const rightRank = questStatusSortOrder[right.status] ?? Number.POSITIVE_INFINITY

      if (leftRank !== rightRank) {
        return leftRank - rightRank
      }

      return right.id - left.id
    })
  })

  const sortedMyApplications = computed(() => {
    return [...myApplications.value].sort((left, right) => {
      const leftRank = applicationStatusSortOrder[left.status] ?? Number.POSITIVE_INFINITY
      const rightRank = applicationStatusSortOrder[right.status] ?? Number.POSITIVE_INFINITY

      if (leftRank !== rightRank) {
        return leftRank - rightRank
      }

      return right.id - left.id
    })
  })

  const isMyQuest = (quest: Quest) => {
    return !!currentUser.value && quest.creatorId === currentUser.value.id
  }

  const myQuests = computed(() => {
    if (dashboardMyQuests.value.length || !quests.value.length) {
      return dashboardMyQuests.value
    }

    return sortedQuests.value.filter((quest) => isMyQuest(quest))
  })
  const activeMyQuests = computed(() => myQuests.value.filter((quest) => quest.status === "ASSIGNED" || quest.status === "IN_PROGRESS"))
  const availableQuests = computed(() => {
    if (dashboardAvailableQuests.value.length || !quests.value.length) {
      return dashboardAvailableQuests.value
    }

    return sortedQuests.value.filter((quest) => !isMyQuest(quest) && quest.status === "OPEN")
  })
  const adminQuests = computed(() => sortedQuests.value)
  const appliedQuestIds = computed(() => new Set(myApplications.value.map((application) => application.questId)))

  const filteredMyQuests = computed(() => {
    if (myQuestStatusFilter.value === "ALL") {
      return myQuests.value
    }

    return myQuests.value.filter((quest) => quest.status === myQuestStatusFilter.value)
  })

  const filteredAdminQuests = computed(() => {
    if (adminQuestStatusFilter.value === "ALL") {
      return adminQuests.value
    }

    return adminQuests.value.filter((quest) => quest.status === adminQuestStatusFilter.value)
  })

  const recentMyQuests = computed(() => myQuests.value.slice(0, 3))
  const recentMyApplications = computed(() => sortedMyApplications.value.slice(0, 3))
  const activeWorkApplications = computed(() => sortedMyApplications.value.filter((application) => {
    return application.status === "APPROVED"
      && (application.questStatus === "ASSIGNED" || application.questStatus === "IN_PROGRESS" || application.questStatus === "WAITING_CONFIRMATION")
  }))
  const pendingWorkApplications = computed(() => sortedMyApplications.value.filter((application) => application.status === "PENDING"))
  const visibleMyQuestsCount = computed(() => dashboardSummary.value?.visibleMyQuestsCount ?? 0)
  const pendingWorkApplicationsCount = computed(() => dashboardSummary.value?.pendingWorkApplicationsCount ?? 0)
  const activeWorkApplicationsCount = computed(() => dashboardSummary.value?.activeWorkApplicationsCount ?? 0)
  const activeMyQuestsCount = computed(() => dashboardSummary.value?.activeMyQuestsCount ?? 0)
  const activeWorkCount = computed(() => dashboardSummary.value?.activeWorkCount ?? 0)
  const completedMyQuestsCount = computed(() => dashboardSummary.value?.completedMyQuestsCount ?? 0)
  const incomingWorkQuests = computed(() => myQuests.value.filter((quest) => quest.status !== "COMPLETED" && quest.status !== "CANCELLED"))
  const outgoingWorkApplications = computed(() => sortedMyApplications.value.filter((application) => {
    return application.status === "PENDING" || application.status === "APPROVED"
  }))
  const visibleMyQuests = computed(() => myQuests.value.filter((quest) => quest.status === "OPEN" || quest.status === "WAITING_CONFIRMATION"))
  const visibleMyApplications = computed(() => pendingWorkApplications.value)
  const recentNewsItems = computed(() => newsItems.value.slice(0, 6))
  const unreadNewsItems = computed(() => newsItems.value.filter((item) => item.readAt === null))
  const recentIncomingCircleRequests = computed(() => incomingCircleRequests.value.slice(0, 4))
  const overviewCards = computed(() => [
    {id: "active-work" as OverviewFocus, label: "Active work", value: activeWorkCount.value, hint: "Jobs you can act on now", tab: "overview" as DashboardTab},
    {id: "posted-work" as OverviewFocus, label: "Your jobs", value: visibleMyQuestsCount.value, hint: "Jobs you manage", tab: "create-job" as DashboardTab},
    {id: "applied-tasks" as OverviewFocus, label: "Pending applications", value: pendingWorkApplicationsCount.value, hint: "Jobs waiting for a reply", tab: "find-work" as DashboardTab},
    {id: "completed" as OverviewFocus, label: "Completed", value: completedMyQuestsCount.value, hint: "Finished jobs", tab: "create-job" as DashboardTab}
  ])

  const applicationsViewForQuest = (questId: number) => applicationsByQuestId.value[questId] ?? null
  const applicationsForQuest = (questId: number) => applicationsViewForQuest(questId)?.visibleApplications ?? []
  const questForId = (questId: number) => quests.value.find((quest) => quest.id === questId) ?? null

  const selectedQuestDialog = computed(() => {
    if (questDialogId.value === null) {
      return null
    }

    return questForId(questDialogId.value)
  })

  const selectedApplicationDialog = computed(() => {
    if (applicationDialogId.value === null) {
      return null
    }

    return sortedMyApplications.value.find((application) => application.id === applicationDialogId.value) ?? null
  })

  const questCreatorUsernameForQuest = (questId: number) => questForId(questId)?.creatorUsername ?? "Unknown"
  const featuredApplicationForQuest = (questId: number) => applicationsViewForQuest(questId)?.featuredApplication ?? null
  const hiddenApplicationsCountForQuest = (questId: number) => applicationsViewForQuest(questId)?.hiddenApplicationsCount ?? 0
  const canRevealHiddenApplicationsForQuest = (questId: number) => applicationsViewForQuest(questId)?.canRevealHiddenApplications ?? false
  const showAllApplicationsForQuest = (questId: number) => applicationsViewForQuest(questId)?.showingAllApplications ?? false
  const applicationRevealLabel = (questId: number) => applicationsViewForQuest(questId)?.revealLabel ?? "Show applications"

  const hasAppliedToQuest = (questId: number) => appliedQuestIds.value.has(questId)
  const formatDateTime = (value: string) => formatInstantForDisplay(value)
  const formatQuestTermLabel = (quest: Quest) => formatQuestTerm(quest.scheduledAt, quest.endsAt, quest.termFixed)
  const formatQuestTermFromParts = (scheduledAt: string | null | undefined, endsAt: string | null | undefined, termFixed: boolean) => formatQuestTerm(scheduledAt, endsAt, termFixed)

  const setQuestTermMode = (mode: "flexible" | "start-only" | "start-end") => {
    questTermMode.value = mode
    questTermFixed.value = mode !== "flexible"
    if (mode === "flexible") {
      questScheduledAt.value = ""
      questEndsAt.value = ""
    }
    if (mode === "start-only") {
      questEndsAt.value = ""
    }
  }

  const setEditQuestTermMode = (mode: "flexible" | "start-only" | "start-end") => {
    editQuestTermMode.value = mode
    editQuestTermFixed.value = mode !== "flexible"
    if (mode === "flexible") {
      editQuestScheduledAt.value = ""
      editQuestEndsAt.value = ""
    }
    if (mode === "start-only") {
      editQuestEndsAt.value = ""
    }
  }

  const showFeedback = (message: string, type: "error" | "success") => {
    if (feedbackTimeout.value !== null) {
      window.clearTimeout(feedbackTimeout.value)
    }

    feedback.value = message
    feedbackType.value = type
    feedbackTimeout.value = window.setTimeout(() => {
      feedback.value = ""
    }, 5000)
  }

  const triggerSuccessPulse = (target: string) => {
    successPulseTarget.value = target

    if (successPulseTimeout !== undefined) {
      window.clearTimeout(successPulseTimeout)
    }

    successPulseTimeout = window.setTimeout(() => {
      successPulseTarget.value = ""
    }, 900)
  }

  const setActiveTab = (tabId: DashboardTab) => {
    activeTab.value = tabId
  }

  const goToTab = (tabId: DashboardTab) => {
    activeTab.value = tabId
    void router.push({
      query: {
        ...route.query,
        tab: tabId
      }
    })
  }

  const toggleOverviewFocus = (focus: OverviewFocus) => {
    overviewFocus.value = overviewFocus.value === focus ? null : focus
  }

  const clearOverviewFocus = () => {
    overviewFocus.value = null
  }

  const openProfileEditDialog = () => {
    if (!currentUser.value) {
      return
    }

    profileUsername.value = currentUser.value.username
    profileDescription.value = currentUser.value.profileDescription ?? ""
    profileAvatarDataUrl.value = currentUser.value.profileAvatarDataUrl ?? ""
    isProfileEditDialogOpen.value = true
  }

  const closeProfileEditDialog = () => {
    isProfileEditDialogOpen.value = false
  }

  const openNotificationsDialog = () => {
    isNotificationsDialogOpen.value = true
  }

  const closeNotificationsDialog = () => {
    isNotificationsDialogOpen.value = false
  }

  const closeQuestDialog = () => {
    if (questDialogId.value !== null) {
      delete applicationMessages.value[questDialogId.value]
      delete proposedPrices.value[questDialogId.value]
    }

    questDialogId.value = null
    cancelEditingQuest()
  }

  const closeApplicationDialog = () => {
    applicationDialogId.value = null
    cancelEditingApplication()
  }

  const openUserProfileDialog = (userId: number) => {
    if (!Number.isFinite(userId)) {
      return
    }

    userProfileDialogId.value = userId
  }

  const closeUserProfileDialog = () => {
    userProfileDialogId.value = null
  }

  const openCreateJobDialog = () => {
    isCreateJobDialogOpen.value = true
  }

  const closeCreateJobDialog = () => {
    isCreateJobDialogOpen.value = false
  }

  const openFindWorkDialog = () => {
    isFindWorkDialogOpen.value = true
  }

  const closeFindWorkDialog = () => {
    isFindWorkDialogOpen.value = false
  }

  const openOpenWorkDialog = () => {
    isOpenWorkDialogOpen.value = true
  }

  const closeOpenWorkDialog = () => {
    isOpenWorkDialogOpen.value = false
  }

  const openApplicationsDialog = () => {
    isApplicationsDialogOpen.value = true
  }

  const closeApplicationsDialog = () => {
    isApplicationsDialogOpen.value = false
  }

  const scrollQuestDisclosureIntoView = async (questId: number) => {
    await nextTick()
    const element = questDisclosureRefs.value[questId]
    if (!element) {
      return
    }

    element.scrollIntoView({behavior: "smooth", block: "center", inline: "nearest"})
  }

  const registerQuestDisclosure = (questId: number, element: HTMLElement | null) => {
    if (element instanceof HTMLDetailsElement) {
      questDisclosureRefs.value[questId] = element
      return
    }

    delete questDisclosureRefs.value[questId]
  }

  const closeQuestDisclosure = (questId: number) => {
    const element = questDisclosureRefs.value[questId]
    if (element) {
      element.open = false
    }

    if (editingQuestId.value === questId) {
      editingQuestId.value = null
    }

    openApplicationsQuestIds.value[questId] = false
    showAllApplicationsQuestIds.value[questId] = false
  }

  const openQuestDisclosure = async (questId: number) => {
    const element = questDisclosureRefs.value[questId]
    if (element) {
      element.open = true
    }

    if (!openApplicationsQuestIds.value[questId]) {
      openApplicationsQuestIds.value[questId] = true
      showAllApplicationsQuestIds.value[questId] = false
    }

    await scrollQuestDisclosureIntoView(questId)
  }

  const toggleApplicationsForQuest = async (questId: number) => {
    if (openApplicationsQuestIds.value[questId]) {
      closeQuestDisclosure(questId)
      return
    }

    await openQuestDisclosure(questId)
  }

  const toggleQuestDisclosure = (questId: number) => {
    const element = questDisclosureRefs.value[questId]
    if (!element) {
      return
    }

    if (element.open) {
      closeQuestDisclosure(questId)
      return
    }

    element.open = true
    const quest = questForId(questId)
    const currentEditingQuest = editingQuestId.value === null ? null : questForId(editingQuestId.value)

    if (currentEditingQuest && currentEditingQuest.id !== questId) {
      cancelEditingQuest()
    }

    if (quest && quest.status === "OPEN") {
      startEditingQuest(quest)
    }

    void scrollQuestDisclosureIntoView(questId)
  }

  const handleDocumentClick = (event: MouseEvent) => {
    const target = event.target
    if (!(target instanceof Node)) {
      return
    }

    for (const [questIdString, element] of Object.entries(questDisclosureRefs.value)) {
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

    if (feedbackTimeout.value !== null) {
      window.clearTimeout(feedbackTimeout.value)
    }
  })

  const startEditingApplication = (application: QuestApplication) => {
    editingApplicationId.value = application.id
    editApplicationMessage.value = application.message
    editApplicationPrice.value = String(application.proposedPrice ?? "")
  }

  const handleApplicationDisclosureToggle = (application: QuestApplication, isOpen: boolean) => {
    if (isOpen) {
      const currentEditingApplication = editingApplicationId.value === null
        ? null
        : myApplications.value.find((entry) => entry.id === editingApplicationId.value) ?? null

      if (currentEditingApplication && currentEditingApplication.id !== application.id) {
        cancelEditingApplication()
      }

      if (application.status === "PENDING") {
        startEditingApplication(application)
      }
      return
    }

    if (editingApplicationId.value === application.id) {
      cancelEditingApplication()
    }
  }

  const cancelEditingApplication = () => {
    editingApplicationId.value = null
  }

  const startEditingQuest = (quest: Quest) => {
    editingQuestId.value = quest.id
    editQuestTitle.value = quest.title
    editQuestDescription.value = quest.description
    editQuestAwardAmount.value = String(quest.awardAmount ?? "")
    editQuestScheduledAt.value = formatInstantForInput(quest.scheduledAt)
    editQuestEndsAt.value = formatInstantForInput(quest.endsAt)
    editQuestTermMode.value = quest.termFixed ? (quest.endsAt ? "start-end" : "start-only") : "flexible"
    editQuestTermFixed.value = quest.termFixed
    editQuestAudience.value = quest.audience
    editQuestSelectedCircleIds.value = quest.visibleToCircles.map((circle) => circle.id)
    editQuestCreatorId.value = String(quest.creatorId)
    editQuestStatus.value = quest.status
    openApplicationsQuestIds.value[quest.id] = false
    showAllApplicationsQuestIds.value[quest.id] = false
    void scrollQuestDisclosureIntoView(quest.id)
  }

  const reopenQuest = (quest: Quest) => {
    questTitle.value = quest.title
    questDescription.value = quest.description
    questAwardAmount.value = String(quest.awardAmount ?? "")
    questScheduledAt.value = formatInstantForInput(quest.scheduledAt)
    questEndsAt.value = formatInstantForInput(quest.endsAt)
    questTermMode.value = quest.termFixed ? (quest.endsAt ? "start-end" : "start-only") : "flexible"
    questTermFixed.value = quest.termFixed
    questAudience.value = quest.audience
    questSelectedCircleIds.value = quest.visibleToCircles.map((circle) => circle.id)
    questCreatorId.value = isAdmin() ? String(quest.creatorId) : ""
    editingQuestId.value = null
    closeQuestDisclosure(quest.id)
    goToTab("create-job")
  }

  const cancelEditingQuest = () => {
    if (editingQuestId.value !== null) {
      closeQuestDisclosure(editingQuestId.value)
    }

    editingQuestId.value = null
  }

  const resetErrorState = () => {
    questsError.value = ""
    questsErrorDetails.value = []
    applicationsError.value = ""
    applicationsErrorDetails.value = []
    newsError.value = ""
    newsErrorDetails.value = []
    usersError.value = ""
    usersErrorDetails.value = []
  }

  const copyDebugInfo = async (lines: string[]) => {
    if (!lines.length) {
      return
    }

    await navigator.clipboard.writeText(formatDebugInfo(lines))
    copiedDebugBanner.show("Copied")
  }

  const init = () => undefined

  return {
    activeTab,
    sectionTitle,
    sectionSubtitle,
    quests,
    dashboardMyQuests,
    dashboardAvailableQuests,
    myApplications,
    newsItems,
    unreadNewsCount,
    dashboardSummary,
    incomingCircleRequests,
    circles,
    appUsers,
    isLoadingQuests,
    isLoadingApplications,
    isLoadingNews,
    isLoadingUsers,
    questsError,
    questsErrorDetails,
    applicationsError,
    applicationsErrorDetails,
    newsError,
    newsErrorDetails,
    usersError,
    usersErrorDetails,
    feedback,
    feedbackType,
    copiedDebug,
    isProfileEditDialogOpen,
    isNotificationsDialogOpen,
    successPulseTarget,
    currentUser,
    isAdmin,
    questTitle,
    questDescription,
    questAwardAmount,
    questScheduledAt,
    questEndsAt,
    questTermFixed,
    questTermMode,
    questAudience,
    questSelectedCircleIds,
    questCreatorId,
    questImages,
    profileUsername,
    profileDescription,
    profileAvatarDataUrl,
    accountCreatedAt,
    myQuestStatusFilter,
    adminQuestStatusFilter,
    applicationMessages,
    proposedPrices,
    applicationsByQuestId,
    openApplicationsQuestIds,
    showAllApplicationsQuestIds,
    questDisclosureRefs,
    editingQuestId,
    editQuestTitle,
    editQuestDescription,
    editQuestAwardAmount,
    editQuestScheduledAt,
    editQuestEndsAt,
    editQuestTermFixed,
    editQuestTermMode,
    editQuestAudience,
    editQuestSelectedCircleIds,
    editQuestCreatorId,
    editQuestStatus,
    editingApplicationId,
    editApplicationMessage,
    editApplicationPrice,
    overviewFocus,
    questDialogId,
    applicationDialogId,
    userProfileDialogId,
    isCreateJobDialogOpen,
    isFindWorkDialogOpen,
    isOpenWorkDialogOpen,
    isApplicationsDialogOpen,
    questStatusOptions,
    questAudienceOptions,
    dashboardTabs,
    sortedQuests,
    sortedMyApplications,
    isMyQuest,
    myQuests,
    activeMyQuests,
    availableQuests,
    adminQuests,
    appliedQuestIds,
    filteredMyQuests,
    filteredAdminQuests,
    recentMyQuests,
    recentMyApplications,
    activeWorkApplications,
    pendingWorkApplications,
    visibleMyQuestsCount,
    pendingWorkApplicationsCount,
    activeWorkApplicationsCount,
    activeMyQuestsCount,
    activeWorkCount,
    incomingWorkQuests,
    outgoingWorkApplications,
    visibleMyQuests,
    visibleMyApplications,
    recentNewsItems,
    unreadNewsItems,
    recentIncomingCircleRequests,
    overviewCards,
    applicationsForQuest,
    questForId,
    selectedQuestDialog,
    selectedApplicationDialog,
    questCreatorUsernameForQuest,
    showAllApplicationsForQuest,
    applicationsViewForQuest,
    featuredApplicationForQuest,
    hiddenApplicationsCountForQuest,
    canRevealHiddenApplicationsForQuest,
    applicationRevealLabel,
    hasAppliedToQuest,
    formatStatus: formatQuestStatus,
    formatApplicationStatus,
    statusBadgeClass,
    statusSurfaceClass,
    formatDateTime,
    formatQuestTermLabel,
    formatQuestTermFromParts,
    parseInstantFromInput,
    setQuestTermMode,
    setEditQuestTermMode,
    showFeedback,
    triggerSuccessPulse,
    setActiveTab,
    goToTab,
    toggleOverviewFocus,
    clearOverviewFocus,
    openProfileEditDialog,
    closeProfileEditDialog,
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
    resetErrorState,
    copyDebugInfo,
    init
  }
}

export type QuestDashboardState = ReturnType<typeof useQuestDashboardState>
