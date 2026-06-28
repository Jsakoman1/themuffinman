import type {QuestApplicationStatus} from "../domain/workmarketDomain.ts"

export type {
  ActionResultDTO,
  AdminQuestApplicationUpdateRequestDTO,
  AdminCircleGroup,
  AdminCircleOverview,
  AdminUserDetailDTO,
  AppUser,
  AppUserRoleOption,
  AppUserRequest,
  AuthResponse,
  CircleBlockCreate,
  CircleCandidate,
  CircleCandidateListResponse,
  CircleContact,
  CircleContactListResponse,
  CircleGroup,
  CircleGroupRequest,
  CircleMembership,
  CircleOverview,
  CircleRelation,
  CircleRequest,
  CircleRequestCreate,
  CircleRequestListResponse,
  CircleSummary,
  ConnectionCircleUpdateRequest,
  DashboardResponse,
  DashboardSections,
  DashboardSummary,
  ExactLocationVisibilityScopeOptionDTO,
  LocationDebugStatusDTO,
  LocationLookupCandidate,
  LocationLookupRequest,
  LocationLookupResponse,
  LocationReverseLookupRequest,
  LocationModeOption,
  LoginRequest,
  NavigationTarget,
  NavigationTargetType,
  ProfilePrimaryAction,
  Quest,
  QuestAudienceOption,
  QuestApplication,
  QuestApplicationDetail,
  QuestApplicationDetailSections,
  QuestApplicationListResponse,
  QuestApplicationsView,
  QuestDetail,
  QuestDetailSections,
  QuestLocationVisibilityOption,
  QuestListPreset,
  QuestListResponse,
  QuestNewsItem,
  QuestSortOptionDTO,
  QuestStatusFilterOption,
  QuestStatusOption,
  RegisterRequest,
  UserLocationSettings,
  UserProfileView,
  UserRatingSummary,
  UserReview,
  UserReviewRequest,
  WorkmarketOptions
} from "../../../contracts/index.ts"

export type ActionResult = import("../../../contracts/index.ts").ActionResultDTO
export type AdminUserDetail = import("../../../contracts/index.ts").AdminUserDetailDTO
export type AdminAgentPlaygroundRequest = import("../../../contracts/index.ts").AdminAgentPlaygroundRequest
export type AdminAgentPlaygroundResponse = import("../../../contracts/index.ts").AdminAgentPlaygroundResponse
export type AdminAgentSimulationRequest = import("../../../contracts/index.ts").AdminAgentSimulationRequest
export type AdminAgentSimulationResponse = import("../../../contracts/index.ts").AdminAgentSimulationResponse
export type QuestRequest = Omit<import("../../../contracts/index.ts").QuestRequestDTO, "awardAmount"> & {
  awardAmount: number | null
  images?: string[]
}
export type QuestApplicationRequest = Omit<import("../../../contracts/index.ts").QuestApplicationRequestDTO, "proposedPrice"> & {
  proposedPrice?: number | null
}
export type AdminQuestApplicationUpdateRequest = Omit<import("../../../contracts/index.ts").AdminQuestApplicationUpdateRequestDTO, "proposedPrice"> & {
  proposedPrice?: number | null
}
export type AdminApplicationsQuery = Partial<Omit<import("../../../contracts/index.ts").AdminApplicationsQueryDTO, "status">> & {
  status?: QuestApplicationStatus | "ALL"
}
export type CircleConnectionsQuery = Partial<import("../../../contracts/index.ts").CircleConnectionsQueryDTO>
export type PageQuery = Partial<import("../../../contracts/index.ts").PageQueryDTO>
export type QuestSearchRequest = Partial<import("../../../contracts/index.ts").QuestSearchRequestDTO> & {
  sort?: import("../domain/workmarketDomain.ts").QuestSortMode
}
export type TextPageQuery = Partial<import("../../../contracts/index.ts").TextPageQueryDTO>
export type LocationDebugStatus = import("../../../contracts/index.ts").LocationDebugStatusDTO
export type ExactLocationVisibilityScopeOption = import("../../../contracts/index.ts").ExactLocationVisibilityScopeOptionDTO
