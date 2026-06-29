# Read Surface Inventory

- Generated at: `2026-06-29T19:54:42Z`
- Read surfaces: `146`
- Transaction-relevant read surfaces: `87`
- Missing explicit or inherited read-only coverage: `2`

## `AdminDatabaseMetricsService`

- `getDatabaseSizeBytes` -> `long` | tx=`class` | relevant=`false` | repos=none | dto=none
- `getTableStatuses` -> `List<DatabaseTableStatusViewDTO>` | tx=`class` | relevant=`true` | repos=none | dto=List<DatabaseTableStatusViewDTO>

## `AdminUserDetailService`

- `getDetail` -> `AdminUserDetailDTO` | tx=`class` | relevant=`true` | repos=none | dto=AdminUserDetailDTO

## `AppUserService`

- `getAllAppUsers` -> `List<AppUser>` | tx=`method` | relevant=`true` | repos=AppUserRepository | dto=none
- `getAppUser` -> `AppUser` | tx=`method` | relevant=`false` | repos=none | dto=none

## `BusinessProfileService`

- `getDirectory` -> `BusinessProfileListResponseDTO` | tx=`class` | relevant=`true` | repos=BusinessProfileRepository | dto=BusinessProfileListResponseDTO
- `getProfileBySlug` -> `BusinessProfileResponseDTO` | tx=`class` | relevant=`true` | repos=BusinessProfileRepository | dto=BusinessProfileResponseDTO
- `getMyProfile` -> `BusinessProfileResponseDTO` | tx=`class` | relevant=`true` | repos=BusinessProfileRepository | dto=BusinessProfileResponseDTO

## `ChatService`

- `getWorkspace` -> `ChatWorkspaceDTO` | tx=`method` | relevant=`true` | repos=ChatConversationRepository,ChatMessageRepository | dto=ChatCircleOptionDTO,ChatWorkspaceDTO
- `getConversationMessages` -> `List<ChatMessageDTO>` | tx=`method` | relevant=`true` | repos=ChatMessageRepository | dto=List<ChatMessageDTO>

## `CircleDiscoveryService`

- `getNearbyUsers` -> `CircleSearchResultListResponseDTO` | tx=`class` | relevant=`true` | repos=AppUserRepository | dto=CircleSearchResultListResponseDTO
- `getInviteCandidates` -> `List<CircleSearchResultDTO>` | tx=`class` | relevant=`true` | repos=none | dto=List<CircleSearchResultDTO>
- `getInviteCandidatesPage` -> `CircleSearchResultListResponseDTO` | tx=`class` | relevant=`true` | repos=AppUserRepository | dto=CircleSearchResultListResponseDTO
- `searchCircleUsers` -> `CircleSearchResultListResponseDTO` | tx=`class` | relevant=`true` | repos=AppUserRepository | dto=CircleSearchResultListResponseDTO
- `searchCircleUsers` -> `List<CircleSearchResultDTO>` | tx=`class` | relevant=`true` | repos=none | dto=List<CircleSearchResultDTO>
- `getBlockedUsers` -> `CircleSearchResultListResponseDTO` | tx=`class` | relevant=`true` | repos=CircleRequestRepository | dto=CircleSearchResultListResponseDTO
- `getRelationWithUser` -> `CircleRelationDTO` | tx=`class` | relevant=`true` | repos=AppUserRepository | dto=CircleRelationDTO

## `CircleMembershipService`

- `getOwnedCirclesByIds` -> `List<CircleGroup>` | tx=`method` | relevant=`true` | repos=CircleGroupRepository | dto=none
- `getMembershipsByOwner` -> `List<CircleMembership>` | tx=`method` | relevant=`true` | repos=CircleMembershipRepository | dto=none
- `getMembershipsForContact` -> `List<CircleMembership>` | tx=`method` | relevant=`true` | repos=CircleMembershipRepository | dto=none
- `getMembershipsByUserIdForOwner` -> `Map<Long, List<CircleMembership>>` | tx=`method` | relevant=`false` | repos=none | dto=none

## `CircleReadService`

