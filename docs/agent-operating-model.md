# Agent Operating Model

This file is the human-review layer for agent-safe product operations.

It exists to reduce the risk of future automation, batch agents, or voice agents performing invalid actions from partial understanding.

The machine-readable source of truth is:
- `docs/agent-operating-model.yaml`
- generated from `docs/agent-operating-model/sections/*.yaml` by `scripts/generate-agent-operating-model.rb`
- cross-checked against `docs/generated/agent-endpoint-inventory.json` and `docs/generated/automation-read-model-inventory.json`
- validated by `docs/agent-operating-model.schema.json`
- enforced by `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`

## Safety Goal

- Do not let an automation layer invent missing business logic.
- Do not let a voice agent skip required prerequisites.
- Do not let stale documentation silently drift away from backend rules.

## Operating Rules

- Treat backend code as the final execution authority.
- Treat `docs/codex-fast-path.md` as the compact execution entrypoint for most feature work.
- `docs/codex-fast-path.md` is the compact execution entrypoint for most feature work.
- For product-direction, UX, adaptive-surface, or Social Useful Network sessions, read `docs/product-memory.md` and `docs/product-vision.md` first so future reasoning starts from durable lessons and the canonical product direction instead of from backlog noise.
- For `/vision` implementation work, read `docs/vision-architecture-patterns.md` before backend orchestration, API, frontend canvas, prompt-handling, or executor decisions.
- Treat `agent-operating-model.yaml` as the machine-operational contract for high-impact workflows.
- Treat `docs/feature-delivery-workflow.md` as the canonical human-readable source for the end-to-end feature delivery process around plans, context, validation evidence, and closeout.
- Treat `docs/validation-memory.md` and `docs/validation-memory.json` as the compact durable memory for canonical validator-facing commands, manifest evidence shape, and repeat closeout pitfalls.
- Treat `docs/generated/local-tooling/validation-memory-closeout-card-summary.md` as the shortest generated reminder for manifest-backed closeout commands when the full validation-memory docs would be excessive.
- Use the full workflow only when the change is high-risk, multi-layer, agent/tooling/workflow-related, or when a resolver requires it.
- Manifest usage is tier-driven and conditional instead of being the default for every non-trivial backend change.
- Treat the section files under `docs/agent-operating-model/sections/` as the editable source for that machine contract and regenerate the combined YAML after changes.
- Treat `docs/implementation-backlog.md` and `docs/agent-improvement-backlog.md` as the persistent open-work registries for deferred implementation and deferred control-system work.
- Treat `docs/program-planning-model.md` as the hierarchy contract for God Plans, Master Plans, Plans, and temporary machine-readable work products.
- For protected documentation-sync phrases, copy the exact canonical sentence verbatim into every required file.
- Keep workflows procedural, explicit, and dependency-ordered.
- For broad, long-running, or high-complexity work, prefer a master plan that coordinates a group of narrower `.agents/*-plan.md` files in explicit sequence instead of treating the entire task as one flat plan.
- Use the master-plan pattern when it safely reduces unnecessary human interaction, increases automation, or makes a larger batch auditable through one final closeout pass.
- For work that spans several master plans, use a God Plan under `.agents/god-plans/` to coordinate implementation state, pros, cons, risks, decisions, and child Master Plans.
- Temporary machine-readable work products belong under `.agents/tmp/`, must name their owning plan, and must be deleted, promoted into durable docs, or explicitly archived when the owning plan closes.
- When `AGENTS.md` records a standing autonomous continuation preference, do not stop only to ask which safe offered follow-up slice should run next; continue with the best sequenced slice unless scope changes, approval is required, or a real blocker appears.
- When `AGENTS.md` records the standing follow-up capture preference, record discovered safe improvements and repeated failure patterns in the appropriate follow-up or backlog surface during the active slice and continue with the best sequenced follow-up slice after the current slice closes.
- Prefer hard failure over implicit fallback when the spec does not define a safe next step.
- Every workflow step should point to concrete source files or concrete endpoint contracts.
- Resolve entities before mutating them.
- Clarify ambiguous natural-language targets instead of guessing.
- A logic change is not complete when only code and tests are updated.
- For protected documentation-sync phrases, reuse canonical wording from YAML instead of writing near-equivalent variants by hand.

## Generic Contracts

