import {api, withAuth} from "../../../api/httpClient.ts"
import type {
  AppUserResponseDTO,
  ActionResultDTO,
  BusinessBookingListResponseDTO,
  BusinessBookingResponseDTO,
  BusinessOwnerCalendarProjectionDTO,
  BusinessOwnerDashboardDTO,
  BusinessOfferingListResponseDTO,
  BusinessOfferingRequestDTO,
  BusinessOfferingResponseDTO,
  BusinessAvailabilityRuleListResponseDTO,
  BusinessAvailabilityRuleRequestDTO,
  BusinessAvailabilityRuleResponseDTO,
  BusinessAvailabilityExceptionListResponseDTO,
  BusinessAvailabilityExceptionRequestDTO,
  BusinessAvailabilityExceptionResponseDTO,
  BusinessBookingRequestDTO,
  BusinessPublicPageDTO,
  BusinessProfileResponseDTO,
  BusinessProfileRequestDTO,
  ChatConversationListDTO,
  ChatConversationSyncDTO,
  ChatMessagePageDTO,
  ChatMessageDTO,
  ChatWorkspaceDTO,
  CircleContactListResponseDTO,
  CircleOverviewDTO,
  CircleGroupResponseDTO,
  CircleGroupRequestDTO,
  CircleSearchResultListResponseDTO,
  CircleRequestListResponseDTO,
  DashboardResponseDTO,
  DashboardSummaryDTO,
  QuestListResponseDTO,
  QuestDetailResponseDTO,
  QuestRequestDTO,
  UserProfileViewDTO,
  UserReviewRequestDTO,
  QuestNewsItemResponseDTO,
} from "../../../contracts/index.ts"

