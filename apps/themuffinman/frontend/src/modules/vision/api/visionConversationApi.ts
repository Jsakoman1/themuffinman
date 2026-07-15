import {api, withAuth} from "../../../api/httpClient.ts"
import {dashboardApi, type DashboardVoiceTranscription} from "./clients/dashboardApi.ts"
import type {DashboardVoiceConfig} from "./contracts.ts"

export type VisionAgentState = "ASKING" | "RECOMMENDING" | "REVIEW_READY" | "COMPLETE" | "BLOCKED"
export type VisionNextAction = "ASK_FOR_SLOT" | "SHOW_RESULTS" | "SHOW_REVIEW" | "COMPLETE" | "BLOCKED"
export type VisionCanvasMode = "clarification" | "results" | "review" | "complete" | "blocked"
export type VisionReviewTarget =
  | "TITLE"
  | "DESCRIPTION"
  | "REWARD"
  | "VISIBILITY"
  | "SCHEDULE"
  | "LOCATION"
  | "CIRCLE_NAME"
  | "TARGET_USER"
  | "TARGET_QUEST"
  | "TARGET_CIRCLE"
  | "APPLICATION_MESSAGE"
  | "APPLICATION_PRICE"
  | "PROFILE_USERNAME"
  | "PROFILE_DESCRIPTION"
  | "PROFILE_LOCATION_MODE"
  | "PROFILE_LOCATION"

export type VisionInputType = "text" | "voice"
export type VisionDeviceRole = "desktop" | "mobile" | "watch"
export type VisionAttentionState = "FOCUSED" | "COORDINATING" | "REVIEWING" | "PASSIVE" | "BLOCKED"

export type VisionRuntimeCue = {
  type: string
  message: string
}

export type VisionRuntimeContext = {
  inputType: VisionInputType
  deviceRole: VisionDeviceRole
  attentionState: VisionAttentionState
  sessionAnchor: string
  actionHints: string[]
  audioCue: VisionRuntimeCue | null
  hapticCue: VisionRuntimeCue | null
  consentRequired: boolean
  consentReason: string | null
  resumeAvailable: boolean
  resumeHint: string | null
  watchFriendly: boolean
  presentationArchetype: string
  density: "glance" | "scan" | "inspect"
  primaryActionLabel: string | null
  visibleFields: string[]
}

export type VisionConversationTurnRequest = {
  conversationId?: number | null
  inputType: VisionInputType
  text: string
  clientCapabilities: string[]
  clientStateVersion: string
  clientLocale: string
  clientTimezone: string
  clientDeviceRole: VisionDeviceRole
  clientRequestId?: string | null
  selectedOptionId?: string | null
  fieldValue?: string | null
  confirmation?: boolean | null
  action?: string
  reviewTarget?: VisionReviewTarget | null
}

export type VisionExecutionCandidate = {
  candidateIntent: string
  capabilityId: string
  confidence: number
  reviewReady: boolean
  executionReady: boolean
  confirmationRequired: boolean
  nextRequiredSlot: string | null
  blockingReason: string
  planningNote: string
  summary: string
}

export type VisionQuestDiscoveryItem = {
  questId: number
  rank: number
  title: string
  description: string
  creatorUsername: string
  rewardLabel: string
  statusLabel: string
  locationLabel: string | null
  scheduledAt: string | null
  matchSummary: string
}

export type VisionQuestDiscovery = {
  capabilityId: string
  query: string
  sort: string
  summary: string
  totalItems: number
  hasMore: boolean
  items: VisionQuestDiscoveryItem[]
}

export type VisionSearchDiscoveryItem = {
  entityFamily: string
  capabilityId: string
  targetId: number
  title: string
  summary: string
  matchSummary: string
  resolutionLabel: string
  exactResolutionEligible: boolean
}

export type VisionSearchDiscovery = {
  capabilityId: string
  query: string
  sort: string
  summary: string
  totalItems: number
  hasMore: boolean
  items: VisionSearchDiscoveryItem[]
}

export type VisionSlotSummary = {
  slotId: string
  label: string
  value: string
}

export type VisionOption = {
  id: string
  label: string
  value: string | null
}

export type VisionConversationSummary = {
  conversationId: number
  intent: string
  entityFamily: string | null
  previousEntityFamily: string | null
  topicSwitchHint: string | null
  status: string
  title: string
  subtitle: string
  stageLabel: string
  progressLabel: string
  groupKey: string
  requestedSlot: string | null
  appliedSlotSummaries: VisionSlotSummary[]
  resumable: boolean
  completed: boolean
  stale: boolean
  updatedAt: string
}

