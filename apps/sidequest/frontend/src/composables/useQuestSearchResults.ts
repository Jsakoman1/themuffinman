import {watch, type WatchSource} from "vue"
import type {Quest, QuestListResponse} from "../api/sidequestApi.ts"
import {usePaginatedResults} from "./usePaginatedResults.ts"

export const useQuestSearchResults = (
  itemsPerPage: number,
  loadPage: (page: number) => Promise<QuestListResponse>
) => {
  const results = usePaginatedResults<Quest>(itemsPerPage)

  const loadQuests = async (page = 1) => {
    results.isLoading.value = true

    try {
      const response = await loadPage(Math.max(0, page - 1))
      results.applyPage(response)
    } catch {
      results.reset()
    } finally {
      results.isLoading.value = false
    }
  }

  const watchAndReload = (sources: WatchSource[]) => {
    watch(sources, () => {
      void loadQuests(1)
    }, {immediate: true})
  }

  return {
    results,
    loadQuests,
    watchAndReload
  }
}
