# Domain Technical Notes

This document is the technical source of truth for core product behavior. It should track entities, relations, validations, permissions, and workflow rules as they exist in code.

## Domain Areas

- `identity`: users, authentication, roles, and profile data
- `social`: circles, memberships, requests, blocking, and relation lookup
- `workmarket`: quests, applications, quest news, dashboards, reviews
- `chat`: direct conversations, messages, presence, realtime updates
- `location`: user location settings, quest location visibility, lookup events
- `internal sandbox`: synthetic test-data orchestration kept separate from production-like user actions

## Identity Source Map

Primary files:
- `identity/controller/AuthController.java`
- `identity/controller/AppUserController.java`
- `identity/model/AppUser.java`
- `identity/model/AppUserRole.java`
- `identity/service/AppUserService.java`
- `identity/service/AppUserLookupService.java`
- `identity/service/UserProfileViewService.java`
- `identity/service/AdminUserDetailService.java`
- `identity/security/JwtService.java`
- `identity/security/JwtAuthFilter.java`
- `identity/security/RepositoryUserDetailsService.java`
- `identity/mapper/AppUserMgr.java`
- `identity/repository/AppUserRepository.java`
- `identity/dto/AppUserRequestDTO.java`
- `identity/dto/AppUserResponseDTO.java`
- `identity/dto/UserProfileViewDTO.java`
- `identity/dto/AdminUserDetailDTO.java`
- `identity/dto/auth/RegisterRequest.java`
- `identity/dto/auth/LoginRequest.java`
- `identity/dto/auth/AuthResponse.java`

Primary migrations:
- `V1__init.sql`
- `V4__insert_column_hashPassword_on_appUser.sql`
- `V5__add_role_to_app_user.sql`
- `V11__add_app_user_profile_fields.sql`
- `V14__add_app_user_created_at.sql`
- `V16__enforce_case_insensitive_user_email.sql`

## Identity subdomain map

### Authentication

Primary files:
- `identity/controller/AuthController.java`
- `identity/security/JwtService.java`
- `identity/security/JwtAuthFilter.java`
- `identity/security/RepositoryUserDetailsService.java`
- `identity/dto/auth/RegisterRequest.java`
- `identity/dto/auth/LoginRequest.java`
- `identity/dto/auth/AuthResponse.java`

### User account management

Primary files:
- `identity/controller/AppUserController.java`
- `identity/service/AppUserService.java`
- `identity/service/AppUserLookupService.java`
- `identity/repository/AppUserRepository.java`
- `identity/dto/AppUserRequestDTO.java`

### Profile and profile view

Primary files:
- `identity/model/AppUser.java`
- `identity/service/UserProfileViewService.java`
- `identity/mapper/AppUserMgr.java`
- `identity/dto/AppUserResponseDTO.java`
- `identity/dto/UserProfileViewDTO.java`

Technical notes:
- `UserProfileViewDTO` now carries deterministic resolution metadata so future automation can refer to an exact profile target without rebuilding labels client-side.
- Current-location profile updates should resolve trusted coordinates before they reuse the normal self-update location flow.

### Admin user detail

Primary files:
- `identity/service/AdminUserDetailService.java`
- `identity/dto/AdminUserDetailDTO.java`

### Security integration

Primary files:
- `identity/security/JwtService.java`
- `identity/security/JwtAuthFilter.java`
- `identity/security/RepositoryUserDetailsService.java`

## Location Source Map

Primary files:
- `location/controller/LocationLookupController.java`
- `location/model/UserLocationMode.java`
- `location/model/QuestLocationVisibility.java`
- `location/model/QuestLocationSource.java`
- `location/model/ExactLocationVisibilityScope.java`
- `location/model/LocationLookupEvent.java`
- `location/model/LocationLookupEventType.java`
- `location/service/LocationSettingsService.java`
- `location/service/LocationLookupService.java`
- `location/service/GeoapifyLocationLookupClient.java`
- `location/service/DisabledLocationLookupClient.java`
- `location/service/AdminDatabaseMetricsService.java`
- `location/repository/LocationLookupEventRepository.java`
- `location/dto/UserLocationSettingsDTO.java`
- `location/dto/UserLocationSettingsRequestDTO.java`
- `location/dto/LocationLookupCandidateDTO.java`
- `location/dto/LocationLookupResponseDTO.java`
- `location/dto/LocationDebugStatusDTO.java`

Primary migrations:
- `V23__add_location_settings.sql`
- `V24__enable_postgis_for_quest_location_search.sql`
- `V25__add_location_provider_metadata.sql`
- `V26__add_user_exact_location_visibility.sql`
- `V28__create_location_lookup_event_table.sql`

## Location subdomain map

### User location settings

Primary files:
- `location/service/LocationSettingsService.java`
- `location/model/UserLocationMode.java`
- `location/model/ExactLocationVisibilityScope.java`
- `location/dto/UserLocationSettingsDTO.java`
- `location/dto/UserLocationSettingsRequestDTO.java`

### Quest location behavior

Primary files:
- `location/service/LocationSettingsService.java`
- `location/model/QuestLocationVisibility.java`
- `location/model/QuestLocationSource.java`

### Lookup and reverse lookup

Primary files:
- `location/controller/LocationLookupController.java`
- `location/service/LocationLookupService.java`
- `location/service/GeoapifyLocationLookupClient.java`
- `location/service/DisabledLocationLookupClient.java`
- `location/dto/LocationLookupCandidateDTO.java`
- `location/dto/LocationLookupResponseDTO.java`

### Exact visibility and nearby discovery support

Primary files:
- `location/service/LocationSettingsService.java`
- `social/service/CircleMembershipService.java`
- `social/service/CircleService.java`

### Provider metrics and audit

Primary files:
- `location/service/LocationLookupService.java`
- `location/service/AdminDatabaseMetricsService.java`
- `location/model/LocationLookupEvent.java`
- `location/repository/LocationLookupEventRepository.java`
- `location/dto/LocationDebugStatusDTO.java`

