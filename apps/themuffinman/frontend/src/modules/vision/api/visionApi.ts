import {api, withAuth} from "../../../api/httpClient.ts"
import {dashboardApi, type DashboardVoiceTranscription} from "../../workmarket/api/clients/dashboardApi.ts"
import type {DashboardVoiceConfig} from "../../workmarket/api/contracts.ts"

export type VisionAgentState = "ASKING" | "REVIEW_READY" | "COMPLETE" | "BLOCKED"
export type VisionNextAction = "ASK_FOR_SLOT" | "SHOW_REVIEW" | "COMPLETE" | "BLOCKED"
export type VisionCanvasMode = "clarification" | "review" | "complete" | "blocked"

export type VisionSlotSummary = {
  slotId: string
  label: string
  value: string
}

export type VisionOption = {
  id: string
  label: string
}

export type VisionQuestReview = {
  title: string
  description: string
  rewardLabel: string
  visibility: string
}

export type VisionCanvasBlock = {
  type: "agent_message" | "recognized_prompt" | "field_request" | "result_summary" | "review_summary" | "warning" | "success"
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
