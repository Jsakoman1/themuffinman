import {api, withAuth} from "../../../api/httpClient.ts"
import {workspaceNavigationApi} from "./workspaceNavigationApi.ts"
import type {
  AppUserResponseDTO,
  AppUserRequestDTO,
  ActionResultDTO,
  BusinessBookingListResponseDTO,
  BusinessBookingResponseDTO,
  BusinessOwnerCalendarProjectionDTO,
  BusinessOwnerDashboardDTO,
  BusinessOfferingListResponseDTO,
  BusinessOfferingRequestDTO,
  BusinessOfferingResponseDTO,
  BusinessAvailabilityRuleListResponseDTO,
  BusinessAvailabilityRuleRequestDTO,
  BusinessAvailabilityRuleResponseDTO,
  BusinessAvailabilityExceptionListResponseDTO,
  BusinessAvailabilityExceptionRequestDTO,
  BusinessAvailabilityExceptionResponseDTO,
  BusinessBookingRequestDTO,
  BusinessPublicPageDTO,
  BusinessProfileResponseDTO,
  BusinessProfileListResponseDTO,
  BusinessProfileRequestDTO,
  BusinessGalleryImageListResponseDTO,
  BusinessGalleryImageRequestDTO,
  RideOfferListResponseDTO,
  RideOfferRequestDTO,
  RideOfferResponseDTO,
  BusinessGalleryImageResponseDTO,
  ChatConversationListDTO,
  ChatConversationSummaryDTO,
  ChatConversationSyncDTO,
  ChatCreateGroupConversationRequestDTO,
  ChatOpenConversationRequestDTO,
  ChatMessagePageDTO,
  ChatMessageDTO,
  ChatMessageRequestDTO,
  ChatAttachmentUploadDTO,
  ChatWorkspaceDTO,
  CircleContactListResponseDTO,
  CircleOverviewDTO,
  CircleGroupResponseDTO,
  CircleGroupRequestDTO,
  CircleSearchResultListResponseDTO,
  CircleRequestListResponseDTO,
  CircleRequestCreateDTO,
  DashboardResponseDTO,
  DashboardSummaryDTO,
  QuestListResponseDTO,
  QuestDetailResponseDTO,
  QuestApplicationDetailResponseDTO,
  QuestRequestDTO,
  QuestApplicationRequestDTO,
  UserProfileViewDTO,
  UserReviewRequestDTO,
  QuestNewsItemResponseDTO,
  LocationLookupResponseDTO,
  LocationLookupCandidateDTO,
  ThingListingListResponseDTO,
  ThingListingRequestDTO,
  ThingListingResponseDTO,
  ThingBorrowRequestResponseDTO,
  NotificationPreferenceResponseDTO,
  NotificationPreferenceUpdateDTO,
  WorkspaceNavigationResponse,
} from "../../../contracts/index.ts"

export type ProfileGalleryImage = {
  id: number
  ownerId: number
  imageUrl: string
  altText: string | null
  sortOrder: number
  active: boolean
  createdAt: string
  updatedAt: string
}

export type ProfileGalleryImageRequest = {
  imageUrl: string
  altText?: string | null
  sortOrder?: number
  active?: boolean
}

export type BusinessFavorite = {
  id: number
  businessProfileId: number
  businessName: string
  slug: string
  bookingEnabled: boolean
  createdAt: string
}

export type CommutePreference = {
  id: number | null
  enabled: boolean
  consentGranted: boolean
  homeArea: string | null
  workArea: string | null
  weekdays: number[]
  departureTime: string | null
  returnTime: string | null
  updatedAt: string | null
}

export type SavedSearchIntent = {id: number; query: string; entityFamily: string | null; paused: boolean; notifyEnabled: boolean; expiresAt: string | null; createdAt: string; updatedAt: string}
export type SafetyReport = {id: number; targetFamily: string; targetId: number | null; reason: string; status: string; createdAt: string}
export type OnboardingProgress = {id: number | null; currentStep: string; skipped: boolean; completed: boolean; updatedAt: string | null}
export type ActivityItem = {kind: string; title: string; summary: string; route: string; primaryActionLabel: string; occurredAt: string; resumeKey: string | null; resumable: boolean}
export type AttentionCenter = {unreadCount: number; items: ActivityItem[]}
export type PersonalShortcut = {targetId: number; targetType: string; title: string; route: string}
export type WorkspaceRailPreference = {railWidthPx: number}
export type AppearancePreference = {theme: "SYSTEM" | "DARK" | "LIGHT"}
export type DisplayDensity = "compact" | "default" | "comfortable"
export type WorkspaceCommandCatalog = import("../../../contracts/index.ts").WorkspaceCommandCatalog
export type WorkspaceCommandGroup = "personal" | "navigation" | "create" | "vision"
export type GuidedIntakeStep = {fieldId: string; inputKind: string; label: string; placeholder: string; choices: string[]; currentValue?: string; valid: boolean; error?: string; nextAction: string; complete: boolean}
export type GuidedIntakeResponse = {flow: string; step: GuidedIntakeStep; draft: Record<string, string>; reviewReady: boolean}

const activeBusinessParams = () => {
  const id = typeof window === "undefined" ? null : window.sessionStorage.getItem("activeBusinessProfileId")
  return id ? {businessProfileId: Number(id)} : {}
}

