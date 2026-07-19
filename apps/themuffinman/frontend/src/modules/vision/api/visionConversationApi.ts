import {api, withAuth} from "../../../api/httpClient.ts"
import {dashboardApi, type DashboardVoiceTranscription} from "./clients/dashboardApi.ts"
import type {DashboardVoiceConfig} from "./contracts.ts"
import {
  toVisionConversationListViewModel,
  toVisionConversationViewModel,
  type VisionConversationListTransportResponse,
  type VisionConversationTurnTransportResponse
} from "./visionConversationTransport.ts"

// Backwards-compatible UI imports. The API module only re-exports view models;
// their definitions are isolated from this transport client.
export type {
  VisionAgentState,
  VisionNextAction,
  VisionCanvasMode,
  VisionReviewTarget,
  VisionInputType,
  VisionDeviceRole,
  VisionAttentionState,
  VisionRuntimeCue,
  VisionRuntimeContext,
  VisionConversationTurnRequest,
  VisionExecutionCandidate,
  VisionQuestDiscoveryItem,
  VisionQuestDiscovery,
  VisionSearchDiscoveryItem,
  VisionSearchDiscovery,
  VisionSearchComparisonItem,
  VisionSearchComparison,
  VisionSlotSummary,
  VisionOption,
  VisionConversationSummary,
  VisionQuestReview,
  VisionMemoryTrail,
  VisionCanvasBlock,
  VisionWorkspaceHandoff,
  VisionConversationTurnResponse,
  VisionConversationListResponse
} from "./visionConversationViewModels.ts"
import type {
  VisionConversationListResponse,
  VisionConversationTurnRequest,
  VisionConversationTurnResponse
} from "./visionConversationViewModels.ts"

export const visionConversationApi = {
  async processConversationTurn(request: VisionConversationTurnRequest): Promise<VisionConversationTurnResponse> {
    const response = await api.post<VisionConversationTurnTransportResponse>("/vision/conversations/turns", {
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
      workspaceContext: request.workspaceContext ?? undefined,
      workspaceSource: request.workspaceSource ?? undefined,
      workspaceReturnTo: request.workspaceReturnTo ?? undefined,
      clientRequestId: request.clientRequestId ?? undefined,
      selectedOptionId: request.selectedOptionId ?? undefined,
      fieldValue: request.fieldValue ?? undefined,
      confirmation: request.confirmation ?? undefined,
      action: request.action ?? "SUBMIT_PROMPT",
      reviewTarget: request.reviewTarget ?? undefined
    }, withAuth())
    return toVisionConversationViewModel(response.data)
  },

  async resetConversation(conversationId: number): Promise<VisionConversationTurnResponse> {
    const response = await api.post<VisionConversationTurnTransportResponse>(`/vision/conversations/${conversationId}/reset`, {}, withAuth())
    return toVisionConversationViewModel(response.data)
  },

  async cancelConversation(conversationId: number): Promise<VisionConversationTurnResponse> {
    const response = await api.post<VisionConversationTurnTransportResponse>(`/vision/conversations/${conversationId}/cancel`, {}, withAuth())
    return toVisionConversationViewModel(response.data)
  },

  async getRecentConversations(): Promise<VisionConversationListResponse> {
    const response = await api.get<VisionConversationListTransportResponse>("/vision/conversations/recent", withAuth())
    return toVisionConversationListViewModel(response.data)
  },

  async getConversation(conversationId: number): Promise<VisionConversationTurnResponse> {
    const response = await api.get<VisionConversationTurnTransportResponse>(`/vision/conversations/${conversationId}`, withAuth())
    return toVisionConversationViewModel(response.data)
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