- `getMyCircles` -> `List<CircleRequestResponseDTO>` | tx=`method` | relevant=`true` | repos=CircleRequestRepository | dto=List<CircleRequestResponseDTO>
- `getAdminOverview` -> `AdminCircleOverviewDTO` | tx=`method` | relevant=`true` | repos=CircleRequestRepository | dto=AdminCircleOverviewDTO
- `getOverview` -> `CircleOverviewDTO` | tx=`method` | relevant=`true` | repos=none | dto=CircleOverviewDTO
- `getCircles` -> `List<CircleGroupResponseDTO>` | tx=`method` | relevant=`true` | repos=CircleGroupRepository | dto=List<CircleGroupResponseDTO>
- `getCirclesForUserAsAdmin` -> `List<CircleGroupResponseDTO>` | tx=`method` | relevant=`true` | repos=CircleGroupRepository | dto=List<CircleGroupResponseDTO>
- `getAllCirclesForAdmin` -> `List<AdminCircleGroupResponseDTO>` | tx=`method` | relevant=`true` | repos=CircleGroupRepository | dto=List<AdminCircleGroupResponseDTO>
- `getConnections` -> `List<CircleContactDTO>` | tx=`method` | relevant=`true` | repos=none | dto=List<CircleContactDTO>
- `getConnections` -> `CircleContactListResponseDTO` | tx=`method` | relevant=`true` | repos=none | dto=CircleContactListResponseDTO
- `getConnectionsForUserAsAdmin` -> `List<CircleContactDTO>` | tx=`method` | relevant=`true` | repos=none | dto=List<CircleContactDTO>
- `getIncomingRequests` -> `List<CircleRequestResponseDTO>` | tx=`method` | relevant=`true` | repos=none | dto=List<CircleRequestResponseDTO>
- `getIncomingRequests` -> `CircleRequestListResponseDTO` | tx=`method` | relevant=`true` | repos=none | dto=CircleRequestListResponseDTO
- `getOutgoingRequests` -> `List<CircleRequestResponseDTO>` | tx=`method` | relevant=`true` | repos=none | dto=List<CircleRequestResponseDTO>
- `getOutgoingRequests` -> `CircleRequestListResponseDTO` | tx=`method` | relevant=`true` | repos=none | dto=CircleRequestListResponseDTO
- `getOwnedCirclesByIds` -> `List<CircleGroup>` | tx=`method` | relevant=`false` | repos=none | dto=none
- `findRelation` -> `Optional<CircleRequest>` | tx=`method` | relevant=`false` | repos=none | dto=none

## `CircleRelationService`

- `findRelation` -> `Optional<CircleRequest>` | tx=`method` | relevant=`true` | repos=CircleRequestRepository | dto=none

## `CircleService`

- `getMyCircles` -> `List<CircleRequestResponseDTO>` | tx=`method` | relevant=`true` | repos=none | dto=List<CircleRequestResponseDTO>
- `getAdminOverview` -> `AdminCircleOverviewDTO` | tx=`method` | relevant=`true` | repos=none | dto=AdminCircleOverviewDTO
- `getOverview` -> `CircleOverviewDTO` | tx=`method` | relevant=`true` | repos=none | dto=CircleOverviewDTO
- `getCircles` -> `List<CircleGroupResponseDTO>` | tx=`method` | relevant=`true` | repos=none | dto=List<CircleGroupResponseDTO>
- `getCirclesForUserAsAdmin` -> `List<CircleGroupResponseDTO>` | tx=`method` | relevant=`true` | repos=none | dto=List<CircleGroupResponseDTO>
- `getAllCirclesForAdmin` -> `List<AdminCircleGroupResponseDTO>` | tx=`method` | relevant=`true` | repos=none | dto=List<AdminCircleGroupResponseDTO>
- `getConnections` -> `List<CircleContactDTO>` | tx=`method` | relevant=`true` | repos=none | dto=List<CircleContactDTO>
- `getConnections` -> `CircleContactListResponseDTO` | tx=`method` | relevant=`true` | repos=none | dto=CircleContactListResponseDTO
- `getConnectionsForUserAsAdmin` -> `List<CircleContactDTO>` | tx=`method` | relevant=`true` | repos=none | dto=List<CircleContactDTO>
- `getIncomingRequests` -> `List<CircleRequestResponseDTO>` | tx=`method` | relevant=`true` | repos=none | dto=List<CircleRequestResponseDTO>
- `getIncomingRequests` -> `CircleRequestListResponseDTO` | tx=`method` | relevant=`true` | repos=none | dto=CircleRequestListResponseDTO
- `getOutgoingRequests` -> `List<CircleRequestResponseDTO>` | tx=`method` | relevant=`true` | repos=none | dto=List<CircleRequestResponseDTO>
- `getOutgoingRequests` -> `CircleRequestListResponseDTO` | tx=`method` | relevant=`true` | repos=none | dto=CircleRequestListResponseDTO
- `getInviteCandidates` -> `List<CircleSearchResultDTO>` | tx=`method` | relevant=`true` | repos=none | dto=List<CircleSearchResultDTO>
- `searchCircleUsers` -> `CircleSearchResultListResponseDTO` | tx=`method` | relevant=`true` | repos=none | dto=CircleSearchResultListResponseDTO
- `getInviteCandidatesPage` -> `CircleSearchResultListResponseDTO` | tx=`method` | relevant=`true` | repos=none | dto=CircleSearchResultListResponseDTO
- `getBlockedUsers` -> `CircleSearchResultListResponseDTO` | tx=`method` | relevant=`true` | repos=none | dto=CircleSearchResultListResponseDTO
- `getRelationWithUser` -> `CircleRelationDTO` | tx=`method` | relevant=`true` | repos=none | dto=CircleRelationDTO
- `searchCircleUsers` -> `List<CircleSearchResultDTO>` | tx=`method` | relevant=`true` | repos=none | dto=List<CircleSearchResultDTO>
- `getOwnedCirclesByIds` -> `List<CircleGroup>` | tx=`method` | relevant=`false` | repos=none | dto=none

