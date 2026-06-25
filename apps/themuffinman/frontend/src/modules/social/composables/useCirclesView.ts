import {onMounted, watch} from "vue"
import {useQuestDashboard} from "../../workmarket/composables/useQuestDashboard.ts"
import {useDebouncedWatch} from "../../../composables/useDebouncedWatch.ts"
import {normalizeSearchQuery} from "../../../lib/searchQuery.ts"
import {createCircleSelectionState} from "./circles/circleSelection.ts"
import {createCirclesPageState} from "./circles/createCirclesPageState.ts"
import {createCirclesViewState} from "./circles/createCirclesViewState.ts"
import {useCirclesDataLoader} from "./circles/useCirclesDataLoader.ts"
import {useCirclesMutationActions} from "./circles/useCirclesMutationActions.ts"
import {useCirclesPagination} from "./circles/useCirclesPagination.ts"

export const useCirclesView = () => {
  const dashboard = useQuestDashboard()
  const pageSize = 8
  const state = createCirclesPageState()
  const {
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
    circleBanner,
    message,
    messageTone,
    normalizedSearchQuery,
    searchHasQuery
  } = state
  const viewState = createCirclesViewState({
    circles,
    inviteCandidates,
    searchResults,
    connectionsPageData,
    incomingPageData,
    outgoingPageData,
    overviewConnectionCount,
    overviewIncomingRequestCount,
    overviewOutgoingRequestCount,
    activeCircleFilter,
    inboxTab,
    searchHasQuery,
    incomingPage,
    outgoingPage
  })
  const {
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
    currentInboxTotal
  } = viewState

  const showMessage = (text: string, tone: "success" | "warning" = "success") => {
    circleBanner.show(text, tone)
  }

  const {
    loadConnectionsPage,
    loadInboxPage,
    loadSuggestions,
    refreshCircleData,
    refreshOverviewAndData,
    refreshWorkspace
  } = useCirclesDataLoader({
    pageSize,
    normalizedSearchQuery,
    searchHasQuery,
    activeCircleFilter,
    connectionsPage,
    incomingPage,
    outgoingPage,
    circles,
    inviteCandidates,
    searchResults,
    connectionsPageData,
    incomingPageData,
    outgoingPageData,
    overviewConnectionCount,
    overviewUnassignedConnectionCount,
    overviewIncomingRequestCount,
    overviewOutgoingRequestCount,
    isSearching,
    error
  })

  const {
    getSelectedCircleIds,
    toggleConnectionCircle,
    resetConnectionCircles,
    hasPendingCircleChanges
  } = createCircleSelectionState(selectedCircleIdsByUserId)

  const {
    createCircle,
    deleteCircle,
    saveConnectionCircles,
    sendRequest,
    blockUser,
    unblockUser,
    acceptRequest,
    removeRequest
  } = useCirclesMutationActions({
    isSaving,
    showMessage,
    newCircleName,
    activeCircleFilter
  }, {
    refreshWorkspace,
    refreshOverviewAndData,
    getSelectedCircleIds
  })

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

  const {
    previousInboxPage,
    nextInboxPage,
    previousConnectionsPage,
    nextConnectionsPage
  } = useCirclesPagination({
    inboxTab,
    incomingPage,
    outgoingPage,
    incomingPages,
    outgoingPages,
    connectionsPage,
    connectionsPages
  }, {
    loadInboxPage,
    loadConnectionsPage
  })

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
