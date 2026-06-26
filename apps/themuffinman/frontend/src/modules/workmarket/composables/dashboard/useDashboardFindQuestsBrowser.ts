import {computed, ref} from "vue"
import {useRouter} from "vue-router"
import {workmarketApi, type NavigationTarget} from "../../api/workmarketApi.ts"
import type {QuestAudience} from "../../domain/workmarketDomain.ts"
import {normalizeSearchQuery} from "../../../../lib/searchQuery.ts"
import {useQuestSearchResults} from "../useQuestSearchResults.ts"
import {routeForNavigationTarget} from "../../shared/navigationTargets.ts"
import {ALL_FILTER_VALUE, toOptionalFilterValue} from "../../shared/filterValues.ts"
import type {Quest} from "../../api/workmarketApi.ts"
import type {DashboardFindQuestsFacade} from "./dashboardFacades.ts"

export const useDashboardFindQuestsBrowser = (dashboard: DashboardFindQuestsFacade) => {
  const router = useRouter()
  const viewerTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone
  const viewerTimezoneOffsetMinutes = new Date().getTimezoneOffset()
  const searchDefaults = dashboard.questSearchDefaults
  const searchQuery = ref("")
  const defaultSort = searchDefaults?.defaultSort
  const sortMode = ref<"recommended" | "newest" | "highest">(
    defaultSort === "newest" || defaultSort === "highest" || defaultSort === "recommended"
      ? defaultSort
      : "recommended"
  )
  const photoOnly = ref(false)
  const flexibleOnly = ref(false)
  const nearbyOnly = ref(Boolean(searchDefaults?.nearbyDefaultEnabled))
  const audienceFilter = ref<QuestAudience | typeof ALL_FILTER_VALUE>(ALL_FILTER_VALUE)
  const dateFrom = ref("")
  const dateTo = ref("")
  const radiusKm = ref<number>(searchDefaults?.defaultRadiusKm ?? 10)
  const hasViewerLocation = ref(Boolean(searchDefaults?.hasViewerLocation))
  const itemsPerPage = 10

  const {results: questResults, loadQuests, watchAndReload} = useQuestSearchResults(itemsPerPage, (page) => workmarketApi.getQuestPreset("AVAILABLE", {
    q: normalizeSearchQuery(searchQuery.value),
    audience: toOptionalFilterValue(audienceFilter.value),
    dateFrom: dateFrom.value || null,
    dateTo: dateTo.value || null,
    viewerTimeZone,
    viewerTimezoneOffsetMinutes,
    withImages: photoOnly.value || null,
    radiusKm: nearbyOnly.value ? radiusKm.value : null,
    sort: sortMode.value,
    page,
    size: itemsPerPage
  }))

  const pagedQuests = computed<Quest[]>(() => {
    if (!flexibleOnly.value) {
      return questResults.items.value
    }

    return questResults.items.value.filter((quest) => quest.scheduledAt === null)
  })
  const totalItems = questResults.totalItems
  const totalPages = questResults.totalPages
  const currentPage = questResults.currentPage
  const isLoading = questResults.isLoading
  const pageStart = questResults.pageStart
  const pageEnd = questResults.pageEnd
  const hasPreviousPage = questResults.hasPreviousPage
  const hasNextPage = questResults.hasNextPage

  watchAndReload([searchQuery, sortMode, photoOnly, audienceFilter, dateFrom, dateTo, flexibleOnly, nearbyOnly, radiusKm])

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
    searchQuery,
    sortMode,
    photoOnly,
    flexibleOnly,
    nearbyOnly,
    audienceFilter,
    dateFrom,
    dateTo,
    hasViewerLocation,
    radiusKm,
    pagedQuests,
    totalItems,
    totalPages,
    currentPage,
    isLoading,
    pageStart,
    pageEnd,
    hasPreviousPage,
    hasNextPage,
    previousPage,
    nextPage,
    openQuest
  }
}
