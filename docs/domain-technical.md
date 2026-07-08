# Domain Technical Notes

This document is the technical source of truth for core product behavior. It should track entities, relations, validations, permissions, and workflow rules as they exist in code.

## Domain Areas

- `identity`: users, authentication, roles, and profile data
- `social`: circles, memberships, requests, blocking, and relation lookup
- `workmarket`: quests, applications, quest news, dashboards, reviews
- `business`: business profiles, directory, and mini-site profile read models
- `things`: lending listings and borrower request workflow
- `rides`: voluntary ride offers with optional circle-scoped visibility
- `chat`: direct conversations, messages, presence, realtime updates
- `location`: user location settings, quest location visibility, lookup events
- `common.event`: lightweight domain event publishing through Spring application events
- `common.concepts`: shared actor identity, module ownership, circle visibility selection, and scheduling-window primitives
- `internal sandbox`: synthetic test-data orchestration kept separate from production-like user actions

Domain capsules:
- Backend domain roots under `apps/themuffinman/src/main/java/com/themuffinman/app/*/README.md` summarize responsibility, main entrypoints, tests, living docs, and forbidden shortcuts for that domain.
- Frontend module roots under `apps/themuffinman/frontend/src/modules/*/README.md` provide the same first-read capsule for route and UI work.
- `docs/cross-domain-glossary.md` defines shared terms such as users, actors, circles, visibility, consent, messaging, quests, applications, reviews, bookings, and synthetic data before deeper domain-specific rules.