- Entity resolution must be reusable across user, quest, circle, application, conversation, and current-location flows.
- Resolution results must use exact candidate rules inside an explicit scope such as owned quests, accepted contacts, owner circles, or quest applications.
- The same read-before-write pattern should be extended across the remaining endpoint surface instead of inventing feature-specific resolution logic.
- Read-only surfaces should still be modeled explicitly so future agents do not infer capability boundaries from UI behavior alone.
- Clarification is a first-class contract, not an ad hoc UI behavior.
- If translation, target resolution, destructive confirmation, or multi-actor context is missing, the safe outcome is to stop.
- Mutating intents must stay classified by machine-readable risk groups so validation can enforce read-only, destructive, multi-actor, and admin-only boundaries consistently.
- Record new deferred implementation or control-system work in the appropriate persistent backlog with a stable ID before closing the change that discovered it.
- Deferred implementation or control-system follow-ups must use stable backlog IDs, and matching inline `TODO(<ID>):` or `FIXME(<ID>):` notes must not outlive the open backlog item they reference.
- `scripts/todo-audit.rb` must keep persistent backlog IDs traceable to plans, feature manifests, docs, code surfaces, or inline `TODO(<ID>):` and `FIXME(<ID>):` references.
- After a completed plan or master plan, append durable lessons to `docs/product-memory.md` and run the standard post-plan control loop so stable memory, failure knowledge, and source-of-truth docs stay aligned.
- Do not paraphrase, shorten, reorder, or partially restate protected canonical wording.

Unified clarification contract:
- fail closed on ambiguity
- prefer exact one-candidate resolution
- surface unresolved fields explicitly
- do not convert approximate matches into silent mutations

Unified destructive confirmation contract:
- destructive actions require exact target resolution first
- destructive actions require explicit confirmation second
- destructive actions must not treat free-form delete phrasing as final authorization

Unified multi-actor contract:
- workflows that need another user's acceptance, application, confirmation, or message authority must require real actor context
- a planning layer may describe those missing actor-side prerequisites, but it must not invent them

## Current Scope

Initial agent-safe workflows:
- `resolve_user_candidate`
- `resolve_circle_candidate`
- `resolve_circle_recipient`
- `resolve_outgoing_circle_request`
- `resolve_news_item_candidate`
- `resolve_current_location_input`
- `create_user`
- `create_user_as_admin`
- `set_profile_location`
- `set_profile_current_location`
- `set_profile_details`
- `open_auth_identity`
- `open_current_user_account`
- `open_app_user_options`
- `open_user_record`
- `open_admin_user_detail`
- `update_user_as_admin`
- `delete_user_as_admin`
- `open_user_profile`
- `create_circle_connection`
- `accept_circle_connection`
- `cancel_circle_request`
- `create_circle`
- `update_circle`
- `delete_circle`
- `block_user`
- `unblock_user`
- `assign_circle_members`
- `open_circle_overview`
- `open_admin_circle_overview`
- `open_my_circle_relations`
- `open_circle_relation`
- `search_nearby_users`
- `find_owned_quest_candidates`
- `inspect_owned_quest_pending_applications`
- `select_oldest_pending_application`
- `find_my_pending_application_candidates`
- `find_admin_application_candidates`
- `open_application_detail`
- `open_quest_applications`
- `resolve_chat_conversation`
- `open_chat_conversation`
- `open_chat_conversation_messages`
- `send_chat_message`
- `heartbeat_chat_presence`
- `mark_chat_conversation_read`
- `mark_all_news_read`
- `mark_news_item_read`
- `open_location_debug_status`
- `open_dashboard`
- `open_dashboard_summary`
- `open_dashboard_voice_config`
- `process_vision_conversation_turn`
- `open_quest_feed`
- `open_quest_preset`
- `open_quest_record`
- `create_quest`
- `update_quest`
- `apply_to_quest`
- `update_my_application`
- `withdraw_my_application`
- `update_admin_application`
- `delete_admin_application`
- `approve_application`
- `decline_application`
- `delete_quest`
- `start_quest`
- `complete_quest`
- `confirm_quest_term_change`
- `reject_quest_term_change`
- `request_owner_term_change`
- `create_review`
- `authenticate_user`
- `create_user_with_quests`
- `create_circle_only_quest_for_selected_people`
- `prepare_circle_only_quest_flow_to_start`
- `voice_prepare_scheduled_circle_only_quest_for_selected_people`

