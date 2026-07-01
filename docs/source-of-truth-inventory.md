# Source of Truth Inventory

This file is a compact map of the main backend domains and the files that should be treated as the primary sources of truth when documenting the product.

## Primary Domains

### `workmarket`

Subdomains:
- `quest core`
- `applications`
- `execution and term negotiation`
- `dashboard and notifications`
- `reviews and ratings`
- `options and presentation contracts`

Core files:
- `src/main/java/com/themuffinman/app/workmarket/controller/QuestController.java`
- `src/main/java/com/themuffinman/app/workmarket/controller/QuestApplicationController.java`
- `src/main/java/com/themuffinman/app/workmarket/controller/QuestNewsController.java`
- `src/main/java/com/themuffinman/app/workmarket/controller/DashboardController.java`
- `src/main/java/com/themuffinman/app/workmarket/controller/UserReviewController.java`
- `src/main/java/com/themuffinman/app/workmarket/model/Quest.java`
- `src/main/java/com/themuffinman/app/workmarket/model/QuestApplication.java`
- `src/main/java/com/themuffinman/app/workmarket/model/QuestStatus.java`
- `src/main/java/com/themuffinman/app/workmarket/model/QuestAudience.java`
- `src/main/java/com/themuffinman/app/workmarket/model/QuestNewsItem.java`
- `src/main/java/com/themuffinman/app/workmarket/model/QuestNewsType.java`
- `src/main/java/com/themuffinman/app/workmarket/model/QuestApplicationStatus.java`
- `src/main/java/com/themuffinman/app/workmarket/model/UserReview.java`
- `src/main/java/com/themuffinman/app/workmarket/service/QuestService.java`
- `src/main/java/com/themuffinman/app/workmarket/service/QuestUpdateService.java`
- `src/main/java/com/themuffinman/app/workmarket/service/QuestValidationService.java`
- `src/main/java/com/themuffinman/app/workmarket/service/QuestStateTransitionService.java`
- `src/main/java/com/themuffinman/app/workmarket/service/QuestVisibilityService.java`
- `src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java`
- `src/main/java/com/themuffinman/app/workmarket/service/QuestQueryService.java`
- `src/main/java/com/themuffinman/app/workmarket/service/QuestNewsService.java`
- `src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`
- `src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`
- `src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java`
- `src/main/java/com/themuffinman/app/workmarket/service/QuestWorkflowNotificationService.java`
- `src/main/java/com/themuffinman/app/workmarket/service/QuestViewAssembler.java`
- `src/main/java/com/themuffinman/app/workmarket/service/VisionOptionsService.java`
- `src/main/java/com/themuffinman/app/workmarket/service/VisionPresentationHelper.java`

Primary schema migrations:
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

### `social`

Subdomains:
- `relationship lifecycle`
- `circle groups`
- `circle memberships`
- `contacts and overviews`
- `search and nearby discovery`
- `presentation and action contracts`

Core files:
- `src/main/java/com/themuffinman/app/social/controller/CircleController.java`
- `src/main/java/com/themuffinman/app/social/model/CircleGroup.java`
- `src/main/java/com/themuffinman/app/social/model/CircleMembership.java`
- `src/main/java/com/themuffinman/app/social/model/CircleRequest.java`
- `src/main/java/com/themuffinman/app/social/service/CircleService.java`
- `src/main/java/com/themuffinman/app/social/service/CircleRelationService.java`
- `src/main/java/com/themuffinman/app/social/service/CircleMembershipService.java`
- `src/main/java/com/themuffinman/app/social/service/CircleDiscoveryService.java`
- `src/main/java/com/themuffinman/app/social/service/CircleViewAssembler.java`
- `src/main/java/com/themuffinman/app/social/service/SocialPresentationHelper.java`
- `src/main/java/com/themuffinman/app/social/service/SocialRelationActionHelper.java`

Primary schema migrations:
- `V12__create_circle_request_table_and_add_quest_audience.sql`
- `V13__add_circle_blocking_and_circle_notifications.sql`
- `V19__create_crews_and_quest_visibility.sql`
- `V20__rename_crew_tables_to_circles.sql`
- `V29__add_circle_request_reference_to_news.sql`

### `chat`

Subdomains:
- `conversation access and lifecycle`
- `message flow`
- `workspace and contact read model`
- `presence and realtime`
- `retention and cleanup`

