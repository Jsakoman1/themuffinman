import {api, withAuth} from "../../../../api/httpClient.ts"
import type {
  ActionResult,
  CircleBlockCreate,
  CircleCandidate,
  CircleCandidateListResponse,
  CircleConnectionsQuery,
  CircleContactListResponse,
  CircleGroup,
  CircleGroupRequest,
  CircleOverview,
  PageQuery,
  CircleRelation,
  CircleRequest,
  CircleRequestCreate,
  CircleRequestListResponse,
  ConnectionCircleUpdateRequest,
  TextPageQuery
} from "../contracts.ts"
import {buildQueryParams} from "../shared/queryParams.ts"

export const circlesApi = {
  async getMyCircles(): Promise<CircleRequest[]> {
    return (await api.get<CircleRequest[]>("/circles", withAuth())).data
  },

  async getCircleOverview(): Promise<CircleOverview> {
    return (await api.get<CircleOverview>("/circles/me/overview", withAuth())).data
  },

  async getCircleGroups(): Promise<CircleGroup[]> {
    return (await api.get<CircleGroup[]>("/circles/groups", withAuth())).data
  },

  async getIncomingCircleRequests(): Promise<CircleRequest[]> {
    return (await api.get<CircleRequestListResponse>("/circles/requests/incoming", withAuth())).data.items
  },

  async getIncomingCircleRequestsPage(params: TextPageQuery): Promise<CircleRequestListResponse> {
    return (await api.get<CircleRequestListResponse>("/circles/requests/incoming", {
      ...withAuth(),
      params: buildQueryParams(params)
    })).data
  },

  async getOutgoingCircleRequests(): Promise<CircleRequest[]> {
    return (await api.get<CircleRequestListResponse>("/circles/requests/outgoing", withAuth())).data.items
  },

  async getOutgoingCircleRequestsPage(params: TextPageQuery): Promise<CircleRequestListResponse> {
    return (await api.get<CircleRequestListResponse>("/circles/requests/outgoing", {
      ...withAuth(),
      params: buildQueryParams(params)
    })).data
  },

  async getInviteCandidates(): Promise<CircleCandidate[]> {
    return (await api.get<CircleCandidateListResponse>("/circles/candidates", withAuth())).data.items
  },

  async getInviteCandidatesPage(params: PageQuery): Promise<CircleCandidateListResponse> {
    return (await api.get<CircleCandidateListResponse>("/circles/candidates", {
      ...withAuth(),
      params: buildQueryParams(params)
    })).data
  },

  async getCircleRelation(userId: number): Promise<CircleRelation> {
    return (await api.get<CircleRelation>(`/circles/relations/${userId}`, withAuth())).data
  },

  async searchCircleUsers(query: string): Promise<CircleCandidate[]> {
    return (await api.get<CircleCandidateListResponse>("/circles/search", {
      ...withAuth(),
      params: {q: query}
    })).data.items
  },

  async searchCircleUsersPage(params: TextPageQuery): Promise<CircleCandidateListResponse> {
    return (await api.get<CircleCandidateListResponse>("/circles/search", {
      ...withAuth(),
      params: buildQueryParams(params)
    })).data
  },

  async getCircleConnectionsPage(params: CircleConnectionsQuery): Promise<CircleContactListResponse> {
    return (await api.get<CircleContactListResponse>("/circles/connections", {
      ...withAuth(),
      params: buildQueryParams(params)
    })).data
  },

  async createCircleRequest(dto: CircleRequestCreate): Promise<ActionResult> {
    return (await api.post<ActionResult>("/circles/requests", dto, withAuth())).data
  },

  async acceptCircleRequest(id: number): Promise<ActionResult> {
    return (await api.patch<ActionResult>(`/circles/requests/${id}/accept`, {}, withAuth())).data
  },

  async deleteCircleRequest(id: number): Promise<ActionResult> {
    return (await api.delete<ActionResult>(`/circles/requests/${id}`, withAuth())).data
  },

  async blockCircleUser(dto: CircleBlockCreate): Promise<ActionResult> {
    return (await api.post<ActionResult>("/circles/blocks", dto, withAuth())).data
  },

  async unblockCircleUser(userId: number): Promise<ActionResult> {
    return (await api.delete<ActionResult>(`/circles/blocks/${userId}`, withAuth())).data
  },

  async createCircle(dto: CircleGroupRequest): Promise<ActionResult> {
    return (await api.post<ActionResult>("/circles/groups", dto, withAuth())).data
  },

  async updateCircle(id: number, dto: CircleGroupRequest): Promise<CircleGroup> {
    return (await api.put<CircleGroup>(`/circles/groups/${id}`, dto, withAuth())).data
  },

  async deleteCircle(id: number): Promise<ActionResult> {
    return (await api.delete<ActionResult>(`/circles/groups/${id}`, withAuth())).data
  },

  async updateConnectionCircles(userId: number, dto: ConnectionCircleUpdateRequest): Promise<ActionResult> {
    return (await api.put<ActionResult>(`/circles/connections/${userId}/circles`, dto, withAuth())).data
  }
}
