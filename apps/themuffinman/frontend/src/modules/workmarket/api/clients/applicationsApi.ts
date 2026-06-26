import {api, withAuth} from "../../../../api/httpClient.ts"
import type {
  ActionResult,
  AdminQuestApplicationUpdateRequest,
  QuestApplication,
  QuestApplicationDetail,
  QuestApplicationRequest,
  QuestApplicationsView,
  UserReview,
  UserReviewRequest
} from "../contracts.ts"

export const applicationsApi = {
  async getQuestApplications(questId: number): Promise<QuestApplication[]> {
    return (await api.get<QuestApplication[]>(`/quests/${questId}/applications`, withAuth())).data
  },

  async getQuestApplicationsView(questId: number, showAll = false): Promise<QuestApplicationsView> {
    return (await api.get<QuestApplicationsView>(`/quests/${questId}/applications/view`, {
      ...withAuth(),
      params: {showAll}
    })).data
  },

  async getMyApplications(): Promise<QuestApplication[]> {
    return (await api.get<QuestApplication[]>("/quests/applications/me", withAuth())).data
  },

  async getApplicationDetail(applicationId: number): Promise<QuestApplicationDetail> {
    return (await api.get<QuestApplicationDetail>(`/applications/${applicationId}/detail`, withAuth())).data
  },

  async applyForQuest(questId: number, dto: QuestApplicationRequest): Promise<ActionResult> {
    return (await api.post<ActionResult>(`/quests/${questId}/applications`, dto, withAuth())).data
  },

  async updateMyApplication(questId: number, dto: QuestApplicationRequest): Promise<ActionResult> {
    return (await api.put<ActionResult>(`/quests/${questId}/applications/me`, dto, withAuth())).data
  },

  async withdrawMyApplication(questId: number): Promise<ActionResult> {
    return (await api.patch<ActionResult>(`/quests/${questId}/applications/me/withdraw`, {}, withAuth())).data
  },

  async approveApplication(questId: number, applicationId: number): Promise<ActionResult> {
    return (await api.patch<ActionResult>(`/quests/${questId}/applications/${applicationId}/approve`, {}, withAuth())).data
  },

  async declineApplication(questId: number, applicationId: number): Promise<ActionResult> {
    return (await api.patch<ActionResult>(`/quests/${questId}/applications/${applicationId}/decline`, {}, withAuth())).data
  },

  async updateAdminApplication(applicationId: number, dto: AdminQuestApplicationUpdateRequest): Promise<ActionResult> {
    return (await api.put<ActionResult>(`/admin/applications/${applicationId}`, dto, withAuth())).data
  },

  async deleteAdminApplication(applicationId: number): Promise<ActionResult> {
    return (await api.delete<ActionResult>(`/admin/applications/${applicationId}`, withAuth())).data
  },

  async createQuestReview(questId: number, dto: UserReviewRequest): Promise<UserReview> {
    return (await api.post<UserReview>(`/quests/${questId}/reviews`, dto, withAuth())).data
  }
}
