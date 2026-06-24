import {computed, onMounted, ref, watch} from "vue"
import {
  workmarketApi,
  type ActionResult,
  type CircleCandidate,
  type CircleContact,
  type CircleContactListResponse,
  type CircleGroup,
  type CircleRequestListResponse
} from "../../workmarket/api/workmarketApi.ts"
import {useQuestDashboard} from "../../workmarket/composables/useQuestDashboard.ts"
import {useDebouncedWatch} from "../../../composables/useDebouncedWatch.ts"
import {useTimedBanner} from "../../../composables/useTimedBanner.ts"
import {hasSearchQuery, normalizeSearchQuery} from "../../../lib/searchQuery.ts"
import {getApiErrorMessage} from "../../../api/apiErrors.ts"

export const useCirclesView = () => {
  const dashboard = useQuestDashboard()
  const pageSize = 8

  const inviteCandidates = ref<CircleCandidate[]>([])
  const searchResults = ref<CircleCandidate[]>([])
  const circles = ref<CircleGroup[]>([])
  const connectionsPageData = ref<CircleContactListResponse | null>(null)
  const incomingPageData = ref<CircleRequestListResponse | null>(null)
  const outgoingPageData = ref<CircleRequestListResponse | null>(null)
  const selectedCircleIdsByUserId = ref<Record<number, number[]>>({})
  const overviewConnectionCount = ref(0)
  const overviewUnassignedConnectionCount = ref(0)
  const overviewIncomingRequestCount = ref(0)
  const overviewOutgoingRequestCount = ref(0)
  const searchQuery = ref("")
  const newCircleName = ref("")
  const activeCircleFilter = ref<number | "all" | "unassigned">("all")
  const inboxTab = ref<"incoming" | "outgoing">("incoming")
  const connectionsPage = ref(1)
  const incomingPage = ref(1)
  const outgoingPage = ref(1)
  const isLoading = ref(false)
  const isSearching = ref(false)
  const isSaving = ref(false)
  const error = ref("")
  const circleBanner = useTimedBanner(4000)
  const message = circleBanner.message
  const messageTone = circleBanner.tone

  const normalizedSearchQuery = computed(() => normalizeSearchQuery(searchQuery.value).toLowerCase())
  const searchHasQuery = computed(() => hasSearchQuery(searchQuery.value))
  const circlesCount = computed(() => circles.value.length)
  const connectionsCount = computed(() => overviewConnectionCount.value)
  const incomingCount = computed(() => overviewIncomingRequestCount.value)
  const outgoingCount = computed(() => overviewOutgoingRequestCount.value)
  const suggestions = computed(() => searchHasQuery.value ? searchResults.value : inviteCandidates.value)
  const connectionsItems = computed(() => connectionsPageData.value?.items ?? [])
  const connectionsPages = computed(() => connectionsPageData.value?.totalPages ?? 1)
  const connectionsTotalItems = computed(() => connectionsPageData.value?.totalItems ?? 0)
  const incomingItems = computed(() => incomingPageData.value?.items ?? [])
  const incomingPages = computed(() => incomingPageData.value?.totalPages ?? 1)
  const incomingTotalItems = computed(() => incomingPageData.value?.totalItems ?? 0)
  const outgoingItems = computed(() => outgoingPageData.value?.items ?? [])
  const outgoingPages = computed(() => outgoingPageData.value?.totalPages ?? 1)
  const outgoingTotalItems = computed(() => outgoingPageData.value?.totalItems ?? 0)
  const activeCircleName = computed(() => {
    if (activeCircleFilter.value === "all") {
      return "All connections"
    }

    if (activeCircleFilter.value === "unassigned") {
      return "Unassigned"
    }

    return circles.value.find((circle) => circle.id === activeCircleFilter.value)?.name ?? "Selected circle"
  })
  const currentInboxItems = computed(() => inboxTab.value === "incoming" ? incomingItems.value : outgoingItems.value)
  const currentInboxPage = computed(() => inboxTab.value === "incoming" ? incomingPage.value : outgoingPage.value)
  const currentInboxPages = computed(() => inboxTab.value === "incoming" ? incomingPages.value : outgoingPages.value)
  const currentInboxTotal = computed(() => inboxTab.value === "incoming" ? incomingTotalItems.value : outgoingTotalItems.value)

  const showMessage = (text: string, tone: "success" | "warning" = "success") => {
    circleBanner.show(text, tone)
  }

  const loadOverview = async () => {
    try {
      const overview = await workmarketApi.getCircleOverview()
      overviewConnectionCount.value = overview.connectionCount
      overviewUnassignedConnectionCount.value = overview.unassignedConnectionCount
      overviewIncomingRequestCount.value = overview.incomingRequestCount
      overviewOutgoingRequestCount.value = overview.outgoingRequestCount
    } catch (requestError) {
      error.value = getApiErrorMessage(requestError, "Could not load circles.")
    }
  }

  const loadCircles = async () => {
    try {
      circles.value = await workmarketApi.getCircleGroups()
    } catch (requestError) {
      error.value = getApiErrorMessage(requestError, "Could not load circles.")
    }
  }

  const loadConnectionsPage = async () => {
    const query = searchHasQuery.value ? normalizedSearchQuery.value : undefined
    const circleId = activeCircleFilter.value === "all" || activeCircleFilter.value === "unassigned"
      ? undefined
      : activeCircleFilter.value
    const unassigned = activeCircleFilter.value === "unassigned"

    try {
      connectionsPageData.value = await workmarketApi.getCircleConnectionsPage({
        q: query,
        circleId,
        unassigned,
        page: connectionsPage.value - 1,
        size: pageSize
      })
    } catch (requestError) {
      error.value = getApiErrorMessage(requestError, "Could not load connections.")
    }
  }

  const loadInboxPage = async () => {
    const query = searchHasQuery.value ? normalizedSearchQuery.value : undefined

    try {
      const [incomingResponse, outgoingResponse] = await Promise.all([
        workmarketApi.getIncomingCircleRequestsPage({
          q: query,
          page: incomingPage.value - 1,
          size: pageSize
        }),
        workmarketApi.getOutgoingCircleRequestsPage({
          q: query,
          page: outgoingPage.value - 1,
          size: pageSize
        })
      ])

      incomingPageData.value = incomingResponse
      outgoingPageData.value = outgoingResponse
    } catch (requestError) {
      error.value = getApiErrorMessage(requestError, "Could not load requests.")
    }
  }

  const loadSuggestions = async () => {
    isSearching.value = true
    error.value = ""

    try {
      if (searchHasQuery.value) {
        searchResults.value = (await workmarketApi.searchCircleUsersPage({
          q: normalizedSearchQuery.value,
          page: 0,
          size: 12
        })).items
        return
      }

      inviteCandidates.value = (await workmarketApi.getInviteCandidatesPage({
        page: 0,
        size: 12
      })).items
      searchResults.value = []
    } catch (requestError) {
      error.value = getApiErrorMessage(requestError, "Could not search users.")
      searchResults.value = []
      inviteCandidates.value = []
    } finally {
      isSearching.value = false
    }
  }

  const refreshCircleData = async () => {
    await Promise.all([
      loadConnectionsPage(),
      loadInboxPage(),
      loadSuggestions()
    ])
  }

  const refreshOverviewAndData = async () => {
    await Promise.all([loadOverview(), refreshCircleData()])
  }

  const refreshWorkspace = async () => {
    await Promise.all([loadOverview(), loadCircles(), refreshCircleData()])
  }

  const runCircleMutation = async (
    action: () => Promise<ActionResult>,
    fallbackMessage: string,
    options: {
      tone?: "success" | "warning"
      refresh?: () => Promise<void>
      onSuccess?: () => void
    } = {}
  ) => {
    isSaving.value = true
    try {
      const result = await action()
      options.onSuccess?.()
      showMessage(result.message, options.tone ?? "success")
      await (options.refresh ?? refreshOverviewAndData)()
    } catch (requestError) {
      showMessage(getApiErrorMessage(requestError, fallbackMessage), "warning")
    } finally {
      isSaving.value = false
    }
  }

  const getSelectedCircleIds = (connection: CircleContact) => {
    return selectedCircleIdsByUserId.value[connection.userId] ?? connection.circleIds
  }

  const createCircle = async () => {
    const name = normalizeSearchQuery(newCircleName.value)
    await runCircleMutation(
      () => workmarketApi.createCircle({name}),
      "Could not create circle.",
      {
        refresh: refreshWorkspace,
        onSuccess: () => {
          newCircleName.value = ""
        }
      }
    )
  }

  const deleteCircle = async (circleId: number) => {
    await runCircleMutation(
      () => workmarketApi.deleteCircle(circleId),
      "Could not delete circle.",
      {
        refresh: refreshWorkspace,
        onSuccess: () => {
          if (activeCircleFilter.value === circleId) {
            activeCircleFilter.value = "all"
          }
        }
      }
    )
  }

  const sameIds = (left: number[], right: number[]) => {
    if (left.length !== right.length) {
      return false
    }

    const leftSorted = [...left].sort((a, b) => a - b)
    const rightSorted = [...right].sort((a, b) => a - b)
    return leftSorted.every((value, index) => value === rightSorted[index])
  }

  const toggleConnectionCircle = (connection: CircleContact, circleId: number) => {
    const currentIds = getSelectedCircleIds(connection)
    selectedCircleIdsByUserId.value = {
      ...selectedCircleIdsByUserId.value,
      [connection.userId]: currentIds.includes(circleId)
        ? currentIds.filter((id) => id !== circleId)
        : [...currentIds, circleId]
    }
  }

  const resetConnectionCircles = (connection: CircleContact) => {
    selectedCircleIdsByUserId.value = {
      ...selectedCircleIdsByUserId.value,
      [connection.userId]: [...connection.circleIds]
    }
  }

  const hasPendingCircleChanges = (connection: CircleContact) => {
    return !sameIds(getSelectedCircleIds(connection), connection.circleIds)
  }

  const saveConnectionCircles = async (connection: CircleContact) => {
    const nextCircleIds = getSelectedCircleIds(connection)
    await runCircleMutation(
      () => workmarketApi.updateConnectionCircles(connection.userId, {circleIds: nextCircleIds}),
      "Could not update circles.",
      {refresh: refreshWorkspace}
    )
  }

  const sendRequest = async (id: number) => {
    await runCircleMutation(() => workmarketApi.createCircleRequest({recipientId: id}), "Could not send invite.")
  }

  const blockUser = async (id: number) => {
    await runCircleMutation(() => workmarketApi.blockCircleUser({blockedUserId: id}), "Could not block user.")
  }

  const unblockUser = async (id: number) => {
    await runCircleMutation(() => workmarketApi.unblockCircleUser(id), "Could not unblock user.")
  }

  const acceptRequest = async (requestId: number) => {
    await runCircleMutation(() => workmarketApi.acceptCircleRequest(requestId), "Could not accept invite.")
  }

  const removeRequest = async (requestId: number, tone: "success" | "warning" = "warning") => {
    await runCircleMutation(
      () => workmarketApi.deleteCircleRequest(requestId),
      "Could not update connection.",
      {tone}
    )
  }

  const loadSearchResults = (query: string) => {
    connectionsPage.value = 1
    incomingPage.value = 1
    outgoingPage.value = 1

    if (normalizeSearchQuery(query).length >= 2) {
      void refreshCircleData()
      return
    }

    void loadConnectionsPage()
    void loadInboxPage()
    void loadSuggestions()
  }

  useDebouncedWatch(searchQuery, loadSearchResults, 250)

  watch(activeCircleFilter, () => {
    connectionsPage.value = 1
    void loadConnectionsPage()
  })

  const movePage = (page: {value: number}, totalPages: number, delta: number, reload: () => void) => {
    page.value = Math.min(Math.max(1, page.value + delta), totalPages)
    reload()
  }

  const previousInboxPage = () => {
    if (inboxTab.value === "incoming") {
      movePage(incomingPage, incomingPages.value, -1, () => void loadInboxPage())
      return
    }

    movePage(outgoingPage, outgoingPages.value, -1, () => void loadInboxPage())
  }

  const nextInboxPage = () => {
    if (inboxTab.value === "incoming") {
      movePage(incomingPage, incomingPages.value, 1, () => void loadInboxPage())
      return
    }

    movePage(outgoingPage, outgoingPages.value, 1, () => void loadInboxPage())
  }

  const previousConnectionsPage = () => {
    movePage(connectionsPage, connectionsPages.value, -1, () => void loadConnectionsPage())
  }

  const nextConnectionsPage = () => {
    movePage(connectionsPage, connectionsPages.value, 1, () => void loadConnectionsPage())
  }

  onMounted(() => {
    isLoading.value = true
    error.value = ""
    void refreshWorkspace().finally(() => {
      isLoading.value = false
    })
    void dashboard.init()
  })

  return {
    dashboard,
    inviteCandidates,
    searchResults,
    circles,
    connectionsPageData,
    incomingPageData,
    outgoingPageData,
    selectedCircleIdsByUserId,
    overviewConnectionCount,
    overviewUnassignedConnectionCount,
    overviewIncomingRequestCount,
    overviewOutgoingRequestCount,
    searchQuery,
    newCircleName,
    activeCircleFilter,
    inboxTab,
    connectionsPage,
    incomingPage,
    outgoingPage,
    isLoading,
    isSearching,
    isSaving,
    error,
    message,
    messageTone,
    circlesCount,
    connectionsCount,
    incomingCount,
    outgoingCount,
    suggestions,
    connectionsItems,
    connectionsPages,
    connectionsTotalItems,
    incomingItems,
    incomingPages,
    incomingTotalItems,
    outgoingItems,
    outgoingPages,
    outgoingTotalItems,
    activeCircleName,
    currentInboxItems,
    currentInboxPage,
    currentInboxPages,
    currentInboxTotal,
    searchHasQuery,
    getSelectedCircleIds,
    createCircle,
    deleteCircle,
    toggleConnectionCircle,
    resetConnectionCircles,
    hasPendingCircleChanges,
    saveConnectionCircles,
    sendRequest,
    blockUser,
    unblockUser,
    acceptRequest,
    removeRequest,
    previousInboxPage,
    nextInboxPage,
    previousConnectionsPage,
    nextConnectionsPage
  }
}