Core files:
- `src/main/java/com/themuffinman/app/chat/controller/ChatController.java`
- `src/main/java/com/themuffinman/app/chat/model/ChatConversation.java`
- `src/main/java/com/themuffinman/app/chat/model/ChatMessage.java`
- `src/main/java/com/themuffinman/app/chat/model/ChatPresence.java`
- `src/main/java/com/themuffinman/app/chat/service/ChatService.java`
- `src/main/java/com/themuffinman/app/chat/service/ChatPresenceService.java`
- `src/main/java/com/themuffinman/app/chat/service/ChatRealtimeService.java`
- `src/main/java/com/themuffinman/app/chat/service/ChatRetentionService.java`
- `src/main/java/com/themuffinman/app/chat/websocket/ChatWebSocketHandler.java`
- `src/main/java/com/themuffinman/app/chat/websocket/ChatWebSocketAuthInterceptor.java`

Primary schema migrations:
- `V22__create_chat_tables.sql`

### `identity`

Subdomains:
- `authentication`
- `user account management`
- `profile and profile view`
- `admin user detail`
- `security integration`

Core files:
- `src/main/java/com/themuffinman/app/identity/controller/AuthController.java`
- `src/main/java/com/themuffinman/app/identity/controller/AppUserController.java`
- `src/main/java/com/themuffinman/app/identity/model/AppUser.java`
- `src/main/java/com/themuffinman/app/identity/model/AppUserRole.java`
- `src/main/java/com/themuffinman/app/identity/service/AppUserService.java`
- `src/main/java/com/themuffinman/app/identity/service/AppUserLookupService.java`
- `src/main/java/com/themuffinman/app/identity/service/UserProfileViewService.java`
- `src/main/java/com/themuffinman/app/identity/service/AdminUserDetailService.java`
- `src/main/java/com/themuffinman/app/identity/security/JwtService.java`
- `src/main/java/com/themuffinman/app/identity/security/JwtAuthFilter.java`
- `src/main/java/com/themuffinman/app/identity/security/RepositoryUserDetailsService.java`
- `src/main/java/com/themuffinman/app/identity/mapper/AppUserMgr.java`
- `src/main/java/com/themuffinman/app/identity/dto/AppUserRequestDTO.java`
- `src/main/java/com/themuffinman/app/identity/dto/AppUserResponseDTO.java`
- `src/main/java/com/themuffinman/app/identity/dto/UserProfileViewDTO.java`
- `src/main/java/com/themuffinman/app/identity/dto/AdminUserDetailDTO.java`
- `src/main/java/com/themuffinman/app/identity/dto/auth/RegisterRequestDTO.java`
- `src/main/java/com/themuffinman/app/identity/dto/auth/LoginRequestDTO.java`
- `src/main/java/com/themuffinman/app/identity/dto/auth/AuthResponseDTO.java`
- `src/main/java/com/themuffinman/app/identity/repository/AppUserRepository.java`

Primary schema migrations:
- `V1__init.sql`
- `V4__insert_column_hashPassword_on_appUser.sql`
- `V5__add_role_to_app_user.sql`
- `V11__add_app_user_profile_fields.sql`
- `V14__add_app_user_created_at.sql`
- `V16__enforce_case_insensitive_user_email.sql`

### `location`

Subdomains:
- `user location settings`
- `quest location behavior`
- `lookup and reverse lookup`
- `exact visibility and nearby discovery support`
- `provider metrics and audit`

Core files:
- `src/main/java/com/themuffinman/app/location/controller/LocationLookupController.java`
- `src/main/java/com/themuffinman/app/location/model/UserLocationMode.java`
- `src/main/java/com/themuffinman/app/location/model/QuestLocationVisibility.java`
- `src/main/java/com/themuffinman/app/location/model/QuestLocationSource.java`
- `src/main/java/com/themuffinman/app/location/model/ExactLocationVisibilityScope.java`
- `src/main/java/com/themuffinman/app/location/model/LocationLookupEvent.java`
- `src/main/java/com/themuffinman/app/location/service/LocationSettingsService.java`
- `src/main/java/com/themuffinman/app/location/service/LocationLookupService.java`
- `src/main/java/com/themuffinman/app/location/service/GeoapifyLocationLookupClient.java`
- `src/main/java/com/themuffinman/app/location/service/DisabledLocationLookupClient.java`
- `src/main/java/com/themuffinman/app/location/service/AdminDatabaseMetricsService.java`
- `src/main/java/com/themuffinman/app/location/repository/LocationLookupEventRepository.java`
- `src/main/java/com/themuffinman/app/location/dto/UserLocationSettingsDTO.java`
- `src/main/java/com/themuffinman/app/location/dto/UserLocationSettingsRequestDTO.java`
- `src/main/java/com/themuffinman/app/location/dto/LocationLookupCandidateDTO.java`
- `src/main/java/com/themuffinman/app/location/dto/LocationLookupResponseDTO.java`
- `src/main/java/com/themuffinman/app/location/dto/LocationDebugStatusViewDTO.java`

