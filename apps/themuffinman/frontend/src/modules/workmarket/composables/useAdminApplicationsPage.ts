import {ref, watch} from "vue"
import {useRouter} from "vue-router"
import {useAutoDismissFeedback} from "../../../composables/useAutoDismissFeedback.ts"
import {createFeedbackMutationRunner} from "../../../composables/createFeedbackMutationRunner.ts"
import {getApiErrorMessage} from "../../../api/apiErrors.ts"
import {normalizeSearchQuery} from "../../../lib/searchQuery.ts"
import {useMountedAsync} from "../../../composables/useMountedAsync.ts"
import {usePaginatedResults} from "../../../composables/usePaginatedResults.ts"
import {routeForNavigationTarget} from "../shared/navigationTargets.ts"
import {workmarketApi, type QuestApplication, type WorkmarketOptions} from "../api/workmarketApi.ts"
import type {QuestApplicationStatus} from "../domain/workmarketDomain.ts"

export const useAdminApplicationsPage = () => {
  const router = useRouter()
  const itemsPerPage = 100
  const results = usePaginatedResults<QuestApplication>(itemsPerPage)
  const applications = results.items
  const isLoading = ref(false)
  const error = ref("")
  const searchQuery = ref("")
  const statusFilter = ref<QuestApplicationStatus | "ALL">("ALL")
  const statusFilterOptions = ref<WorkmarketOptions["questApplicationStatusFilters"]>([])
  const feedbackState = useAutoDismissFeedback<"success" | "error" | "warning">(5000, "success")
  const feedback = feedbackState.message
  const feedbackTone = feedbackState.tone
  const setFeedback = feedbackState.show
  const {runWithFeedback} = createFeedbackMutationRunner({showFeedback: setFeedback})

  const loadApplications = async (page = 1) => {
    isLoading.value = true
    error.value = ""

    try {
      const response = await workmarketApi.getAdminApplications({
        q: normalizeSearchQuery(searchQuery.value),
        status: statusFilter.value,
        page: Math.max(0, page - 1),
        size: itemsPerPage
      })
      results.applyPage(response)
    } catch (requestError) {
      error.value = getApiErrorMessage(requestError, "Could not load applications.")
      results.reset()
    } finally {
      isLoading.value = false
    }
  }

  const loadStatusFilterOptions = async () => {
    try {
      const options = await workmarketApi.getAppUserOptions()
      statusFilterOptions.value = options.questApplicationStatusFilters
    } catch (requestError) {
      setFeedback(getApiErrorMessage(requestError, "Could not load application filters."), "error")
      statusFilterOptions.value = []
    }
  }

  const openQuest = (application: QuestApplication) => {
    void router.push(routeForNavigationTarget(application.questNavigation))
  }

  const openApplicant = (application: QuestApplication) => {
    void router.push(routeForNavigationTarget(application.applicantNavigation))
  }

  const approveApplication = async (application: QuestApplication) => {
    await runWithFeedback({
      run: () => workmarketApi.approveApplication(application.questId, application.id),
      successMessage: (result) => result.message,
      errorMessage: "Could not approve application.",
      afterSuccess: () => loadApplications()
    })
  }

  const declineApplication = async (application: QuestApplication) => {
    await runWithFeedback({
      run: () => workmarketApi.declineApplication(application.questId, application.id),
      successMessage: (result) => result.message,
      errorMessage: "Could not decline application.",
      afterSuccess: () => loadApplications()
    })
  }

  const previousPage = () => {
    void results.previousPage(loadApplications)
  }

  const nextPage = () => {
    void results.nextPage(loadApplications)
  }

  watch([searchQuery, statusFilter], () => {
    void loadApplications(1)
  })

  useMountedAsync(async () => {
    await loadStatusFilterOptions()
    await loadApplications()
  })

  return {
    applications,
    isLoading,
    error,
    searchQuery,
    statusFilter,
    statusFilterOptions,
    feedback,
    feedbackTone,
    totalItems: results.totalItems,
    totalPages: results.totalPages,
    currentPage: results.currentPage,
    hasPreviousPage: results.hasPreviousPage,
    hasNextPage: results.hasNextPage,
    pageStart: results.pageStart,
    pageEnd: results.pageEnd,
    openQuest,
    openApplicant,
    approveApplication,
    declineApplication,
    loadApplications,
    previousPage,
    nextPage
  }
}
