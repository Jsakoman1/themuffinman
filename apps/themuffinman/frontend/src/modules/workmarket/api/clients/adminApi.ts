import {api, withAuth} from "../../../../api/httpClient.ts"
import type {
  ActionResult,
  AdminAgentSimulationRequest,
  AdminAgentSimulationResponse,
  AdminAgentPlaygroundRequest,
  AdminAgentPlaygroundResponse,
  AdminApplicationsQuery,
  AdminCircleOverview,
  AdminUserDetail,
  LocationDebugStatus,
  QuestApplicationListResponse
} from "../contracts.ts"
import {buildQueryParams} from "../shared/queryParams.ts"

export const adminApi = {
  async getAdminUserDetail(id: number): Promise<AdminUserDetail> {
    return (await api.get<AdminUserDetail>(`/app_users/${id}/admin-detail`, withAuth())).data
  },

  async getAdminApplications(params: AdminApplicationsQuery): Promise<QuestApplicationListResponse> {
    return (await api.get<QuestApplicationListResponse>("/admin/applications", {
      ...withAuth(),
      params: buildQueryParams({
        ...params,
        status: params.status === "ALL" ? undefined : params.status
      })
    })).data
  },

  async getAdminCircleOverview(query?: string): Promise<AdminCircleOverview> {
    return (await api.get<AdminCircleOverview>("/circles/admin/overview", {
      ...withAuth(),
      params: buildQueryParams({
        q: query
      })
    })).data
  },

  async deleteAdminCircle(id: number): Promise<ActionResult> {
    return (await api.delete<ActionResult>(`/circles/admin/groups/${id}`, withAuth())).data
  },

  async deleteAdminCircleRequest(id: number): Promise<ActionResult> {
    return (await api.delete<ActionResult>(`/circles/requests/${id}`, withAuth())).data
  },

  async getAdminLocationStatus(): Promise<LocationDebugStatus> {
    return (await api.get<LocationDebugStatus>("/location/admin/status", withAuth())).data
  },

  async runAdminAgentPrompt(dto: AdminAgentPlaygroundRequest): Promise<AdminAgentPlaygroundResponse> {
    return (await api.post<AdminAgentPlaygroundResponse>("/admin/agent/playground", dto, withAuth())).data
  },

  async runAdminAgentSimulation(dto: AdminAgentSimulationRequest): Promise<AdminAgentSimulationResponse> {
    return (await api.post<AdminAgentSimulationResponse>("/admin/agent/simulate", dto, withAuth())).data
  }
}