Related admin tooling:
- an admin agent playground may help classify prompts and surface workflow warnings
- that playground should translate prompts into a stable planning language before deterministic workflow classification
- that playground stays planning-first, but the first guarded direct execution slice may create synthetic quest batches for one exact target user only after explicit admin confirmation
- when configured, the same admin endpoint may request a backend-managed OpenAI planning summary and still fall back to deterministic backend rules
- planner responses should keep deterministic matched signals and unresolved inputs separate from provider-authored summary text
- the dedicated `/vision/conversations/turns` backend is the persisted prompt-bearing path for stepwise adaptive orchestration, `GET /vision/conversations/recent` and `GET /vision/conversations/{conversationId}` provide resume state, dedicated `/vision/conversations/{conversationId}/reset` and `/vision/conversations/{conversationId}/cancel` endpoints own lifecycle control, and dashboard prompt decoding remains a lighter planning surface
- the first persisted `/vision` quest conversation now stepwise-collects reward, visibility, schedule, and location decisions, pauses for explicit candidate choice when a custom location lookup finds one or more more precise matches, accepts a broader deterministic HR/EN spoken-time vocabulary while still rejecting ambiguous spoken times without a day-period signal, and can route a review correction back to one named field instead of reopening a legacy form
- the same persisted `/vision` turn flow can now also resolve an `OPEN_CHAT` intent into the existing chat-open boundary when the semantic plan names a target contact, while still refusing ambiguous or non-accessible chat targets
- explicit turn actions now exist for prompt submission, review confirmation, and typed review-edit targeting, while lifecycle control moved onto dedicated conversation endpoints
- when the execution flag is enabled, that same persisted conversation path may cross from review into the first real `create_quest` executor only after explicit confirmation

Initial machine policies:
- `uniqueness_policy`
- `edge_case_policy`
- `automation_read_model_inventory`
- `intent_safety_catalog`
- `capability_registry`
- `intent_lineage_tracking`
- `prompt_drift_detection`
- `dry_run_execution_simulation`
- `backend_contract_snapshots`
- `service_workflow_inventory`
- `permission_matrix`
- `state_transition_audit`
- `request_validation_gate`
- `frontend_contract_generation`
- `automation_safe_ui_contract_layer`
- `frontend_safety_regressions`
- `frontend_feature_expectations`
- `documentation_coverage_manifest`
- `documentation_ownership_matrix`
- `documentation_templates_by_change_type`
- `closeout_doc_delta_summary`
- `doc_staleness_scoring_report`
- `self_test_matrix_by_risk`
- `validation_evidence_records`
- `cross_domain_concept_glossary`
- `frontend_contract_gate`
- `dead_path_tracker`
- `feature_completion_manifest_validation`

Additional control surfaces:
- `mutating_intent_contracts`
- `backend_audit_coverage`
- generated source-of-truth audit
- workflow-aware frontend helper generation
- canonical workflow scenarios

Documentation templates:
- `documentation_ownership.documentation_templates` in `docs/agent-operating-model.yaml` registers short templates for new workflows, endpoints, DTO contracts, modules, permission rules, and schema migrations.
- Template files live under `.agents/templates/docs/` and keep living-doc updates concise while preserving required behavior, permission, validation, documentation-delta, and validation-evidence sections.
- `docs/example-scenario-library.md` provides compact canonical implementation examples for adding endpoints, changing workflow transitions, adding DTO contracts, adding migrations, and updating docs.

Closeout doc delta:
- `policies.closeout_doc_delta` requires logic-drift closeout to state what behavior changed, which living docs or agent artifacts were updated, and what related surfaces were intentionally left unchanged.
- Temporary plans and feature manifests provide fields for that summary before final closeout.

Doc staleness scoring:
- `make audit-doc-staleness-scoring` generates `docs/generated/local-tooling/doc-staleness-scoring.json` and a summary report.
- The audit is report-first: it scores documentation sections against current code changes, endpoint inventory, DTO source freshness, and workflow/state-transition source freshness without failing closeout.

Self-test matrix:
- `policies.self_test_matrix` defines validation tiers for syntax-only, targeted unit, domain scenario, contract/type-check, generated-artifact validation, and full validation.
- Risk tiers and change profiles map to minimum validation tiers so low-risk changes can stay focused while high-risk and executor-critical work still requires broad validation.

