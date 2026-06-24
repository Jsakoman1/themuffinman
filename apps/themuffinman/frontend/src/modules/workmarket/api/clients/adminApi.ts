import {api, withAuth} from "../../../../api/httpClient.ts"
import type {ActionResult, AdminApplicationsQuery, AdminCircleOverview, QuestApplicationListResponse} from "../contracts.ts"
import {buildQueryParams} from "../shared/queryParams.ts"

export const adminApi = {
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
  }
}
