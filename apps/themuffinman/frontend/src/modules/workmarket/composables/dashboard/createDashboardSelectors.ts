import {computed, type Ref} from "vue"
import type {DashboardTab, QuestStatusFilter} from "../../domain/workmarketDomain.ts"
import type {
  DashboardSections,
  DashboardSummary,
  Quest,
  QuestApplication,
  QuestApplicationDetail,
  QuestDetail,
  QuestApplicationsView,
  QuestNewsItem,
  CircleRequest
} from "../../api/workmarketApi.ts"

export const createDashboardSelectors = (state: {
  activeTab: Ref<DashboardTab>
  quests: Ref<Quest[]>
  dashboardMyQuests: Ref<Quest[]>
  dashboardAvailableQuests: Ref<Quest[]>
  myApplications: Ref<QuestApplication[]>
  newsItems: Ref<QuestNewsItem[]>
  dashboardSummary: Ref<DashboardSummary | null>
  dashboardSections: Ref<DashboardSections | null>
  incomingCircleRequests: Ref<CircleRequest[]>
  adminQuestStatusFilter: Ref<QuestStatusFilter>
  applicationsByQuestId: Ref<Record<number, QuestApplicationsView>>
  questDetailsById: Ref<Record<number, QuestDetail>>
  applicationDetailsById: Ref<Record<number, QuestApplicationDetail>>
  questDialogId: Ref<number | null>
  applicationDialogId: Ref<number | null>
  dashboardTabs: Array<{id: DashboardTab; title: string; description: string}>
}) => {
  const sectionTitle = computed(() => {
    return state.dashboardTabs.find((tab) => tab.id === state.activeTab.value)?.title ?? "Overview"
  })

  const sectionSubtitle = computed(() => {
    return state.dashboardTabs.find((tab) => tab.id === state.activeTab.value)?.description ?? ""
  })

  const recentMyQuests = computed(() => state.dashboardSections.value?.recentMyQuests ?? [])
  const recentMyApplications = computed(() => state.dashboardSections.value?.recentMyApplications ?? [])
  const activeWorkApplications = computed(() => state.dashboardSections.value?.activeWorkApplications ?? [])
  const pendingWorkApplications = computed(() => state.dashboardSections.value?.visibleMyApplications ?? [])
  const visibleMyQuestsCount = computed(() => state.dashboardSummary.value?.visibleMyQuestsCount ?? 0)
  const pendingWorkApplicationsCount = computed(() => state.dashboardSummary.value?.pendingWorkApplicationsCount ?? 0)
  const activeWorkApplicationsCount = computed(() => state.dashboardSummary.value?.activeWorkApplicationsCount ?? 0)
  const activeMyQuestsCount = computed(() => state.dashboardSummary.value?.activeMyQuestsCount ?? 0)
  const activeWorkCount = computed(() => state.dashboardSummary.value?.activeWorkCount ?? 0)
  const completedMyQuestsCount = computed(() => state.dashboardSummary.value?.completedMyQuestsCount ?? 0)
  const incomingWorkQuests = computed(() => state.dashboardSections.value?.incomingWorkQuests ?? [])
  const outgoingWorkApplications = computed(() => state.dashboardSections.value?.outgoingWorkApplications ?? [])
  const visibleMyQuests = computed(() => state.dashboardSections.value?.visibleMyQuests ?? [])
  const visibleMyApplications = computed(() => state.dashboardSections.value?.visibleMyApplications ?? [])
  const recentNewsItems = computed(() => state.dashboardSections.value?.notifications?.recentItems ?? [])
  const unreadNewsItems = computed(() => state.dashboardSections.value?.notifications?.unreadItems ?? [])
  const recentIncomingCircleRequests = computed(() => state.dashboardSections.value?.recentIncomingCircleRequests ?? [])

  const applicationsViewForQuest = (questId: number) => (
    state.applicationsByQuestId.value[questId]
    ?? state.questDetailsById.value[questId]?.applicationsView
    ?? null
  )
  const applicationsForQuest = (questId: number) => applicationsViewForQuest(questId)?.visibleApplications ?? []
  const approvedApplicationsForQuest = (questId: number) => applicationsViewForQuest(questId)?.approvedApplications ?? []
  const questForId = (questId: number) => state.quests.value.find((quest) => quest.id === questId) ?? null

  const selectedQuestDialog = computed(() => {
    if (state.questDialogId.value === null) {
      return null
    }

    return questForId(state.questDialogId.value)
      ?? state.questDetailsById.value[state.questDialogId.value]?.quest
      ?? null
  })

  const selectedQuestDetail = computed(() => {
    if (state.questDialogId.value === null) {
      return null
    }

    return state.questDetailsById.value[state.questDialogId.value] ?? null
  })

  const selectedApplicationDialog = computed(() => {
    if (state.applicationDialogId.value === null) {
      return null
    }

    return state.myApplications.value.find((application) => application.id === state.applicationDialogId.value) ?? null
  })

  const selectedApplicationDetail = computed(() => {
    if (state.applicationDialogId.value === null) {
      return null
    }

    return state.applicationDetailsById.value[state.applicationDialogId.value] ?? null
  })

  const questCreatorUsernameForQuest = (questId: number) => questForId(questId)?.creatorUsername ?? "Unknown"
  const featuredApplicationForQuest = (questId: number) => applicationsViewForQuest(questId)?.featuredApplication ?? null
  const hiddenApplicationsCountForQuest = (questId: number) => applicationsViewForQuest(questId)?.hiddenApplicationsCount ?? 0
  const canRevealHiddenApplicationsForQuest = (questId: number) => applicationsViewForQuest(questId)?.canRevealHiddenApplications ?? false
  const showAllApplicationsForQuest = (questId: number) => applicationsViewForQuest(questId)?.showingAllApplications ?? false
  const applicationRevealLabel = (questId: number) => applicationsViewForQuest(questId)?.revealLabel ?? "Show applications"
  return {
    sectionTitle,
    sectionSubtitle,
    recentMyQuests,
    recentMyApplications,
    activeWorkApplications,
    pendingWorkApplications,
    visibleMyQuestsCount,
    pendingWorkApplicationsCount,
    activeWorkApplicationsCount,
    activeMyQuestsCount,
    activeWorkCount,
    completedMyQuestsCount,
    incomingWorkQuests,
    outgoingWorkApplications,
    visibleMyQuests,
    visibleMyApplications,
    recentNewsItems,
    unreadNewsItems,
    recentIncomingCircleRequests,
    applicationsViewForQuest,
    applicationsForQuest,
    approvedApplicationsForQuest,
    questForId,
    selectedQuestDialog,
    selectedQuestDetail,
    selectedApplicationDialog,
    selectedApplicationDetail,
    questCreatorUsernameForQuest,
    featuredApplicationForQuest,
    hiddenApplicationsCountForQuest,
    canRevealHiddenApplicationsForQuest,
    showAllApplicationsForQuest,
    applicationRevealLabel
  }
}