## `CircleViewAssembler`

- `buildOverview` -> `CircleOverviewDTO` | tx=`none` | relevant=`false` | repos=none | dto=CircleOverviewDTO
- `buildAdminOverview` -> `AdminCircleOverviewDTO` | tx=`none` | relevant=`false` | repos=none | dto=AdminCircleOverviewDTO
- `toContact` -> `CircleContactDTO` | tx=`none` | relevant=`false` | repos=none | dto=CircleContactDTO
- `toCircleDto` -> `CircleGroupResponseDTO` | tx=`none` | relevant=`false` | repos=none | dto=CircleGroupResponseDTO
- `toAdminCircleDto` -> `AdminCircleGroupResponseDTO` | tx=`none` | relevant=`false` | repos=none | dto=AdminCircleGroupResponseDTO
- `toViewerRequest` -> `CircleRequestResponseDTO` | tx=`none` | relevant=`false` | repos=none | dto=CircleRequestResponseDTO
- `toSearchResult` -> `CircleSearchResultDTO` | tx=`none` | relevant=`false` | repos=none | dto=CircleSearchResultDTO
- `toRelationDto` -> `CircleRelationDTO` | tx=`none` | relevant=`false` | repos=none | dto=CircleRelationDTO
- `buildCircleContactListResponse` -> `CircleContactListResponseDTO` | tx=`none` | relevant=`false` | repos=none | dto=CircleContactListResponseDTO
- `buildCircleRequestListResponse` -> `CircleRequestListResponseDTO` | tx=`none` | relevant=`false` | repos=none | dto=CircleRequestListResponseDTO
- `buildCircleSearchResultListResponse` -> `CircleSearchResultListResponseDTO` | tx=`none` | relevant=`false` | repos=none | dto=CircleSearchResultListResponseDTO
- `resolveRelationStatus` -> `CircleRelationStatusDTO` | tx=`none` | relevant=`false` | repos=none | dto=CircleRelationStatusDTO

## `DashboardPlannerAssembler`

- `buildPlannerSection` -> `DashboardPlannerSectionDTO` | tx=`none` | relevant=`false` | repos=none | dto=DashboardPlannerSectionDTO

## `DashboardSectionGrouper`

- `buildQuestGroups` -> `List<DashboardQuestGroupDTO>` | tx=`none` | relevant=`true` | repos=none | dto=List<DashboardQuestGroupDTO>
- `buildApplicationGroups` -> `List<DashboardApplicationGroupDTO>` | tx=`none` | relevant=`true` | repos=none | dto=List<DashboardApplicationGroupDTO>

## `DashboardService`

- `getMyDashboard` -> `DashboardResponseDTO` | tx=`class` | relevant=`true` | repos=QuestApplicationRepository | dto=DashboardResponseDTO,DashboardSummaryDTO
- `getMySummary` -> `DashboardSummaryDTO` | tx=`class` | relevant=`true` | repos=QuestApplicationRepository | dto=DashboardSummaryDTO

## `DisabledLocationLookupClient`