## Chat Source Map

Primary files:
- `chat/controller/ChatController.java`
- `chat/model/ChatConversation.java`
- `chat/model/ChatMessage.java`
- `chat/model/ChatPresence.java`
- `chat/service/ChatService.java`
- `chat/service/ChatPresenceService.java`
- `chat/service/ChatRealtimeService.java`
- `chat/service/ChatRetentionService.java`
- `chat/repository/ChatConversationRepository.java`
- `chat/repository/ChatMessageRepository.java`
- `chat/repository/ChatPresenceRepository.java`
- `chat/websocket/ChatWebSocketHandler.java`
- `chat/websocket/ChatWebSocketAuthInterceptor.java`
- `chat/dto/ChatWorkspaceDTO.java`
- `chat/dto/ChatConversationSummaryDTO.java`
- `chat/dto/ChatMessageDTO.java`
- `chat/dto/ChatMessageRequestDTO.java`
- `chat/dto/ChatOpenConversationRequestDTO.java`
- `chat/dto/ChatSocketEventDTO.java`
- `chat/dto/ChatSocketClientMessageDTO.java`
- `chat/dto/ChatContactDTO.java`
- `chat/dto/ChatCircleOptionDTO.java`

Primary migrations:
- `V22__create_chat_tables.sql`

## Chat subdomain map

### Conversation access and lifecycle

Primary files:
- `chat/service/ChatService.java`
- `chat/model/ChatConversation.java`
- `chat/repository/ChatConversationRepository.java`

### Message flow

Primary files:
- `chat/service/ChatService.java`
- `chat/model/ChatMessage.java`
- `chat/repository/ChatMessageRepository.java`

### Workspace and contact read model

Primary files:
- `chat/service/ChatService.java`
- `chat/dto/ChatWorkspaceDTO.java`
- `chat/dto/ChatConversationSummaryDTO.java`
- `chat/dto/ChatContactDTO.java`
- `chat/dto/ChatCircleOptionDTO.java`

Technical notes:
- `ChatConversationSummaryDTO` and `ChatContactDTO` now carry deterministic resolution metadata for exact conversation or accepted-contact targeting.
- Chat read models are still filtered by current accepted relation state before any resolution metadata is returned.
- Agent-safe chat-read planning now relies on current workspace conversations instead of stale client memory.

### Presence and realtime

Primary files:
- `chat/service/ChatPresenceService.java`
- `chat/service/ChatRealtimeService.java`
- `chat/model/ChatPresence.java`
- `chat/websocket/ChatWebSocketHandler.java`
- `chat/websocket/ChatWebSocketAuthInterceptor.java`
- `chat/dto/ChatSocketEventDTO.java`
- `chat/dto/ChatSocketClientMessageDTO.java`

### Retention and cleanup

Primary files:
- `chat/service/ChatRetentionService.java`
- `chat/service/ChatService.java`
- `chat/repository/ChatMessageRepository.java`

## Internal sandbox notes

Technical rules:
- Sandbox flow must stay separate from real-life product flow.
- Internal sandbox flow is a separate capability from production-like voice flow.
- sandbox behavior must stay separate from production mutation semantics
- sandbox flows are intended for admin or developer-controlled environments only
- synthetic multi-actor actions must be modeled explicitly instead of being hidden inside production intents
- synthetic entities should carry an identifiable marker once backend implementation exists

Planned capabilities:
- synthetic user creation batches
- synthetic circles and memberships
- synthetic quest applications
- synthetic quest lifecycle transitions for test scenarios
- synthetic review creation after synthetic completion

## Admin agent playground

Primary files:
- `agent/controller/AdminAgentController.java`
- `agent/service/AdminAgentPlaygroundService.java`
- `agent/dto/AdminAgentPlaygroundRequestDTO.java`
- `agent/dto/AdminAgentPlaygroundResponseDTO.java`
- `config/AgentProperties.java`

Technical notes:
- The current admin agent playground is an admin-only planning surface.
- Prompt classification now runs through a translation layer before intent heuristics.
- Local translation is a deterministic fallback, while provider-backed translation is the path for arbitrary languages such as Mandarin.
- The response now also includes structured resolution requirements, clarification contract data, and execution-readiness metadata.
- Planner coverage now extends the same reusable fail-closed contract across owned-quest updates, pending-application self-service, outgoing request cancellation, owner-circle management, and chat read actions.
- The same planner contract now also covers self-profile updates, exact notification targeting, and admin-side application correction or deletion.
- The machine model now also enumerates common read surfaces and auth-adjacent capabilities so future executors do not derive endpoint affordances from frontend behavior alone.
- It accepts free-form prompts and returns structured warnings, suggested workflows, and next steps from deterministic backend classification.
- The deterministic payload should keep structured planner fields separate from provider-written summary text, including matched signals and unresolved required inputs.
- Provider-written summary text should be constrained to the deterministic workflow ids, signals, and unresolved inputs already classified by the backend.
- It does not execute mutations.
- If `app.agent.provider=openai` and a backend API key is configured, the service also requests a concise planning summary from OpenAI through the Responses API.
- If the provider is missing or the external call fails, the service falls back to the deterministic local planner and keeps the same response contract.
- Provider credentials stay on the server side and are never exposed through frontend configuration.

## Social Source Map

Primary files:
- `social/controller/CircleController.java`
- `social/model/CircleGroup.java`
- `social/model/CircleMembership.java`
- `social/model/CircleRequest.java`
- `social/service/CircleService.java`
- `social/service/CircleRelationService.java`
- `social/service/CircleMembershipService.java`
- `social/service/CircleDiscoveryService.java`
- `social/service/CircleViewAssembler.java`
- `social/service/SocialPresentationHelper.java`
- `social/service/SocialRelationActionHelper.java`
- `social/mapper/CircleRequestMgr.java`

Primary migrations:
- `V12__create_circle_request_table_and_add_quest_audience.sql`
- `V13__add_circle_blocking_and_circle_notifications.sql`
- `V19__create_crews_and_quest_visibility.sql`
- `V20__rename_crew_tables_to_circles.sql`
- `V29__add_circle_request_reference_to_news.sql`

