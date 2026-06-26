import {ref} from "vue"
import type {
  AppUser,
  CircleGroup,
  CircleRequest,
  DashboardSections,
  DashboardSummary,
  Quest,
  QuestApplication,
  QuestApplicationDetail,
  QuestDetail,
  QuestApplicationsView,
  QuestNewsItem,
  WorkmarketOptions
} from "../../api/workmarketApi.ts"

export const createDashboardDataState = () => {
  const quests = ref<Quest[]>([])
  const dashboardMyQuests = ref<Quest[]>([])
  const dashboardAvailableQuests = ref<Quest[]>([])
  const myApplications = ref<QuestApplication[]>([])
  const newsItems = ref<QuestNewsItem[]>([])
  const unreadNewsCount = ref(0)
  const dashboardOptions = ref<WorkmarketOptions | null>(null)
  const dashboardSummary = ref<DashboardSummary | null>(null)
  const dashboardSections = ref<DashboardSections | null>(null)
  const incomingCircleRequests = ref<CircleRequest[]>([])
  const circles = ref<CircleGroup[]>([])
  const appUsers = ref<AppUser[]>([])
  const applicationsByQuestId = ref<Record<number, QuestApplicationsView>>({})
  const questDetailsById = ref<Record<number, QuestDetail>>({})
  const applicationDetailsById = ref<Record<number, QuestApplicationDetail>>({})

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

  return {
    quests,
    dashboardMyQuests,
    dashboardAvailableQuests,
    myApplications,
    newsItems,
    unreadNewsCount,
    dashboardOptions,
    dashboardSummary,
    dashboardSections,
    incomingCircleRequests,
    circles,
    appUsers,
    applicationsByQuestId,
    questDetailsById,
    applicationDetailsById,
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
    usersErrorDetails
  }
}
