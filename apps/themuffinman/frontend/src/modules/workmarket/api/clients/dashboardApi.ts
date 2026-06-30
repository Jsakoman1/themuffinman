import {api, withAuth} from "../../../../api/httpClient.ts"
import type {DashboardResponse, DashboardSummary, DashboardVoiceConfig} from "../contracts.ts"

export const dashboardApi = {
  async getDashboardSummary(): Promise<DashboardSummary> {
    return (await api.get<DashboardSummary>("/dashboard/me/summary", withAuth())).data
  },

  async getDashboard(): Promise<DashboardResponse> {
    return (await api.get<DashboardResponse>("/dashboard/me", withAuth())).data
  },

  async getDashboardVoiceConfig(): Promise<DashboardVoiceConfig> {
    return (await api.get<DashboardVoiceConfig>("/dashboard/me/voice-config", withAuth())).data
  }
}
