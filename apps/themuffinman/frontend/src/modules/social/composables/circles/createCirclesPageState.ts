import {computed, ref} from "vue"
import type {
  CircleCandidate,
  CircleContactListResponse,
  CircleGroup,
  CircleRequestListResponse
} from "../../../workmarket/api/workmarketApi.ts"
import {hasSearchQuery, normalizeSearchQuery} from "../../../../lib/searchQuery.ts"
import {useTimedBanner} from "../../../../composables/useTimedBanner.ts"

export const createCirclesPageState = () => {
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

  return {
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
  }
}