Primary schema migrations:
- `V23__add_location_settings.sql`
- `V24__enable_postgis_for_quest_location_search.sql`
- `V25__add_location_provider_metadata.sql`
- `V26__add_user_exact_location_visibility.sql`
- `V28__create_location_lookup_event_table.sql`

### `common`

Subdomains:
- `service error contract`
- `request validation and exception mapping`
- `normalization and sanitization`
- `simple pagination helpers`

Core files:
- `src/main/java/com/themuffinman/app/common/errors/ServiceErrors.java`
- `src/main/java/com/themuffinman/app/common/controller/GlobalExceptionHandler.java`
- `src/main/java/com/themuffinman/app/common/validation/RichTextInputValidator.java`
- `src/main/java/com/themuffinman/app/common/normalization/UserInputNormalizer.java`
- `src/main/java/com/themuffinman/app/common/normalization/SearchQueryNormalizer.java`
- `src/main/java/com/themuffinman/app/common/normalization/ProfileValueNormalizer.java`
- `src/main/java/com/themuffinman/app/common/pagination/PageResponseFactory.java`
- `src/main/java/com/themuffinman/app/common/pagination/PageWindow.java`

### `config`

Subdomains:
- `security and cors`
- `websocket wiring`
- `retention operations`
- `location provider settings`
- `bootstrap and seed users`

Core files:
- `src/main/java/com/themuffinman/app/config/SecurityConfig.java`
- `src/main/java/com/themuffinman/app/config/SecurityProperties.java`
- `src/main/java/com/themuffinman/app/config/WebSocketConfig.java`
- `src/main/java/com/themuffinman/app/config/RetentionProperties.java`
- `src/main/java/com/themuffinman/app/config/BootstrapProperties.java`
- `src/main/java/com/themuffinman/app/config/AdminBootstrapConfig.java`
- `src/main/java/com/themuffinman/app/config/LocationProviderProperties.java`

## Existing Supporting Docs

- `docs/location-services.md` is the current implementation-specific setup note for geocoding and PostGIS.

## Frontend And Contract Sources

Primary frontend source-of-truth files:
- `apps/themuffinman/frontend/src/router.ts`
- `apps/themuffinman/frontend/src/api/httpClient.ts`
- `apps/themuffinman/frontend/src/api/apiErrors.ts`
- `apps/themuffinman/frontend/src/modules/identity/api/authApi.ts`
- `apps/themuffinman/frontend/src/modules/vision/api/visionApi.ts`
- `apps/themuffinman/frontend/src/modules/vision/api/visionConversationApi.ts`
- `apps/themuffinman/frontend/src/modules/vision/api/contracts.ts`
- `apps/themuffinman/frontend/src/modules/vision/api/clients/questsApi.ts`
- `apps/themuffinman/frontend/src/modules/vision/api/clients/applicationsApi.ts`
- `apps/themuffinman/frontend/src/modules/vision/api/clients/dashboardApi.ts`
- `apps/themuffinman/frontend/src/modules/vision/api/clients/newsApi.ts`
- `apps/themuffinman/frontend/src/modules/vision/api/clients/usersApi.ts`
- `apps/themuffinman/frontend/src/modules/vision/api/clients/circlesApi.ts`
- `apps/themuffinman/frontend/src/modules/vision/api/clients/locationApi.ts`
- `apps/themuffinman/frontend/src/modules/chat/api/chatApi.ts`
- `apps/themuffinman/frontend/src/contracts/index.ts`
- `apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts`

Important route and page entrypoints:
- `apps/themuffinman/frontend/src/modules/vision/views/VisionQuestDetailView.vue`
- `apps/themuffinman/frontend/src/modules/vision/views/VisionApplicationDetailView.vue`
- `apps/themuffinman/frontend/src/modules/vision/views/VisionCirclesView.vue`
- `apps/themuffinman/frontend/src/modules/vision/views/VisionUserProfileView.vue`
- `apps/themuffinman/frontend/src/modules/vision/views/VisionUserSettingsView.vue`
- `apps/themuffinman/frontend/src/modules/vision/views/VisionChatWorkspaceView.vue`
- `apps/themuffinman/frontend/src/modules/identity/views/LoginView.vue`
- `apps/themuffinman/frontend/src/modules/identity/views/RegisterView.vue`

## How to Use This Inventory

- Start with the domain controller, service, and model files listed above.
- Treat migrations as the schema history for the same domain.
- If a business rule is not obvious from the model, resolve it in the service layer before documenting it.
- When we start module-by-module documentation updates, begin with `workmarket` backend domain naming, not the deleted legacy frontend module surface.
