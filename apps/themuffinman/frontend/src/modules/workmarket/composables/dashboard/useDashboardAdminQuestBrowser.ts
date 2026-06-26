import {computed, ref} from "vue"
import {useRouter} from "vue-router"
import {workmarketApi, type NavigationTarget} from "../../api/workmarketApi.ts"
import type {QuestAudience} from "../../domain/workmarketDomain.ts"
import {normalizeSearchQuery} from "../../../../lib/searchQuery.ts"
import {useQuestSearchResults} from "../useQuestSearchResults.ts"
import {routeForNavigationTarget} from "../../shared/navigationTargets.ts"
import {ALL_FILTER_VALUE, toOptionalFilterValue} from "../../shared/filterValues.ts"
import type {DashboardAdminFacade} from "./dashboardFacades.ts"

export const useDashboardAdminQuestBrowser = (dashboard: DashboardAdminFacade) => {
  const router = useRouter()
  const viewerTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone
  const viewerTimezoneOffsetMinutes = new Date().getTimezoneOffset()
  const questSearch = ref("")
  const audienceFilter = ref<QuestAudience | typeof ALL_FILTER_VALUE>(ALL_FILTER_VALUE)
  const dateFrom = ref("")
  const dateTo = ref("")
  const itemsPerPage = 100

  const {results: questResults, loadQuests, watchAndReload} = useQuestSearchResults(itemsPerPage, (page) => workmarketApi.searchQuests({
    q: normalizeSearchQuery(questSearch.value),
    status: toOptionalFilterValue(dashboard.adminQuestStatusFilter),
    audience: toOptionalFilterValue(audienceFilter.value),
    dateFrom: dateFrom.value || null,
    dateTo: dateTo.value || null,
    viewerTimeZone,
    viewerTimezoneOffsetMinutes,
    sort: "recommended",
    page,
    size: itemsPerPage
  }))

  const pagedQuests = computed(() => questResults.items.value)
  const totalItems = questResults.totalItems
  const totalPages = questResults.totalPages
  const currentPage = questResults.currentPage
  const isLoading = questResults.isLoading
  const pageStart = questResults.pageStart
  const pageEnd = questResults.pageEnd
  const hasPreviousPage = questResults.hasPreviousPage
  const hasNextPage = questResults.hasNextPage

  watchAndReload([questSearch, () => dashboard.adminQuestStatusFilter, audienceFilter, dateFrom, dateTo])

  const previousPage = () => {
    void questResults.previousPage(loadQuests)
  }

  const nextPage = () => {
    void questResults.nextPage(loadQuests)
  }

  const openQuest = async (navigation: NavigationTarget | null) => {
    await router.push(routeForNavigationTarget(navigation))
  }

  return {
    questSearch,
    audienceFilter,
    dateFrom,
    dateTo,
    pagedQuests,
    totalItems,
    totalPages,
    currentPage,
    isLoading,
    pageStart,
    pageEnd,
    hasPreviousPage,
    hasNextPage,
    loadQuests,
    previousPage,
    nextPage,
    openQuest
  }
}