## Social subdomain map

### Relationship lifecycle

Primary files:
- `social/service/CircleRelationService.java`
- `social/model/CircleRequest.java`
- `social/dto/CircleRelationStatus.java`
- `social/mapper/CircleRequestMgr.java`

### Circle groups

Primary files:
- `social/service/CircleService.java`
- `social/model/CircleGroup.java`

### Circle memberships

Primary files:
- `social/service/CircleMembershipService.java`
- `social/model/CircleMembership.java`

### Contacts and overviews

Primary files:
- `social/service/CircleService.java`
- `social/service/CircleViewAssembler.java`

### Search and nearby discovery

Primary files:
- `social/service/CircleService.java`
- `social/service/CircleDiscoveryService.java`
- `social/service/CircleViewAssembler.java`

### Presentation and action contracts

Primary files:
- `social/service/SocialPresentationHelper.java`
- `social/service/SocialRelationActionHelper.java`
- `social/service/CircleViewAssembler.java`

Technical notes:
- `CircleSearchResultDTO` and `CircleRelationDTO` now carry deterministic resolution metadata for exact candidate targeting in future automation.
- `CircleRequestResponseDTO` and `CircleGroupResponseDTO` now also carry deterministic resolution metadata for exact outgoing-request and owner-circle targeting.
- `AppUserResponseDTO` now also carries deterministic resolution metadata for exact account targeting in admin or general user-management flows.

## Workmarket Source Map

Primary files:
- `workmarket/controller/QuestController.java`
- `workmarket/controller/QuestApplicationController.java`
- `workmarket/controller/QuestNewsController.java`
- `workmarket/controller/DashboardController.java`
- `workmarket/controller/UserReviewController.java`
- `workmarket/model/Quest.java`
- `workmarket/model/QuestApplication.java`
- `workmarket/model/QuestApplicationStatus.java`
- `workmarket/model/QuestAudience.java`
- `workmarket/model/QuestNewsItem.java`
- `workmarket/model/QuestNewsType.java`
- `workmarket/model/QuestStatus.java`
- `workmarket/service/QuestService.java`
- `workmarket/service/QuestUpdateService.java`
- `workmarket/service/QuestValidationService.java`
- `workmarket/service/QuestStateTransitionService.java`
- `workmarket/service/QuestVisibilityService.java`
- `workmarket/service/QuestApplicationService.java`
- `workmarket/service/QuestQueryService.java`
- `workmarket/service/QuestNewsService.java`
- `workmarket/service/DashboardService.java`
- `workmarket/service/QuestAccessPolicyService.java`
- `workmarket/service/QuestWorkflowNotificationService.java`
- `workmarket/service/QuestViewAssembler.java`
- `workmarket/service/WorkmarketOptionsService.java`
- `workmarket/service/WorkmarketPresentationHelper.java`

Primary migrations:
- `V2__create_quest_tables.sql`
- `V3__create_quest_application_table.sql`
- `V7__add_quest_term_fields.sql`
- `V8__add_quest_reopened_at.sql`
- `V9__create_quest_news_item_table.sql`
- `V10__add_quest_news_read_at.sql`
- `V12__create_circle_request_table_and_add_quest_audience.sql`
- `V15__create_quest_image_table.sql`
- `V17__add_quest_end_term_fields.sql`
- `V18__add_quest_assignee_target.sql`
- `V21__create_user_review_table.sql`
- `V29__add_circle_request_reference_to_news.sql`
- `V30__add_public_approved_workers_flag.sql`
- `V27__add_quest_location_source.sql`

## Workmarket subdomain map

### Quest core

Primary files:
- `workmarket/service/QuestService.java`
- `workmarket/service/QuestUpdateService.java`
- `workmarket/service/QuestValidationService.java`
- `workmarket/service/QuestVisibilityService.java`
- `workmarket/service/QuestAccessPolicyService.java`
- `workmarket/service/QuestQueryService.java`
- `workmarket/service/QuestViewAssembler.java`
- `workmarket/model/Quest.java`
- `workmarket/model/QuestStatus.java`
- `workmarket/model/QuestAudience.java`
- `workmarket/mapper/QuestMgr.java`
- `workmarket/dto/QuestRequestDTO.java`
- `workmarket/dto/QuestSearchRequestDTO.java`
- `workmarket/dto/QuestListPreset.java`

Technical notes:
- `QuestRequestDTO` requires `awardAmount`, but the accepted floor is `0.00`, not `0.01`.
- `QuestValidationService` treats `awardAmount == 0` as a valid free-quest configuration and still rejects negative values.

### Applications

Primary files:
- `workmarket/service/QuestApplicationService.java`
- `workmarket/model/QuestApplication.java`
- `workmarket/model/QuestApplicationStatus.java`
- `workmarket/mapper/QuestApplicationMgr.java`
- `workmarket/dto/QuestApplicationRequestDTO.java`
- `workmarket/dto/AdminQuestApplicationUpdateRequestDTO.java`
- `workmarket/dto/QuestApplicationsViewDTO.java`
- `workmarket/dto/QuestApplicationDetailResponseDTO.java`
- `workmarket/dto/AdminApplicationsQueryDTO.java`

Technical notes:
- `QuestApplicationRequestDTO.proposedPrice` is conditional on quest pricing rather than universally required.
- Paid quests require `proposedPrice >= 0.01`.
- Free quests require `proposedPrice == null`.
- `QuestApplicationsViewDTO` now carries deterministic owner-side pending selection metadata through `pendingApplicationCount` and `oldestPendingApplicationId`.
- `QuestResponseDTO` and `QuestApplicationResponseDTO` now carry deterministic resolution metadata for exact target selection.
- Applicant-side pending-application update and withdrawal flows are modeled as exact application resolution followed by the same backend validation rules used by the existing service methods.
- `QuestNewsItemResponseDTO` now also carries deterministic resolution metadata so item-specific notification actions can target one exact backend row.
- `QuestApplicationService.approveApplication` and `QuestApplicationService.declineApplication` both require an `OPEN` quest plus owner-or-admin authority.
- `QuestApplicationService.declineApplication` only mutates `PENDING` applications and transitions them to `DECLINED`.

