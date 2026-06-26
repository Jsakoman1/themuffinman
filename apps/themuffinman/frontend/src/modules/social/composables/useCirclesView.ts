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
    blockedResults,
    nearbyResults,
    circles,
    connectionsPageData,
    incomingPageData,
    outgoingPageData,
    blockedPageData,
    nearbyPageData,
    selectedCircleIdsByUserId,
    overviewConnectionCount,
    overviewUnassignedConnectionCount,
    overviewIncomingRequestCount,
    overviewOutgoingRequestCount,
    directoryQuery,
    discoverQuery,
    newCircleName,
    activeCircleFilter,
    inboxTab,
    connectionsPage,
    incomingPage,
    outgoingPage,
    blockedPage,
    nearbyPage,
    nearbyRadiusKm,
    isLoading,
    isSearching,
    isSaving,
    error,
    circleBanner,
    message,
    messageTone,
    normalizedDirectoryQuery,
    normalizedDiscoverQuery,
    discoverHasQuery
  } = state
  const viewState = createCirclesViewState({
    circles,
    inviteCandidates,
    searchResults,
    blockedResults,
    nearbyResults,
    connectionsPageData,
    incomingPageData,
    outgoingPageData,
    blockedPageData,
    nearbyPageData,
    overviewConnectionCount,
    overviewIncomingRequestCount,
    overviewOutgoingRequestCount,
    activeCircleFilter,
    inboxTab,
    discoverHasQuery,
    incomingPage,
    outgoingPage,
    blockedPage,
    nearbyPage
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
    blockedItems,
    blockedPages,
    blockedTotalItems,
    nearbyItems,
    nearbyPages,
    nearbyTotalItems,
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
    loadBlockedPage,
    loadSuggestions,
    loadNearbyPage,
    refreshOverviewAndData,
    refreshWorkspace
  } = useCirclesDataLoader({
    pageSize,
    normalizedDirectoryQuery,
    normalizedDiscoverQuery,
    discoverHasQuery,
    activeCircleFilter,
    connectionsPage,
    incomingPage,
    outgoingPage,
    blockedPage,
    nearbyPage,
    nearbyRadiusKm,
    circles,
    inviteCandidates,
    searchResults,
    blockedResults,
    nearbyResults,
    connectionsPageData,
    incomingPageData,
    outgoingPageData,
    blockedPageData,
    nearbyPageData,
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
    renameCircle,
    saveConnectionCircles,
    bulkUpdateConnections,
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

  const loadDirectoryResults = () => {
    connectionsPage.value = 1
    incomingPage.value = 1
    outgoingPage.value = 1
    blockedPage.value = 1
    void loadConnectionsPage()
    void loadInboxPage()
    void loadBlockedPage()
  }

  const loadDiscoverResults = (query: string) => {
    if (normalizeSearchQuery(query).length >= 2) {
      void loadSuggestions()
      return
    }

    void loadSuggestions()
  }

  useDebouncedWatch(directoryQuery, loadDirectoryResults, 250)
  useDebouncedWatch(discoverQuery, loadDiscoverResults, 250)

  watch(activeCircleFilter, () => {
    connectionsPage.value = 1
    void loadConnectionsPage()
  })

  watch(nearbyRadiusKm, () => {
    nearbyPage.value = 1
    void loadNearbyPage()
  })

  const {
    previousIncomingPage,
    nextIncomingPage,
    previousOutgoingPage,
    nextOutgoingPage,
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

  const previousBlockedPage = () => {
    if (blockedPage.value <= 1) {
      return
    }

    blockedPage.value -= 1
    void loadBlockedPage()
  }

  const nextBlockedPage = () => {
    if (blockedPage.value >= blockedPages.value) {
      return
    }

    blockedPage.value += 1
    void loadBlockedPage()
  }

  const previousNearbyPage = () => {
    if (nearbyPage.value <= 1) {
      return
    }

    nearbyPage.value -= 1
    void loadNearbyPage()
  }

  const nextNearbyPage = () => {
    if (nearbyPage.value >= nearbyPages.value) {
      return
    }

    nearbyPage.value += 1
    void loadNearbyPage()
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
    blockedPageData,
    nearbyPageData,
    selectedCircleIdsByUserId,
    overviewConnectionCount,
    overviewUnassignedConnectionCount,
    overviewIncomingRequestCount,
    overviewOutgoingRequestCount,
    directoryQuery,
    discoverQuery,
    newCircleName,
    activeCircleFilter,
    inboxTab,
    connectionsPage,
    incomingPage,
    outgoingPage,
    blockedPage,
    nearbyPage,
    nearbyRadiusKm,
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
    blockedItems,
    blockedPages,
    blockedTotalItems,
    nearbyItems,
    nearbyPages,
    nearbyTotalItems,
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
    discoverHasQuery,
    getSelectedCircleIds,
    createCircle,
    deleteCircle,
    renameCircle,
    toggleConnectionCircle,
    resetConnectionCircles,
    hasPendingCircleChanges,
    saveConnectionCircles,
    bulkUpdateConnections,
    sendRequest,
    blockUser,
    unblockUser,
    acceptRequest,
    removeRequest,
    previousIncomingPage,
    nextIncomingPage,
    previousOutgoingPage,
    nextOutgoingPage,
    previousInboxPage,
    nextInboxPage,
    previousConnectionsPage,
    nextConnectionsPage,
    previousBlockedPage,
    nextBlockedPage,
    previousNearbyPage,
    nextNearbyPage
  }
}
