import {api, withAuth} from "../../../../api/httpClient.ts"
import type {ActionResult, Quest, QuestDetail, QuestListPreset, QuestListResponse, QuestRequest, QuestSearchRequest} from "../contracts.ts"
import {buildQueryParams} from "../shared/queryParams.ts"

export const questsApi = {
  async getQuests(): Promise<Quest[]> {
    return (await api.get<Quest[]>("/quests", withAuth())).data
  },

  async searchQuests(params: QuestSearchRequest): Promise<QuestListResponse> {
    return (await api.get<QuestListResponse>("/quests/search", {
      ...withAuth(),
      params: buildQueryParams(params)
    })).data
  },

  async getQuestPreset(preset: QuestListPreset, params: Omit<QuestSearchRequest, "status" | "excludeMine">): Promise<QuestListResponse> {
    return (await api.get<QuestListResponse>(`/quests/presets/${preset}`, {
      ...withAuth(),
      params: buildQueryParams(params)
    })).data
  },

  async getQuest(id: number): Promise<Quest> {
    return (await api.get<Quest>(`/quests/${id}`, withAuth())).data
  },

  async getQuestDetail(id: number): Promise<QuestDetail> {
    return (await api.get<QuestDetail>(`/quests/${id}/detail`, withAuth())).data
  },

  async createQuest(dto: QuestRequest): Promise<ActionResult> {
    return (await api.post<ActionResult>("/quests", dto, withAuth())).data
  },

  async updateQuest(id: number, dto: QuestRequest): Promise<ActionResult> {
    return (await api.put<ActionResult>(`/quests/${id}`, dto, withAuth())).data
  },

  async deleteQuest(id: number): Promise<ActionResult> {
    return (await api.delete<ActionResult>(`/quests/${id}`, withAuth())).data
  },

  async startQuest(id: number): Promise<ActionResult> {
    return (await api.patch<ActionResult>(`/quests/${id}/start`, {}, withAuth())).data
  },

  async completeQuest(id: number): Promise<ActionResult> {
    return (await api.patch<ActionResult>(`/quests/${id}/complete`, {}, withAuth())).data
  },

  async confirmQuestTermChange(id: number): Promise<Quest> {
    return (await api.patch<Quest>(`/quests/${id}/term/confirm`, {}, withAuth())).data
  },

  async rejectQuestTermChange(id: number): Promise<Quest> {
    return (await api.patch<Quest>(`/quests/${id}/term/reject`, {}, withAuth())).data
  }
}
