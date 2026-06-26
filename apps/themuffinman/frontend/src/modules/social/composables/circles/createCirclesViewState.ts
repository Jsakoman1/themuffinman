import {computed, type Ref} from "vue"
import type {
  CircleCandidate,
  CircleContactListResponse,
  CircleGroup,
  CircleRequestListResponse
} from "../../../workmarket/api/workmarketApi.ts"

export const createCirclesViewState = (state: {
  circles: Ref<CircleGroup[]>
  inviteCandidates: Ref<CircleCandidate[]>
  searchResults: Ref<CircleCandidate[]>
  blockedResults: Ref<CircleCandidate[]>
  nearbyResults: Ref<CircleCandidate[]>
  connectionsPageData: Ref<CircleContactListResponse | null>
  incomingPageData: Ref<CircleRequestListResponse | null>
  outgoingPageData: Ref<CircleRequestListResponse | null>
  blockedPageData: Ref<import("../../../workmarket/api/workmarketApi.ts").CircleCandidateListResponse | null>
  nearbyPageData: Ref<import("../../../workmarket/api/workmarketApi.ts").CircleCandidateListResponse | null>
  overviewConnectionCount: Ref<number>
  overviewIncomingRequestCount: Ref<number>
  overviewOutgoingRequestCount: Ref<number>
  activeCircleFilter: Ref<number | "all" | "unassigned">
  inboxTab: Ref<"incoming" | "outgoing">
  discoverHasQuery: Ref<boolean>
  incomingPage: Ref<number>
  outgoingPage: Ref<number>
  blockedPage: Ref<number>
  nearbyPage: Ref<number>
}) => {
  const circlesCount = computed(() => state.circles.value.length)
  const connectionsCount = computed(() => state.overviewConnectionCount.value)
  const incomingCount = computed(() => state.overviewIncomingRequestCount.value)
  const outgoingCount = computed(() => state.overviewOutgoingRequestCount.value)
  const suggestions = computed(() => state.discoverHasQuery.value ? state.searchResults.value : state.inviteCandidates.value)
  const connectionsItems = computed(() => state.connectionsPageData.value?.items ?? [])
  const connectionsPages = computed(() => state.connectionsPageData.value?.totalPages ?? 1)
  const connectionsTotalItems = computed(() => state.connectionsPageData.value?.totalItems ?? 0)
  const incomingItems = computed(() => state.incomingPageData.value?.items ?? [])
  const incomingPages = computed(() => state.incomingPageData.value?.totalPages ?? 1)
  const incomingTotalItems = computed(() => state.incomingPageData.value?.totalItems ?? 0)
  const outgoingItems = computed(() => state.outgoingPageData.value?.items ?? [])
  const outgoingPages = computed(() => state.outgoingPageData.value?.totalPages ?? 1)
  const outgoingTotalItems = computed(() => state.outgoingPageData.value?.totalItems ?? 0)
  const blockedItems = computed(() => state.blockedPageData.value?.items ?? state.blockedResults.value ?? [])
  const blockedPages = computed(() => state.blockedPageData.value?.totalPages ?? 1)
  const blockedTotalItems = computed(() => state.blockedPageData.value?.totalItems ?? blockedItems.value.length)
  const nearbyItems = computed(() => state.nearbyPageData.value?.items ?? state.nearbyResults.value ?? [])
  const nearbyPages = computed(() => state.nearbyPageData.value?.totalPages ?? 1)
  const nearbyTotalItems = computed(() => state.nearbyPageData.value?.totalItems ?? nearbyItems.value.length)
  const activeCircleName = computed(() => {
    if (state.activeCircleFilter.value === "all") {
      return "All connections"
    }

    if (state.activeCircleFilter.value === "unassigned") {
      return "Unassigned"
    }

    return state.circles.value.find((circle) => circle.id === state.activeCircleFilter.value)?.name ?? "Selected circle"
  })
  const currentInboxItems = computed(() => state.inboxTab.value === "incoming" ? incomingItems.value : outgoingItems.value)
  const currentInboxPage = computed(() => state.inboxTab.value === "incoming" ? state.incomingPage.value : state.outgoingPage.value)
  const currentInboxPages = computed(() => state.inboxTab.value === "incoming" ? incomingPages.value : outgoingPages.value)
  const currentInboxTotal = computed(() => state.inboxTab.value === "incoming" ? incomingTotalItems.value : outgoingTotalItems.value)

  return {
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
  }
}
