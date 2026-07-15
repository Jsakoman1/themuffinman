import {api, withAuth} from "../../../api/httpClient.ts"
import type {
  AppUserResponseDTO,
  BusinessBookingListResponseDTO,
  BusinessOwnerCalendarProjectionDTO,
  BusinessOwnerDashboardDTO,
  BusinessProfileResponseDTO,
  ChatConversationListDTO,
  ChatConversationSyncDTO,
  ChatMessagePageDTO,
  ChatWorkspaceDTO,
  CircleContactListResponseDTO,
  CircleOverviewDTO,
  CircleRequestListResponseDTO,
  DashboardResponseDTO,
  DashboardSummaryDTO,
  QuestListResponseDTO,
  UserProfileViewDTO
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

  async getMyApplications(page = 0, size = 20): Promise<import("../../../contracts/index.ts").QuestApplicationListResponseDTO> {
    return (await api.get<import("../../../contracts/index.ts").QuestApplicationListResponseDTO>("/quests/applications/me", {
      params: {page, size},
      ...withAuth()
    })).data
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

  async getBusinessDashboard(): Promise<BusinessOwnerDashboardDTO> {
    return (await api.get<BusinessOwnerDashboardDTO>("/business/dashboard/me", withAuth())).data
  },

  async getBusinessProfile(): Promise<BusinessProfileResponseDTO> {
    return (await api.get<BusinessProfileResponseDTO>("/business/profiles/me", withAuth())).data
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

  async getCirclesOverview(): Promise<CircleOverviewDTO> {
    return (await api.get<CircleOverviewDTO>("/circles/me/overview", withAuth())).data
  },

  async getIncomingCircleRequests(): Promise<CircleRequestListResponseDTO> {
    return (await api.get<CircleRequestListResponseDTO>("/circles/requests/incoming", {
      params: {page: 0, size: 50},
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