### Execution and term negotiation

Primary files:
- `workmarket/service/QuestStateTransitionService.java`
- `workmarket/service/QuestService.java`
- `workmarket/service/QuestAccessPolicyService.java`
- `workmarket/service/QuestWorkflowNotificationService.java`
- `workmarket/dto/QuestAllowedAction.java`
- `workmarket/dto/QuestDetailExecutionAction.java`
- `workmarket/dto/QuestDetailExecutionSectionDTO.java`
- `workmarket/dto/QuestDetailTermChangeSectionDTO.java`

Technical notes:
- `QuestStateTransitionService.validateQuestExecutionAuthority` allows owner, admin, or approved applicant to run execution actions.
- `QuestService.startQuest` requires `ASSIGNED` and transitions the quest to `IN_PROGRESS`.
- `QuestService.completeQuest` requires `IN_PROGRESS` and transitions the quest to `COMPLETED`.
- `QuestStateTransitionService.validateQuestTermDecisionAuthority` allows only admin or approved applicant to confirm or reject pending term changes.
- `QuestStateTransitionService.confirmQuestTermChange` applies pending term fields, restores the previous quest status, and clears the pending state.
- `QuestStateTransitionService.rejectQuestTermChange` restores the previous quest status without applying pending term fields, then clears the pending state.
- If `termChangePreviousStatus` is missing while resolving a term decision, the backend fails instead of inventing a fallback status.
- `QuestAccessPolicyService` exposes these transitions to the frontend as allowed actions: `START`, `COMPLETE`, `CONFIRM_TERM_CHANGE`, and `REJECT_TERM_CHANGE`.

Voice-flow notes:
- Relative schedule input such as `tomorrow at 15:00` must be resolved into an absolute timestamp in caller timezone before `QuestRequestDTO` is sent.
- Selected-people audience preparation must resolve names or phrases into concrete accepted connections before circle assignment.
- Owner-side quest commands such as approve, decline, or delete must resolve exactly one owned quest before mutation.
- Approve-the-first-applicant style commands must use deterministic backend pending-selection metadata instead of incidental frontend ordering.
- A prepare-to-start automation flow still requires separate applicant-side `apply` actions and owner-side `approve` actions before `start`.
- Unexpected applicants outside the resolved selected-people set must not be auto-approved; they require explicit decline or manual handling.
- Owner-side term change after `ASSIGNED` or `IN_PROGRESS` can route through `QuestUpdateService` into `WAITING_CONFIRMATION`.
- Review creation is a separate post-completion mutation and must only target the employer or an approved worker of the completed quest.

### Dashboard and notifications

Primary files:
- `workmarket/service/DashboardService.java`
- `workmarket/service/DashboardSectionsFactory.java`
- `workmarket/service/QuestNewsService.java`
- `workmarket/model/QuestNewsItem.java`
- `workmarket/model/QuestNewsType.java`
- `workmarket/mapper/QuestNewsMgr.java`
- `workmarket/dto/DashboardResponseDTO.java`
- `workmarket/dto/DashboardSummaryDTO.java`
- `workmarket/dto/DashboardSectionsDTO.java`
- `workmarket/dto/QuestNewsItemResponseDTO.java`

### Reviews and ratings

Primary files:
- `workmarket/service/UserReviewService.java`
- `workmarket/model/UserReview.java`
- `workmarket/model/ReviewRole.java`
- `workmarket/mapper/UserReviewMgr.java`
- `workmarket/dto/UserReviewRequestDTO.java`
- `workmarket/dto/UserReviewResponseDTO.java`
- `workmarket/dto/UserRatingSummaryDTO.java`
- `workmarket/repository/UserReviewRepository.java`

### Options and presentation contracts

Primary files:
- `workmarket/service/WorkmarketOptionsService.java`
- `workmarket/service/WorkmarketPresentationHelper.java`
- `workmarket/dto/QuestResponseDTO.java`
- `workmarket/dto/QuestApplicationResponseDTO.java`
- `workmarket/dto/WorkmarketOptionsDTO.java`
- `workmarket/dto/QuestPresentationDTO.java`
- `workmarket/dto/QuestApplicationPresentationDTO.java`
- `workmarket/dto/QuestSearchDefaultsDTO.java`

## Frontend Source Map

Primary files:
- `frontend/src/router.ts`
- `frontend/src/api/httpClient.ts`
- `frontend/src/api/apiErrors.ts`
- `frontend/src/modules/identity/api/authApi.ts`
- `frontend/src/modules/workmarket/api/workmarketApi.ts`
- `frontend/src/modules/workmarket/api/contracts.ts`
- `frontend/src/modules/workmarket/api/clients/questsApi.ts`
- `frontend/src/modules/workmarket/api/clients/applicationsApi.ts`
- `frontend/src/modules/workmarket/api/clients/dashboardApi.ts`
- `frontend/src/modules/workmarket/api/clients/newsApi.ts`
- `frontend/src/modules/workmarket/api/clients/usersApi.ts`
- `frontend/src/modules/workmarket/api/clients/circlesApi.ts`
- `frontend/src/modules/workmarket/api/clients/locationApi.ts`
- `frontend/src/modules/chat/api/chatApi.ts`
- `frontend/src/contracts/index.ts`
- `frontend/src/contracts/generated/themuffinmanContract.ts`

Primary route entrypoints:
- `frontend/src/modules/workmarket/pages/QuestsPage.vue`
- `frontend/src/modules/workmarket/views/QuestDetailView.vue`
- `frontend/src/modules/workmarket/views/ApplicationDetailView.vue`
- `frontend/src/modules/workmarket/pages/AdminOverviewPage.vue`
- `frontend/src/modules/workmarket/pages/AdminUsersPage.vue`
- `frontend/src/modules/social/views/CirclesView.vue`
- `frontend/src/modules/social/views/UserProfileView.vue`
- `frontend/src/modules/social/views/UserSettingsView.vue`
- `frontend/src/modules/identity/views/LoginView.vue`
- `frontend/src/modules/identity/views/RegisterView.vue`

