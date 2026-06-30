import {getApiErrorMessage} from "../../../../api/apiErrors.ts"
import {workmarketApi} from "../../../workmarket/api/workmarketApi.ts"
import type {CircleCandidate, CircleCandidateListResponse, CircleContactListResponse, CircleGroup, CircleRequestListResponse} from "../../../../contracts/index.ts"

type CirclesDataLoaderState = {
  pageSize: number
  normalizedDirectoryQuery: {value: string}
  normalizedDiscoverQuery: {value: string}
  discoverHasQuery: {value: boolean}
  activeCircleFilter: {value: number | "all" | "unassigned"}
  connectionsPage: {value: number}
  incomingPage: {value: number}
  outgoingPage: {value: number}
  blockedPage: {value: number}
  nearbyPage: {value: number}
  nearbyRadiusKm: {value: number}
  circles: {value: CircleGroup[]}
  inviteCandidates: {value: CircleCandidate[]}
  searchResults: {value: CircleCandidate[]}
  blockedResults: {value: CircleCandidate[]}
  nearbyResults: {value: CircleCandidate[]}
  connectionsPageData: {value: CircleContactListResponse | null}
  incomingPageData: {value: CircleRequestListResponse | null}
  outgoingPageData: {value: CircleRequestListResponse | null}
  blockedPageData: {value: CircleCandidateListResponse | null}
  nearbyPageData: {value: CircleCandidateListResponse | null}
  overviewConnectionCount: {value: number}
  overviewUnassignedConnectionCount: {value: number}
  overviewIncomingRequestCount: {value: number}
  overviewOutgoingRequestCount: {value: number}
  isSearching: {value: boolean}
  error: {value: string}
}

export const useCirclesDataLoader = (state: CirclesDataLoaderState) => {
  const loadOverview = async () => {
    try {
      const overview = await workmarketApi.getCircleOverview()
      state.overviewConnectionCount.value = overview.connectionCount
      state.overviewUnassignedConnectionCount.value = overview.unassignedConnectionCount
      state.overviewIncomingRequestCount.value = overview.incomingRequestCount
      state.overviewOutgoingRequestCount.value = overview.outgoingRequestCount
    } catch (requestError) {
      state.error.value = getApiErrorMessage(requestError, "Could not load circles.")
    }
  }

  const loadCircles = async () => {
    try {
      state.circles.value = await workmarketApi.getCircleGroups()
    } catch (requestError) {
      state.error.value = getApiErrorMessage(requestError, "Could not load circles.")
    }
  }

  const loadConnectionsPage = async () => {
    const query = state.normalizedDirectoryQuery.value.length >= 2 ? state.normalizedDirectoryQuery.value : null
    const circleId = state.activeCircleFilter.value === "all" || state.activeCircleFilter.value === "unassigned"
      ? null
      : state.activeCircleFilter.value
    const unassigned = state.activeCircleFilter.value === "unassigned"

    try {
      state.connectionsPageData.value = await workmarketApi.getCircleConnectionsPage({
        q: query,
        circleId,
        unassigned,
        page: state.connectionsPage.value - 1,
        size: state.pageSize
      })
    } catch (requestError) {
      state.error.value = getApiErrorMessage(requestError, "Could not load connections.")
    }
  }

  const loadInboxPage = async () => {
    const query = state.normalizedDirectoryQuery.value.length >= 2 ? state.normalizedDirectoryQuery.value : null

    try {
      const [incomingResponse, outgoingResponse] = await Promise.all([
        workmarketApi.getIncomingCircleRequestsPage({
          q: query,
          page: state.incomingPage.value - 1,
          size: state.pageSize
        }),
        workmarketApi.getOutgoingCircleRequestsPage({
          q: query,
          page: state.outgoingPage.value - 1,
          size: state.pageSize
        })
      ])

      state.incomingPageData.value = incomingResponse
      state.outgoingPageData.value = outgoingResponse
    } catch (requestError) {
      state.error.value = getApiErrorMessage(requestError, "Could not load requests.")
    }
  }

  const loadBlockedPage = async () => {
    const query = state.normalizedDirectoryQuery.value.length >= 2 ? state.normalizedDirectoryQuery.value : null

    try {
      state.blockedPageData.value = await workmarketApi.getBlockedCircleUsersPage({
        q: query,
        page: state.blockedPage.value - 1,
        size: state.pageSize
      })
    } catch (requestError) {
      state.error.value = getApiErrorMessage(requestError, "Could not load blocked users.")
    }
  }

  const loadSuggestions = async () => {
    state.isSearching.value = true
    state.error.value = ""

    try {
      if (state.discoverHasQuery.value) {
        state.searchResults.value = (await workmarketApi.searchCircleUsersPage({
          q: state.normalizedDiscoverQuery.value,
          page: 0,
          size: 12
        })).items
        return
      }

      state.inviteCandidates.value = (await workmarketApi.getInviteCandidatesPage({
        page: 0,
        size: 12
      })).items
      state.searchResults.value = []
    } catch (requestError) {
      state.error.value = getApiErrorMessage(requestError, "Could not search users.")
      state.searchResults.value = []
      state.inviteCandidates.value = []
    } finally {
      state.isSearching.value = false
    }
  }

  const loadNearbyPage = async () => {
    try {
      state.nearbyPageData.value = await workmarketApi.getNearbyCircleUsersPage({
        radiusKm: state.nearbyRadiusKm.value,
        page: state.nearbyPage.value - 1,
        size: state.pageSize
      })
    } catch (requestError) {
      state.error.value = getApiErrorMessage(requestError, "Could not load nearby people.")
    }
  }

  const refreshCircleData = async () => {
    await Promise.all([
      loadConnectionsPage(),
      loadInboxPage(),
      loadBlockedPage(),
      loadSuggestions(),
      loadNearbyPage()
    ])
  }

  const refreshOverviewAndData = async () => {
    await Promise.all([loadOverview(), refreshCircleData()])
  }

  const refreshWorkspace = async () => {
    await Promise.all([loadOverview(), loadCircles(), refreshCircleData()])
  }

  return {
    loadOverview,
    loadCircles,
    loadConnectionsPage,
    loadInboxPage,
    loadBlockedPage,
    loadSuggestions,
    loadNearbyPage,
    refreshCircleData,
    refreshOverviewAndData,
    refreshWorkspace
  }
}