Frontend vision surface note:
- `apps/themuffinman/frontend/src/modules/vision/views/VisionSurfaceModernView.vue` is the experimental authenticated route for the long-term adaptive canvas direction.
- The `/vision` route is the focused adaptive surface for text and voice prompt intake, and the older vision shell has been removed from the app.
- The surface now keeps its prompt composer and canvas content in one inline flow instead of a separate floating dock, so the adaptive stage can expand or contract around the current state.
- The `/vision` route shell should prefer contextual reveal controls for state and recent-task memory instead of permanent page-level chrome, so the blank-canvas default stays visually quiet.
- `/vision` uses backend-managed prompt decoding, transcription, speech synthesis, and agent planning through `POST /dashboard/me/vision/prompt`, `GET /dashboard/me/voice-config`, `POST /dashboard/me/voice/transcribe`, and `POST /dashboard/me/voice/speak`, while typed `app.voice.*` config still controls whether the frontend may record or play voice. Prompt understanding defaults to `gpt-4o-mini`, with an explicit upgrade flag for heavier semantic interpretation when needed.
- `app.voice.max-recording-millis`, `app.voice.max-audio-bytes`, and `app.voice.max-speech-text-length` are included in the dashboard voice config; the frontend applies them before upload/playback and `DashboardVoiceService` enforces audio and speech-text size limits before calling OpenAI.
- Phase 1 `/vision` orchestration now also exposes `POST /vision/conversations/turns`, backed by persisted `vision_conversation` and `vision_turn` records, for stepwise backend-owned conversation state.
- The `/vision/conversations/turns` request is versioned with `inputType`, `text`, `clientCapabilities`, `clientStateVersion`, and optional choice/confirmation fields so the frontend can keep the canvas contract explicit while the backend still understands the legacy prompt path during transition.
- The first persisted `/vision` orchestration scope is `create_quest`: collect title, description, reward/free, visibility, `schedule_mode`, optional `scheduled_date`, optional `scheduled_time`, `location_mode`, and optional `location_label` one slot at a time while execution stays behind `app.vision.execution-enabled`.
- `VisionConversationService` now routes supported mutation previews through `VisionCapabilityPreviewService` draft helpers, and those previews render the current slot data as form-like result summary blocks so the frontend can show recognized circle, circle-request, quest, application, and profile fields, plus additional draft details, before confirmation.
- When the semantic create-quest confidence remains below the route threshold after those visible slots are filled, the backend keeps the conversation in clarification mode and asks for a clearer title or task instead of treating the draft as review-ready.
- `VisionChatExecutionService` now strips generic user words from direct chat target fragments and returns explicit candidate suggestions when more than one contact matches the normalized fragment.
- The semantic audit matrix now carries explicit non-quest read-route cases for settings and chat workspace so route drift shows up outside the create-quest path.
- The backend semantic model is the primary semantic interpreter for `/vision` prompt understanding across quests, circles, applications, profiles, and chat; the deterministic local parser is now restricted to an English-only emergency mode for safe read surfaces and fail-closes mutation-style prompts when OpenAI understanding is unavailable or stays unsupported.
- Target entity resolution now uses explicit backend resolver families and per-intent confidence thresholds, so the semantic layer can resolve the relevant person, quest, circle, or application separately from the action intent before clarification or DTO mapping proceeds.
- Family-specific alias normalization now runs before entity resolution, so quest, circle, application, and user lookups can canonicalize multilingual synonyms and common paraphrases before the resolver selects a concrete entity.
- OpenAI semantic requests now include canonical family-alias hints for quest, circle, application, and user lookups so the model can normalize the target entity before the backend resolver runs.
- OpenAI semantic requests now also carry route purposes, DTO types, route examples, slot descriptions, field names, slot aliases, slot anti-examples, and allowed values so the model can extract the shortest entity phrase into the correct slot before backend sanitization runs.
- OpenAI semantic requests now also carry explicit conversation draft context through `activeSlot` and `draftSnapshot`, so follow-up turns can fill one field without reinterpreting the whole task.
- Follow-up target resolution now prefers route-slot priority plus semantic-plan fallbacks such as `targetUserQuery` and `searchQuery`, so route metadata stays authoritative while the parser can still recover the intended target without family-specific special casing.
- Draft merge now honors backend confidence gates before overwriting slot data: high-confidence values may replace an existing draft field, medium-confidence values only fill missing fields, and low-confidence values are ignored.
- Slot normalization happens after semantic extraction in the merge layer, with deterministic cleanup for money, date, time, and location values instead of hardcoded punctuation parsing inside the semantic model prompt.
- Weak or partial semantic turns now get one focused repair pass for the most relevant slot before the backend finalizes the understanding result.
- `VisionLearningService` now persists repair signals into feedback details and a repair-related preference key, so correction history can influence later memory summaries and prompt context.
- Canonicalized slot candidates and target entity queries now flow through the semantic envelope, so replay records, resolver lookups, and DTO mapping all see the same normalized entity text.
- `VisionPromptUnderstandingResult`, `VisionConversationTurnResponseDTO`, and `DashboardVisionPromptResponseDTO` now expose `understandingProvider` and `understandingStatus` so callers can distinguish `openai_primary`, `openai_local_rescue`, `openai_unsupported`, `local_emergency`, and `local_fail_closed` handling states.
- A dedicated `VisionPromptUnderstandingService` now sits between prompt transcription and slot merging so the backend can use the configured semantic model to extract explicit structured quest fields before deterministic slot validation takes over.
- The semantic request contract now treats route examples, slot aliases, route slot descriptions, and allowed values as the authoritative schema for all supported entity families, so new create/view/update/delete flows can be added by extending the shared route catalog rather than teaching each handler its own parser.
- `VisionSemanticResponseValidator` now rejects any semantic response that extracts slots or generic semantic fields outside the selected route contract before sanitization runs, so route ownership stays backend-defined.
- `VisionSemanticOrchestrationContextService` now builds a backend memory pack with user memory, session memory, and recent turn snapshots so semantic understanding can keep stable preferences separate from the current conversation thread.
- The same memory pack also carries recent entity families and the current session entity family so ambiguous follow-ups can stay within the active topic unless the semantic route clearly changes.
- `VisionSemanticOrchestrationContextService` now also loads learned preference rows and the latest memory summary so the semantic request can use durable user habits alongside the live conversation rail.
- `VisionSemanticUserMemoryContext` now also carries learned preference signals with confidence scores, so prompt understanding can see which habits are strong enough to use and which ones have already decayed.
- Learned preference signals are ranked by backend priority before they enter the semantic memory pack or the learning-memory snapshot, so high-value habits like input mode and topic family stay ahead of lower-signal rows.
- `VisionSemanticUserMemoryContext` and the `learningMemory` DTO now also carry compact explainability records for preference ranking, intent selection, and slot-focus decisions, so runtime memory can explain why a habit or route won without reconstructing raw turn history.
- `VisionSemanticUserMemoryContext` now also exposes a retrieval focus summary and retrieval entity-family hint, so weak learned-family confidence can fall back to the most recent durable topic rail instead of leaving retrieval implicit.
- Conversation routing now keeps a weak ambiguous follow-up inside the current thread when the semantic confidence is low and the prompt does not clearly signal a different entity family; explicit topic changes still switch or open a new conversation.
- `VisionClarificationService` now can shape a small subset of create-quest clarification text from the user's learned input preference, so the backend can keep follow-up prompts aligned with the user's most reliable interaction mode.
- Fallback focus resolution now skips the previous requested slot when the new prompt clearly belongs to a different entity family, so a stale quest slot does not hijack a fresh circles, applications, profile, or chat turn.
- The vision turn response now includes a compact `memoryTrail` DTO with active entity family, current intent, current requested slot, current session status, session summary, open questions, recent actions, last prompts, and recent family/intent lists for frontend debug and adaptive preview use.
- The vision turn response also exposes `previousEntityFamily` and `topicSwitchHint` inside `memoryTrail`, so the frontend can render a visible topic transition when the active entity family changes.
- `VisionConversationSummaryDTO` now also carries `entityFamily`, `previousEntityFamily`, and `topicSwitchHint`, so the recent-conversation rail can show topic transitions across resumable threads.
- The Vision shell now renders recent conversation summaries as a clickable resume strip above the main terminal surface, using the summary DTO without rebuilding task state on the client.
- `vision_conversation` now also persists a compact `session_memory_snapshot` JSON blob so the backend can retain the current session memory outside the live turn row.
- The prompt-understanding request now reads that persisted session snapshot back into the semantic memory pack so the next turn can use the durable session rail as context, with explicit summary, open-question, and recent-action fields.
- `vision_user_preference` stores durable preference signals such as preferred input type, learned entity family, last intent, and other compact turn-derived habits, plus a confidence score and confidence update timestamp used for decay-aware ranking.
- `vision_memory_feedback_event` stores correction, confirmation, execution, blocked, and cancelled feedback events so the backend can trace why the assistant should adapt.
- `vision_memory_summary` stores compact rollups of the most recent preference and feedback history so the semantic request can use compressed memory instead of raw replay.
- `VisionLearningService` writes those preference and feedback records from the active turn flow and periodically compacts them into summary rows.
- The turn response also exposes a `learningMemory` DTO with the latest compact summary, recent feedback kinds, structured preference signals, and compact explainability records so the frontend can show learned context directly.
- `VisionSemanticResponseValidator` now fail-closes any semantic response whose candidate intent, capability id, or focus slot falls outside the published backend response contract before sanitization and routing continue.
- Create-quest slot merging now prefers a cleaned core task summary for fallback title and description values, and it only treats numbers as reward candidates when the prompt carries reward-like cues instead of every date or time numeral.
- `VisionSemanticContractSanitizer` now rejects prompt-like location labels and profile-location labels that look like full commands or task descriptions, while still allowing short place-like labels and address fragments.
- `VisionLocationParserService` now parses short place labels and simple city-country pairs more carefully instead of assuming every comma-separated label begins with a street line.
- `VisionScheduleParserService` now checks explicit `am` / `pm` time phrases before hour-only fallback, so combined weekday-and-time voice turns do not misread `next Tuesday at 7 pm` as a morning appointment.
- `VisionLocationParserService` now also understands simple postal-code-plus-locality and locality-plus-postal-code fragments, which helps Swiss-style location input stay structured before any lookup candidate resolution.
- `VisionScheduleParserService` now also recognizes explicit evening phrasing such as `in the evening` when an hour is already present.
- `VisionScheduleParserService` now also resolves a plain weekday reference such as `this Friday` into the next matching calendar day, even when the user does not say `next`.
- The local emergency parser is English-only and fail-closes on non-English prompts; arbitrary-language normalization is handled by the OpenAI semantic path, but safe English `CREATE_QUEST`, `CREATE_CIRCLE`, `CREATE_APPLICATION`, and `UPDATE_PROFILE` turns may still route through the emergency path when OpenAI is unavailable.
- `VisionPromptUnderstandingService` now also returns a generic `VisionSemanticPlan` with candidate intent, confidence, capability id, and planning note before create_quest slot extraction, while deterministic backend routing and services still decide supported execution.
- `SemanticEnvelope` and `SemanticReplayRecord` now carry entity-resolution status, canonical label, confidence, and ambiguity reason alongside the raw and normalized prompt text for audit and replay use.
- `VisionIntentRouter` now recognizes both `CREATE_QUEST` and `DISCOVER_QUESTS` so read-only browse/search prompts can be routed without depending on the create-quest flag.
- `VisionConversationService` now switches to a new persisted conversation when the prompt clearly changes intent, so discovery and creation can coexist on the same adaptive surface without forcing one thread to masquerade as another.
- `VisionIntentRouter` now also recognizes `OPEN_CHAT`, and `VisionChatExecutionService` resolves an explicit chat target before delegating to the existing chat opening boundary.
- `VisionIntentRouter` now also recognizes `VIEW_PROFILE`, `VIEW_CIRCLES`, and `VIEW_APPLICATIONS`, and `VisionCapabilityPreviewService` translates existing backend DTOs into read-only terminal preview blocks for those self-scoped flows.
- `VisionConversationController` routes reset, cancel, load, and recent conversation actions through `VisionConversationLifecycleService`, while `VisionConversationService` keeps the main turn-processing boundary.
- `VisionIntentRouter` now also recognizes `VIEW_QUEST_NEWS`, and `VisionCapabilityPreviewService` renders the authenticated user's quest-news feed as a read-only terminal preview block.
- `VisionIntentRouter` now also recognizes `CREATE_CIRCLE`, and `VisionConversationService` runs a one-slot circle draft/review/confirm flow that executes through `CircleService.createCircle(...)` after explicit confirmation.
- `VisionIntentRouter` now lets an explicit create-circle prompt override a circles snapshot semantic drift, so phrases like "make new circle of friends" still enter the circle draft flow instead of staying on `VIEW_CIRCLES`.
- The same snapshot override logic also lets explicit circle requests, application mutations, profile updates, and direct chat prompts escape their matching read-only snapshot intents when the prompt clearly asks for an action.
- `VisionIntentRouter` now also recognizes `CREATE_APPLICATION`, and `VisionConversationService` runs a review-gated application flow that resolves one applyable quest, collects `application_message`, collects `application_proposed_price` only when the resolved quest is paid, and then executes through `WorkmarketQuestApplicationService.applyForQuest(...)`.
- `VisionIntentRouter` now also recognizes `UPDATE_APPLICATION` and `WITHDRAW_APPLICATION`; `VisionConversationService` resolves one exact pending self application through `VisionCapabilityPreviewService.resolveMyPendingApplication(...)`, reuses the same review/confirm boundary, and executes through `WorkmarketQuestApplicationService.updateMyApplication(...)` or `WorkmarketQuestApplicationService.withdrawMyApplication(...)` only after explicit confirmation.
- `VisionIntentRouter` now also recognizes `UPDATE_PROFILE`, and `VisionConversationService` runs a review-gated self-profile mutation flow for `profile_username` and `profile_description`; `VisionCapabilityPreviewService.updateProfile(...)` preserves the current email, avatar, and location settings when it builds the full `AppUserRequestDTO` for `AppUserService.updateAppUser(...)`.
- `VisionSurfaceModernView.vue` should use `POST /vision/conversations/turns` as the primary prompt-bearing conversation path, `GET /vision/conversations/recent` and `GET /vision/conversations/{conversationId}` for resume behavior, plus dedicated `POST /vision/conversations/{conversationId}/reset` and `POST /vision/conversations/{conversationId}/cancel` lifecycle endpoints; the backend also short-circuits common free-text shortcut commands like `cancel`, `stop`, and `reset` to that lifecycle flow during an active conversation, and the frontend exposes `Escape` as a local composer cancel shortcut.
- The `/vision` conversation response now includes backend-driven `canvasMode` plus ordered `blocks`, with the first vocabulary covering `agent_message`, `recognized_prompt`, `field_request`, `result_summary`, `quest_discovery`, `review_summary`, `info`, `success`, and `warning`.
- `VisionConversationSummaryDTO` is the compact long-session continuity read model for `/vision`; the backend keeps stage, progress, stale, resumable, completed, and pending-slot context there so the frontend does not need to reconstruct resume state from raw turn history.
- Review-ready quest conversations may now retarget one named field back into clarification mode, so reward, schedule, and location corrections stay inside the same persisted conversation timeline.
- `VisionConversationTurnRequestDTO` now supports explicit `action` values for prompt submission, review confirmation, and typed review-edit requests, plus a `reviewTarget` field that maps deterministic review corrections like title, reward, schedule, or location back onto one editable slot.
- `VisionConversationSummaryDTO` now also exposes compact continuity metadata for recent-task resume surfaces: `stageLabel`, `progressLabel`, `groupKey`, `resumable`, `completed`, and `stale`, so the frontend does not have to infer resume meaning from raw status strings or local task grouping heuristics.
- `VisionExecutionPlanner` now produces a read-only `VisionExecutionCandidateDTO` for create_quest review turns so the canvas can show readiness, blockers, and the next required field without crossing into mutation execution.
- `VisionQuestDiscoveryService` now produces a read-only `VisionQuestDiscoveryDTO` for `DISCOVER_QUESTS` turns so the canvas can show ranked available quests without crossing into mutation execution.
- Schedule extraction for `/vision` quest creation is now centralized in `VisionScheduleParserService`, which deterministically parses ISO date-time, common European date-time, tomorrow, day-after-tomorrow, tonight, next week, next weekday, relative offsets such as `in two weeks` and `za dva tjedna`, weekend phrases such as `next weekend`, am/pm phrases, noon, midnight, hour-only day-period phrases, a growing HR/EN spoken-time vocabulary, and basic Croatian relative phrases such as `sutra`, `prekosutra`, and `sljedeći tjedan` into separate `scheduled_date` and `scheduled_time` slots; the backend derives `scheduled_at` only at the execution boundary once both values are present, while still rejecting ambiguous spoken times without a clear day-period signal.
- Custom `/vision` quest locations now pass through `VisionLocationParserService` first and may be enriched by `VisionLocationResolutionService`; when lookup finds one or more more precise candidates, the conversation must pause for an explicit user choice among those candidates or the originally typed location before review may continue.
- When `app.vision.execution-enabled` is true, a review-ready `create_quest` or `create_circle` conversation may accept an explicit confirmation on the same turn endpoint and execute through `VisionExecutionService`, which owns the review-confirmation gate and dispatches typed capability adapters for the actual quest or circle creation boundary.
- `docs/vision-architecture-patterns.md` is the durable implementation guide for future `/vision` backend orchestration, API contracts, frontend canvas rendering, and executor rollout.

