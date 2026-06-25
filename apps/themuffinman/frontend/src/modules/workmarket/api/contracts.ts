import type {QuestApplicationStatus} from "../domain/workmarketDomain.ts"

export type {
  ActionResultDTO,
  AdminCircleGroup,
  AdminCircleOverview,
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
  QuestApplicationRequest,
  QuestApplicationsView,
  QuestDetail,
  QuestDetailSections,
  QuestListPreset,
  QuestListResponse,
  QuestNewsItem,
  QuestSortOptionDTO,
  QuestStatusFilterOption,
  QuestStatusOption,
  RegisterRequest,
  UserProfileView,
  UserRatingSummary,
  UserReview,
  UserReviewRequest,
  WorkmarketOptions
} from "../../../contracts/index.ts"

export type ActionResult = import("../../../contracts/index.ts").ActionResultDTO
export type QuestRequest = import("../../../contracts/index.ts").QuestRequestDTO & {
  images?: string[]
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