- `lookup` -> `List<LocationLookupCandidateDTO>` | tx=`none` | relevant=`false` | repos=none | dto=List<LocationLookupCandidateDTO>

## `GeoapifyLocationLookupClient`

- `lookup` -> `List<LocationLookupCandidateDTO>` | tx=`none` | relevant=`false` | repos=none | dto=List<LocationLookupCandidateDTO>,LocationLookupCandidateDTO

## `LocationGeoService`

- `resolveUserApproximateLocationLabel` -> `String` | tx=`class` | relevant=`false` | repos=none | dto=none

## `LocationLookupService`

- `lookup` -> `LocationLookupResponseDTO` | tx=`none` | relevant=`false` | repos=none | dto=LocationLookupResponseDTO
- `lookupFirst` -> `LocationLookupCandidateDTO` | tx=`none` | relevant=`false` | repos=none | dto=LocationLookupCandidateDTO
- `getDebugStatus` -> `LocationDebugStatusViewDTO` | tx=`method` | relevant=`true` | repos=AppUserRepository,LocationLookupEventRepository,QuestRepository | dto=LocationDebugStatusViewDTO

## `LocationQuestPresentationService`

- `resolveQuestLocationLabel` -> `String` | tx=`class` | relevant=`false` | repos=none | dto=none
- `resolveQuestLocationSourceSummary` -> `String` | tx=`class` | relevant=`false` | repos=none | dto=none
- `resolveQuestLocationVisibilitySummary` -> `String` | tx=`class` | relevant=`false` | repos=none | dto=none

## `LocationSettingsViewService`

- `toDto` -> `UserLocationSettingsDTO` | tx=`class` | relevant=`false` | repos=none | dto=UserLocationSettingsDTO
- `resolveUserLocationSharingSummary` -> `String` | tx=`class` | relevant=`false` | repos=none | dto=none
- `resolveUserLocationVisibilitySummary` -> `String` | tx=`class` | relevant=`false` | repos=none | dto=none

## `QuestAccessPolicyService`

- `resolveViewerRelation` -> `QuestViewerRelationDTO` | tx=`none` | relevant=`false` | repos=none | dto=QuestViewerRelationDTO
- `resolveAllowedActions` -> `List<QuestAllowedActionDTO>` | tx=`none` | relevant=`false` | repos=none | dto=List<QuestAllowedActionDTO>

## `QuestApplicationAdminQueryService`

- `getAllApplicationsForAdmin` -> `List<QuestApplicationResponseDTO>` | tx=`class` | relevant=`true` | repos=QuestApplicationRepository | dto=List<QuestApplicationResponseDTO>
- `searchApplicationsForAdmin` -> `QuestApplicationListResponseDTO` | tx=`class` | relevant=`true` | repos=QuestApplicationRepository | dto=QuestApplicationListResponseDTO

## `QuestApplicationPresentationAssembler`

- `resolveApplicantActions` -> `List<ApplicationAllowedActionDTO>` | tx=`none` | relevant=`false` | repos=none | dto=List<ApplicationAllowedActionDTO>
- `resolveManagementActions` -> `List<ApplicationAllowedActionDTO>` | tx=`none` | relevant=`false` | repos=none | dto=List<ApplicationAllowedActionDTO>

## `QuestApplicationReadService`

- `getApplicationsForQuest` -> `List<QuestApplicationResponseDTO>` | tx=`class` | relevant=`true` | repos=QuestApplicationRepository | dto=List<QuestApplicationResponseDTO>
- `getApplicationsViewForQuest` -> `QuestApplicationsViewDTO` | tx=`class` | relevant=`true` | repos=QuestApplicationRepository | dto=QuestApplicationsViewDTO
- `getPublicApprovedApplicationsViewForQuest` -> `QuestApplicationsViewDTO` | tx=`class` | relevant=`true` | repos=QuestApplicationRepository | dto=QuestApplicationsViewDTO
- `getApplicationsForApplicant` -> `List<QuestApplicationResponseDTO>` | tx=`class` | relevant=`true` | repos=QuestApplicationRepository | dto=List<QuestApplicationResponseDTO>
- `toApplicantResponse` -> `QuestApplicationResponseDTO` | tx=`class` | relevant=`false` | repos=none | dto=QuestApplicationResponseDTO
- `toViewerResponse` -> `QuestApplicationResponseDTO` | tx=`class` | relevant=`false` | repos=none | dto=QuestApplicationResponseDTO