Test fixture standard:
- `apps/themuffinman/src/test/java/com/themuffinman/app/testing/TestFixtures.java` is the shared backend fixture builder for common users, admin users, profile-location users, circles, quests, quest applications, and chat conversations.
- New backend tests should use `TestFixtures` for common entities unless the test is explicitly verifying unusual field defaults.

Validation evidence standard:
- `.agents/validation-evidence/*.yaml` records must name exact validation commands, validation scope, generated-artifact actions, and skipped-check reasons.
- `make audit-validation-evidence-quality` rejects vague closeout evidence such as "tests not run", "build passed", "ok", or placeholder summaries.

Regression scenario standard:
- `docs/regression-scenario-catalog.yaml` maps critical regression scenarios to test files and focused commands by domain.
- `docs/regression-scenario-catalog.md` is the human-readable summary of the same scenario set.

Docs-as-contract standard:
- `docs/docs-as-contract-slices.yaml` maps selected living-document sections to the runtime tests or audit checks that prove the documented behavior.
- `docs/docs-as-contract-slices.md` is the human-readable summary of the same contract-slice set.

Architecture drift standard:
- `make audit-architecture-drift` generates report-first findings for oversized backend services/controllers, Vue views with product logic, and oversized living-doc sections.

Service layering standard:
- Controller-facing facade services keep endpoint orchestration and read-model entrypoints stable for controllers.
- `*UseCase` classes own one mutation workflow each and expose exactly one public `execute` method.
- `*PolicyService` classes own named permission, ownership, and authority decisions.
- `*QueryService`, `*ViewAssembler`, and `*SectionsFactory` classes own read filtering, DTO assembly, and backend-prepared UI sections.
- `*PrimitiveService` classes own low-level reusable mutation primitives such as target resolution, state gating, persistence, and notification fan-out.
- Domain event handlers own cross-cutting side effects that can be emitted from mutation workflows without injecting downstream services directly into every use case.
- `ServiceLayeringConventionTest` enforces the narrow public entrypoint rule for workmarket use-case services.
- Repository read methods that feed DTO surfaces should expose named fetch profiles such as `findForQuestList`, `findForQuestDetail`, `findForApplicantDashboard`, and `findAvailableForCatalog`; `RepositoryFetchProfileContractTest` protects the current workmarket and thing-sharing profile set.

Shared concept standard:
- `common/concepts/ActorIdentity.java` is the shared representation of authenticated actor id and role checks.
- `common/concepts/ModuleOwnership.java` owns reusable owner/admin management checks for resources that belong to one user.
- `common/concepts/CircleVisibilitySelection.java` owns selected-circle id normalization and distinct-id accounting before module services resolve concrete `CircleGroup` rows.
- `common/concepts/SchedulingWindow.java` owns reusable start/end time shape checks such as past start, missing start for an end time, and invalid ranges.
- `CoreConceptsTest` is the regression layer for these primitives before they are reused by more modules.

Domain event standard:
- `common/event/DomainEvent.java` is the marker interface for lightweight backend events.
- `common/event/DomainEventPublisher.java` is the domain-facing publisher contract.
- `common/event/SpringDomainEventPublisher.java` bridges domain events to Spring application events.
- Domain-specific event records and handlers stay in the owning domain package; workmarket application news currently uses `workmarket/event/QuestApplicationNewsEvent.java` and `QuestApplicationNewsEventHandler.java`.
- Event delivery is synchronous today, preserving existing transaction-local behavior while separating mutation orchestration from side-effect wiring.

## Identity Source Map

Primary files:
- `identity/controller/AuthController.java`
- `identity/controller/AppUserController.java`
- `identity/model/AppUser.java`
- `identity/model/AppUserRole.java`
- `identity/service/AppUserService.java`
- `identity/service/AppUserReadService.java`
- `identity/service/AppUserLookupService.java`
- `identity/service/UserProfileViewService.java`
- `identity/service/AdminUserDetailService.java`
- `identity/service/IdentityUserSummaryAssembler.java`
- `identity/security/JwtService.java`
- `identity/security/JwtAuthFilter.java`
- `identity/security/RepositoryUserDetailsService.java`
- `identity/mapper/AppUserMgr.java`
- `identity/repository/AppUserRepository.java`
- `identity/dto/AppUserRequestDTO.java`
- `identity/dto/AppUserResponseDTO.java`
- `identity/dto/UserProfileViewDTO.java`
- `identity/dto/AdminUserDetailDTO.java`
- `identity/dto/auth/RegisterRequestDTO.java`
- `identity/dto/auth/LoginRequestDTO.java`
- `identity/dto/auth/AuthResponseDTO.java`

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
- `identity/service/AuthService.java`
- `identity/mapper/AuthMgr.java`
- `identity/security/JwtService.java`
- `identity/security/JwtAuthFilter.java`
- `identity/security/RepositoryUserDetailsService.java`
- `identity/dto/auth/RegisterRequestDTO.java`
- `identity/dto/auth/LoginRequestDTO.java`
- `identity/dto/auth/AuthResponseDTO.java`

Technical notes:
- `AuthController` is transport-only and delegates register, login, and current-user response assembly to `AuthService`.
- `AuthMgr` is the backend-owned mapper for `AuthResponse`, so auth response shaping stays consistent with the rest of the identity module.

## Business Hub Source Map

Primary files:
- `business/controller/BusinessProfileController.java`
- `business/model/BusinessProfile.java`
- `business/repository/BusinessProfileRepository.java`
- `business/service/BusinessProfileService.java`
- `business/mapper/BusinessProfileMgr.java`
- `business/dto/BusinessProfileRequestDTO.java`
- `business/dto/BusinessProfileResponseDTO.java`
- `business/dto/BusinessProfileListResponseDTO.java`
- `frontend/src/modules/business/api/businessApi.ts`
- `frontend/src/modules/business/views/BusinessHubView.vue`

Primary migrations:
- `V31__create_business_profile_table.sql`

Technical notes:
- Business Hub currently implements the first business-profile slice: one profile per owner account, active profile directory, public slug lookup, and current-user profile editing.
- `BusinessProfileService` owns profile validation, slug normalization, active-directory visibility, and rich-text description sanitization.
- Slugs are unique across all business profiles; inactive profiles remain available to the owner through `/business/profiles/me` but are hidden from the active directory and public slug endpoint.

## Thing Sharing Source Map

Primary files:
- `things/controller/ThingSharingController.java`
- `things/model/ThingListing.java`
- `things/model/ThingBorrowRequest.java`
- `things/model/ThingBorrowRequestStatus.java`
- `things/repository/ThingListingRepository.java`
- `things/repository/ThingBorrowRequestRepository.java`
- `things/service/ThingSharingService.java`
- `things/mapper/ThingSharingMgr.java`
- `things/dto/ThingListingRequestDTO.java`
- `things/dto/ThingBorrowRequestDTO.java`
- `things/dto/ThingListingResponseDTO.java`
- `things/dto/ThingBorrowRequestResponseDTO.java`
- `things/dto/ThingListingListResponseDTO.java`
- `frontend/src/modules/things/api/thingsApi.ts`
- `frontend/src/modules/things/views/ThingSharingView.vue`

Primary migrations:
- `V32__create_thing_sharing_tables.sql`

Technical notes:
- Thing Sharing currently implements the first lending workflow slice: owner-created listings, available-listing directory, owner listing list, and pending borrow requests.
- `ThingSharingService` rejects owner self-requests, unavailable listings, and duplicate pending requests for the same borrower and listing.
- Borrow request lifecycle is intentionally limited to `PENDING` and `CANCELLED` until owner approval, handoff, and return workflows are added.

## Ride Sharing Source Map

Primary files:
- `rides/controller/RideOfferController.java`
- `rides/model/RideOffer.java`
- `rides/repository/RideOfferRepository.java`
- `rides/service/RideOfferService.java`
- `rides/mapper/RideOfferMgr.java`
- `rides/dto/RideOfferRequestDTO.java`
- `rides/dto/RideOfferResponseDTO.java`
- `rides/dto/RideOfferListResponseDTO.java`
- `frontend/src/modules/rides/api/ridesApi.ts`
- `frontend/src/modules/rides/views/RideSharingView.vue`

Primary migrations:
- `V33__create_ride_offer_tables.sql`

Technical notes:
- Ride Sharing currently implements voluntary ride offers with driver-owned records and optional circle-scoped visibility.
- Empty `visibleCircleIds` means an active ride is visible to authenticated users; selected circles limit visibility to the driver and members of those circles.
- `RideOfferService` rejects past departures and selected visibility circles not owned by the driver.

## Control-System Technical Notes

