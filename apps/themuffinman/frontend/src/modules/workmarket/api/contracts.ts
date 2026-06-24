import type {
  QuestAudience,
  QuestApplicationStatus,
  QuestStatus
} from "../domain/workmarketDomain.ts"

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

export interface QuestSearchRequest {
  q?: string
  status?: QuestStatus | null
  audience?: QuestAudience | null
  dateFrom?: string | null
  dateTo?: string | null
  excludeMine?: boolean
  withImages?: boolean
  scheduledOnly?: boolean
  sort?: import("../domain/workmarketDomain.ts").QuestSortMode
  page?: number
  size?: number
}

export interface PageQuery {
  page?: number
  size?: number
}

export interface TextPageQuery extends PageQuery {
  q?: string
}

export interface CircleConnectionsQuery extends TextPageQuery {
  circleId?: number
  unassigned?: boolean
}

export interface AdminApplicationsQuery extends PageQuery {
  q?: string
  status?: QuestApplicationStatus | "ALL"
}