Tiered workflow routing:
- Tier 1 tiny changes use compact context, no manifest by default, targeted validation, and `make audit-todo`.
- Tier 2 normal features require a short plan, use resolver-driven manifest decisions, and close through `make audit-plan-completion`.
- Tier 3 high-risk or multi-layer features require plan, manifest, validation evidence, `make validation-memory-closeout-card`, and full closeout.
- Tier 4 agent, tooling, or workflow changes keep the strictest path because they affect future Codex behavior.
- Final responses must state what changed, what was validated, and any remaining risks or not-run checks.

Context-first session workflow:
- start with `docs/generated/local-tooling/diff-summary.md` to understand the changed-file shape before broad repository exploration
- read `docs/generated/local-tooling/audit-summary-index.md` to choose the smallest relevant generated report
- generate or read a topic context pack with `make codex-context topic=<topic> intent='<intent>'` when the task has a clear feature, domain, or changed-file focus; it now chains the diff summary, audit summary index, the most relevant audit, targeted tests, and a concise evidence bundle
- treat `docs/generated/local-tooling/codex-context/latest.execution.json` as the canonical machine-readable manifest for the current context batch and `docs/codex-context-execution-manifest.schema.json` as its contract
- for product-direction sessions, prepend `docs/product-memory.md` and `docs/product-vision.md` before broader business or technical docs so stable lessons and vision anchor the rest of the search
- for `/vision` implementation sessions, include `docs/vision-architecture-patterns.md` before deciding backend orchestration, API, frontend canvas, prompt-handling, or executor patterns
- use `docs/generated/local-tooling/repo-map-summary.md` and `docs/generated/local-tooling/symbol-index-summary.md` only after the compact diff, audit index, and context pack do not identify the needed files
- fall back to broad `rg` exploration only after the compact context path is insufficient

Broad implementation checkpoints:
- use these checkpoints for broad, long-running, high-complexity, multi-layer, high-risk, or master-plan-driven changes
- if the work spans several master plans, update the relevant God Plan before changing child plan status
- plan checkpoint: create the temporary plan or master child plan, list scope, risk, affected surfaces, expected validation, and any up-front approval needs before substantial edits
- first backend slice checkpoint: when backend is in scope, complete the smallest meaningful backend behavior, contract, or generated-artifact edit and record a targeted backend check or not-applicable reason before broadening backend edits
- first frontend slice checkpoint: when frontend is in scope, complete the smallest meaningful frontend contract, state, route, or component edit and record type-check, build, contract validation, or a not-applicable reason before broadening frontend edits
- docs/artifacts sync checkpoint: update affected living docs and regenerate affected generated artifacts before treating behavior, contract, workflow, or automation-assumption changes as complete
- validation checkpoint: record exact targeted checks plus any required full checks or concrete skipped-check reasons before marking the slice complete
- validation checkpoint: when a manifest is in scope, prefer canonical validator-facing command strings in recorded evidence instead of only equivalent shell variants
- commit boundary checkpoint: remove the persistent backlog item only after implementation and validation are complete, align temporary and master plan status with reality, and skip commit or push unless the user explicitly requested it

Current mutation execution pattern:
- controllers stay transport-only and delegate to backend services
- auth flows resolve through `AuthService` plus `AuthMgr`
- quest mutations enter explicit use-case services such as `CreateQuestUseCase`, `StartQuestUseCase`, and `ConfirmQuestTermChangeUseCase`
- shared quest execution primitives handle target resolution, actor authority, state validation, persistence, and notification fan-out
- every mutating intent should now also declare explicit preconditions, state changes, side effects, notifications, and blocking conditions in the machine contract