Primary files:
- `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/QuestWorkflowScenarioTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/QuestWorkflowScenarioTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentExecutionServiceTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminSyntheticQuestExecutionPlannerTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/config/ServiceTransactionConfigurationTest.java`
- `scripts/local_tooling_common.rb`
- `scripts/audits/audit-change-impact-preflight.rb`
- `scripts/audits/audit-read-surface-inventory.rb`
- `scripts/audits/audit-mapper-usage.rb`
- `scripts/audits/audit-generated-artifact-freshness.rb`
- `scripts/audits/audit-api-contract-drift.rb`
- `scripts/audits/audit-repository-fetch.rb`
- `scripts/audits/audit-endpoint-callsite-linker.rb`
- `scripts/audits/audit-frontend-route-surfaces.rb`
- `scripts/audits/audit-frontend-stale-surfaces.rb`
- `scripts/audits/audit-frontend-state-logic-duplication.rb`
- `scripts/audits/audit-duplicate-logic.rb`
- `scripts/audits/audit-permission-rule-duplication.rb`
- `scripts/audits/local_tooling_batch_audits.rb`
- `scripts/audits/audit-frontend-dead-code.rb`
- `scripts/audits/audit-backend-dead-code.rb`
- `scripts/audits/audit-dead-code.rb`
- `scripts/audits/audit-state-transition-coverage.rb`
- `scripts/audits/audit-docs-to-code-drift.rb`
- `scripts/audits/audit-doc-coverage-gap.rb`
- `scripts/audits/audit-automation-readiness-gap.rb`
- `scripts/audits/audit-agent-model-feature-coverage.rb`
- `scripts/audits/audit-sandbox-generation-coverage.rb`
- `scripts/audits/audit-domain-ownership-inventory.rb`
- `scripts/audits/audit-config-sprawl.rb`
- `scripts/audits/audit-naming-consistency.rb`
- `scripts/audits/audit-dormant-code.rb`
- `scripts/audits/audit-manual-cleanup-candidate-report.rb`
- `scripts/audits/audit-file-relation-graph.rb`
- `scripts/audits/audit-test-surface-inventory.rb`
- `scripts/audits/audit-error-pattern.rb`
- `scripts/audits/audit-rich-text-safety.rb`
- `scripts/audits/audit-async-mutation-flow.rb`
- `scripts/audits/audit-style-token-usage.rb`
- `scripts/audits/audit-feature-intro-check.rb`
- `scripts/audits/audit-make-target-index.rb`
- `scripts/audits/audit-documentation.rb`
- `scripts/generate-agent-operating-model.rb`
- `scripts/generate-agent-endpoint-inventory.rb`
- `scripts/generate-automation-read-model-inventory.rb`
- `scripts/generate-source-of-truth-audit.rb`
- `apps/themuffinman/frontend/scripts/generate-vision-contracts.mjs`