export type VisionQuestReview = {
  title: string
  description: string
  rewardLabel: string
  visibility: string
  schedule: string | null
  location: string | null
}

export type VisionMemoryTrail = {
  activeEntityFamily: string | null
  previousEntityFamily: string | null
  topicSwitchHint: string | null
  currentIntent: string | null
  currentRequestedSlot: string | null
  currentStatus: string | null
  sessionSummary: string | null
  lastUserPrompt: string | null
  lastNormalizedPrompt: string | null
  lastAssistantMessage: string | null
  sessionMemorySnapshot: string | null
  openQuestions: string[]
  recentActions: string[]
  recentEntityFamilies: string[]
  recentIntentTypes: string[]
}

export type VisionCanvasBlock = {
  type: "agent_message" | "recognized_prompt" | "field_request" | "result_summary" | "quest_discovery" | "search_discovery" | "review_summary" | "warning" | "success" | "info"
  title: string | null
  body: string | null
  fieldId: string | null
  fieldKind: string | null
  required: boolean
  placeholder: string | null
  options: VisionOption[]
  items: VisionSlotSummary[]
  questDiscovery: VisionQuestDiscovery | null
  searchDiscovery: VisionSearchDiscovery | null
  review: VisionQuestReview | null
  tone: string | null
}

export type VisionConversationTurnResponse = {
  conversationId: number
  turnId: number
  intent: string
  agentState: VisionAgentState
  canvasMode: VisionCanvasMode
  nextAction: VisionNextAction
  message: string
  requestedSlot: string | null
  normalizedPrompt: string
  translationApplied: boolean
  translationReliable: boolean
  executionEnabled: boolean
  runtimeContext: VisionRuntimeContext | null
  executionCandidate: VisionExecutionCandidate | null
  questDiscovery: VisionQuestDiscovery | null
  memoryTrail: VisionMemoryTrail | null
  blocks: VisionCanvasBlock[]
  appliedSlotSummaries: VisionSlotSummary[]
  slotSummaries: VisionSlotSummary[]
  review: VisionQuestReview | null
  recentConversations: VisionConversationSummary[]
}

export type VisionConversationListResponse = {
  items: VisionConversationSummary[]
}

export const visionConversationApi = {
  async processConversationTurn(request: VisionConversationTurnRequest): Promise<VisionConversationTurnResponse> {
    const auth = withAuth()
    return (await api.post<VisionConversationTurnResponse>("/vision/conversations/turns", {
      conversationId: request.conversationId ?? undefined,
      prompt: request.text,
      text: request.text,
      source: request.inputType,
      inputType: request.inputType,
      clientCapabilities: request.clientCapabilities,
      clientStateVersion: request.clientStateVersion,
      clientLocale: request.clientLocale,
      clientTimezone: request.clientTimezone,
      clientDeviceRole: request.clientDeviceRole,
      clientRequestId: request.clientRequestId ?? undefined,
      selectedOptionId: request.selectedOptionId ?? undefined,
      fieldValue: request.fieldValue ?? undefined,
      confirmation: request.confirmation ?? undefined,
      action: request.action ?? "SUBMIT_PROMPT",
      reviewTarget: request.reviewTarget ?? undefined
    }, auth)).data
  },

  async resetConversation(conversationId: number): Promise<VisionConversationTurnResponse> {
    const auth = withAuth()
    return (await api.post<VisionConversationTurnResponse>(`/vision/conversations/${conversationId}/reset`, {}, auth)).data
  },

  async cancelConversation(conversationId: number): Promise<VisionConversationTurnResponse> {
    const auth = withAuth()
    return (await api.post<VisionConversationTurnResponse>(`/vision/conversations/${conversationId}/cancel`, {}, auth)).data
  },

  async getRecentConversations(): Promise<VisionConversationListResponse> {
    const auth = withAuth()
    return (await api.get<VisionConversationListResponse>("/vision/conversations/recent", auth)).data
  },

  async getConversation(conversationId: number): Promise<VisionConversationTurnResponse> {
    const auth = withAuth()
    return (await api.get<VisionConversationTurnResponse>(`/vision/conversations/${conversationId}`, auth)).data
  },

  async getVoiceConfig(): Promise<DashboardVoiceConfig> {
    return dashboardApi.getDashboardVoiceConfig()
  },

  async transcribeVoiceAudio(audio: Blob): Promise<DashboardVoiceTranscription> {
    return dashboardApi.transcribeVoiceAudio(audio)
  },

  async speakVoiceText(text: string): Promise<ArrayBuffer> {
    return dashboardApi.speakVoiceText(text)
  }
}