Current control-test pattern:
- use-case contract tests verify target resolution, authority gate, state gate, persistence, notification, and fail-closed behavior across quest and applicant-side application workflows
- canonical scenario tests verify multi-step runtime flows such as quest lifecycle, term-change confirmation, destructive failure paths, and ambiguity failure
- workflow state-machine catalog tests cross-check documented transition intent ids against the agent operating model, requiring real transition intents to resolve to known mutating intents while allowing only explicit placeholders for derived or planned flows
- generated source-of-truth audit fails if tracked controllers, mappers, services, or tests exist in code without matching agent-documentation coverage
- documentation ownership maps product domains and change categories to required living docs, generated artifacts, and validation checks so agents do not infer propagation scope from memory
- `docs/cross-domain-glossary.md` keeps reused product terms stable across users, circles, visibility, consent, messaging, quests, applications, reviews, bookings, and synthetic data
- generated backend audit inventory classifies the full backend into `executor_critical`, `automation_relevant`, `internal_support`, and `out_of_scope`
- generated backend audit inventory also assigns every backend file to an explicit domain and owner so drift review can stay product-oriented instead of file-list oriented
- generated source-of-truth audit also emits ownership-aware candidate entries plus domain and owner summaries for tracked controllers, services, mappers, and tests
- Backend-provided read-only voice configuration keeps adaptive `/vision` speech defaults explicit instead of letting clients infer them ad hoc.
- current fail-hard enforcement stays intentionally limited to `executor_critical`, while broader backend coverage stays inventory-first and report-first
- the first stricter `automation_relevant` subset is the admin-agent planner DTO contract surface, which now requires source registration plus documentation coverage even though the wider tier is still report-first
- the second stricter `automation_relevant` subset is the chat DTO contract surface, so chat workspace, conversation, message, and socket contract files cannot drift away from source registration or documentation coverage
- the third stricter `automation_relevant` subset is the identity DTO contract surface, so auth, profile, and admin-user contract DTOs now sit behind the same registration and documentation gate
- the fourth stricter `automation_relevant` subset is the location DTO contract surface, so lookup, visibility, debug, and user-location contract DTOs now sit behind the same registration and documentation gate
- the fifth stricter `automation_relevant` subset is the social request/relation DTO contract surface, so circle request, block, relation-state, and connection-circle assignment DTO files now sit behind the same registration and documentation gate
- the sixth stricter `automation_relevant` subset is the social overview/member DTO contract surface, so circle overview, group summary, and member DTO files now sit behind the same registration and documentation gate
- the seventh stricter `automation_relevant` subset is the social search/contact DTO contract surface, so circle search, contact-list, candidate, and query DTO files now sit behind the same registration and documentation gate
- the eighth stricter `automation_relevant` subset is the social admin circle DTO contract surface, so admin circle overview, group, and relation-row DTO files now sit behind the same registration and documentation gate
- the ninth stricter `automation_relevant` subset is the workmarket dashboard DTO contract surface, so dashboard response, summary, navigation, grouping, planner, open-work, and notification DTO files now sit behind the same registration and documentation gate
- the tenth stricter `automation_relevant` subset is the workmarket quest-detail DTO contract surface, so quest detail response, section, action, presentation, and viewer-relation DTO files now sit behind the same registration and documentation gate
- the eleventh stricter `automation_relevant` subset is the workmarket application-detail DTO contract surface, so application detail response, section, context, action, and presentation DTO files now sit behind the same registration and documentation gate
- the twelfth stricter `automation_relevant` subset is the workmarket list/search/options DTO contract surface, so quest list, application list, search query, admin application query, and workmarket option DTO files now sit behind the same registration and documentation gate
- the thirteenth stricter `automation_relevant` subset is the workmarket news read-model DTO contract surface, so news item and destination DTO files now sit behind the same registration and documentation gate
- the fourteenth stricter `automation_relevant` subset is the common action/navigation DTO contract surface, so action result, label-value, and navigation target primitives now sit behind the same registration and documentation gate
- the broad `automation_relevant` service catch-all remains report-first; future service hardening should happen only through small rule-scoped slices that are stable, domain-owned, and low-noise

Initial dependency domains:
- identity
- workmarket
- location
- social
- common

## Chat Safety Notes

- Pending circle requests do not create chat eligibility.
- Chat workspace only includes current accepted circle contacts.
- Existing conversation history does not preserve chat access after the accepted relation is lost.
- Any future automation that opens or accesses chat must re-check current accepted relation state instead of trusting stale workspace or conversation rows.

## Maintenance Rule

Whenever a change affects:
- required inputs
- defaults
- endpoint usage
- enum values
- permission checks
- workflow order
- fallback behavior
- cross-domain dependencies
- uniqueness rules
- batch generation rules
- edge-case resolution rules
- admin-generation or sandbox-generation coverage for entities and workflows