Technical notes:
- Feature manifests are now classified by `changeMode`, `changeImpact`, and `changeProfiles` so required docs, tests, generator commands, and close-out audits can be enforced in validation.
- Feature manifest decisions are explicit: multi-file, multi-layer, high-risk, executor-critical, workflow-expansion, agent-contract, frontend-contract, backend-logic, generated-artifact, and master-plan-driven changes require a manifest; cosmetic and single-file contract-neutral refactors may skip it only when they do not alter behavior, contracts, generated artifacts, validation scope, or documentation meaning.
- Manifest decision auditing is available through `make audit-manifest-decision [files=<csv>]`, which reports whether the current changeset requires a feature manifest and lists the matching high-risk, multi-layer, workflow, contract, schema, generated-artifact, automation-safety, or skip-allowed reasons.
- Feature manifest artifact lists are now also validator-checked for path existence, duplicate entries, and clean separation between runtime code and test-only files.
- Validation evidence records live under `.agents/validation-evidence/`, use `docs/validation-evidence.schema.json`, and capture command results, `ranAt`, generated artifact actions, skipped checks, and reasons.
- Feature completion manifests embed structured closeout evidence under `validationEvidence`, `generatedArtifacts`, `planCompletion`, and `closeoutDecision`; `make feature-closeout-audit manifest=<manifest-file>` hard-fails completed manifests that lack required command evidence, skipped-check reasons, profile-specific validation, completed plan evidence, or clean artifact bucket ownership.
- Feature closeout enforcement is also available through `make enforce-feature-closeout manifest=<manifest-file>`, which writes machine-readable closeout-enforcement reports while hard-failing missing complete status, checklist evidence, required profile validation, generated artifact paths, duplicate artifact buckets, backlog links, or plan completion evidence.
- Validation command evidence can be recorded with `make record-validation manifest=<manifest-file> command='<command>'`, which runs the command, appends compact result metadata to the feature manifest, and writes companion validation-evidence JSON without storing raw logs.
- Plan completion evidence is independently auditable with `make audit-plan-completion plan=<plan-file> [manifest=<manifest-file>]`, which fails missing completion evidence, non-final completion status, unresolved task checkboxes, incomplete master-plan child rows, and completed manifests that reference incomplete plan evidence.
- Closeout report generation lives at `make closeout-report manifest=<manifest-file>` and writes `docs/generated/local-tooling/closeout-reports/<feature-id>.json` plus a short Markdown summary from manifest artifacts, validation evidence, docs delta, generated-artifact evidence, backlog delta, residual risks, and the current changed-file list.
- Codex-facing changed-file reports now share a generated-noise filter for `diff-summary`, `context-pack`, `audit-router`, and `fast-check`; default output excludes generated reports and transient `.agents` plan/evidence files while `include_generated=true` and `include_agents=true` restore full debug visibility.
- Context pack and session handoff reports support `budget=small|medium|large`; the default `small` budget caps file lists at 20 entries and records omitted sections plus read-next guidance, while `medium` and `large` opt into 50 and 100 entries.
- Generated commit scope reporting lives at `make audit-generated-commit-scope` and classifies changed generated files using `docs/generated/artifact-policy.yaml` into `task_required`, `supporting_context`, `stale_or_unrelated`, or `do_not_commit_by_default`.
- Feature slice recommendations are generated with `make recommend-feature-slices topic=<topic> [files=<csv>]`, producing backend, frontend, docs/artifact, and final-validation slices from topic-inferred or explicit files plus existing validation routing.
- Architecture decision indexing is available through `make architecture-decision-index`, producing a compact source-linked list of stable repository decisions Codex should read before editing backend, frontend, docs, generated artifacts, migrations, sandbox, git, config, or closeout workflows.
- Changeset risk scoring is available through `make changeset-risk [files=<csv>]`, using transparent weighted factors for controller contracts, DTO/model/schema changes, workflow or permission services, frontend contracts, agent/docs contracts, generated churn, mixed domains, and tooling changes.
- Failure knowledge-base generation is available through `make failure-knowledge-base [source=<diagnostic-report>]`, producing a compact troubleshooting index from diagnostic failure lines without storing raw validation logs.
- Product memory lives in `docs/product-memory.md` and records stable lessons, repeated patterns, and durable product rules without turning them back into active backlog items.
- The standard post-plan control loop is available through `make post-plan-memory-update plan=<plan-file> [manifest=<manifest-file>] [source=<diagnostic-report>]`, which refreshes plan completion, failure knowledge, documentation indexes, and staleness checks after a plan closes.
- Codebase capsule generation is available through `make codebase-capsule`, producing a short read-first Markdown and JSON summary with repository layout, active conventions, open backlog/master-plan context, and preferred first commands.
- Targeted test recommendations are available through `make recommend-targeted-tests [files=<csv>]`, producing a report-only command set with direct backend tests, frontend type/build checks, docs validation, generated-artifact checks, scenario catalog matches, and residual risk notes.
- Test history tracking is available through the diagnostic validation wrappers and `make test-history-summary`, storing compact command metadata, duration, pass/fail state, failing test names, and top error patterns without raw validation logs.
- Backend test fixture duplication is reportable with `make audit-test-fixture-duplication`, which scans repeated user, circle, quest, application, location, and chat setup patterns and recommends `TestFixtures` extraction candidates.
- Documentation template coverage is reportable with `make audit-doc-template-coverage [files=<csv>]`, which maps workflow, endpoint, DTO, schema migration, permission, and module changes to `.agents/templates/docs/` section coverage signals in living docs.
- Documentation-sync duplication review is reportable with `make audit-doc-sync-duplicates`, which finds duplicated protected phrases, fragment-only policy bullets, and conflicting doc-sync wording without rewriting canonical text.
- Changeset playbooks are available through `make changeset-playbook [files=<csv>]`, which combines diff shape, manifest decision, focused audit routing, likely documentation targets, and validation preset guidance into one ordered implementation workflow.
- Manifest path resolution is available through `make resolve-manifest-path [files=<csv>]`, which scores existing feature manifests against changed files, related plans, and artifact overlap and returns either one resolved manifest path or a review-required candidate list.
- Symbol-to-test linking is available through `make link-symbol-to-tests symbol=<symbol-name>`, which maps a Java or frontend symbol to matching definition files, direct test references, nearby domain tests, regression-scenario coverage, and a compact recommended command set.
- DTO usage packs are available through `make dto-usage-pack dto=<dto-name>`, which map one backend DTO to controller methods, backend references, frontend usage, generated contract mentions, docs references, tests, and a compact recommended command set.
- Workflow slice packs are available through `make workflow-slice-pack workflow=<workflow-id>`, which pull one documented workflow slice from the state-machine catalog, owning services, related tests, frontend actions, docs references, and scenario-backed validation commands.
- Plan/code maps are available through `make plan-code-map plan=<plan-file>`, which map one implementation plan to likely code files, living docs, generated artifacts, validations, manifests, and related audit targets.
- Changeset hotspot ranking is available through `make rank-changeset-hotspots [files=<csv>]`, which scores the current files by category weight, fanout, dependency edges, workflow sensitivity, and nearby tests so Codex can read the most central files first.
- Compact domain packs are available through `make domain-pack domain=<domain-id>`, which generate small stable packs for a named domain with key backend files, DTO groups, frontend surfaces, workflows, docs, tests, and a first command set.
- Validation preset recommendations are available through `make recommend-validation-preset [files=<csv>]`, which classifies the current changeset into deterministic presets such as `fast`, `standard-backend`, `standard-frontend`, `full-closeout`, or `manifest-required` and emits the associated command set.
- Audit delta review is available through `make audit-delta-report audit=<audit-id>`, which compares the latest JSON report to the previous archived snapshot and highlights count deltas plus newly introduced or fixed risk signals.
- Contract test gap auditing is available through `make audit-contract-test-gaps`, mapping endpoints and DTOs to backend test signals, generated frontend contracts, frontend usage, documented references, and priority-ranked missing coverage signals.
- Mutation safety coverage is reportable with `make audit-mutation-safety`, which scans mutating endpoints and service methods for missing static test signals around permissions, ownership, invalid transitions, scenario coverage, and side effects.
- Documentation-as-test coverage is reportable with `make audit-docs-as-tests`, which maps protected and behavioral living-doc statements to nearby test, audit, scenario, workflow, or operating-model evidence signals.
- `scripts/todo-audit.rb` now reports backlog traceability for each open persistent backlog ID across plans, feature manifests, docs, code surfaces, and inline `TODO(<ID>):` or `FIXME(<ID>):` references, and fails when an open ID has no trace outside its backlog file.
- Persistent backlog files must exist and must not keep resolved items as closed checkboxes, but they may legitimately contain zero open items after a backlog batch is fully implemented.
- Broad, long-running, high-complexity, multi-layer, high-risk, or master-plan-driven changes use operating-model implementation checkpoints for plan, first backend slice, first frontend slice, docs/artifacts sync, validation, and commit boundary.
- `documentation_ownership` in the agent operating model maps backend audit domains and change categories to required living docs, generated artifacts, and validation checks so documentation scope is machine-readable instead of inferred ad hoc.
- `documentation_ownership.documentation_templates` registers short `.agents/templates/docs/` templates for new workflows, endpoints, DTO contracts, modules, permission rules, and schema migrations.
- `policies.closeout_doc_delta` requires logic-drift closeout summaries to name behavior changed, docs updated, and related surfaces intentionally left unchanged.
- `make audit-doc-staleness-scoring` produces a report-first section-level documentation staleness score from current code changes, endpoint inventory freshness, DTO source freshness, and workflow/state-transition source freshness.
- `make post-merge-retrospective` writes a disposable retrospective artifact under `docs/generated/local-tooling/post-merge-retrospectives/` after large changes, summarizing failure points, missing tools, documentation gaps, and reusable patterns from existing local audit reports.
- `docs/example-scenario-library.md` keeps compact canonical implementation examples for endpoint, workflow-transition, DTO-contract, schema-migration, and documentation-update changes.
- `policies.self_test_matrix` maps risk tiers and change profiles to validation tiers: syntax-only, targeted unit, domain scenario, contract/type-check, generated-artifact validation, and full validation.
- Mutating intents are modeled explicitly in the operating contract with `preconditions`, `state_changes`, `side_effects`, `notifications`, and `blocking_conditions`.
- Named workflow state-machine contracts are cataloged in `docs/workflow-state-machines.yaml` and explained in `docs/workflow-state-machines.md`; enum-backed catalog states must match the Java enum source exactly.
- Workflow state-machine catalog validation also cross-checks documented transition intent ids against the agent operating model, so real transition intents must resolve to known mutating intents while explicit placeholders remain limited to derived or planned flows.
- Use-case workflow tests standardize the mutation pattern around target resolution, authority, state validation, persistence, notification, and fail-closed exits; applicant-side application use cases now have the same contract harness coverage as quest execution use cases.
- Scenario tests are the runtime proof layer above unit tests and should cover canonical multi-step workflows plus negative fail-closed behavior.
- `docs/generated/source-of-truth-audit.json` is the drift report for executor-critical files, stricter automation-relevant slices, and tracked tests that are missing source registration or workflow/documentation coverage; it also includes ownership-aware candidate entries plus domain and owner summaries for routing review.
- `docs/generated/backend-audit-inventory.json` is the full-backend classification report, includes explicit domain and owner assignment for every backend file, and stays the staging layer for future stricter audit coverage beyond the current executor-critical surface.
- The first stricter `automation_relevant` slice is the admin-agent DTO contract surface, which now requires source registration and documentation coverage before broader DTO tightening.
- The second stricter `automation_relevant` slice is the chat DTO contract surface, which keeps workspace, conversation, message, and socket DTO contracts inside the same registration and documentation gate.
- The third stricter `automation_relevant` slice is the identity DTO contract surface, which keeps auth, profile, and admin-user DTO contracts inside the same registration and documentation gate.
- The fourth stricter `automation_relevant` slice is the location DTO contract surface, which keeps lookup, visibility, debug, and user-location DTO contracts inside the same registration and documentation gate.
- The fifth stricter `automation_relevant` slice is the social request/relation DTO contract surface, which keeps circle request, block, relation-state, and connection-circle assignment DTO contracts inside the same registration and documentation gate.
- The sixth stricter `automation_relevant` slice is the social overview/member DTO contract surface, which keeps circle overview, group summary, and member DTO contracts inside the same registration and documentation gate.
- The seventh stricter `automation_relevant` slice is the social search/contact DTO contract surface, which keeps circle search, contact-list, candidate, and query DTO contracts inside the same registration and documentation gate.
- The eighth stricter `automation_relevant` slice is the social admin circle DTO contract surface, which keeps admin circle overview, group, and relation-row DTO contracts inside the same registration and documentation gate.
- The ninth stricter `automation_relevant` slice is the workmarket dashboard DTO contract surface, which keeps dashboard response, summary, navigation, grouping, planner, open-work, and notification DTO contracts inside the same registration and documentation gate.
- The tenth stricter `automation_relevant` slice is the workmarket quest-detail DTO contract surface, which keeps quest detail response, section, action, presentation, and viewer-relation DTO contracts inside the same registration and documentation gate.
- The eleventh stricter `automation_relevant` slice is the workmarket application-detail DTO contract surface, which keeps application detail response, section, context, action, and presentation DTO contracts inside the same registration and documentation gate.
- The twelfth stricter `automation_relevant` slice is the workmarket list/search/options DTO contract surface, which keeps quest list, application list, search query, admin application query, and workmarket option DTO contracts inside the same registration and documentation gate.
- The thirteenth stricter `automation_relevant` slice is the workmarket news read-model DTO contract surface, which keeps news item and destination DTO contracts inside the same registration and documentation gate.
- The fourteenth stricter `automation_relevant` slice is the common action/navigation DTO contract surface, which keeps shared action result, label-value, and navigation target primitives inside the same registration and documentation gate.
- The broad `automation_relevant` service catch-all remains report-first; future service hardening should promote only small rule-scoped service slices after ownership, source registration, documentation coverage, and validation evidence are low-noise.
- Frontend planner support prefers generated workflow-aware helpers from the operating model over hand-maintained intent or safety identifiers; the admin-agent UI now validates simulation intent ids, endpoint ids, and safety-flag ids against the generated frontend contract.
- `docs/generated/local-tooling/change-impact-preflight.json` and `change-impact-preflight-summary.md` provide a compact dependency map for changed files so Codex can start from likely docs, tests, generated artifacts, and sibling read surfaces instead of broad repo discovery.
- Change impact preflight now includes report-only scope guardrails for mixed product domains, runtime-plus-tooling changes, broad generated-report churn, and unexpected generated report files.
- `docs/generated/artifact-policy.yaml` and `docs/generated/README.md` define which generated artifacts are source-of-truth snapshots, tracked review context, disposable local context, or do-not-commit-by-default outputs.
- Context-first session starts should read diff summary, audit summary index, and a topic context pack before broad repository exploration; repo map and symbol index are secondary compact lookups when the first three sources are insufficient.
- `docs/generated/local-tooling/read-surface-inventory.json` and `read-surface-inventory-summary.md` inventory read-oriented backend service methods, DTO assembly hints, repository usage, read-only transaction coverage, and a narrower `transaction_relevant` slice so helper-only services do not inflate the main transaction-gap count.
- `docs/generated/local-tooling/mapper-usage-audit.json` and `mapper-usage-audit-summary.md` show where rich mappers are used and classify caller context as controller-facing, mutating, read-oriented, or supporting.
- `docs/generated/local-tooling/generated-artifact-freshness.json` and `generated-artifact-freshness-summary.md` report whether generated contracts and inventories appear stale relative to their tracked source inputs.
- `docs/generated/local-tooling/api-contract-drift.json` and `api-contract-drift-summary.md` compare backend DTO fields, generated frontend contract fields, and observed frontend field references.
- `docs/generated/local-tooling/api-contract-drift-cleanup-shortlist.md` is the review-first shortlist for manual DTO cleanup, separating likely UI-dead fields from admin, automation, and diagnostics payloads that should not be removed blindly.
- `docs/generated/local-tooling/repository-fetch-audit.json` and `repository-fetch-audit-summary.md` connect repository query methods, explicit fetch coverage, downstream service callers, helper usage, and likely lazy relation dereferences so Codex can audit fetch risk without manual controller-to-repository tracing.
- Workmarket and thing-sharing repositories now use named fetch profiles for their primary DTO read surfaces, with compatibility delegates retained only for older call sites and tests.
- `docs/generated/local-tooling/endpoint-callsite-linker.json` and `endpoint-callsite-linker-summary.md` connect backend endpoints to frontend API client methods and importing pages, views, or composables so feature-entry navigation can start from one compact report.
- `docs/generated/local-tooling/frontend-route-surface-inventory.json` and `frontend-route-surface-inventory-summary.md` invert that same navigation from the frontend side, listing each route surface, its primary composables, API clients, and linked backend endpoints.
- Older frontend stale-surface snapshot files have been retired; current local-tooling audits now rely on the active generated inventories instead of those deleted snapshots.
- `docs/generated/local-tooling/frontend-state-logic-duplication-audit.json` and `frontend-state-logic-duplication-audit-summary.md` cluster repeated frontend mutation runners, workflow action names, dialog open-close helpers, and feedback-error patterns across active route-backed surfaces.
- `docs/generated/local-tooling/duplicate-logic-audit.json` and `duplicate-logic-audit-summary.md` shortlist frontend-local status mapping, permission gating, and transition-eligibility helpers that may duplicate backend presentation or policy logic.
- `docs/generated/local-tooling/permission-rule-duplication-audit.json` and `permission-rule-duplication-audit-summary.md` separate backend permission sources, backend presentation-flag derivation, frontend passthrough gates, and frontend-local permission gates so cross-layer action drift can be reviewed from one compact report.
- The batch audit engine under `scripts/audits/local_tooling_batch_audits.rb` now also generates compact dead-code, transition-coverage, docs-drift, automation-readiness, sandbox-coverage, ownership, config, naming, dormant-code, cleanup, relation-graph, test-surface, error-pattern, rich-text-safety, async-mutation, style-token, feature-intro, make-target-index, and audit-documentation reports.