## Key Entity Map

### Identity

- `AppUser`
- `AppUserRole`

## Identity Core Entities

`AppUser` currently stores:
- email
- username
- profile description
- profile avatar data URL
- created timestamp
- password hash
- role
- location mode
- exact location visibility scope
- location radius
- saved location label and provider metadata
- saved coordinates and saved-location timestamps
- exact-location allowed circles
- exact-location allowed users

Technical rules:
- email is unique
- role defaults to `USER`
- created timestamp is set at entity creation
- location settings live directly on the user aggregate instead of a separate profile settings table
- exact-location visibility is represented by both a scope enum and explicit circle or user allow-lists

`AppUserRole` currently distinguishes:
- `USER`
- `ADMIN`

## Identity Authentication Flow

Register flow:
- `/auth/register` normalizes email before uniqueness checks
- registration persists a user with encoded password and role `USER`
- registration returns an auth response plus a freshly issued JWT token

Login flow:
- `/auth/login` resolves user by normalized email
- password verification uses the stored password hash
- failed email lookup and failed password match both return the same unauthorized error message
- successful login returns a fresh JWT token

Session flow:
- `/auth/me` returns the authenticated principal mapped into auth response shape
- this response does not mint a new token

JWT rules:
- token subject is user email
- custom claims include `userId` and `username`
- expiration is driven by `SecurityProperties.Jwt`
- invalid JWTs clear security context and continue filter chain without authenticating the request

## Identity Account Management Rules

Create and update validation:
- request DTO is required
- email is required, normalized, and validated with explicit pattern and max length checks
- username is required after trim
- username length must be between `3` and `50`
- password is required for create
- password length must be between `8` and `100`
- email uniqueness is checked case-insensitively

Update behavior:
- self update changes username, email, profile description, avatar, and location settings
- self update does not let the user change role directly
- admin update can also change role
- admin update can replace password when a non-blank password is supplied
- profile description is sanitized on write
- avatar value is normalized on write
- location settings are delegated to `LocationSettingsService`

Delete behavior:
- a user cannot delete their own account
- the last remaining admin cannot be deleted

List and lookup behavior:
- general user list filtering uses normalized search query text
- search matches username, email, role, and profile description
- result ordering is by username case-insensitive ascending
- profile stats enrichments come from open quests created by the user

## Identity Profile and Read Models

Base user DTO currently exposes:
- id
- email
- username
- profile navigation target
- sanitized description
- avatar
- location settings DTO
- created timestamp
- resolved role name

Profile-view rules:
- own-profile view bypasses social relation lookup and forces a neutral relation state
- other-profile view resolves relation through `CircleService`
- primary profile action is derived through `SocialRelationActionHelper`
- profile view includes block-action affordances when relation is not already blocked
- profile view includes open quest count plus up to `6` recent open quests
- profile view includes employer and worker rating summaries plus recent reviews

Admin-detail rules:
- admin access is mandatory
- admin detail combines:
  - enriched user DTO
  - selectable app-user roles
  - selectable location modes
  - selectable exact-location visibility scopes
  - circles for target user from admin perspective
  - contacts for target user from admin perspective

### Social

- `CircleGroup`
- `CircleMembership`
- `CircleRequest`

### Workmarket

- `Quest`
- `QuestApplication`
- `QuestNewsItem`
- `UserReview`

### Chat

- `ChatConversation`
- `ChatMessage`
- `ChatPresence`

### Location

- `LocationLookupEvent`
- quest location fields are stored on `Quest`
- user location settings are stored on the user profile

## Location Core Concepts

Location enums currently include:
- `UserLocationMode`: `OFF`, `APPROXIMATE`, `EXACT`
- `QuestLocationVisibility`: `INHERIT`, `OFF`, `APPROXIMATE`, `EXACT`
- `QuestLocationSource`: `PROFILE`, `CUSTOM`
- `ExactLocationVisibilityScope`: `NOBODY`, `EVERYONE`, `CIRCLES`, `USERS`

`LocationLookupEvent` currently stores:
- provider
- request type
- created timestamp

Technical notes:
- user location data is stored directly on `AppUser`
- quest location data is stored directly on `Quest`
- lookup-event storage is append-only operational telemetry for provider usage

## Social Core Entities

`CircleRequest` currently stores:
- requester
- recipient
- created timestamp
- accepted timestamp
- blocked timestamp
- blocker reference

Technical rules:
- unique directional pair on `(requester_id, recipient_id)`
- accepted and blocked states are represented on the same relation record type
- blocking is represented by `blockedAt` plus `blockedBy`

`CircleGroup` currently stores:
- owner
- name
- created timestamp
- memberships

Technical rules:
- unique name per owner

`CircleMembership` currently stores:
- circle
- member
- created timestamp

Technical rules:
- unique `(circle_id, member_user_id)` pair

## Social Relationship Lifecycle

Current relation states are derived from one `CircleRequest` record:
- `NONE`
- `INCOMING_REQUEST`
- `OUTGOING_REQUEST`
- `CIRCLE`
- `BLOCKED`

Lifecycle behavior:
- creating an invite creates a `CircleRequest` with no `acceptedAt` and no `blockedAt`
- accepting an invite sets `acceptedAt`
- deleting an accessible non-blocked relation removes the row
- blocking deletes any existing non-blocked relation first, then inserts a blocked row
- unblocking deletes the blocked row

Authority and validation rules:
- self-invite is rejected
- self-block is rejected
- self-unblock is rejected
- if a blocked relation exists, new invites are rejected
- only the recipient can accept a request
- only requester or recipient can delete a non-blocked accessible request
- blocked relations cannot be removed through the generic delete-request action
- only the user recorded in `blockedBy` can unblock

