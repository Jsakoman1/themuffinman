import {ref} from "vue"
import type {
  AppUser,
  CircleGroup,
  DashboardSummary,
  Quest,
  QuestApplication,
  QuestApplicationDetail,
  QuestDetail,
  QuestApplicationsView,
  WorkmarketOptions
} from "../../api/workmarketApi.ts"

export const createDashboardDataState = () => {
  const quests = ref<Quest[]>([])
  const myApplications = ref<QuestApplication[]>([])
  const dashboardOptions = ref<WorkmarketOptions | null>(null)
  const dashboardSummary = ref<DashboardSummary | null>(null)
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
    myApplications,
    dashboardOptions,
    dashboardSummary,
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
