import {computed, ref} from "vue"
import {getPaginationState} from "../lib/questListPagination.ts"

type PageResult<T> = {
  items: T[]
  totalItems: number
  totalPages: number
  page: number
}

export const usePaginatedResults = <T>(itemsPerPage: number) => {
  const items = ref<T[]>([])
  const totalItems = ref(0)
  const totalPages = ref(1)
  const currentPage = ref(1)
  const isLoading = ref(false)

  const pagination = computed(() => getPaginationState(totalItems.value, currentPage.value, itemsPerPage))
  const pageStart = computed(() => pagination.value.pageStart)
  const pageEnd = computed(() => pagination.value.pageEnd)
  const hasPreviousPage = computed(() => pagination.value.hasPreviousPage)
  const hasNextPage = computed(() => pagination.value.hasNextPage)

  const applyPage = (result: PageResult<T>) => {
    items.value = result.items
    totalItems.value = result.totalItems
    totalPages.value = result.totalPages
    currentPage.value = result.page + 1
  }

  const reset = () => {
    items.value = []
    totalItems.value = 0
    totalPages.value = 1
    currentPage.value = 1
  }

  const previousPage = async (loadPage: (page: number) => Promise<void>) => {
    if (hasPreviousPage.value) {
      await loadPage(currentPage.value - 1)
    }
  }

  const nextPage = async (loadPage: (page: number) => Promise<void>) => {
    if (hasNextPage.value) {
      await loadPage(currentPage.value + 1)
    }
  }

  return {
    items,
    totalItems,
    totalPages,
    currentPage,
    isLoading,
    pagination,
    pageStart,
    pageEnd,
    hasPreviousPage,
    hasNextPage,
    applyPage,
    reset,
    previousPage,
    nextPage
  }
}