## `QuestApplicationService`

- `getApplicationsForQuest` -> `List<QuestApplicationResponseDTO>` | tx=`class` | relevant=`true` | repos=none | dto=List<QuestApplicationResponseDTO>
- `getApplicationsViewForQuest` -> `QuestApplicationsViewDTO` | tx=`class` | relevant=`true` | repos=none | dto=QuestApplicationsViewDTO
- `getPublicApprovedApplicationsViewForQuest` -> `QuestApplicationsViewDTO` | tx=`class` | relevant=`true` | repos=none | dto=QuestApplicationsViewDTO
- `getApplicationsForApplicant` -> `List<QuestApplicationResponseDTO>` | tx=`class` | relevant=`true` | repos=none | dto=List<QuestApplicationResponseDTO>
- `toApplicantResponse` -> `QuestApplicationResponseDTO` | tx=`class` | relevant=`false` | repos=none | dto=QuestApplicationResponseDTO
- `toViewerResponse` -> `QuestApplicationResponseDTO` | tx=`class` | relevant=`false` | repos=none | dto=QuestApplicationResponseDTO
- `getAllApplicationsForAdmin` -> `List<QuestApplicationResponseDTO>` | tx=`class` | relevant=`true` | repos=none | dto=List<QuestApplicationResponseDTO>
- `searchApplicationsForAdmin` -> `QuestApplicationListResponseDTO` | tx=`class` | relevant=`true` | repos=none | dto=QuestApplicationListResponseDTO

## `QuestApplicationViewAssembler`

- `toApplicantResponse` -> `QuestApplicationResponseDTO` | tx=`none` | relevant=`false` | repos=none | dto=QuestApplicationResponseDTO
- `toManagementResponse` -> `QuestApplicationResponseDTO` | tx=`none` | relevant=`false` | repos=none | dto=QuestApplicationResponseDTO
- `toPublicResponse` -> `QuestApplicationResponseDTO` | tx=`none` | relevant=`false` | repos=none | dto=QuestApplicationResponseDTO
- `toViewerResponse` -> `QuestApplicationResponseDTO` | tx=`none` | relevant=`false` | repos=none | dto=QuestApplicationResponseDTO

## `QuestExecutionPrimitiveService`

- `resolveTarget` -> `Quest` | tx=`method` | relevant=`true` | repos=QuestRepository | dto=none
- `resolveTargetForTermDecision` -> `Quest` | tx=`method` | relevant=`false` | repos=none | dto=none
- `resolveCreator` -> `AppUser` | tx=`method` | relevant=`false` | repos=none | dto=none

## `QuestNewsService`

- `getMyNews` -> `List<QuestNewsItem>` | tx=`class` | relevant=`true` | repos=QuestNewsRepository | dto=none
- `getUnreadCount` -> `long` | tx=`class` | relevant=`true` | repos=QuestNewsRepository | dto=none

## `QuestPresentationAssembler`

- `buildDefaultPresentation` -> `QuestPresentationDTO` | tx=`none` | relevant=`false` | repos=none | dto=QuestPresentationDTO
- `buildPresentation` -> `QuestPresentationDTO` | tx=`none` | relevant=`false` | repos=none | dto=QuestPresentationDTO

## `QuestQueryService`

- `buildQuestPage` -> `PageWindow<Quest>` | tx=`none` | relevant=`false` | repos=none | dto=none

## `QuestReadService`

- `getAllQuests` -> `List<Quest>` | tx=`class` | relevant=`true` | repos=QuestRepository | dto=none
- `getAllQuestResponses` -> `List<QuestResponseDTO>` | tx=`class` | relevant=`true` | repos=none | dto=List<QuestResponseDTO>
- `searchQuests` -> `QuestListResponseDTO` | tx=`class` | relevant=`true` | repos=none | dto=QuestListResponseDTO
- `getQuestListPreset` -> `QuestListResponseDTO` | tx=`class` | relevant=`true` | repos=none | dto=QuestListResponseDTO
- `getQuestById` -> `Quest` | tx=`class` | relevant=`false` | repos=none | dto=none
- `getQuestResponseById` -> `QuestResponseDTO` | tx=`class` | relevant=`true` | repos=none | dto=QuestResponseDTO
- `getQuestDetailResponseById` -> `QuestDetailResponseDTO` | tx=`class` | relevant=`true` | repos=none | dto=NavigationTargetDTO,QuestDetailNavigationSectionDTO,QuestDetailResponseDTO,QuestDetailSectionsDTO
- `getApplicationDetailResponseById` -> `QuestApplicationDetailResponseDTO` | tx=`class` | relevant=`true` | repos=QuestApplicationRepository | dto=QuestApplicationDetailContextSectionDTO,QuestApplicationDetailNavigationSectionDTO,QuestApplicationDetailResponseDTO,QuestApplicationDetailSectionsDTO
- `toResponse` -> `QuestResponseDTO` | tx=`class` | relevant=`false` | repos=none | dto=QuestResponseDTO
- `toResponses` -> `List<QuestResponseDTO>` | tx=`class` | relevant=`false` | repos=none | dto=List<QuestResponseDTO>

