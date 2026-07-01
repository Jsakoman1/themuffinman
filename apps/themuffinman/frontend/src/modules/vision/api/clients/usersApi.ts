import {api, withAuth} from "../../../../api/httpClient.ts"
import type {ActionResult, AppUser, AppUserRequest, UserProfileView, VisionOptions} from "../contracts.ts"
import {buildQueryParams} from "../shared/queryParams.ts"

export const usersApi = {
  async getAppUsers(query?: string): Promise<AppUser[]> {
    return (await api.get<AppUser[]>("/app_users", {
      ...withAuth(),
      params: buildQueryParams({
        q: query
      })
    })).data
  },

  async getAppUserOptions(): Promise<VisionOptions> {
    return (await api.get<VisionOptions>("/app_users/options", withAuth())).data
  },

  async getAppUser(id: number): Promise<AppUser> {
    return (await api.get<AppUser>(`/app_users/${id}`, withAuth())).data
  },

  async getCurrentAppUser(): Promise<AppUser> {
    return (await api.get<AppUser>("/app_users/me", withAuth())).data
  },

  async getUserProfileView(id: number): Promise<UserProfileView> {
    return (await api.get<UserProfileView>(`/app_users/${id}/profile-view`, withAuth())).data
  },

  async createAppUser(dto: AppUserRequest): Promise<ActionResult> {
    return (await api.post<ActionResult>("/app_users", dto, withAuth())).data
  },

  async updateAppUser(id: number, dto: AppUserRequest): Promise<ActionResult> {
    return (await api.put<ActionResult>(`/app_users/${id}`, dto, withAuth())).data
  },

  async updateCurrentAppUser(dto: AppUserRequest): Promise<AppUser> {
    return (await api.put<AppUser>("/app_users/me", dto, withAuth())).data
  },

  async deleteAppUser(id: number): Promise<ActionResult> {
    return (await api.delete<ActionResult>(`/app_users/${id}`, withAuth())).data
  }
}