## Circle Groups and Memberships

Group management rules:
- create requires a normalized name
- names are unique only inside the owner scope
- owner can update and delete their own groups
- admin can delete groups platform-wide

Membership rules:
- membership changes only affect circles owned by the acting owner
- requested circle ids are validated against owner-owned circles
- contacts can only be organized into circles if an accepted relation exists
- syncing memberships removes circles not present in the requested set and inserts missing ones
- bulk updates apply one add/remove action across many connected users

## Contacts, Overviews, and Request Lists

Overview contract:
- `CircleOverviewDTO` summarizes:
  - connection count
  - unassigned connection count
  - incoming request count
  - outgoing request count

Contact shaping rules:
- contacts are built from accepted relations
- contact circle assignments are resolved only from circles owned by the current viewer
- unassigned contacts remain valid and get summary label `Unassigned`

Request-list rules:
- incoming requests are pending requests where current user is recipient
- outgoing requests are pending requests where current user is requester
- viewer-facing request DTOs rewrite counterpart fields depending on incoming vs outgoing perspective

## Search and Discovery Contracts

Search behavior:
- general circle user search requires normalized query length of at least 2 characters
- invite candidates are users with relation status `NONE`
- blocked-users view only returns users blocked by the current user
- search results are relation-aware and include action DTOs

Nearby discovery behavior:
- nearby discovery requires current user to be discoverable via saved location
- candidate must not be self
- candidate must also be discoverable nearby
- blocked candidates are excluded
- distance is computed from saved coordinates
- default nearby radius is `2 km` when not provided
- provided radius is normalized through shared location rules

Nearby result shaping:
- includes approximate location label
- includes numeric distance
- includes human-readable distance label in meters or kilometers

## Social Presentation and Action Contracts

`SocialPresentationHelper` currently owns:
- relation labels
- relation badge classes
- circle summary label
- member preview label
- request summary label

Current label semantics:
- `NONE` -> `Not connected`
- `CIRCLE` -> `Connected`
- `INCOMING_REQUEST` -> `Invite received`
- `OUTGOING_REQUEST` -> `Invite sent`
- `BLOCKED` -> `Blocked`

`SocialRelationActionHelper` currently owns:
- search-result primary and secondary actions
- request-list actions
- profile primary action
- block action label

Current action semantics:
- `NONE` search result -> send invite + block
- blocked-by-current-user result -> unblock
- blocked-by-other result -> disabled blocked state
- incoming request -> accept + decline
- outgoing request -> cancel
- connected profile -> disabled connected state
- incoming-request profile -> navigation to circles

## Admin Social Views

Admin overview currently assembles:
- all circles
- accepted connections
- pending requests
- blocked relations

Admin filtering behavior:
- circle/admin relation rows are filtered by normalized query
- accepted/pending/blocked rows are derived from timestamps on the relation record

## Chat Core Entities

`ChatConversation` currently stores:
- normalized left participant
- normalized right participant
- created timestamp
- last message timestamp
- last message preview
- last message sender
- last-message-has-image flag

Technical rules:
- one unique pair per normalized participant ids
- participants are ordered by user id for uniqueness

`ChatMessage` currently stores:
- conversation
- sender
- optional message body
- optional image data url
- created timestamp
- optional read timestamp

Technical rules:
- message body max length is 2000 at request-contract level
- image data url max length is 350000 at request-contract level
- message must contain text, image, or both

`ChatPresence` currently stores:
- user
- last active timestamp

Technical rules:
- one presence row per user

## Chat Conversation Access and Lifecycle

Access rules:
- opening a conversation requires another user id
- self-chat is rejected
- counterpart must currently be connected through accepted social relation
- Pending circle requests do not create chat eligibility.
- accessing existing conversation messages requires that the current user is one of the two participants
- even for an existing conversation, chat access is denied if the social connection no longer exists
- Existing conversation history does not preserve chat access after the accepted relation is lost.

Lifecycle behavior:
- `openConversation` reuses an existing normalized pair when present
- otherwise it creates a new conversation with participants ordered by id
- conversation ordering in workspace uses `coalesce(lastMessageAt, createdAt)` descending

## Chat Message Flow

Request contract:
- `ChatMessageRequestDTO` supports optional `messageBody`
- `ChatMessageRequestDTO` supports optional `imageDataUrl`
- `imageDataUrl` must match `data:image/...`

Send-message behavior:
- request cannot be empty after normalization
- saved message updates conversation `lastMessageAt`
- saved message updates conversation `lastMessageSender`
- saved message updates conversation preview and image-summary flag
- text preview is truncated to 140 characters with ellipsis
- image-only preview falls back to `Photo`

Read behavior:
- marking a conversation read only updates unread incoming messages
- outgoing messages are never marked read by this operation
- read operation notifies realtime listeners only when something changed

Response contracts:
- `ChatMessageDTO` exposes sender snapshot, timestamps, message payload, and `ownMessage`
- `ChatConversationSummaryDTO` exposes counterpart snapshot, presence snapshot, last message summary, and unread count

## Chat Workspace and Contact Model

`ChatWorkspaceDTO` currently contains:
- conversations
- contacts
- circles
- unread conversation count
- online contact count

Workspace assembly behavior:
- conversation unread counts are aggregated repository-side by conversation id
- contact list is built from current user's owned circle memberships plus any conversation counterpart not already present
- Chat workspace only includes current accepted circle contacts.
- pending or stale relations must be filtered out of chat workspace contacts and conversations
- circle options are built from distinct circles owned by the current user
- contacts are sorted by username
- owned circles are sorted by circle name
- conversation summaries include whether the last message was sent by the current user

## Presence and Realtime

Presence behavior:
- online window is 120 seconds
- `markActive` creates a presence row if needed and sets `lastActiveAt = now`
- `markInactive` moves `lastActiveAt` outside the online window
- `isOnline` compares `lastActiveAt` against `now - 120s`

