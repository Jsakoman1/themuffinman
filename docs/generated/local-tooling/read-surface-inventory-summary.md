# Read Surface Inventory

- Generated at: `2026-06-30T10:54:35Z`
- Read surfaces: `148`
- Transaction-relevant read surfaces: `86`
- Missing explicit or inherited read-only coverage: `2`

## Top services

- `CircleService`: `20` surfaces, tx-relevant=`19`
  - `getMyCircles` -> `List<CircleRequestResponseDTO>` | tx=`method` | repos=none | dto=List<CircleRequestResponseDTO>
  - `getAdminOverview` -> `AdminCircleOverviewDTO` | tx=`method` | repos=none | dto=AdminCircleOverviewDTO
  - `getOverview` -> `CircleOverviewDTO` | tx=`method` | repos=none | dto=CircleOverviewDTO
- `CircleReadService`: `15` surfaces, tx-relevant=`13`
  - `getMyCircles` -> `List<CircleRequestResponseDTO>` | tx=`method` | repos=CircleRequestRepository | dto=List<CircleRequestResponseDTO>
  - `getAdminOverview` -> `AdminCircleOverviewDTO` | tx=`method` | repos=CircleRequestRepository | dto=AdminCircleOverviewDTO
  - `getOverview` -> `CircleOverviewDTO` | tx=`method` | repos=none | dto=CircleOverviewDTO
- `CircleViewAssembler`: `11` surfaces, tx-relevant=`0`
  - `buildOverview` -> `CircleOverviewDTO` | tx=`none` | repos=none | dto=CircleOverviewDTO
  - `toContact` -> `CircleContactDTO` | tx=`none` | repos=none | dto=CircleContactDTO
  - `toCircleDto` -> `CircleGroupResponseDTO` | tx=`none` | repos=none | dto=CircleGroupResponseDTO
- `QuestReadService`: `10` surfaces, tx-relevant=`7`
  - `getAllQuests` -> `List<Quest>` | tx=`class` | repos=QuestRepository | dto=none
  - `getAllQuestResponses` -> `List<QuestResponseDTO>` | tx=`class` | repos=none | dto=List<QuestResponseDTO>
  - `searchQuests` -> `QuestListResponseDTO` | tx=`class` | repos=none | dto=QuestListResponseDTO
- `QuestApplicationService`: `8` surfaces, tx-relevant=`6`
  - `getApplicationsForQuest` -> `List<QuestApplicationResponseDTO>` | tx=`class` | repos=none | dto=List<QuestApplicationResponseDTO>
  - `getApplicationsViewForQuest` -> `QuestApplicationsViewDTO` | tx=`class` | repos=none | dto=QuestApplicationsViewDTO
  - `getPublicApprovedApplicationsViewForQuest` -> `QuestApplicationsViewDTO` | tx=`class` | repos=none | dto=QuestApplicationsViewDTO
- `CircleDiscoveryService`: `7` surfaces, tx-relevant=`7`
  - `getNearbyUsers` -> `CircleSearchResultListResponseDTO` | tx=`class` | repos=AppUserRepository | dto=CircleSearchResultListResponseDTO
  - `getInviteCandidates` -> `List<CircleSearchResultDTO>` | tx=`class` | repos=none | dto=List<CircleSearchResultDTO>
  - `getInviteCandidatesPage` -> `CircleSearchResultListResponseDTO` | tx=`class` | repos=AppUserRepository | dto=CircleSearchResultListResponseDTO
- `QuestApplicationReadService`: `6` surfaces, tx-relevant=`4`
  - `getApplicationsForQuest` -> `List<QuestApplicationResponseDTO>` | tx=`class` | repos=QuestApplicationRepository | dto=List<QuestApplicationResponseDTO>
  - `getApplicationsViewForQuest` -> `QuestApplicationsViewDTO` | tx=`class` | repos=QuestApplicationRepository | dto=QuestApplicationsViewDTO
  - `getPublicApprovedApplicationsViewForQuest` -> `QuestApplicationsViewDTO` | tx=`class` | repos=QuestApplicationRepository | dto=QuestApplicationsViewDTO
- `QuestViewAssembler`: `5` surfaces, tx-relevant=`0`
  - `toResponse` -> `QuestResponseDTO` | tx=`class` | repos=QuestApplicationRepository | dto=QuestResponseDTO
  - `buildQuestDetailReviewSection` -> `QuestDetailReviewSectionDTO` | tx=`class` | repos=UserReviewRepository | dto=QuestDetailReviewSectionDTO
  - `buildQuestDetailExecutionSection` -> `QuestDetailExecutionSectionDTO` | tx=`class` | repos=none | dto=QuestDetailExecutionSectionDTO
- ... 36 more services

## Read surface sample

- `BusinessProfileService.getDirectory` -> `BusinessProfileListResponseDTO` | tx=`class` | relevant=`true`
- `BusinessProfileService.getProfileBySlug` -> `BusinessProfileResponseDTO` | tx=`class` | relevant=`true`
- `BusinessProfileService.getMyProfile` -> `BusinessProfileResponseDTO` | tx=`class` | relevant=`true`
- `ChatService.getWorkspace` -> `ChatWorkspaceDTO` | tx=`method` | relevant=`true`
- `ChatService.getConversationMessages` -> `List<ChatMessageDTO>` | tx=`method` | relevant=`true`
- `AdminUserDetailService.getDetail` -> `AdminUserDetailDTO` | tx=`class` | relevant=`true`
- `AppUserService.getAllAppUsers` -> `List<AppUser>` | tx=`method` | relevant=`true`
- `AppUserService.getAppUser` -> `AppUser` | tx=`method` | relevant=`false`
- `IdentityUserSummaryAssembler.buildProfileSummary` -> `AppUserResponseDTO` | tx=`none` | relevant=`false`
- `UserProfileViewService.getProfileView` -> `UserProfileViewDTO` | tx=`class` | relevant=`true`