## `QuestService`

- `getQuestById` -> `Quest` | tx=`class` | relevant=`false` | repos=none | dto=none
- `getQuestResponseById` -> `QuestResponseDTO` | tx=`class` | relevant=`true` | repos=none | dto=QuestResponseDTO
- `toResponse` -> `QuestResponseDTO` | tx=`class` | relevant=`false` | repos=none | dto=QuestResponseDTO

## `QuestViewAssembler`

- `toResponse` -> `QuestResponseDTO` | tx=`class` | relevant=`false` | repos=QuestApplicationRepository | dto=QuestResponseDTO
- `buildQuestDetailReviewSection` -> `QuestDetailReviewSectionDTO` | tx=`class` | relevant=`false` | repos=UserReviewRepository | dto=QuestDetailReviewSectionDTO
- `buildQuestDetailExecutionSection` -> `QuestDetailExecutionSectionDTO` | tx=`class` | relevant=`false` | repos=none | dto=QuestDetailExecutionSectionDTO
- `buildQuestDetailTermChangeSection` -> `QuestDetailTermChangeSectionDTO` | tx=`class` | relevant=`false` | repos=none | dto=QuestDetailTermChangeSectionDTO
- `buildQuestDetailManagementSection` -> `QuestDetailManagementSectionDTO` | tx=`class` | relevant=`false` | repos=none | dto=QuestDetailManagementSectionDTO

## `QuestVisibilityService`

- `getVisibleCircles` -> `List<CircleGroup>` | tx=`none` | relevant=`false` | repos=none | dto=none

## `RideOfferService`

- `getVisibleOffers` -> `RideOfferListResponseDTO` | tx=`class` | relevant=`true` | repos=RideOfferRepository | dto=RideOfferListResponseDTO
- `getMyOffers` -> `RideOfferListResponseDTO` | tx=`class` | relevant=`true` | repos=RideOfferRepository | dto=RideOfferListResponseDTO

## `SocialRelationActionHelper`

- `searchActions` -> `SearchActions` | tx=`none` | relevant=`false` | repos=none | dto=none

## `ThingSharingService`

- `getAvailableListings` -> `ThingListingListResponseDTO` | tx=`class` | relevant=`true` | repos=ThingListingRepository | dto=ThingListingListResponseDTO
- `getMyListings` -> `ThingListingListResponseDTO` | tx=`class` | relevant=`true` | repos=ThingListingRepository | dto=ThingListingListResponseDTO

## `UserProfileViewService`

- `getProfileView` -> `UserProfileViewDTO` | tx=`class` | relevant=`true` | repos=none | dto=CircleRelationDTO,UserProfileViewDTO

## `UserReviewService`

- `getRatingSummary` -> `UserRatingSummaryDTO` | tx=`method` | relevant=`true` | repos=none | dto=UserRatingSummaryDTO
- `getRecentReviewsForProfile` -> `List<UserReviewResponseDTO>` | tx=`method` | relevant=`true` | repos=UserReviewRepository | dto=List<UserReviewResponseDTO>

## `WorkmarketOptionsService`

- `getOptions` -> `WorkmarketOptionsDTO` | tx=`class` | relevant=`true` | repos=none | dto=AppUserRoleOptionDTO,ExactLocationVisibilityScopeOptionDTO,LocationModeOptionDTO,QuestApplicationStatusFilterOptionDTO,QuestAudienceFilterOptionDTO,QuestAudienceOptionDTO,QuestLocationVisibilityOptionDTO,QuestSearchDefaultsDTO,QuestSortOptionDTO,QuestStatusFilterOptionDTO,QuestStatusOptionDTO,WorkmarketOptionsDTO