### User account management

Primary files:
- `identity/controller/AppUserController.java`
- `identity/service/AppUserService.java`
- `identity/service/AppUserReadService.java`
- `identity/service/AppUserLookupService.java`
- `identity/repository/AppUserRepository.java`
- `identity/dto/AppUserRequestDTO.java`

### Profile and profile view

Primary files:
- `identity/model/AppUser.java`
- `identity/service/UserProfileViewService.java`
- `identity/service/IdentityUserSummaryAssembler.java`
- `identity/mapper/AppUserMgr.java`
- `identity/dto/AppUserResponseDTO.java`
- `identity/dto/UserProfileViewDTO.java`

Technical notes:
- `UserProfileViewDTO` now carries deterministic resolution metadata so future automation can refer to an exact profile target without rebuilding labels client-side.
- `IdentityUserSummaryAssembler` owns the shared enriched `AppUserResponseDTO` assembly used by profile-view and admin-detail surfaces so the repeated stats wiring stays in one place.
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
- `location/service/LocationDebugStatusAssembler.java`
- `location/service/GeoapifyLocationLookupClient.java`
- `location/service/DisabledLocationLookupClient.java`
- `location/service/AdminDatabaseMetricsService.java`
- `location/repository/LocationLookupEventRepository.java`
- `location/dto/UserLocationSettingsDTO.java`
- `location/dto/UserLocationSettingsRequestDTO.java`
- `location/dto/LocationLookupCandidateDTO.java`
- `location/dto/LocationLookupResponseDTO.java`
- `location/dto/LocationDebugStatusViewDTO.java`

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
- `location/service/LocationDebugStatusAssembler.java`
- `location/service/AdminDatabaseMetricsService.java`
- `location/model/LocationLookupEvent.java`
- `location/repository/LocationLookupEventRepository.java`
- `location/dto/LocationDebugStatusViewDTO.java`

Technical notes:
- `LocationDebugStatusAssembler` owns the final debug/status DTO shaping so `LocationLookupService` can stay focused on lookup execution, rate limiting, caching, and audit recording.

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
- The frontend includes both the global chat tray and a standalone `/chat` workspace surface backed by `ChatWorkspaceDTO`.
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
- `agent/sandbox/SandboxGenerationPlanner.java`
- `agent/sandbox/SandboxGenerationPlan.java`
- `agent/dto/AdminAgentPlaygroundRequestDTO.java`
- `agent/dto/AdminAgentPlaygroundResponseDTO.java`
- `config/AgentProperties.java`

Technical notes:
- The current admin agent playground is an admin-only planning surface with one Phase 1 guarded execution slice for synthetic quest batch generation.
- Production admin planning lives under `agent/service`, while sandbox and synthetic-data planning helpers live under `agent/sandbox`.
- `SandboxGenerationPlanner` contributes sandbox-only workflows, synthetic marker requirements, and warnings back into the admin playground response.
- `AdminAgentSurfacePolicy` and `VisionSurfacePolicy` now standardize the authority split between admin-scoped and user-scoped execution instead of leaving that split implicit in each surface.
- `AdminAgentPromptPreparationService` now centralizes prompt normalization and English-only planning preparation so planner and executor use the same backend-managed boundary.
- `PromptSemanticsSupport` now provides the shared prompt-normalization and semantic-classification rules used by both Vision and Admin Playground.
- `AdminSyntheticQuestExecutionPlanner` extracts the first reusable direct-execution plan shape from natural language, currently limited to synthetic quest batches for one exact target user.
- `AdminAgentExecutionService` now backs `POST /admin/agent/execute`, reuses the existing quest creation use case through `creatorId`, requires explicit confirmation, and caps batch size through typed `AgentProperties`.
- Prompt classification now runs directly on the normalized prompt before intent heuristics.
- The local admin path is English-only and no longer rewrites prompts before planning, while provider-backed translation remains the path for arbitrary languages such as Mandarin.
- The response now also includes structured resolution requirements, clarification contract data, and execution-readiness metadata.
- The shared semantic envelope now carries required-slot lists, a clarification flag, and replay metadata so resolution traces can be reproduced without reconstructing them from free-form text.
- Planner coverage now extends the same reusable fail-closed contract across owned-quest updates, pending-application self-service, outgoing request cancellation, owner-circle management, and chat read actions.
- The same contract now also backs owner-side application approval or decline, owned-circle rename or delete, and self profile-location updates inside `/vision`.
- The same contract now also backs create/accept/delete circle-request mutations inside `/vision`.
- Managed application mutations in `/vision` must resolve one exact manageable quest plus one exact pending applicant before review-ready state.
- Owned-circle mutations in `/vision` must resolve one exact owned circle before rename or delete review-ready state.
- Circle-request mutations in `/vision` must resolve one exact counterpart user or one exact pending request before review-ready state.
- Profile-location mutation in `/vision` must preserve unrelated profile identity fields and existing location visibility/radius settings instead of sending sparse DTO writes.
- The same planner contract now also covers self-profile updates, exact notification targeting, and admin-side application correction or deletion.
- The machine model now also enumerates common read surfaces and auth-adjacent capabilities so future executors do not derive endpoint affordances from frontend behavior alone.
- It accepts free-form prompts and returns structured warnings, suggested workflows, and next steps from deterministic backend classification.
- The deterministic payload should keep structured planner fields separate from provider-written summary text, including matched signals and unresolved required inputs.
- Provider-written summary text should be constrained to the deterministic workflow ids, signals, and unresolved inputs already classified by the backend.
- The admin playground summary path now stays deterministic and local-only.
- `AgentProperties` now carries separate default, semantic-upgrade, and creative model settings plus a shared reasoning-effort default for the Responses API payload.
- If an explicit semantic-upgrade flag is enabled and a heavier model is configured, the vision semantic path can temporarily use that model; otherwise it stays on the low-cost default.
- The admin playground no longer requests a live OpenAI planning summary; it falls back to deterministic local planning and keeps the same response contract.
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
- `social/service/CircleAdminOverviewAssembler.java`
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
- `social/dto/CircleRelationStatusDTO.java`
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
- `social/service/CircleReadService.java`
- `social/service/CircleRelationshipReadService.java`
- `social/service/CircleAdminOverviewAssembler.java`
- `social/service/CircleViewAssembler.java`

### Search and nearby discovery

Primary files:
- `social/service/CircleReadService.java`
- `social/service/CircleRelationshipReadService.java`
- `social/service/CircleDiscoveryService.java`
- `social/service/CircleViewAssembler.java`

### Presentation and action contracts

Primary files:
- `social/service/SocialPresentationHelper.java`
- `social/service/SocialRelationActionHelper.java`
- `social/service/CircleViewAssembler.java`

