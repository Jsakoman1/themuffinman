import {api, withAuth} from "../../../api/httpClient.ts"
import type {
  AppUserResponseDTO,
  BusinessBookingListResponseDTO,
  BusinessOwnerCalendarProjectionDTO,
  BusinessOwnerDashboardDTO,
  BusinessProfileResponseDTO,
  ChatConversationSyncDTO,
  ChatWorkspaceDTO,
  CircleContactListResponseDTO,
  CircleOverviewDTO,
  CircleRequestListResponseDTO,
  DashboardResponseDTO,
  DashboardSummaryDTO,
  UserProfileViewDTO
} from "../../../contracts/index.ts"

export const userShellApi = {
  async getDashboard(): Promise<DashboardResponseDTO> {
    return (await api.get<DashboardResponseDTO>("/dashboard/me", withAuth())).data
  },

  async getDashboardSummary(): Promise<DashboardSummaryDTO> {
    return (await api.get<DashboardSummaryDTO>("/dashboard/me/summary", withAuth())).data
  },

  async getChatWorkspace(): Promise<ChatWorkspaceDTO> {
    return (await api.get<ChatWorkspaceDTO>("/chat/workspace", {params: {conversationLimit: 8}, ...withAuth()})).data
  },

  async getChatConversationSync(conversationId: number): Promise<ChatConversationSyncDTO> {
    return (await api.get<ChatConversationSyncDTO>(`/chat/conversations/${conversationId}/sync`, withAuth())).data
  },

  async getBusinessDashboard(): Promise<BusinessOwnerDashboardDTO> {
    return (await api.get<BusinessOwnerDashboardDTO>("/business/dashboard/me", withAuth())).data
  },

  async getBusinessProfile(): Promise<BusinessProfileResponseDTO> {
    return (await api.get<BusinessProfileResponseDTO>("/business/profiles/me", withAuth())).data
  },

  async getBusinessOwnerBookings(): Promise<BusinessBookingListResponseDTO> {
    return (await api.get<BusinessBookingListResponseDTO>("/business/bookings/owner", {
      params: {page: 0, size: 6},
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
      params: {page: 0, size: 4},
      ...withAuth()
    })).data
  },

  async getCircleConnections(): Promise<CircleContactListResponseDTO> {
    return (await api.get<CircleContactListResponseDTO>("/circles/connections", {
      params: {page: 0, size: 4},
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
