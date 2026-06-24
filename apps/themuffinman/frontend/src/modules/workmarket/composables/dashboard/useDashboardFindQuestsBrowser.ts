import {computed, ref} from "vue"
import {useRouter} from "vue-router"
import {workmarketApi, type NavigationTarget} from "../../api/workmarketApi.ts"
import type {QuestAudience} from "../../domain/workmarketDomain.ts"
import {normalizeSearchQuery} from "../../../../lib/searchQuery.ts"
import {useQuestSearchResults} from "../useQuestSearchResults.ts"
import {richTextToPlainText} from "../../../../shared/richText.ts"
import {routeForNavigationTarget} from "../../shared/navigationTargets.ts"
import {ALL_FILTER_VALUE, toOptionalFilterValue} from "../../shared/filterValues.ts"

export const useDashboardFindQuestsBrowser = () => {
  const router = useRouter()
  const searchQuery = ref("")
  const sortMode = ref<"recommended" | "newest" | "highest">("recommended")
  const photoOnly = ref(false)
  const scheduledOnly = ref(false)
  const audienceFilter = ref<QuestAudience | typeof ALL_FILTER_VALUE>(ALL_FILTER_VALUE)
  const dateFrom = ref("")
  const dateTo = ref("")
  const itemsPerPage = 10

  const {results: questResults, loadQuests, watchAndReload} = useQuestSearchResults(itemsPerPage, (page) => workmarketApi.getQuestPreset("AVAILABLE", {
    q: normalizeSearchQuery(searchQuery.value),
    audience: toOptionalFilterValue(audienceFilter.value),
    dateFrom: dateFrom.value || null,
    dateTo: dateTo.value || null,
    withImages: photoOnly.value || undefined,
    scheduledOnly: scheduledOnly.value || undefined,
    sort: sortMode.value,
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

  watchAndReload([searchQuery, sortMode, photoOnly, scheduledOnly, audienceFilter, dateFrom, dateTo])

  const previousPage = () => {
    void questResults.previousPage(loadQuests)
  }

  const nextPage = () => {
    void questResults.nextPage(loadQuests)
  }

  const previewText = (value: string) => {
    return richTextToPlainText(value).replace(/\s+/g, " ").trim()
  }

  const openQuest = async (navigation: NavigationTarget | null) => {
    await router.push(routeForNavigationTarget(navigation))
  }

  return {
    searchQuery,
    sortMode,
    photoOnly,
    scheduledOnly,
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
    previousPage,
    nextPage,
    previewText,
    openQuest
  }
}