update:
- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml`
- `docs/implementation-backlog.md` when new deferred implementation work is discovered or resolved
- `docs/agent-improvement-backlog.md` when deferred control-system work is discovered, reprioritized, or resolved
- `docs/documentation-sync-policy.md`
- When a backlog item is implemented, remove it from the open backlog and clear matching inline `TODO(<ID>):` or `FIXME(<ID>):` references in the same change.

and keep the validation test passing.

update all affected living docs in the same change

No logic-only change is complete until the affected docs, agent artifacts, and validation tests are updated together.

## Current Voice-Agent Boundary

Modeled high-impact execution actions for future executor wiring:
- approve application after deterministic target resolution
- decline application after deterministic target resolution
- delete quest only after exact owned-quest resolution plus explicit destructive confirmation
- complete quest
- confirm term change
- reject term change

Still intentionally not ready for autonomous execution today because no executor exists:
- any mutation path that only has planning output and no authenticated execution layer yet
- admin-only quest correction flows not represented in normal owner or worker automation

Composite execution boundary:
- A voice agent may prepare a circle-only quest flow up to `ASSIGNED` or `STARTED` only if each actor-side step has real authenticated context.
- It must not invent applications, connection acceptances, or term confirmations on behalf of another user.
- If the flow requires another actor and that actor context is missing, the safe outcome is to stop.

Pre-executor readiness rule:
- Before any future executor mutates quests, applications, or circle relations from natural-language input, it must first resolve the target entity through documented read workflows.
- Approval commands such as "approve the first applicant" must rely on deterministic backend read data, not incidental UI ordering.
- Delete commands such as "delete my quest that and that" must stop on ambiguous quest matches and require explicit destructive confirmation before mutation.
- The same fail-closed pattern now also applies to outgoing circle-request cancellation, pending-application self-service, owner-circle deletion, and chat read actions.
- Executor-critical read DTOs must stay explicitly inventoried in `docs/agent-operating-model.yaml`, including required resolution or selection fields plus at least one verification test path.
- Validation must fail if those inventoried DTOs drop required fields or if their declared verification tests disappear from the repo.
- Mutating intents must stay listed in the machine safety catalog with explicit write-risk grouping, exact-target resolution links where needed, and destructive or multi-actor requirements where applicable.
- Capability dependencies such as external translation, current location, admin authority, second-actor context, and destructive confirmation must stay registered explicitly instead of being inferred from planner text.
- Dry-run simulation must be able to traverse resolution, clarification, capability checks, safety policy checks, and endpoint selection without mutating production state.
- Intent lineage must keep source prompts, resolution workflows, target endpoints, safety policies, and expected read DTO usage connected so intent changes can be audited deterministically.
- Prompt drift detection must keep the multilingual golden matrix stable by validating translation outcome, classified intent, unresolved fields, blocking contracts, and drift fingerprints over time.
- Backend contract snapshots must fail fast when key planner, resolution, chat, or executor-read DTO shapes drift through renames, removals, or reordered semantics.
- Service workflow inventory must keep mutating backend service methods, their covered intents, docs, and tests explicitly registered instead of relying on tribal knowledge.
- Permission matrix rules must keep destructive, multi-actor, admin-only, and orchestration intents tied to explicit actor scopes instead of letting future executors infer authority from prompt wording.
- State-transition audit must keep quest and application lifecycle transitions explicit, including allowed actors, preconditions, forbidden states, and verification tests.
- Request validation completeness gates must keep bean-validation annotations, service-side validation markers, docs, and verification tests aligned for mutation request DTOs.
- Frontend planner and simulation contracts should prefer generated DTO aliases over hand-maintained duplicate types whenever the backend source already exists.
- The admin-agent UI must separate informational planner output from execution-blocking safety flags instead of interpreting ambiguity, destructive confirmation, translation reliability, or multi-actor risk ad hoc inside the page.
- Frontend safety regressions must keep explicit negative scenarios for ambiguity, destructive confirmation, multi-actor, translation-unreliable, and current-location-required states, plus at least one safe dry-run path.
- Feature-scoped frontend expectations should keep the admin-agent page, API client, contract gate, and safety-view-model files tied to the same planner contract surface.
- Documentation coverage scans must fail if automation-relevant controllers, mappers, or tracked agent service files appear in code without matching source-of-truth registration.
- Source-of-truth audit must also fail if those files are missing from documentation coverage or mutating service workflow inventory.
- Full backend inventory should still classify the rest of the backend even when those files are not yet part of strict fail-hard registration.
- The multilingual golden prompt matrix must stay executable so Croatian, English, German, Mandarin, and mixed-slang prompts keep the same planning contracts over time.
- Frontend contract gates must fail fast if the admin-agent planner response shape drifts away from the frontend TypeScript contract surface.
- Frontend workflow-aware helpers validate generated intent ids, endpoint ids, and safety-flag ids before the admin-agent UI treats simulation output as contract-shaped.
- Feature completion manifests may be kept under `.agents/feature-manifests/` and validated against `docs/feature-completion-manifest.schema.json` when a change uses the plan-driven workflow.
- Feature manifests should classify changes by mode, impact, and change profiles so required docs, tests, generators, and close-out commands are enforced mechanically.
- Validation evidence records may be kept under `.agents/validation-evidence/` and validated against `docs/validation-evidence.schema.json` when a change needs a compact command, generated-artifact, and skipped-check evidence trail.
- Backend audit tightening should happen in phases: first classify everything, then harden `automation_relevant` through small rule-scoped DTO, read-model, or service slices, and only later decide whether additional support tiers deserve stronger gates.
- Broad service coverage remains report-first until specific service slices have stable ownership, source registration, documentation coverage, and low-noise validation evidence.

## Sandbox Boundary

- Internal sandbox flow is a separate capability from production-like voice flow.
- Sandbox flow must stay separate from real-life product flow.
- sandbox behavior must stay separate from production mutation semantics
- Sandbox flow may generate synthetic multi-actor data for testing and demo preparation.
- Sandbox flow must be limited to admin or developer-controlled environments.
- Sandbox flow must never reuse production intents in a way that hides synthetic behavior from the caller.
- Synthetic flows should eventually write identifiable markers on created data once backend support exists.
- When product logic expands, affected admin-generation and sandbox-generation flows must be reviewed and extended in the same change so generated data stays aligned with current backend rules.
- review and extend affected admin or sandbox generation flows
- synthetic admin-generation flows must be kept current with newly introduced feature logic, validations, and edge cases

## Modeling Rule

- Do not encode one-off example counts such as `10` as canonical workflow behavior.
- Model reusable capabilities instead:
  - single-item intents
  - batch intents with caller-supplied counts
  - uniqueness rules
  - partial-failure rules
  - stop conditions

## New Workmarket Execution Notes

- `create_quest` accepts `awardAmount == 0` for a free quest, but still rejects negative amounts.
- `apply_to_quest` requires a proposed price only for paid quests.
- `apply_to_quest` must omit proposed price for free quests instead of sending `0`.
- `start_quest` requires `ASSIGNED` status and execution authority.
- `complete_quest` requires `IN_PROGRESS` status and execution authority.
- `confirm_quest_term_change` requires `WAITING_CONFIRMATION` plus approved-worker or admin authority.
- `reject_quest_term_change` requires `WAITING_CONFIRMATION` plus approved-worker or admin authority.
- `decline_application` requires `OPEN` quest state, owner-or-admin authority, and a `PENDING` application.
- Term-decision flows restore the previous quest status; they are not standalone terminal states.

## End-To-End Voice Scenario

Reference scenario:
- Voice instruction: "Create a quest for tomorrow at 15:00 only for my selected circle friends and prepare the whole flow to start."

Required interpretation rules:
- `tomorrow at 15:00` must be resolved in the caller's timezone before backend mutation.
- Relative time must become an absolute scheduled timestamp before quest creation.
- `selected circle friends` must resolve to concrete user ids, not just free text names.

Required flow:
1. Resolve owner identity, caller timezone, selected people, and quest payload.
2. Verify each selected person is already an accepted connection or stop on the unresolved subset.
3. Create or reuse the owner circle that will hold the selected people.
4. Sync accepted selected people into that owner circle.
5. Create the circle-only quest with the resolved absolute schedule.
6. If the flow should continue toward execution, require real applicant-side authenticated application steps.
7. Approve only the intended pending applications from owner or admin context.
8. Start only after the quest is `ASSIGNED` and an execution-authorized actor is present.

Stop conditions:
- selected people cannot be resolved to accepted connections
- relative time cannot be resolved safely
- applicant-side authenticated context is missing
- quest never reaches `ASSIGNED`

Extended branches:
- unexpected applicants can be explicitly declined instead of auto-approved
- owner-requested term changes after assignment or execution enter `WAITING_CONFIRMATION`
- completion can hand off into valid employer-worker review creation only after `COMPLETED`