Websocket auth behavior:
- websocket handshake requires `token` query parameter
- token email is extracted through `JwtService`
- handshake user is resolved by email lookup
- invalid or missing token yields unauthorized handshake

Realtime event behavior:
- `chat.workspace.updated` is emitted for conversation, presence, and workspace-affecting changes
- `news.updated` is emitted for unread news count changes
- registering a websocket session marks the user active and notifies accepted circle contacts of presence change
- unregistering the last websocket session marks the user inactive and notifies accepted circle contacts of presence change
- websocket client ping type `chat.presence.ping` refreshes activity only

Socket event contract:
- `ChatSocketEventDTO` currently carries event type plus optional conversation id, actor user id, reason, and unread news count
- `ChatSocketClientMessageDTO` currently uses `type` and optional `conversationId`

## Chat Retention and Cleanup

Retention behavior:
- expired images are redacted before message deletion
- image redaction clears `imageDataUrl`
- when redacting an image-only message, placeholder text is inserted if body was null
- expired messages are deleted by creation timestamp
- both cleanup paths collect affected conversation ids and refresh conversation previews afterward

Operational behavior:
- scheduled cleanup cron comes from `app.retention.chat.cleanup-cron` with default `0 45 3 * * *`
- image retention days and message retention days come from retention config
- requester
- recipient
- creation timestamp
- optional accepted timestamp
- optional blocked timestamp
- optional blocking user

Technical rules:
- unique pair on `(requester_id, recipient_id)`
- accepted and blocked states are represented on the same entity instead of separate tables

`CircleGroup` currently stores:
- owner
- name
- creation timestamp
- memberships

Technical rules:
- circle name is unique within owner scope

`CircleMembership` currently stores:
- circle
- member
- creation timestamp

Technical rules:
- unique pair on `(circle_id, member_user_id)`

## User Location Settings

Profile-location rules:
- location mode defaults to `OFF` when not provided
- exact-visibility scope defaults to `NOBODY` when not provided
- radius is normalized with default `10 km`
- allowed radius range is `1..30 km`
- `updatedAt` is refreshed on each settings apply
- when mode is `OFF`, all stored location coordinates and provider metadata are cleared
- when mode is not `OFF`, usable coordinates are required before save completes
- if coordinates are missing, the service attempts auto-resolution from address parts or label

Exact visibility target rules:
- selected circles and selected users are always re-synced from request input
- `CIRCLES` scope requires at least one selected circle
- `USERS` scope requires at least one selected user
- selected circles must belong to the location owner
- selected users must exist
- selected users other than self must already have `CIRCLE` relation with the owner

User presentation rules:
- sharing summary distinguishes `Hidden`, `Approximate area only`, and `Exact location enabled`
- visibility summary distinguishes private, everyone, circles, and selected-people exposure
- approximate label prefers `locality, country`, then locality, then country, then label
- a user is nearby-discoverable only when mode is not `OFF` and coordinates exist

## Quest Location Behavior

Quest-write rules:
- quest visibility defaults to `INHERIT`
- quest location source defaults to `PROFILE`
- visibility `OFF` clears all quest location fields
- custom location clears prior provider metadata first, then attempts auto-resolution
- custom location save fails if usable coordinates cannot be resolved
- profile-source location copies the creator's saved location snapshot into the quest
- requesting profile-backed quest location while the creator profile mode is `OFF`:
  - clears location when quest visibility is `INHERIT`
  - throws validation error when quest explicitly requests visible location

Quest-read rules:
- quest is searchable by location only when visibility is not `OFF` and coordinates exist
- effective quest location visibility resolves inherited profile mode into `OFF`, `APPROXIMATE`, or `EXACT`
- exact quest visibility can degrade to approximate for viewers who lack exact-location access
- exact-location access always allows the owner
- `CIRCLES` exact access checks membership in one of the owner's allowed circles
- `USERS` exact access checks explicit allow-list membership
- approximate label uses locality and country before falling back to label
- visibility summary and source summary are backend-generated presentation strings

## Workmarket Cross-Module Dependencies

Identity coupling:
- controllers inject authenticated `AppUser` directly with `@AuthenticationPrincipal`
- creator ownership, applicant ownership, and admin authority all derive from `AppUser` and `AppUserRole`
- admin quest edits can reassign creator through `AppUserLookupService`
- user-profile enrichment pulls open-quest counts and recent open quests from workmarket back into identity read models

Social coupling:
- circle-only quest visibility is enforced through `QuestVisibilityService`
- selected visible circles are resolved from owner-owned circles only
- fallback circle visibility uses `circleService.isCircleBetween(currentUser, quest.getCreator())`
- workmarket notifications also carry circle-request events and actions, so the news feed is not quest-only

Location coupling:
- quest writes delegate location handling to `LocationSettingsService`
- nearby search scope depends on current user saved location and normalized radius
- quest DTO presentation includes location label, source summary, and visibility summary generated from location rules
- exact quest address visibility can degrade per viewer based on the creator profile-sharing rules

Common-platform coupling:
- workmarket services use `ServiceErrors` for business-failure shaping
- quest and application rich-text fields are sanitized through `RichTextInputValidator`
- list and search flows use `PageWindow` and `PageResponseFactory`
- search input normalization depends on shared normalizer helpers

## Frontend Route and API Contracts

Route guards:
- `/login` and `/register` are public routes
- all workmarket, social, profile, and admin routes require auth
- admin routes additionally require `isAdmin()`
- logged-in admins are redirected from `/work` and `/quests` to `/admin/work`
- logged-in users are redirected away from auth screens to their role-appropriate landing route

Frontend transport contract:
- `API_BASE_URL` defaults to `http://localhost:8080`
- axios client is shared across modules
- auth headers come from `withAuth()`
- only unauthorized `/auth/me` responses trigger automatic session clearing and redirect logic

Contract-shape model:
- `frontend/src/contracts/generated/themuffinmanContract.ts` is the generated DTO source
- `frontend/src/contracts/index.ts` re-exports generated contract types for app use
- `frontend/src/modules/workmarket/api/contracts.ts` aliases generated DTOs into module-facing names and adds a few frontend-side request refinements
- `workmarketApi` aggregates endpoint clients so non-workmarket screens can still consume user, circle, and location contracts through one import surface