Technical notes:
- `CircleAdminOverviewAssembler` owns admin circle overview filtering and row shaping so `CircleReadService` stays focused on orchestration and `CircleViewAssembler` stays focused on the non-admin presentation surface.
- `CircleRelationshipReadService` owns direct relationship, membership, and exact relation lookup helpers so `CircleReadService` can stay focused on overview and list orchestration.

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
- `workmarket/service/WorkmarketQuestService.java`
- `workmarket/service/WorkmarketQuestUpdateService.java`
- `workmarket/service/WorkmarketQuestValidationService.java`
- `workmarket/service/WorkmarketQuestStateTransitionService.java`
- `workmarket/service/WorkmarketQuestVisibilityService.java`
- `workmarket/service/WorkmarketQuestApplicationService.java`
- `workmarket/service/WorkmarketQuestQueryService.java`
- `workmarket/service/WorkmarketQuestNewsService.java`
- `workmarket/service/WorkmarketDashboardReadService.java`
- `workmarket/service/DashboardVoiceService.java`
- `workmarket/service/OpenAiVoiceClient.java`
- `workmarket/service/WorkmarketQuestAccessPolicyService.java`
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
- `workmarket/service/WorkmarketQuestService.java`
- `workmarket/service/WorkmarketCreateQuestUseCase.java`
- `workmarket/service/WorkmarketUpdateQuestUseCase.java`
- `workmarket/service/WorkmarketDeleteQuestUseCase.java`
- `workmarket/service/WorkmarketQuestUpdateService.java`
- `workmarket/service/WorkmarketQuestValidationService.java`
- `workmarket/service/WorkmarketQuestVisibilityService.java`
- `workmarket/service/WorkmarketQuestAccessPolicyService.java`
- `workmarket/service/WorkmarketQuestQueryService.java`
- `workmarket/service/WorkmarketQuestExecutionPrimitiveService.java`
- `workmarket/service/QuestViewAssembler.java`
- `workmarket/model/Quest.java`
- `workmarket/model/QuestStatus.java`
- `workmarket/model/QuestAudience.java`
- `workmarket/mapper/QuestMgr.java`
- `workmarket/dto/QuestRequestDTO.java`
- `workmarket/dto/QuestResponseDTO.java`
- `workmarket/dto/QuestListResponseDTO.java`
- `workmarket/dto/QuestSearchRequestDTO.java`
- `workmarket/dto/QuestListPreset.java`

Technical notes:
- `QuestRequestDTO` requires `awardAmount`, but the accepted floor is `0.00`, not `0.01`.
- `WorkmarketQuestValidationService` treats `awardAmount == 0` as a valid free-quest configuration and still rejects negative values.
- `WorkmarketQuestService` now stays as the controller-facing mutation facade while dedicated workmarket read services own quest detail and list assembly.
- `WorkmarketQuestExecutionPrimitiveService` centralizes quest target resolution, owner or execution authority checks, state gating, persistence, and notification fan-out for agent-safe mutation flows.

### Applications

Primary files:
- `workmarket/service/WorkmarketQuestApplicationService.java`
- `workmarket/service/WorkmarketQuestApplicationViewAssembler.java`
- `workmarket/service/WorkmarketQuestApplicationWorkflowSupport.java`
- `workmarket/service/WorkmarketApplyForQuestUseCase.java`
- `workmarket/service/WorkmarketUpdateMyApplicationUseCase.java`
- `workmarket/service/WorkmarketWithdrawMyApplicationUseCase.java`
- `workmarket/service/WorkmarketApproveApplicationUseCase.java`
- `workmarket/service/WorkmarketDeclineApplicationUseCase.java`
- `workmarket/model/QuestApplication.java`
- `workmarket/model/QuestApplicationStatus.java`
- `workmarket/mapper/WorkmarketQuestApplicationMgr.java`
- `workmarket/dto/QuestApplicationRequestDTO.java`
- `workmarket/dto/AdminQuestApplicationUpdateRequestDTO.java`
- `workmarket/dto/QuestApplicationResponseDTO.java`
- `workmarket/dto/QuestApplicationListResponseDTO.java`
- `workmarket/dto/QuestApplicationsViewDTO.java`
- `workmarket/dto/ApplicationAllowedAction.java`
- `workmarket/dto/QuestApplicationDetailContextSectionDTO.java`
- `workmarket/dto/QuestApplicationDetailNavigationSectionDTO.java`
- `workmarket/dto/QuestApplicationDetailResponseDTO.java`
- `workmarket/dto/QuestApplicationDetailSectionsDTO.java`
- `workmarket/dto/QuestApplicationPresentationDTO.java`
- `workmarket/dto/AdminApplicationsQueryDTO.java`

Technical notes:
- `QuestApplicationRequestDTO.proposedPrice` is conditional on quest pricing rather than universally required.
- Paid quests require `proposedPrice >= 0.01`.
- Free quests require `proposedPrice == null`.
- `QuestApplicationsViewDTO` now carries deterministic owner-side pending selection metadata through `pendingApplicationCount` and `oldestPendingApplicationId`.
- `QuestResponseDTO` and `QuestApplicationResponseDTO` now carry deterministic resolution metadata for exact target selection.
- `WorkmarketQuestApplicationService` stays as the controller-facing mutation facade, while dedicated read services own applicant, management, public, and detail application assembly.
- `QuestApplicationViewAssembler` is the single application DTO assembly path for applicant, management, public, and viewer-specific responses; dashboard and detail read surfaces should route viewer-specific application DTOs through the dedicated workmarket read services.
- `WorkmarketQuestApplicationWorkflowSupport` centralizes application workflow guards for quest visibility, open-state checks, duplicate applications, pending application resolution, and price/message validation.
- `WorkmarketQuestAccessPolicyService` owns named workmarket permission decisions such as quest management, application management, application detail access, application eligibility, quest execution, and term-change decisions.
- `QuestApplicationResponseDTO` read surfaces must map through fetch-safe application repository queries that include the quest, quest creator, and applicant, then apply role-specific assembly through the dedicated workmarket application read helpers.
- Applicant-side pending-application update and withdrawal flows are modeled as exact application resolution followed by the same backend validation rules used by the existing service methods.
- `WorkmarketQuestReadService.getQuestDetailResponseById` and `WorkmarketQuestApplicationReadService.getApplicationDetailResponseById` preserve applicant self-service action flags on self-owned pending applications so the frontend can render withdraw actions consistently on detail surfaces.
- `QuestNewsItemResponseDTO` now also carries deterministic resolution metadata so item-specific notification actions can target one exact backend row.
- `WorkmarketQuestApplicationService.approveApplication` and `WorkmarketQuestApplicationService.declineApplication` both require an `OPEN` quest plus owner-or-admin authority.
- `WorkmarketQuestApplicationService.declineApplication` only mutates `PENDING` applications and transitions them to `DECLINED`.

### Execution and term negotiation

Primary files:
- `workmarket/service/WorkmarketQuestStateTransitionService.java`
- `workmarket/service/WorkmarketQuestService.java`
- `workmarket/service/WorkmarketStartQuestUseCase.java`
- `workmarket/service/WorkmarketCompleteQuestUseCase.java`
- `workmarket/service/WorkmarketConfirmQuestTermChangeUseCase.java`
- `workmarket/service/WorkmarketRejectQuestTermChangeUseCase.java`
- `workmarket/service/WorkmarketQuestExecutionPrimitiveService.java`
- `workmarket/service/WorkmarketQuestAccessPolicyService.java`
- `workmarket/service/QuestWorkflowNotificationService.java`
- `workmarket/dto/QuestAllowedAction.java`
- `workmarket/dto/QuestDetailExecutionAction.java`
- `workmarket/dto/QuestDetailExecutionSectionDTO.java`
- `workmarket/dto/QuestDetailManagementSectionDTO.java`
- `workmarket/dto/QuestDetailNavigationSectionDTO.java`
- `workmarket/dto/QuestDetailResponseDTO.java`
- `workmarket/dto/QuestDetailReviewSectionDTO.java`
- `workmarket/dto/QuestDetailReviewTargetDTO.java`
- `workmarket/dto/QuestDetailSectionsDTO.java`
- `workmarket/dto/QuestDetailTermChangeSectionDTO.java`
- `workmarket/dto/QuestPresentationDTO.java`
- `workmarket/dto/QuestViewerRelation.java`

Technical notes:
- `WorkmarketQuestStateTransitionService.validateQuestExecutionAuthority` allows owner, admin, or approved applicant to run execution actions.
- `WorkmarketStartQuestUseCase.execute` requires `ASSIGNED` and transitions the quest to `IN_PROGRESS`.
- `WorkmarketCompleteQuestUseCase.execute` requires `IN_PROGRESS` and transitions the quest to `COMPLETED`.
- `WorkmarketQuestStateTransitionService.validateQuestTermDecisionAuthority` allows only admin or approved applicant to confirm or reject pending term changes.
- `WorkmarketConfirmQuestTermChangeUseCase.execute` and `WorkmarketRejectQuestTermChangeUseCase.execute` both enter through the shared execution primitive service before delegating the actual transition to `WorkmarketQuestStateTransitionService`.
- `WorkmarketQuestStateTransitionService.confirmQuestTermChange` applies pending term fields, restores the previous quest status, and clears the pending state.
- `WorkmarketQuestStateTransitionService.rejectQuestTermChange` restores the previous quest status without applying pending term fields, then clears the pending state.
- If `termChangePreviousStatus` is missing while resolving a term decision, the backend fails instead of inventing a fallback status.
- `WorkmarketQuestAccessPolicyService` exposes these transitions to the frontend as allowed actions: `START`, `COMPLETE`, `CONFIRM_TERM_CHANGE`, and `REJECT_TERM_CHANGE`.

Voice-flow notes:
- Relative schedule input such as `tomorrow at 15:00` must be resolved into an absolute timestamp in caller timezone before `QuestRequestDTO` is sent.
- Selected-people audience preparation must resolve names or phrases into concrete accepted connections before circle assignment.
- Owner-side quest commands such as approve, decline, or delete must resolve exactly one owned quest before mutation.
- Approve-the-first-applicant style commands must use deterministic backend pending-selection metadata instead of incidental frontend ordering.
- A prepare-to-start automation flow still requires separate applicant-side `apply` actions and owner-side `approve` actions before `start`.
- Unexpected applicants outside the resolved selected-people set must not be auto-approved; they require explicit decline or manual handling.
- Owner-side term change after `ASSIGNED` or `IN_PROGRESS` can route through `WorkmarketQuestUpdateService` into `WAITING_CONFIRMATION`.
- Review creation is a separate post-completion mutation and must only target the employer or an approved worker of the completed quest.

