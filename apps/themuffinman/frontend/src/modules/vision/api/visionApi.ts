import {api, withAuth} from "../../../api/httpClient.ts"
import {dashboardApi, type DashboardVoiceTranscription} from "../../workmarket/api/clients/dashboardApi.ts"
import type {DashboardVoiceConfig} from "../../workmarket/api/contracts.ts"

export type VisionAgentState = "ASKING" | "REVIEW_READY" | "COMPLETE" | "BLOCKED"
export type VisionNextAction = "ASK_FOR_SLOT" | "SHOW_REVIEW" | "COMPLETE" | "BLOCKED"
export type VisionCanvasMode = "clarification" | "review" | "complete" | "blocked"
export type VisionReviewTarget = "TITLE" | "DESCRIPTION" | "REWARD" | "VISIBILITY" | "SCHEDULE" | "LOCATION"

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
  status: string
  title: string
  subtitle: string
  stageLabel: string
  progressLabel: string
  groupKey: string
  requestedSlot: string | null
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

export type VisionCanvasBlock = {
  type: "agent_message" | "recognized_prompt" | "field_request" | "result_summary" | "review_summary" | "warning" | "success" | "info"
  title: string | null
  body: string | null
  fieldId: string | null
  fieldKind: string | null
  required: boolean
  placeholder: string | null
  options: VisionOption[]
  items: VisionSlotSummary[]
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
  blocks: VisionCanvasBlock[]
  slotSummaries: VisionSlotSummary[]
  review: VisionQuestReview | null
  recentConversations: VisionConversationSummary[]
}

export type VisionConversationListResponse = {
  items: VisionConversationSummary[]
}

export const visionApi = {
  async processConversationTurn(
    prompt: string,
    conversationId?: number | null,
    source = "text",
    action = "SUBMIT_PROMPT",
    reviewTarget?: VisionReviewTarget | null
  ): Promise<VisionConversationTurnResponse> {
    const auth = withAuth()
    return (await api.post<VisionConversationTurnResponse>("/vision/conversations/turns", {
      conversationId: conversationId ?? undefined,
      prompt,
      source,
      action,
      reviewTarget: reviewTarget ?? undefined
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
