import {api, withAuth} from "../../../api/httpClient.ts"
import type {
  ActionResultDTO,
  ChatConversationSummary,
  ChatMessage,
  ChatMessageRequest,
  ChatOpenConversationRequest,
  ChatWorkspace
} from "../../../contracts/index.ts"

export const chatApi = {
  async getWorkspace(): Promise<ChatWorkspace> {
    return (await api.get<ChatWorkspace>("/chat/workspace", withAuth())).data
  },

  async openConversation(dto: ChatOpenConversationRequest): Promise<ChatConversationSummary> {
    return (await api.post<ChatConversationSummary>("/chat/conversations/open", dto, withAuth())).data
  },

  async getConversationMessages(conversationId: number): Promise<ChatMessage[]> {
    return (await api.get<ChatMessage[]>(`/chat/conversations/${conversationId}/messages`, withAuth())).data
  },

  async sendMessage(conversationId: number, dto: ChatMessageRequest): Promise<ChatMessage> {
    return (await api.post<ChatMessage>(`/chat/conversations/${conversationId}/messages`, dto, withAuth())).data
  },

  async markConversationRead(conversationId: number): Promise<ActionResultDTO> {
    return (await api.patch<ActionResultDTO>(`/chat/conversations/${conversationId}/read`, {}, withAuth())).data
  },

  async heartbeat(): Promise<ActionResultDTO> {
    return (await api.post<ActionResultDTO>("/chat/presence/heartbeat", {}, withAuth())).data
  }
}