### Dashboard and notifications

Primary files:
- `workmarket/service/WorkmarketDashboardReadService.java`
- `workmarket/service/WorkmarketDashboardSummaryAssembler.java`
- `workmarket/service/WorkmarketDashboardSectionsFactory.java`
- `workmarket/service/WorkmarketQuestNewsService.java`
- `workmarket/model/QuestNewsItem.java`
- `workmarket/model/QuestNewsType.java`
- `workmarket/mapper/QuestNewsMgr.java`
- `workmarket/dto/DashboardResponseDTO.java`
- `workmarket/dto/DashboardSummaryDTO.java`
- `workmarket/dto/DashboardSectionsDTO.java`
- `workmarket/dto/DashboardNavigationSectionDTO.java`
- `workmarket/dto/DashboardNavigationItemDTO.java`
- `workmarket/dto/DashboardApplicationGroupDTO.java`
- `workmarket/dto/DashboardQuestGroupDTO.java`
- `workmarket/dto/DashboardOpenWorkSectionDTO.java`
- `workmarket/dto/DashboardPlannerSectionDTO.java`
- `workmarket/dto/DashboardPlannerItemDTO.java`
- `workmarket/dto/DashboardNotificationsSectionDTO.java`
- `workmarket/dto/DashboardNotificationItemDTO.java`
- `workmarket/dto/DashboardNotificationDestinationType.java`
- `workmarket/dto/QuestNewsItemResponseDTO.java`

Technical notes:
- `WorkmarketDashboardSummaryAssembler` owns the workmarket dashboard summary counts while `WorkmarketDashboardReadService` stays focused on dashboard orchestration and screen assembly.
- `WorkmarketDashboardSectionsFactory` owns dashboard navigation section labels and descriptions, plus grouped quest/application, planner, open-work, and notification sections.
- Frontend dashboard selectors should prefer `DashboardSectionsDTO.navigation.tabs` for dashboard section titles and descriptions, with local tab ids kept only for routing fallback before the dashboard response loads.

### Reviews and ratings

Primary files:
- `workmarket/service/WorkmarketUserReviewService.java`
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
- `workmarket/dto/AppUserRoleOptionDTO.java`
- `workmarket/dto/QuestApplicationStatusFilterOptionDTO.java`
- `workmarket/dto/QuestAudienceFilterOptionDTO.java`
- `workmarket/dto/QuestAudienceOptionDTO.java`
- `workmarket/dto/QuestSortOptionDTO.java`
- `workmarket/dto/QuestStatusFilterOptionDTO.java`
- `workmarket/dto/QuestStatusOptionDTO.java`

## Frontend Source Map

Primary files:
- `frontend/src/router.ts`
- `frontend/src/api/httpClient.ts`
- `frontend/src/api/apiErrors.ts`
- `frontend/src/modules/identity/api/authApi.ts`
- `frontend/src/modules/vision/api/visionApi.ts`
- `frontend/src/modules/vision/api/visionConversationApi.ts`
- `frontend/src/modules/vision/api/contracts.ts`
- `frontend/src/modules/vision/api/clients/questsApi.ts`
- `frontend/src/modules/vision/api/clients/applicationsApi.ts`
- `frontend/src/modules/vision/api/clients/dashboardApi.ts`
- `frontend/src/modules/vision/api/clients/newsApi.ts`
- `frontend/src/modules/vision/api/clients/usersApi.ts`
- `frontend/src/modules/vision/api/clients/circlesApi.ts`
- `frontend/src/modules/vision/api/clients/locationApi.ts`
- `frontend/src/modules/chat/api/chatApi.ts`
- `frontend/src/contracts/index.ts`
- `frontend/src/contracts/generated/themuffinmanContract.ts`

Primary route entrypoints:
- `frontend/src/modules/vision/views/VisionQuestDetailView.vue`
- `frontend/src/modules/vision/views/VisionApplicationDetailView.vue`
- `frontend/src/modules/vision/views/VisionCirclesView.vue`
- `frontend/src/modules/vision/views/VisionChatWorkspaceView.vue`
- `frontend/src/modules/vision/views/VisionUserProfileView.vue`
- `frontend/src/modules/vision/views/VisionUserSettingsView.vue`
- `frontend/src/modules/identity/views/LoginView.vue`
- `frontend/src/modules/identity/views/RegisterView.vue`

Frontend state notes:
- `VisionQuestDetailView.vue` and `VisionApplicationDetailView.vue` stay thin rendering surfaces. Quest-detail section visibility, owner/application surface state, and edit dirty-state checks still live in `useQuestDetailView` and related composables so future workflow changes are made in reusable logic instead of the template file.

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
- other-profile view resolves relation through `CircleRelationshipReadService`
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
- `LocationAccessPolicyService` owns the named exact-location permission decision used by quest location labels and visibility summaries
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
- circle-only quest visibility is enforced through `WorkmarketQuestVisibilityService`
- selected visible circles are resolved from owner-owned circles only
- fallback circle visibility uses `circleService.isCircleBetween(currentUser, quest.getCreator())`
- workmarket notifications also carry circle-request events and actions, so the news feed is not quest-only
- workmarket ownership, admin, application, and execution permissions must route through `WorkmarketQuestAccessPolicyService` instead of duplicating role or owner checks

Location coupling:
- quest writes delegate location handling to `LocationSettingsService`
- nearby search scope depends on current user saved location and normalized radius
- quest DTO presentation includes location label, source summary, and visibility summary generated from location rules
- exact quest address visibility can degrade per viewer based on the creator profile-sharing rules
- exact quest address access must route through `LocationAccessPolicyService` instead of duplicating scope-specific checks

Common-platform coupling:
- workmarket services use `ServiceErrors` for business-failure shaping
- quest and application rich-text fields are sanitized through `RichTextInputValidator`
- list and search flows use `PageWindow` and `PageResponseFactory`
- search input normalization depends on shared normalizer helpers

## Frontend Route and API Contracts

Route guards:
- `/login` and `/register` are public routes
- all authenticated user interactions now resolve through `/vision`
- admin-capability actions are surfaced through Vision rather than through a separate frontend route tree
- logged-in users are redirected away from auth screens to `/vision`

Frontend transport contract:
- `API_BASE_URL` defaults to `http://localhost:8080`
- axios client is shared across modules
- auth headers come from `withAuth()`
- only unauthorized `/auth/me` responses trigger automatic session clearing and redirect logic

Contract-shape model:
- `frontend/src/contracts/generated/themuffinmanContract.ts` is the generated DTO source
- `frontend/src/contracts/index.ts` re-exports generated contract types for app use
- `frontend/src/modules/vision/api/contracts.ts` aliases generated DTOs into Vision-facing names and adds a few frontend-side request refinements
- `visionApi` aggregates endpoint clients so surviving Vision screens can consume user, circle, location, quest, and application contracts through one import surface
- the generated frontend contract namespaces enum exports by backend domain when Java models reuse the same simple enum name in more than one package
- `npm run generate:contracts` writes the generated frontend contract, while `npm run validate:contracts` and the normal frontend build fail when the checked-in generated contract is stale.
- `VisionSurfaceModernView` now presents the main turn flow as a compact terminal rail and keeps the richer summary preview in the side column only for result or review states.
- `VisionFlowDebugPanel` now stays minimal and line-oriented so debug state can be read without mixing it into the main preview narrative.
- `useVisionConversation` now normalizes some slot labels and placeholders down to short terminal-friendly words so the active flow line carries the entity context and the slot label stays compact.
- `VisionTerminalRow` now provides the shared compact row grammar used by the flow debug rail and the intent preview rail.
- `VisionCanvasRenderer` now renders discovery, summary, and choice output through the same shared row grammar instead of separate card-like surfaces.

Endpoint client mapping:
- `questsApi` maps `/quests`, `/quests/search`, `/quests/presets/:preset`, `/quests/:id`, `/quests/:id/detail`, and lifecycle mutation routes
- `applicationsApi` maps applicant, owner, admin, and review endpoints
- `dashboardApi` maps dashboard read models and summary sections
- `newsApi` maps unread count and read-state mutations
- `usersApi` maps `/app_users`, `/app_users/options`, `/app_users/me`, and `/app_users/:id/profile-view`
- `circlesApi` maps social and circle-management endpoints through the Vision-facing client layer
- `locationApi` maps `/location/lookup`, `/location/reverse-lookup`, and `/location/admin/status`
- `chatApi` is separate from `visionApi` and maps `/chat/*`

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

## Config Source Map

Primary files:
- `config/AgentProperties.java`
- `config/BootstrapProperties.java`
- `config/LocationProviderProperties.java`
- `config/RetentionProperties.java`
- `config/SecurityProperties.java`
- `config/AdminBootstrapConfig.java`
- `config/SecurityConfig.java`
- `config/WebSocketConfig.java`

## Update Rule

- Treat this file as a technical contract.
- When entity fields, relations, validations, permission checks, or workflow rules change, update this file in the same change.
- If a change introduces a new invariant, write it down here explicitly.