const activeBusinessProfileId = () => {
  const id = activeBusinessParams().businessProfileId
  return typeof id === "number" && Number.isFinite(id) ? id : null
}

export const setActiveBusinessProfileId = (profileId: number | null) => {
  if (typeof window === "undefined") return
  if (profileId === null) window.sessionStorage.removeItem("activeBusinessProfileId")
  else window.sessionStorage.setItem("activeBusinessProfileId", String(profileId))
}

export const userShellApi = {
  async advanceGuidedIntake(request: {flow: string; draft?: Record<string, string>; fieldId?: string; fieldValue?: string; action?: string}): Promise<GuidedIntakeResponse> {
    return (await api.post<GuidedIntakeResponse>("/guided-intake/step", request, withAuth())).data
  },
  async getDashboard(): Promise<DashboardResponseDTO> {
    return (await api.get<DashboardResponseDTO>("/dashboard/me", withAuth())).data
  },

  async getDashboardSummary(): Promise<DashboardSummaryDTO> {
    return (await api.get<DashboardSummaryDTO>("/dashboard/me/summary", withAuth())).data
  },

  async searchQuests(query: {
    q?: string
    preset?: "AVAILABLE" | "MY_VISIBLE" | "MY_ACTIVE"
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

  async searchUniversal(query: string, page = 0, family?: string): Promise<import("../../../contracts/index.ts").VisionSearchDiscoveryDTO> {
    return (await api.get<import("../../../contracts/index.ts").VisionSearchDiscoveryDTO>("/search", {params: {q: query, page, family}, ...withAuth()})).data
  },
  async compareUniversal(query: string, selections: string[]): Promise<import("../../../contracts/index.ts").VisionSearchComparison> {
    return (await api.get<import("../../../contracts/index.ts").VisionSearchComparison>("/search/compare", {params: {q: query, selection: selections}, ...withAuth()})).data
  },
  async getSavedSearchIntents(): Promise<SavedSearchIntent[]> { return (await api.get<SavedSearchIntent[]>("/search/saved", withAuth())).data },
  async createSavedSearchIntent(request: {query: string; entityFamily?: string | null; paused?: boolean; notifyEnabled?: boolean; expiresAt?: string | null}): Promise<SavedSearchIntent> { return (await api.post<SavedSearchIntent>("/search/saved", request, withAuth())).data },
  async updateSavedSearchIntent(id: number, request: {query: string; entityFamily?: string | null; paused?: boolean; notifyEnabled?: boolean; expiresAt?: string | null}): Promise<SavedSearchIntent> { return (await api.put<SavedSearchIntent>(`/search/saved/${id}`, request, withAuth())).data },
  async deleteSavedSearchIntent(id: number): Promise<void> { await api.delete(`/search/saved/${id}`, withAuth()) },
  async createSafetyReport(request: {targetUserId?: number; targetFamily: string; targetId?: number; reason: string}): Promise<SafetyReport> { return (await api.post<SafetyReport>("/trust/reports", request, withAuth())).data },
  async getOnboardingProgress(): Promise<OnboardingProgress> { return (await api.get<OnboardingProgress>("/profile/onboarding/me", withAuth())).data },
  async updateOnboardingProgress(request: {currentStep: string; skipped?: boolean; completed?: boolean}): Promise<OnboardingProgress> { return (await api.put<OnboardingProgress>("/profile/onboarding/me", request, withAuth())).data },
  async resetOnboardingProgress(): Promise<OnboardingProgress> { return (await api.post<OnboardingProgress>("/profile/onboarding/me/reset", undefined, withAuth())).data },
  async getActivity(): Promise<ActivityItem[]> { return (await api.get<ActivityItem[]>("/activity/me", withAuth())).data },
  async getRecentActivity(): Promise<ActivityItem[]> { return (await api.get<ActivityItem[]>("/activity/me/recent", withAuth())).data },
  async dismissActivityResume(resumeKey: string): Promise<void> { await api.post(`/activity/resume/${encodeURIComponent(resumeKey)}/dismiss`, undefined, withAuth()) },
  async getAttentionCenter(signal?: AbortSignal): Promise<AttentionCenter> { return (await api.get<AttentionCenter>("/attention/me", {signal, ...withAuth()})).data },
  async getPersonalShortcuts(): Promise<PersonalShortcut[]> { return (await api.get<PersonalShortcut[]>("/personal-shortcuts/me", withAuth())).data },
  async getWorkspaceRailPreference(): Promise<WorkspaceRailPreference> { return (await api.get<WorkspaceRailPreference>("/personal-shortcuts/me/rail-preference", withAuth())).data },
  async updateWorkspaceRailPreference(railWidthPx: number): Promise<WorkspaceRailPreference> { return (await api.put<WorkspaceRailPreference>("/personal-shortcuts/me/rail-preference", {railWidthPx}, withAuth())).data },
  async getAppearancePreference(): Promise<AppearancePreference> { return (await api.get<AppearancePreference>("/personal-shortcuts/me/appearance-preference", withAuth())).data },
  async updateAppearancePreference(theme: AppearancePreference["theme"]): Promise<AppearancePreference> { return (await api.put<AppearancePreference>("/personal-shortcuts/me/appearance-preference", {theme}, withAuth())).data },
  async getWorkspaceCommandCatalog(signal?: AbortSignal): Promise<WorkspaceCommandCatalog> { return (await api.get<WorkspaceCommandCatalog>("/workspace/commands", {signal, ...withAuth()})).data },
  /** Navigation is a read-only shell contract; command actions remain on /workspace/commands. */
  async getWorkspaceNavigation(signal?: AbortSignal): Promise<WorkspaceNavigationResponse> { return workspaceNavigationApi.get(signal) },
  async pinQuest(questId: number): Promise<void> { await api.put(`/personal-shortcuts/me/quests/${questId}`, undefined, withAuth()) },
  async unpinQuest(questId: number): Promise<void> { await api.delete(`/personal-shortcuts/me/quests/${questId}`, withAuth()) },

  async getQuestDetail(questId: number): Promise<QuestDetailResponseDTO> {
    return (await api.get<QuestDetailResponseDTO>(`/quests/${questId}/detail`, withAuth())).data
  },

  async getQuestPreview(questId: number): Promise<import("../../../contracts/index.ts").QuestPreview> {
    return (await api.get<import("../../../contracts/index.ts").QuestPreview>(`/quests/${questId}/preview`, withAuth())).data
  },

  async getQuestApplications(questId: number, showAll = false): Promise<import("../../../contracts/index.ts").QuestApplicationsViewDTO> {
    return (await api.get<import("../../../contracts/index.ts").QuestApplicationsViewDTO>(`/quests/${questId}/applications/view`, {params: {showAll}, ...withAuth()})).data
  },

  async decideQuestApplication(questId: number, applicationId: number, decision: "approve" | "decline"): Promise<ActionResultDTO> {
    return (await api.patch<ActionResultDTO>(`/quests/${questId}/applications/${applicationId}/${decision}`, undefined, withAuth())).data
  },

  async releaseQuestWorker(questId: number, applicationId: number): Promise<import("../../../contracts/index.ts").QuestApplicationResponseDTO> {
    return (await api.patch<import("../../../contracts/index.ts").QuestApplicationResponseDTO>(`/quests/${questId}/workers/${applicationId}/release`, undefined, withAuth())).data
  },

  async replaceQuestWorker(questId: number, applicationId: number, replacementApplicationId: number): Promise<import("../../../contracts/index.ts").QuestApplicationResponseDTO> {
    return (await api.patch<import("../../../contracts/index.ts").QuestApplicationResponseDTO>(`/quests/${questId}/workers/${applicationId}/replace`, {replacementApplicationId}, withAuth())).data
  },

  async applyForQuest(questId: number, request: QuestApplicationRequestDTO): Promise<ActionResultDTO> {
    return (await api.post<ActionResultDTO>(`/quests/${questId}/applications`, request, withAuth())).data
  },

  async createQuest(request: QuestRequestDTO): Promise<ActionResultDTO> {
    return (await api.post<ActionResultDTO>("/quests", request, withAuth())).data
  },

  async updateQuest(questId: number, request: QuestRequestDTO): Promise<ActionResultDTO> {
    return (await api.put<ActionResultDTO>(`/quests/${questId}`, request, withAuth())).data
  },

  async executeQuestAction(questId: number, action: "START" | "COMPLETE" | "DELETE" | "CANCEL" | "PAUSE" | "RESUME"): Promise<ActionResultDTO> {
    const paths = {START: `/quests/${questId}/start`, COMPLETE: `/quests/${questId}/complete`, DELETE: `/quests/${questId}`, CANCEL: `/quests/${questId}/cancel`, PAUSE: `/quests/${questId}/pause`, RESUME: `/quests/${questId}/resume`}
    if (action === "DELETE") return (await api.delete<ActionResultDTO>(paths[action], withAuth())).data
    return (await api.patch<ActionResultDTO>(paths[action], undefined, withAuth())).data
  },

  async decideQuestTerm(questId: number, decision: "confirm" | "reject"): Promise<import("../../../contracts/index.ts").QuestResponseDTO> {
    return (await api.patch<import("../../../contracts/index.ts").QuestResponseDTO>(`/quests/${questId}/term/${decision}`, undefined, withAuth())).data
  },

  async submitQuestReview(questId: number, request: UserReviewRequestDTO): Promise<import("../../../contracts/index.ts").UserReviewResponseDTO> {
    return (await api.post<import("../../../contracts/index.ts").UserReviewResponseDTO>(`/quests/${questId}/reviews`, request, withAuth())).data
  },

  async updateMyApplication(questId: number, request: import("../../../contracts/index.ts").QuestApplicationRequestDTO): Promise<ActionResultDTO> {
    return (await api.put<ActionResultDTO>(`/quests/${questId}/applications/me`, request, withAuth())).data
  },

  async withdrawMyApplication(questId: number): Promise<ActionResultDTO> {
    return (await api.patch<ActionResultDTO>(`/quests/${questId}/applications/me/withdraw`, undefined, withAuth())).data
  },

  async getMyApplications(page = 0, size = 20): Promise<import("../../../contracts/index.ts").QuestApplicationListResponseDTO> {
    return (await api.get<import("../../../contracts/index.ts").QuestApplicationListResponseDTO>("/quests/applications/me", {
      params: {page, size},
      ...withAuth()
    })).data
  },

  async getApplicationDetail(applicationId: number): Promise<QuestApplicationDetailResponseDTO> {
    return (await api.get<QuestApplicationDetailResponseDTO>(`/applications/${applicationId}/detail`, withAuth())).data
  },

  async getMyNews(): Promise<QuestNewsItemResponseDTO[]> {
    return (await api.get<QuestNewsItemResponseDTO[]>("/news/me", withAuth())).data
  },

  async markNewsAsRead(): Promise<ActionResultDTO> {
    return (await api.patch<ActionResultDTO>("/news/me/read", undefined, withAuth())).data
  },

  async markNewsItemAsRead(newsId: number): Promise<ActionResultDTO> {
    return (await api.patch<ActionResultDTO>(`/news/me/${newsId}/read`, undefined, withAuth())).data
  },

  async getNotificationPreferences(): Promise<NotificationPreferenceResponseDTO> {
    return (await api.get<NotificationPreferenceResponseDTO>("/notification-preferences/me", withAuth())).data
  },

  async updateNotificationPreferences(updates: NotificationPreferenceUpdateDTO[]): Promise<NotificationPreferenceResponseDTO> {
    return (await api.put<NotificationPreferenceResponseDTO>("/notification-preferences/me", updates, withAuth())).data
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

  async createChatGroup(request: ChatCreateGroupConversationRequestDTO): Promise<ChatConversationSummaryDTO> {
    return (await api.post<ChatConversationSummaryDTO>("/chat/conversations/groups", request, withAuth())).data
  },
  async checkChatGroupEligibility(request: ChatCreateGroupConversationRequestDTO): Promise<import("../../../contracts/index.ts").ChatGroupEligibilityDTO> {
    return (await api.post<import("../../../contracts/index.ts").ChatGroupEligibilityDTO>("/chat/conversations/groups/eligibility", request, withAuth())).data
  },

  async openChat(request: ChatOpenConversationRequestDTO): Promise<ChatConversationSummaryDTO> {
    return (await api.post<ChatConversationSummaryDTO>("/chat/conversations/open", request, withAuth())).data
  },

  async leaveChatConversation(conversationId: number): Promise<import("../../../contracts/index.ts").ChatMembershipTransitionDTO> {
    return (await api.delete<import("../../../contracts/index.ts").ChatMembershipTransitionDTO>(`/chat/conversations/${conversationId}/participants/me`, withAuth())).data
  },

  async getChatConversationSync(conversationId: number): Promise<ChatConversationSyncDTO> {
    return (await api.get<ChatConversationSyncDTO>(`/chat/conversations/${conversationId}/sync`, {params: {limit: 50}, ...withAuth()})).data
  },
  async getChatRefreshHint(conversationId: number, knownLatestMessageId?: number | null): Promise<import("../../../contracts/index.ts").ChatRefreshHint> {
    return (await api.get<import("../../../contracts/index.ts").ChatRefreshHint>(`/chat/conversations/${conversationId}/refresh-hint`, {params: {knownLatestMessageId}, ...withAuth()})).data
  },

  async getChatMessages(conversationId: number, limit = 30, beforeMessageId?: number | null): Promise<ChatMessagePageDTO> {
    return (await api.get<ChatMessagePageDTO>(`/chat/conversations/${conversationId}/messages`, {
      params: {limit, beforeMessageId: beforeMessageId || undefined},
      ...withAuth()
    })).data
  },

  async uploadChatAttachment(file: File): Promise<ChatAttachmentUploadDTO> {
    const form = new FormData()
    form.append("file", file)
    return (await api.post<ChatAttachmentUploadDTO>("/chat/attachments", form, withAuth())).data
  },
  async cancelChatAttachment(uploadId: number): Promise<ChatAttachmentUploadDTO> {
    return (await api.delete<ChatAttachmentUploadDTO>(`/chat/attachments/${uploadId}`, withAuth())).data
  },
  async refreshChatAttachment(storageKey: string): Promise<import("../../../contracts/index.ts").ChatAttachmentAccessDTO> {
    return (await api.get<import("../../../contracts/index.ts").ChatAttachmentAccessDTO>("/chat/attachments/access", {params: {key: storageKey}, ...withAuth()})).data
  },

  async sendChatMessage(conversationId: number, messageBody: string, attachment?: ChatAttachmentUploadDTO | null, replyToMessageId?: number | null): Promise<ChatMessageDTO> {
    if (!Number.isInteger(conversationId) || conversationId <= 0) throw new Error("A valid conversation is required to send a message.")
    const request: ChatMessageRequestDTO = {messageBody: messageBody || undefined, clientMessageId: crypto.randomUUID(), attachmentName: attachment?.attachmentName, attachmentMimeType: attachment?.attachmentMimeType, attachmentUploadId: attachment?.uploadId, replyToMessageId: replyToMessageId ?? undefined}
    return (await api.post<ChatMessageDTO>(`/chat/conversations/${conversationId}/messages`, request, withAuth())).data
  },

  async markChatConversationRead(conversationId: number, upToMessageId?: number | null): Promise<ActionResultDTO> {
    return (await api.patch<ActionResultDTO>(`/chat/conversations/${conversationId}/read`, upToMessageId ? {upToMessageId} : undefined, withAuth())).data
  },

  async updateChatMessage(conversationId: number, messageId: number, messageBody: string): Promise<ChatMessageDTO> {
    return (await api.patch<ChatMessageDTO>(`/chat/conversations/${conversationId}/messages/${messageId}`, {messageBody}, withAuth())).data
  },

  async deleteChatMessage(conversationId: number, messageId: number): Promise<ActionResultDTO> {
    return (await api.delete<ActionResultDTO>(`/chat/conversations/${conversationId}/messages/${messageId}`, withAuth())).data
  },

  async addChatReaction(conversationId: number, messageId: number, emoji: string): Promise<ChatMessageDTO> {
    return (await api.post<ChatMessageDTO>(`/chat/conversations/${conversationId}/messages/${messageId}/reactions`, {emoji}, withAuth())).data
  },

  async removeChatReaction(conversationId: number, messageId: number, emoji: string): Promise<ChatMessageDTO> {
    return (await api.delete<ChatMessageDTO>(`/chat/conversations/${conversationId}/messages/${messageId}/reactions/${encodeURIComponent(emoji)}`, withAuth())).data
  },

  async getBusinessDashboard(): Promise<BusinessOwnerDashboardDTO> {
    return (await api.get<BusinessOwnerDashboardDTO>("/business/dashboard/me", withAuth())).data
  },

  async getBusinessProfile(): Promise<BusinessProfileResponseDTO> {
    const activeId = activeBusinessProfileId()
    if (activeId !== null) return this.getBusinessProfileById(activeId)
    return (await api.get<BusinessProfileResponseDTO>("/business/profiles/me", withAuth())).data
  },

  async getMyBusinessProfiles(): Promise<BusinessProfileResponseDTO[]> {
    return (await api.get<BusinessProfileResponseDTO[]>("/business/profiles/me/all", withAuth())).data
  },

  async getBusinessProfileById(profileId: number): Promise<BusinessProfileResponseDTO> {
    return (await api.get<BusinessProfileResponseDTO>(`/business/profiles/me/${profileId}`, withAuth())).data
  },

  async createBusinessProfile(request: BusinessProfileRequestDTO): Promise<BusinessProfileResponseDTO> {
    return (await api.post<BusinessProfileResponseDTO>("/business/profiles/me", request, withAuth())).data
  },

  async getBusinessDirectory(query = ""): Promise<BusinessProfileListResponseDTO> {
    return (await api.get<BusinessProfileListResponseDTO>("/business/profiles", {params: {q: query || undefined}, ...withAuth()})).data
  },

  async updateBusinessProfile(request: BusinessProfileRequestDTO): Promise<BusinessProfileResponseDTO> {
    return (await api.put<BusinessProfileResponseDTO>("/business/profiles/me", request, withAuth())).data
  },

  async updateBusinessProfileById(profileId: number, request: BusinessProfileRequestDTO): Promise<BusinessProfileResponseDTO> {
    return (await api.put<BusinessProfileResponseDTO>(`/business/profiles/me/${profileId}`, request, withAuth())).data
  },

  async archiveBusinessProfile(profileId: number): Promise<BusinessProfileResponseDTO> {
    return (await api.post<BusinessProfileResponseDTO>(`/business/profiles/me/${profileId}/archive`, {}, withAuth())).data
  },

  async getBusinessGallery(): Promise<BusinessGalleryImageListResponseDTO> {
    return (await api.get<BusinessGalleryImageListResponseDTO>("/business/gallery/me", {params: activeBusinessParams(), ...withAuth()})).data
  },

  async createBusinessGalleryImage(request: BusinessGalleryImageRequestDTO): Promise<BusinessGalleryImageResponseDTO> {
    return (await api.post<BusinessGalleryImageResponseDTO>("/business/gallery/me", request, {params: activeBusinessParams(), ...withAuth()})).data
  },

  async uploadBusinessGalleryImage(file: File, altText = "", sortOrder = 0): Promise<BusinessGalleryImageResponseDTO> {
    const form = new FormData()
    form.append("file", file)
    if (altText.trim()) form.append("altText", altText.trim())
    return (await api.post<BusinessGalleryImageResponseDTO>("/business/gallery/me/upload", form, {params: {...activeBusinessParams(), sortOrder}, ...withAuth()})).data
  },

  async updateBusinessGalleryImage(imageId: number, request: BusinessGalleryImageRequestDTO): Promise<BusinessGalleryImageResponseDTO> {
    return (await api.put<BusinessGalleryImageResponseDTO>(`/business/gallery/me/${imageId}`, request, withAuth())).data
  },

  async deleteBusinessGalleryImage(imageId: number): Promise<void> {
    await api.delete(`/business/gallery/me/${imageId}`, withAuth())
  },

  async getBusinessOfferings(): Promise<BusinessOfferingListResponseDTO> {
    return (await api.get<BusinessOfferingListResponseDTO>("/business/offerings/me", {params: activeBusinessParams(), ...withAuth()})).data
  },

  async createBusinessOffering(request: BusinessOfferingRequestDTO): Promise<BusinessOfferingResponseDTO> {
    return (await api.post<BusinessOfferingResponseDTO>("/business/offerings/me", request, {params: activeBusinessParams(), ...withAuth()})).data
  },

  async updateBusinessOffering(offeringId: number, request: BusinessOfferingRequestDTO): Promise<BusinessOfferingResponseDTO> {
    return (await api.put<BusinessOfferingResponseDTO>(`/business/offerings/${offeringId}/me`, request, withAuth())).data
  },

  async deleteBusinessOffering(offeringId: number): Promise<void> {
    await api.delete(`/business/offerings/${offeringId}/me`, withAuth())
  },

  async getBusinessAvailabilityRules(): Promise<BusinessAvailabilityRuleListResponseDTO> {
    return (await api.get<BusinessAvailabilityRuleListResponseDTO>("/business/availability-rules/me", {params: activeBusinessParams(), ...withAuth()})).data
  },

  async createBusinessAvailabilityRule(request: BusinessAvailabilityRuleRequestDTO): Promise<BusinessAvailabilityRuleResponseDTO> {
    return (await api.post<BusinessAvailabilityRuleResponseDTO>("/business/availability-rules/me", request, {params: activeBusinessParams(), ...withAuth()})).data
  },

  async updateBusinessAvailabilityRule(ruleId: number, request: BusinessAvailabilityRuleRequestDTO): Promise<BusinessAvailabilityRuleResponseDTO> {
    return (await api.put<BusinessAvailabilityRuleResponseDTO>(`/business/availability-rules/${ruleId}/me`, request, withAuth())).data
  },

  async deleteBusinessAvailabilityRule(ruleId: number): Promise<void> {
    await api.delete(`/business/availability-rules/${ruleId}/me`, withAuth())
  },

  async getBusinessAvailabilityExceptions(): Promise<BusinessAvailabilityExceptionListResponseDTO> {
    return (await api.get<BusinessAvailabilityExceptionListResponseDTO>("/business/availability-exceptions/me", {params: activeBusinessParams(), ...withAuth()})).data
  },

  async createBusinessAvailabilityException(request: BusinessAvailabilityExceptionRequestDTO): Promise<BusinessAvailabilityExceptionResponseDTO> {
    return (await api.post<BusinessAvailabilityExceptionResponseDTO>("/business/availability-exceptions/me", request, {params: activeBusinessParams(), ...withAuth()})).data
  },

  async updateBusinessAvailabilityException(exceptionId: number, request: BusinessAvailabilityExceptionRequestDTO): Promise<BusinessAvailabilityExceptionResponseDTO> {
    return (await api.put<BusinessAvailabilityExceptionResponseDTO>(`/business/availability-exceptions/${exceptionId}/me`, request, withAuth())).data
  },

  async deleteBusinessAvailabilityException(exceptionId: number): Promise<void> {
    await api.delete(`/business/availability-exceptions/${exceptionId}/me`, withAuth())
  },

  async getPublicBusinessPage(slug: string): Promise<BusinessPublicPageDTO> {
    return (await api.get<BusinessPublicPageDTO>(`/business/public/${encodeURIComponent(slug)}`, withAuth())).data
  },
  async previewPublicBooking(slug: string, request: {businessOfferingId: number; startsAt: string}): Promise<import("../../../contracts/index.ts").BusinessBookingPreviewResponseDTO> {
    return (await api.post<import("../../../contracts/index.ts").BusinessBookingPreviewResponseDTO>(`/business/public/${encodeURIComponent(slug)}/booking-preview`, request, withAuth())).data
  },

  async getBusinessFavorites(): Promise<BusinessFavorite[]> {
    return (await api.get<BusinessFavorite[]>("/business/favorites/me", withAuth())).data
  },

  async addBusinessFavorite(businessProfileId: number): Promise<BusinessFavorite> {
    return (await api.post<BusinessFavorite>(`/business/favorites/me/${businessProfileId}`, undefined, withAuth())).data
  },

  async removeBusinessFavorite(businessProfileId: number): Promise<void> {
    await api.delete(`/business/favorites/me/${businessProfileId}`, withAuth())
  },

  async createCustomerBooking(request: BusinessBookingRequestDTO): Promise<BusinessBookingResponseDTO> {
    return (await api.post<BusinessBookingResponseDTO>("/business/bookings", request, withAuth())).data
  },

  async getMyBusinessBookings(page = 0, size = 50): Promise<BusinessBookingListResponseDTO> {
    return (await api.get<BusinessBookingListResponseDTO>("/business/bookings/me", {params: {page, size}, ...withAuth()})).data
  },

  async cancelMyBusinessBooking(bookingId: number): Promise<BusinessBookingResponseDTO> {
    return (await api.post<BusinessBookingResponseDTO>(`/business/bookings/me/${bookingId}/cancel`, undefined, withAuth())).data
  },

  async rescheduleMyBusinessBooking(bookingId: number, startsAt: string, endsAt: string, reason = ""): Promise<BusinessBookingResponseDTO> {
    return (await api.post<BusinessBookingResponseDTO>(`/business/bookings/me/${bookingId}/reschedule`, {startsAt, endsAt, reason}, withAuth())).data
  },

  async getBusinessOwnerBookings(): Promise<BusinessBookingListResponseDTO> {
    return (await api.get<BusinessBookingListResponseDTO>("/business/bookings/owner", {
      params: {page: 0, size: 50, ...activeBusinessParams()},
      ...withAuth()
    })).data
  },

  async getBusinessOwnerCalendar(): Promise<BusinessOwnerCalendarProjectionDTO> {
    return (await api.get<BusinessOwnerCalendarProjectionDTO>("/business/bookings/owner/calendar", withAuth())).data
  },

  async executeBusinessBookingAction(bookingId: number, action: "confirm" | "reject" | "cancel" | "complete" | "mark-no-show"): Promise<BusinessBookingResponseDTO> {
    return (await api.post<BusinessBookingResponseDTO>(`/business/bookings/owner/${bookingId}/${action}`, undefined, withAuth())).data
  },

  async rescheduleBusinessBookingAsOwner(bookingId: number, startsAt: string, endsAt: string, reason = ""): Promise<BusinessBookingResponseDTO> {
    return (await api.post<BusinessBookingResponseDTO>(`/business/bookings/owner/${bookingId}/reschedule`, {startsAt, endsAt, reason}, withAuth())).data
  },

  async getCirclesOverview(): Promise<CircleOverviewDTO> {
    return (await api.get<CircleOverviewDTO>("/circles/me/overview", withAuth())).data
  },

  async getCircleGroups(): Promise<CircleGroupResponseDTO[]> {
    return (await api.get<CircleGroupResponseDTO[]>("/circles/groups", withAuth())).data
  },

  async createCircleGroup(request: CircleGroupRequestDTO): Promise<ActionResultDTO> {
    return (await api.post<ActionResultDTO>("/circles/groups", request, withAuth())).data
  },

  async updateCircleGroup(groupId: number, request: CircleGroupRequestDTO): Promise<CircleGroupResponseDTO> {
    return (await api.put<CircleGroupResponseDTO>(`/circles/groups/${groupId}`, request, withAuth())).data
  },

  async deleteCircleGroup(groupId: number): Promise<ActionResultDTO> {
    return (await api.delete<ActionResultDTO>(`/circles/groups/${groupId}`, withAuth())).data
  },

  async leaveCircle(circleId: number): Promise<ActionResultDTO> {
    return (await api.delete<ActionResultDTO>(`/circles/groups/${circleId}/membership/me`, withAuth())).data
  },

  async removeCircleMember(circleId: number, userId: number): Promise<ActionResultDTO> {
    return (await api.put<ActionResultDTO>("/circles/connections/circles/bulk", {
      circleId,
      userIds: [userId],
      action: "REMOVE"
    }, withAuth())).data
  },

  async acceptCircleRequest(requestId: number): Promise<ActionResultDTO> {
    return (await api.patch<ActionResultDTO>(`/circles/requests/${requestId}/accept`, undefined, withAuth())).data
  },

  async createCircleRequest(request: CircleRequestCreateDTO): Promise<ActionResultDTO> {
    return (await api.post<ActionResultDTO>("/circles/requests", request, withAuth())).data
  },

  async deleteCircleRequest(requestId: number): Promise<ActionResultDTO> {
    return (await api.delete<ActionResultDTO>(`/circles/requests/${requestId}`, withAuth())).data
  },

  async getOutgoingCircleRequests(page = 0, size = 50): Promise<CircleRequestListResponseDTO> {
    return (await api.get<CircleRequestListResponseDTO>("/circles/requests/outgoing", {params: {page, size}, ...withAuth()})).data
  },

  async searchCircleUsers(q: string): Promise<CircleSearchResultListResponseDTO> {
    return (await api.get<CircleSearchResultListResponseDTO>("/circles/search", {params: {q, page: 0, size: 20}, ...withAuth()})).data
  },

  async blockCircleUser(userId: number): Promise<ActionResultDTO> {
    return (await api.post<ActionResultDTO>("/circles/blocks", {blockedUserId: userId}, withAuth())).data
  },

  async getBlockedCircleUsers(page = 0, size = 50): Promise<CircleSearchResultListResponseDTO> {
    return (await api.get<CircleSearchResultListResponseDTO>("/circles/blocked", {params: {page, size}, ...withAuth()})).data
  },

  async unblockCircleUser(userId: number): Promise<ActionResultDTO> {
    return (await api.delete<ActionResultDTO>(`/circles/blocks/${userId}`, withAuth())).data
  },

  async getIncomingCircleRequests(page = 0, size = 50): Promise<CircleRequestListResponseDTO> {
    return (await api.get<CircleRequestListResponseDTO>("/circles/requests/incoming", {
      params: {page, size},
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

  async updateCurrentAppUser(request: AppUserRequestDTO): Promise<AppUserResponseDTO> {
    return (await api.put<AppUserResponseDTO>("/app_users/me", request, withAuth())).data
  },

  async getMyProfileGallery(): Promise<{items: ProfileGalleryImage[]}> {
    return (await api.get<{items: ProfileGalleryImage[]}>("/profile/gallery/me", withAuth())).data
  },

  async createProfileGalleryImage(request: ProfileGalleryImageRequest): Promise<ProfileGalleryImage> {
    return (await api.post<ProfileGalleryImage>("/profile/gallery/me", request, withAuth())).data
  },

  async deleteProfileGalleryImage(imageId: number): Promise<void> {
    await api.delete(`/profile/gallery/me/${imageId}`, withAuth())
  },

  async lookupLocations(query: string): Promise<LocationLookupResponseDTO> {
    return (await api.post<LocationLookupResponseDTO>("/location/lookup", {query}, withAuth())).data
  },

  async reverseLookupLocation(latitude: number, longitude: number): Promise<LocationLookupCandidateDTO> {
    return (await api.post<LocationLookupCandidateDTO>("/location/reverse-lookup", {latitude, longitude}, withAuth())).data
  },

  async getCurrentProfileView(userId: number): Promise<UserProfileViewDTO> {
    return (await api.get<UserProfileViewDTO>(`/app_users/${userId}/profile-view`, withAuth())).data
  },

  async getThingListings(): Promise<ThingListingListResponseDTO> {
    return (await api.get<ThingListingListResponseDTO>("/things/listings", withAuth())).data
  },

  async getMyThingListings(): Promise<ThingListingListResponseDTO> {
    return (await api.get<ThingListingListResponseDTO>("/things/listings/me", withAuth())).data
  },

  async getThingListing(listingId: number): Promise<ThingListingResponseDTO> {
    return (await api.get<ThingListingResponseDTO>(`/things/listings/${listingId}`, withAuth())).data
  },

  async getThingPreview(listingId: number): Promise<import("../../../contracts/index.ts").ThingPreview> {
    return (await api.get<import("../../../contracts/index.ts").ThingPreview>(`/things/listings/${listingId}/preview`, withAuth())).data
  },

  async createThingListing(request: ThingListingRequestDTO): Promise<ThingListingResponseDTO> {
    return (await api.post<ThingListingResponseDTO>("/things/listings", request, withAuth())).data
  },

  async updateThingListing(listingId: number, request: ThingListingRequestDTO): Promise<ThingListingResponseDTO> {
    return (await api.put<ThingListingResponseDTO>(`/things/listings/${listingId}`, request, withAuth())).data
  },

  async archiveThingListing(listingId: number): Promise<void> {
    await api.delete(`/things/listings/${listingId}`, withAuth())
  },

  async requestThingBorrow(listingId: number, message: string): Promise<ThingBorrowRequestResponseDTO> {
    return (await api.post<ThingBorrowRequestResponseDTO>(`/things/listings/${listingId}/borrow-requests`, {message}, withAuth())).data
  },

  async cancelThingBorrow(requestId: number): Promise<ThingBorrowRequestResponseDTO> {
    return (await api.patch<ThingBorrowRequestResponseDTO>(`/things/borrow-requests/${requestId}/cancel`, undefined, withAuth())).data
  },

  async getMyThingBorrowRequests(): Promise<ThingBorrowRequestResponseDTO[]> {
    return (await api.get<ThingBorrowRequestResponseDTO[]>("/things/borrow-requests/me", withAuth())).data
  },

  async getThingOwnerBorrowRequests(): Promise<ThingBorrowRequestResponseDTO[]> {
    return (await api.get<ThingBorrowRequestResponseDTO[]>("/things/listings/me/borrow-requests", withAuth())).data
  },

  async decideThingBorrow(requestId: number, approve: boolean): Promise<ThingBorrowRequestResponseDTO> {
    return (await api.patch<ThingBorrowRequestResponseDTO>(`/things/borrow-requests/${requestId}/decision`, undefined, {
      ...withAuth(),
      params: {approve}
    })).data
  },

  async returnThingBorrow(requestId: number): Promise<ThingBorrowRequestResponseDTO> {
    return (await api.patch<ThingBorrowRequestResponseDTO>(`/things/borrow-requests/${requestId}/return`, undefined, withAuth())).data
  },

  async getRideOffers(): Promise<RideOfferListResponseDTO> { return (await api.get<RideOfferListResponseDTO>("/rides/offers", withAuth())).data },
  async getRideOffer(id: number): Promise<RideOfferResponseDTO> { return (await api.get<RideOfferResponseDTO>(`/rides/offers/${id}`, withAuth())).data },
  async findRideMatches(filters: {origin?: string; destination?: string; departureFrom?: string; departureTo?: string} = {}): Promise<RideOfferListResponseDTO> {
    return (await api.get<RideOfferListResponseDTO>("/rides/offers/matches", {params: {...filters, origin: filters.origin || undefined, destination: filters.destination || undefined}, ...withAuth()})).data
  },
  async getCommutePreference(): Promise<CommutePreference> {
    return (await api.get<CommutePreference>("/rides/commute/me", withAuth())).data
  },
  async updateCommutePreference(request: Omit<CommutePreference, "id" | "updatedAt">): Promise<CommutePreference> {
    return (await api.put<CommutePreference>("/rides/commute/me", request, withAuth())).data
  },
  async getMyRideOffers(): Promise<RideOfferListResponseDTO> { return (await api.get<RideOfferListResponseDTO>("/rides/offers/me", withAuth())).data },
  async createRideOffer(request: RideOfferRequestDTO): Promise<RideOfferResponseDTO> { return (await api.post<RideOfferResponseDTO>("/rides/offers", request, withAuth())).data },
  async updateRideOffer(id: number, request: RideOfferRequestDTO): Promise<RideOfferResponseDTO> { return (await api.put<RideOfferResponseDTO>(`/rides/offers/${id}`, request, withAuth())).data },
  async joinRide(id: number): Promise<RideOfferResponseDTO> { return (await api.post<RideOfferResponseDTO>(`/rides/offers/${id}/join`, undefined, withAuth())).data },
  async leaveRide(id: number): Promise<RideOfferResponseDTO> { return (await api.post<RideOfferResponseDTO>(`/rides/offers/${id}/leave`, undefined, withAuth())).data },
  async cancelRide(id: number): Promise<RideOfferResponseDTO> { return (await api.post<RideOfferResponseDTO>(`/rides/offers/${id}/cancel`, undefined, withAuth())).data },
  async startRide(id: number): Promise<RideOfferResponseDTO> { return (await api.post<RideOfferResponseDTO>(`/rides/offers/${id}/start`, undefined, withAuth())).data },
  async completeRide(id: number): Promise<RideOfferResponseDTO> { return (await api.post<RideOfferResponseDTO>(`/rides/offers/${id}/complete`, undefined, withAuth())).data }
}
