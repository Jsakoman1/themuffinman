import {computed, ref} from "vue"
import type {
  CircleCandidate,
  CircleCandidateListResponse,
  CircleContactListResponse,
  CircleGroup,
  CircleRequestListResponse
} from "../../../../contracts/index.ts"
import {hasSearchQuery, normalizeSearchQuery} from "../../../../lib/searchQuery.ts"
import {useTimedBanner} from "../../../../composables/useTimedBanner.ts"

export const createCirclesPageState = () => {
  const inviteCandidates = ref<CircleCandidate[]>([])
  const searchResults = ref<CircleCandidate[]>([])
  const blockedResults = ref<CircleCandidate[]>([])
  const nearbyResults = ref<CircleCandidate[]>([])
  const circles = ref<CircleGroup[]>([])
  const connectionsPageData = ref<CircleContactListResponse | null>(null)
  const incomingPageData = ref<CircleRequestListResponse | null>(null)
  const outgoingPageData = ref<CircleRequestListResponse | null>(null)
  const blockedPageData = ref<CircleCandidateListResponse | null>(null)
  const nearbyPageData = ref<CircleCandidateListResponse | null>(null)
  const selectedCircleIdsByUserId = ref<Record<number, number[]>>({})
  const overviewConnectionCount = ref(0)
  const overviewUnassignedConnectionCount = ref(0)
  const overviewIncomingRequestCount = ref(0)
  const overviewOutgoingRequestCount = ref(0)
  const directoryQuery = ref("")
  const discoverQuery = ref("")
  const newCircleName = ref("")
  const activeCircleFilter = ref<number | "all" | "unassigned">("all")
  const inboxTab = ref<"incoming" | "outgoing">("incoming")
  const connectionsPage = ref(1)
  const incomingPage = ref(1)
  const outgoingPage = ref(1)
  const blockedPage = ref(1)
  const nearbyPage = ref(1)
  const nearbyRadiusKm = ref(2)
  const isLoading = ref(false)
  const isSearching = ref(false)
  const isSaving = ref(false)
  const error = ref("")
  const circleBanner = useTimedBanner(4000)
  const message = circleBanner.message
  const messageTone = circleBanner.tone
  const normalizedDirectoryQuery = computed(() => normalizeSearchQuery(directoryQuery.value).toLowerCase())
  const normalizedDiscoverQuery = computed(() => normalizeSearchQuery(discoverQuery.value).toLowerCase())
  const discoverHasQuery = computed(() => hasSearchQuery(discoverQuery.value))

  return {
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
  }
}
