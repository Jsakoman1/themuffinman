import {api, withAuth} from "../../../../api/httpClient.ts"
import type {ActionResult, QuestNewsItem} from "../contracts.ts"

export const newsApi = {
  async getMyNews(): Promise<QuestNewsItem[]> {
    return (await api.get<QuestNewsItem[]>("/news/me", withAuth())).data
  },

  async getMyNewsUnreadCount(): Promise<number> {
    return (await api.get<number>("/news/me/unread-count", withAuth())).data
  },

  async markMyNewsAsRead(): Promise<ActionResult> {
    return (await api.patch<ActionResult>("/news/me/read", {}, withAuth())).data
  },

  async markMyNewsItemAsRead(id: number): Promise<ActionResult> {
    return (await api.patch<ActionResult>(`/news/me/${id}/read`, {}, withAuth())).data
  }
}