export const userShellApi = {
  async getDashboard(): Promise<DashboardResponseDTO> {
    return (await api.get<DashboardResponseDTO>("/dashboard/me", withAuth())).data
  },

  async getDashboardSummary(): Promise<DashboardSummaryDTO> {
    return (await api.get<DashboardSummaryDTO>("/dashboard/me/summary", withAuth())).data
  },

  async searchQuests(query: {
    q?: string
    preset?: "AVAILABLE" | "MY_ACTIVE"
    sort?: string
    page?: number
    size?: number
    scheduledOnly?: boolean
    signal?: AbortSignal
  } = {}): Promise<QuestListResponseDTO> {
    const params = {
      q: query.q || undefined,
      sort: query.sort || undefined,
      page: query.page ?? 0,
      size: query.size ?? 12,
      scheduledOnly: query.scheduledOnly || undefined
    }
    const path = query.preset ? `/quests/presets/${query.preset}` : "/quests/search"
    return (await api.get<QuestListResponseDTO>(path, {params, signal: query.signal, ...withAuth()})).data
  },

  async getQuestDetail(questId: number): Promise<QuestDetailResponseDTO> {
    return (await api.get<QuestDetailResponseDTO>(`/quests/${questId}/detail`, withAuth())).data
  },

  async getQuestApplications(questId: number, showAll = false): Promise<import("../../../contracts/index.ts").QuestApplicationsViewDTO> {
    return (await api.get<import("../../../contracts/index.ts").QuestApplicationsViewDTO>(`/quests/${questId}/applications/view`, {params: {showAll}, ...withAuth()})).data
  },

  async decideQuestApplication(questId: number, applicationId: number, decision: "approve" | "decline"): Promise<ActionResultDTO> {
    return (await api.patch<ActionResultDTO>(`/quests/${questId}/applications/${applicationId}/${decision}`, undefined, withAuth())).data
  },

  async createQuest(request: QuestRequestDTO): Promise<ActionResultDTO> {
    return (await api.post<ActionResultDTO>("/quests", request, withAuth())).data
  },

  async updateQuest(questId: number, request: QuestRequestDTO): Promise<ActionResultDTO> {
    return (await api.put<ActionResultDTO>(`/quests/${questId}`, request, withAuth())).data
  },

  async executeQuestAction(questId: number, action: "START" | "COMPLETE" | "DELETE"): Promise<ActionResultDTO> {
    const paths = {START: `/quests/${questId}/start`, COMPLETE: `/quests/${questId}/complete`, DELETE: `/quests/${questId}`}
    if (action === "DELETE") return (await api.delete<ActionResultDTO>(paths[action], withAuth())).data
    return (await api.patch<ActionResultDTO>(paths[action], undefined, withAuth())).data
  },

  async decideQuestTerm(questId: number, decision: "confirm" | "reject"): Promise<import("../../../contracts/index.ts").QuestResponseDTO> {
    return (await api.patch<import("../../../contracts/index.ts").QuestResponseDTO>(`/quests/${questId}/term/${decision}`, undefined, withAuth())).data
  },

  async submitQuestReview(questId: number, request: UserReviewRequestDTO): Promise<import("../../../contracts/index.ts").UserReviewResponseDTO> {
    return (await api.post<import("../../../contracts/index.ts").UserReviewResponseDTO>(`/quests/${questId}/reviews`, request, withAuth())).data
  },

  async updateMyApplication(questId: number, request: import("../../../contracts/index.ts").QuestApplicationRequestDTO): Promise<ActionResultDTO> {
    return (await api.put<ActionResultDTO>(`/quests/${questId}/applications/me`, request, withAuth())).data
  },

  async withdrawMyApplication(questId: number): Promise<ActionResultDTO> {
    return (await api.patch<ActionResultDTO>(`/quests/${questId}/applications/me/withdraw`, undefined, withAuth())).data
  },

  async getMyApplications(page = 0, size = 20): Promise<import("../../../contracts/index.ts").QuestApplicationListResponseDTO> {
    return (await api.get<import("../../../contracts/index.ts").QuestApplicationListResponseDTO>("/quests/applications/me", {
      params: {page, size},
      ...withAuth()
    })).data
  },

  async getMyNews(): Promise<QuestNewsItemResponseDTO[]> {
    return (await api.get<QuestNewsItemResponseDTO[]>("/news/me", withAuth())).data
  },

  async markNewsAsRead(): Promise<ActionResultDTO> {
    return (await api.patch<ActionResultDTO>("/news/me/read", undefined, withAuth())).data
  },

  async markNewsItemAsRead(newsId: number): Promise<ActionResultDTO> {
    return (await api.patch<ActionResultDTO>(`/news/me/${newsId}/read`, undefined, withAuth())).data
  },

  async getChatWorkspace(): Promise<ChatWorkspaceDTO> {
    return (await api.get<ChatWorkspaceDTO>("/chat/workspace", {params: {conversationLimit: 50}, ...withAuth()})).data
  },

  async getChatConversations(page = 0, limit = 20, beforeLastMessageAt?: string | null, beforeConversationId?: number | null): Promise<ChatConversationListDTO> {
    return (await api.get<ChatConversationListDTO>("/chat/conversations", {
      params: {page, limit, beforeLastMessageAt: beforeLastMessageAt || undefined, beforeConversationId: beforeConversationId || undefined},
      ...withAuth()
    })).data
  },

  async getChatConversationSync(conversationId: number): Promise<ChatConversationSyncDTO> {
    return (await api.get<ChatConversationSyncDTO>(`/chat/conversations/${conversationId}/sync`, {params: {limit: 50}, ...withAuth()})).data
  },

  async getChatMessages(conversationId: number, limit = 30, beforeMessageId?: number | null): Promise<ChatMessagePageDTO> {
    return (await api.get<ChatMessagePageDTO>(`/chat/conversations/${conversationId}/messages`, {
      params: {limit, beforeMessageId: beforeMessageId || undefined},
      ...withAuth()
    })).data
  },

  async sendChatMessage(conversationId: number, messageBody: string): Promise<ChatMessageDTO> {
    return (await api.post<ChatMessageDTO>(`/chat/conversations/${conversationId}/messages`, {messageBody, clientMessageId: crypto.randomUUID()}, withAuth())).data
  },

  async updateChatMessage(conversationId: number, messageId: number, messageBody: string): Promise<ChatMessageDTO> {
    return (await api.patch<ChatMessageDTO>(`/chat/conversations/${conversationId}/messages/${messageId}`, {messageBody}, withAuth())).data
  },

  async deleteChatMessage(conversationId: number, messageId: number): Promise<ActionResultDTO> {
    return (await api.delete<ActionResultDTO>(`/chat/conversations/${conversationId}/messages/${messageId}`, withAuth())).data
  },

  async addChatReaction(conversationId: number, messageId: number, emoji: string): Promise<ChatMessageDTO> {
    return (await api.post<ChatMessageDTO>(`/chat/conversations/${conversationId}/messages/${messageId}/reactions`, {emoji}, withAuth())).data
  },

  async removeChatReaction(conversationId: number, messageId: number, emoji: string): Promise<ChatMessageDTO> {
    return (await api.delete<ChatMessageDTO>(`/chat/conversations/${conversationId}/messages/${messageId}/reactions/${encodeURIComponent(emoji)}`, withAuth())).data
  },

  async getBusinessDashboard(): Promise<BusinessOwnerDashboardDTO> {
    return (await api.get<BusinessOwnerDashboardDTO>("/business/dashboard/me", withAuth())).data
  },

  async getBusinessProfile(): Promise<BusinessProfileResponseDTO> {
    return (await api.get<BusinessProfileResponseDTO>("/business/profiles/me", withAuth())).data
  },

  async updateBusinessProfile(request: BusinessProfileRequestDTO): Promise<BusinessProfileResponseDTO> {
    return (await api.put<BusinessProfileResponseDTO>("/business/profiles/me", request, withAuth())).data
  },

  async getBusinessOfferings(): Promise<BusinessOfferingListResponseDTO> {
    return (await api.get<BusinessOfferingListResponseDTO>("/business/offerings/me", withAuth())).data
  },

  async createBusinessOffering(request: BusinessOfferingRequestDTO): Promise<BusinessOfferingResponseDTO> {
    return (await api.post<BusinessOfferingResponseDTO>("/business/offerings/me", request, withAuth())).data
  },

  async updateBusinessOffering(offeringId: number, request: BusinessOfferingRequestDTO): Promise<BusinessOfferingResponseDTO> {
    return (await api.put<BusinessOfferingResponseDTO>(`/business/offerings/${offeringId}/me`, request, withAuth())).data
  },

  async deleteBusinessOffering(offeringId: number): Promise<void> {
    await api.delete(`/business/offerings/${offeringId}/me`, withAuth())
  },

  async getBusinessAvailabilityRules(): Promise<BusinessAvailabilityRuleListResponseDTO> {
    return (await api.get<BusinessAvailabilityRuleListResponseDTO>("/business/availability-rules/me", withAuth())).data
  },

  async createBusinessAvailabilityRule(request: BusinessAvailabilityRuleRequestDTO): Promise<BusinessAvailabilityRuleResponseDTO> {
    return (await api.post<BusinessAvailabilityRuleResponseDTO>("/business/availability-rules/me", request, withAuth())).data
  },

  async updateBusinessAvailabilityRule(ruleId: number, request: BusinessAvailabilityRuleRequestDTO): Promise<BusinessAvailabilityRuleResponseDTO> {
    return (await api.put<BusinessAvailabilityRuleResponseDTO>(`/business/availability-rules/${ruleId}/me`, request, withAuth())).data
  },

  async deleteBusinessAvailabilityRule(ruleId: number): Promise<void> {
    await api.delete(`/business/availability-rules/${ruleId}/me`, withAuth())
  },

  async getBusinessAvailabilityExceptions(): Promise<BusinessAvailabilityExceptionListResponseDTO> {
    return (await api.get<BusinessAvailabilityExceptionListResponseDTO>("/business/availability-exceptions/me", withAuth())).data
  },

  async createBusinessAvailabilityException(request: BusinessAvailabilityExceptionRequestDTO): Promise<BusinessAvailabilityExceptionResponseDTO> {
    return (await api.post<BusinessAvailabilityExceptionResponseDTO>("/business/availability-exceptions/me", request, withAuth())).data
  },

  async updateBusinessAvailabilityException(exceptionId: number, request: BusinessAvailabilityExceptionRequestDTO): Promise<BusinessAvailabilityExceptionResponseDTO> {
    return (await api.put<BusinessAvailabilityExceptionResponseDTO>(`/business/availability-exceptions/${exceptionId}/me`, request, withAuth())).data
  },

  async deleteBusinessAvailabilityException(exceptionId: number): Promise<void> {
    await api.delete(`/business/availability-exceptions/${exceptionId}/me`, withAuth())
  },

  async getPublicBusinessPage(slug: string): Promise<BusinessPublicPageDTO> {
    return (await api.get<BusinessPublicPageDTO>(`/business/public/${encodeURIComponent(slug)}`, withAuth())).data
  },

  async createCustomerBooking(request: BusinessBookingRequestDTO): Promise<BusinessBookingResponseDTO> {
    return (await api.post<BusinessBookingResponseDTO>("/business/bookings", request, withAuth())).data
  },

  async getMyBusinessBookings(page = 0, size = 50): Promise<BusinessBookingListResponseDTO> {
    return (await api.get<BusinessBookingListResponseDTO>("/business/bookings/me", {params: {page, size}, ...withAuth()})).data
  },

  async cancelMyBusinessBooking(bookingId: number): Promise<BusinessBookingResponseDTO> {
    return (await api.post<BusinessBookingResponseDTO>(`/business/bookings/me/${bookingId}/cancel`, undefined, withAuth())).data
  },

  async getBusinessOwnerBookings(): Promise<BusinessBookingListResponseDTO> {
    return (await api.get<BusinessBookingListResponseDTO>("/business/bookings/owner", {
      params: {page: 0, size: 50},
      ...withAuth()
    })).data
  },

  async getBusinessOwnerCalendar(): Promise<BusinessOwnerCalendarProjectionDTO> {
    return (await api.get<BusinessOwnerCalendarProjectionDTO>("/business/bookings/owner/calendar", withAuth())).data
  },

  async executeBusinessBookingAction(bookingId: number, action: "confirm" | "reject" | "cancel" | "complete" | "mark-no-show"): Promise<BusinessBookingResponseDTO> {
    return (await api.post<BusinessBookingResponseDTO>(`/business/bookings/owner/${bookingId}/${action}`, undefined, withAuth())).data
  },

  async getCirclesOverview(): Promise<CircleOverviewDTO> {
    return (await api.get<CircleOverviewDTO>("/circles/me/overview", withAuth())).data
  },

  async getCircleGroups(): Promise<CircleGroupResponseDTO[]> {
    return (await api.get<CircleGroupResponseDTO[]>("/circles/groups", withAuth())).data
  },

  async createCircleGroup(request: CircleGroupRequestDTO): Promise<ActionResultDTO> {
    return (await api.post<ActionResultDTO>("/circles/groups", request, withAuth())).data
  },

  async acceptCircleRequest(requestId: number): Promise<ActionResultDTO> {
    return (await api.patch<ActionResultDTO>(`/circles/requests/${requestId}/accept`, undefined, withAuth())).data
  },

  async deleteCircleRequest(requestId: number): Promise<ActionResultDTO> {
    return (await api.delete<ActionResultDTO>(`/circles/requests/${requestId}`, withAuth())).data
  },

  async getOutgoingCircleRequests(page = 0, size = 50): Promise<CircleRequestListResponseDTO> {
    return (await api.get<CircleRequestListResponseDTO>("/circles/requests/outgoing", {params: {page, size}, ...withAuth()})).data
  },

  async searchCircleUsers(q: string): Promise<CircleSearchResultListResponseDTO> {
    return (await api.get<CircleSearchResultListResponseDTO>("/circles/search", {params: {q, page: 0, size: 20}, ...withAuth()})).data
  },

  async blockCircleUser(userId: number): Promise<ActionResultDTO> {
    return (await api.post<ActionResultDTO>("/circles/blocks", {blockedUserId: userId}, withAuth())).data
  },

  async getBlockedCircleUsers(page = 0, size = 50): Promise<CircleSearchResultListResponseDTO> {
    return (await api.get<CircleSearchResultListResponseDTO>("/circles/blocks", {params: {page, size}, ...withAuth()})).data
  },

  async unblockCircleUser(userId: number): Promise<ActionResultDTO> {
    return (await api.delete<ActionResultDTO>(`/circles/blocks/${userId}`, withAuth())).data
  },

  async getIncomingCircleRequests(page = 0, size = 50): Promise<CircleRequestListResponseDTO> {
    return (await api.get<CircleRequestListResponseDTO>("/circles/requests/incoming", {
      params: {page, size},
      ...withAuth()
    })).data
  },

  async getCircleConnections(): Promise<CircleContactListResponseDTO> {
    return (await api.get<CircleContactListResponseDTO>("/circles/connections", {
      params: {page: 0, size: 50},
      ...withAuth()
    })).data
  },

  async getCurrentAppUser(): Promise<AppUserResponseDTO> {
    return (await api.get<AppUserResponseDTO>("/app_users/me", withAuth())).data
  },

  async getCurrentProfileView(userId: number): Promise<UserProfileViewDTO> {
    return (await api.get<UserProfileViewDTO>(`/app_users/${userId}/profile-view`, withAuth())).data
  }
}