Endpoint client mapping:
- `questsApi` maps `/quests`, `/quests/search`, `/quests/presets/:preset`, `/quests/:id`, `/quests/:id/detail`, and lifecycle mutation routes
- `applicationsApi` maps applicant, owner, admin, and review endpoints
- `dashboardApi` maps dashboard read models and summary sections
- `newsApi` maps unread count and read-state mutations
- `usersApi` maps `/app_users`, `/app_users/options`, `/app_users/me`, and `/app_users/:id/profile-view`
- `circlesApi` maps social and circle-management endpoints even though the import surface lives under the workmarket module
- `locationApi` maps `/location/lookup`, `/location/reverse-lookup`, and `/location/admin/status`
- `chatApi` is separate from `workmarketApi` and maps `/chat/*`

## Location Lookup Behavior

Lookup rules:
- provider client uses Geoapify when configured, otherwise the disabled client
- normalized search query must be at least `3` characters
- unconfigured provider returns an empty configured=`false` response for normal lookup
- reverse lookup on unconfigured provider throws a validation error
- lookup responses are cached for `14` days
- reverse lookup candidates are cached for `60` days
- reverse-lookup cache keys round coordinates to `4` decimal places

Rate limiting and telemetry:
- forward lookup limit is `30` requests per minute per actor key
- reverse lookup limit is `20` requests per minute per actor key
- actor key falls back to `anonymous`
- exceeding rate limit throws a validation-style error and increments telemetry counters
- each provider call persists one `LocationLookupEvent`
- debug status includes in-memory counters, cache sizes, provider request counts, current-month event totals, user and quest coordinate counts, provider-metadata counts, and database-size context
- `docs/location-services.md` remains the implementation-specific setup note for geocoding and PostGIS

## Common Contracts

Service-error helpers:
- `ServiceErrors` centralizes `notFound`, `badRequest`, `conflict`, `forbidden`, and `unauthorized`
- services use these helpers to express business failures as HTTP-aware exceptions

Global API-error behavior:
- `ResponseStatusException` is translated into structured `ApiErrorResponseDTO`
- bean-validation failures return code `VALIDATION_ERROR`
- field-level errors are mapped into `ApiFieldErrorDTO`
- missing request parameters produce code `REQUIRED`
- type mismatches produce code `TYPE_MISMATCH`
- malformed request bodies return `BAD_REQUEST` with message `Malformed request body`

Normalization helpers:
- `UserInputNormalizer.normalizeEmail` trims and lowercases emails using `Locale.ROOT`
- `SearchQueryNormalizer.normalize` trims queries and strips leading `@`
- `ProfileValueNormalizer.normalizeText` trims and nulls blank strings
- `ProfileValueNormalizer.normalizeAvatarDataUrl` requires `data:image/` prefix and enforces max length `250000`

Rich-text rules:
- blank rich-text content collapses to `null`
- plain-text extraction strips tags and collapses whitespace
- allowed tags are:
  - `b`, `strong`, `i`, `em`, `u`, `p`, `div`, `ul`, `ol`, `li`, `span`, `blockquote`, `br`, `a`, `img`
- blocked tags are:
  - `script`, `style`, `iframe`, `object`, `embed`
- anchor URLs are only allowed for `http`, `https`, `mailto`, root-relative, or hash links
- image URLs are only allowed for `data:image/...;base64,...`
- sanitized links are emitted with `target="_blank"` and `rel="noreferrer noopener"`

Pagination helpers:
- `PageWindow.of` normalizes negative page to `0`
- `PageWindow.of` normalizes size below `1` to `1`
- pagination is list-slice based, not repository-page based

## Config and Operations

Runtime configuration is centralized in `config/` classes, including:
- retention
- bootstrap settings
- security
- location provider configuration

Operational settings should be kept in typed config classes rather than scattered `@Value` fields.

Security configuration:
- `SecurityProperties.Jwt.secret` supplies the JWT signing secret
- `SecurityProperties.Jwt.expirationMillis` defaults to `86400000`
- `SecurityProperties.Cors.allowedOrigins` defaults to `http://localhost:5173`
- `SecurityConfig` disables CSRF and uses stateless sessions
- open endpoints are `/auth/register`, `/auth/login`, and `/ws/chat/**`
- `/app_users/**` is admin-only except authenticated self/profile endpoints explicitly opened
- CORS allows `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`
- password hashing uses `BCryptPasswordEncoder`

Websocket configuration:
- websocket handler is registered at `/ws/chat`
- websocket allowed origins reuse the shared CORS origin list
- websocket auth uses `ChatWebSocketAuthInterceptor`

Retention configuration:
- `app.retention.notifications.days` defaults to `7`
- `app.retention.notifications.cleanup-cron` defaults to `0 30 3 * * *`
- `app.retention.chat.image-days` defaults to `30`
- `app.retention.chat.message-days` defaults to `180`
- `app.retention.chat.cleanup-cron` defaults to `0 45 3 * * *`
- `app.retention.chat.expired-image-placeholder` defaults to `Image expired`

Location provider configuration:
- `app.location.provider` defaults to `none`
- `app.location.geoapifyBaseUrl` defaults to `https://api.geoapify.com/v1`
- provider integration depends on explicit configuration rather than being assumed available

Bootstrap configuration:
- `app.seed.users.enabled` controls seeding of default admin and test users
- seeded default users require explicit credentials for both `app.admin` and `app.test`
- `app.bootstrap.admin.enabled` controls a separate bootstrap-admin ensure flow
- bootstrap-admin flow requires explicit email, username, and password
- seed and bootstrap flows upsert by normalized email lookup and overwrite username, password hash, and role

## Update Rule

- Treat this file as a technical contract.
- When entity fields, relations, validations, permission checks, or workflow rules change, update this file in the same change.
- If a change introduces a new invariant, write it down here explicitly.
