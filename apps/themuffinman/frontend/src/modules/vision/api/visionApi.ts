import {api, withAuth} from "../../../api/httpClient.ts"
import {dashboardApi, type DashboardVoiceTranscription} from "../../workmarket/api/clients/dashboardApi.ts"
import type {DashboardVoiceConfig} from "../../workmarket/api/contracts.ts"

export type VisionAgentState = "ASKING" | "REVIEW_READY" | "BLOCKED"
export type VisionNextAction = "ASK_FOR_SLOT" | "SHOW_REVIEW" | "BLOCKED"

export type VisionSlotSummary = {
  slotId: string
  label: string
  value: string
}

export type VisionQuestReview = {
  title: string
  description: string
  rewardLabel: string
  visibility: string
}

export type VisionConversationTurnResponse = {
  conversationId: number
  turnId: number
  intent: string
  agentState: VisionAgentState
  nextAction: VisionNextAction
  message: string
  requestedSlot: string | null
  normalizedPrompt: string
  translationApplied: boolean
  translationReliable: boolean
  executionEnabled: boolean
  slotSummaries: VisionSlotSummary[]
  review: VisionQuestReview | null
}

export const visionApi = {
  async processConversationTurn(prompt: string, conversationId?: number | null, source = "text"): Promise<VisionConversationTurnResponse> {
    const auth = withAuth()
    return (await api.post<VisionConversationTurnResponse>("/vision/conversations/turns", {
      conversationId: conversationId ?? undefined,
      prompt,
      source
    }, auth)).data
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
