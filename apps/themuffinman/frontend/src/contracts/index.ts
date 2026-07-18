export * from "./generated/themuffinmanContract.ts"

export type ApplicationAllowedAction = import("./generated/themuffinmanContract.ts").ApplicationAllowedActionDTO
export type AuthResponse = import("./generated/themuffinmanContract.ts").AuthResponseDTO
export type CircleRelationStatus = import("./generated/themuffinmanContract.ts").CircleRelationStatusDTO
export type ExactLocationVisibilityScopeOption = import("./generated/themuffinmanContract.ts").ExactLocationVisibilityScopeOptionDTO
export type LocationDebugStatusDTO = import("./generated/themuffinmanContract.ts").LocationDebugStatusViewDTO
export type LoginRequest = import("./generated/themuffinmanContract.ts").LoginRequestDTO
export type QuestAllowedAction = import("./generated/themuffinmanContract.ts").QuestAllowedActionDTO
export type QuestApplicationAllowedAction = import("./generated/themuffinmanContract.ts").ApplicationAllowedActionDTO
export type QuestListPreset = import("./generated/themuffinmanContract.ts").QuestListPresetDTO
export type QuestViewerRelation = import("./generated/themuffinmanContract.ts").QuestViewerRelationDTO
export type QuestDetailRailItem = import("./generated/themuffinmanContract.ts").QuestDetailRailItemDTO
export type RegisterRequest = import("./generated/themuffinmanContract.ts").RegisterRequestDTO
export type PasswordRecoveryRequest = import("./generated/themuffinmanContract.ts").PasswordRecoveryRequestDTO
export type PasswordRecoveryResponse = import("./generated/themuffinmanContract.ts").PasswordRecoveryResponseDTO
export type PasswordResetRequest = import("./generated/themuffinmanContract.ts").PasswordResetRequestDTO
export type QuestPreview = { id: number; title: string; summary: string; creatorUsername: string; status: string; canOpenDetail: boolean }
export type ThingPreview = { id: number; title: string; summary: string; ownerUsername: string; available: boolean; myPendingRequestId?: number; canOpenDetail: boolean }
export type ThingAllowedAction = import("./generated/themuffinmanContract.ts").ThingAllowedActionDTO
export type PersonalWorkspaceAttentionItem = import("./generated/themuffinmanContract.ts").ActivityItemDTO

export type WorkspaceCommandItem = {id: string; group: string; label: string; description: string; route: string; kind: "NAVIGATE" | "CREATE_ROUTE" | "VISION_ROUTE"}
export type WorkspaceCommandCatalog = {personal: WorkspaceCommandItem[]; navigation: WorkspaceCommandItem[]; create: WorkspaceCommandItem[]; vision: WorkspaceCommandItem[]}
export type ChatRefreshHint = {conversationId: number; latestMessageId: number | null; refreshRequired: boolean; reason: string}
export type VisionWorkspaceHandoff = {contextLabel: string | null; source: string | null; returnTo: string | null; explanation: string}
export type VisionSearchComparisonItem = {entityFamily: string; targetId: number; title: string; sourceRoute: string; fields: Record<string, string | null>}
export type VisionSearchComparison = {capabilityId: string; query: string; selectionLimit: number; omittedSelectionCount: number; fallbackMessage: string | null; comparableFields: string[]; items: VisionSearchComparisonItem[]}
