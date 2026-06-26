import {adminApi} from "./clients/adminApi.ts"
import {applicationsApi} from "./clients/applicationsApi.ts"
import {circlesApi} from "./clients/circlesApi.ts"
import {dashboardApi} from "./clients/dashboardApi.ts"
import {locationApi} from "./clients/locationApi.ts"
import {newsApi} from "./clients/newsApi.ts"
import {questsApi} from "./clients/questsApi.ts"
import {usersApi} from "./clients/usersApi.ts"

export type {
  ActionResult,
  AdminQuestApplicationUpdateRequest,
  AdminCircleGroup,
  AdminCircleOverview,
  AdminUserDetail,
  AppUser,
  AppUserRoleOption,
  AppUserRequest,
  CircleBlockCreate,
  CircleCandidate,
  CircleCandidateListResponse,
  CircleContact,
  CircleContactListResponse,
  CircleGroup,
  CircleGroupRequest,
  CircleMembership,
  CircleRelation,
  CircleRequest,
  CircleRequestCreate,
  CircleRequestListResponse,
  CircleSummary,
  ConnectionCircleUpdateRequest,
  DashboardResponse,
  DashboardSections,
  DashboardSummary,
  ExactLocationVisibilityScopeOption,
  LocationDebugStatus,
  LocationLookupCandidate,
  LocationLookupRequest,
  LocationLookupResponse,
  LocationReverseLookupRequest,
  LocationModeOption,
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
  QuestLocationVisibilityOption,
  QuestListPreset,
  QuestListResponse,
  QuestNewsItem,
  QuestRequest,
  QuestStatusFilterOption,
  QuestStatusOption,
  UserLocationSettings,
  UserProfileView,
  UserRatingSummary,
  UserReview,
  UserReviewRequest,
  WorkmarketOptions
} from "./contracts.ts"

export const workmarketApi = {
  ...questsApi,
  ...applicationsApi,
  ...dashboardApi,
  ...locationApi,
  ...newsApi,
  ...usersApi,
  ...circlesApi,
  ...adminApi
}
