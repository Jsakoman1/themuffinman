# Implementation Backlog

This file is the persistent source of truth for open implementation and product-delivery work that should survive across Codex sessions.

It complements `docs/agent-improvement-backlog.md`, which stays focused on agent-safety, audit tightening, and control-system work.

Keep only currently open items here. When an item is implemented, remove it from the open backlog in the same change and clear matching inline `TODO(<ID>):` or `FIXME(<ID>):` references.

## Open Items

- [ ] IMPL-QUEST-APPLICATION-DTO-AUDIT: Audit every backend read surface that returns `QuestApplicationResponseDTO` or derivative application collections, remove duplicated applicant-action DTO assembly, and harden remaining lazy-loading paths before more self-service actions are added.
  Scope to cover in the next pass: `QuestApplicationService.getApplicationsForQuest`, `getApplicationsViewForQuest`, `getPublicApprovedApplicationsViewForQuest`, `getAllApplicationsForAdmin`, `searchApplicationsForAdmin`, and the default transaction strategy for non-mutating application read methods.
  Working note: `.agents/quest-application-dto-audit-todo.md`
- [ ] IMPL-BUSINESS-HUB-MODULE: Replace the Business Hub placeholder with the first implemented module slice for business profiles, mini websites, calendars, or appointment booking.
- [ ] IMPL-THING-SHARING-MODULE: Replace the Thing Sharing placeholder with the first implemented lending and borrowing workflow slice.
- [ ] IMPL-CAR-SHARING-MODULE: Replace the Car Sharing placeholder with the first implemented voluntary circle-based ride coordination slice.
- [ ] IMPL-SHARED-CHAT-MODULE-SURFACE: Promote shared chat from cross-module capability into a fully implemented standalone module surface instead of a planned placeholder entry.
- [ ] IMPL-WORKMARKET-USE-CASE-BOUNDARIES: Continue extracting workmarket mutations into narrow use-case services with one public orchestration path per workflow.
  Goal: make future Codex changes cheaper by reducing the number of service methods that must be read before safely changing quest or application behavior.
- [ ] IMPL-READ-MODEL-ASSEMBLY-STANDARDIZATION: Standardize one service-level DTO/read-model assembly path per viewer role for high-traffic surfaces such as dashboard, quest detail, application detail, and admin views.
  Goal: reduce duplicated mapper/action wiring and lower the risk of LazyInitializationException fixes missing sibling read paths.
- [ ] IMPL-FRONTEND-THIN-STATE-SURFACES: Refactor complex Vue view/composable combinations toward backend-prepared sections and smaller frontend state adapters.
  Goal: keep business rules out of frontend views and make Codex inspect fewer frontend files for future UI changes.
- [ ] IMPL-GENERATED-ARTIFACT-SCOPE-CLEANUP: Decide which generated audit outputs belong in version control and move disposable local run outputs behind ignored paths or opt-in commit guidance.
  Goal: reduce large diffs, cheaper code review, and fewer tokens spent reading generated noise.
- [ ] IMPL-DOMAIN-MODULE-README-CAPSULES: Add short per-domain README capsules under backend and frontend module roots that summarize responsibilities, main entrypoints, tests, docs, and forbidden shortcuts.
  Goal: give Codex a small domain-specific first read before touching identity, workmarket, social, chat, location, or future modules.
- [ ] IMPL-TEST-FIXTURE-STANDARDIZATION: Standardize backend test fixture builders for users, circles, quests, applications, location settings, and chat conversations.
  Goal: make new tests cheaper to write and reduce repeated setup code across service/scenario tests.
- [ ] IMPL-DOMAIN-APPLICATION-SERVICE-LAYERING: Define and enforce a clearer split between application/use-case services, domain policy services, query/read services, and low-level primitives.
  Goal: make it obvious where new behavior belongs and reduce the need for Codex to inspect many neighboring services before editing.
- [ ] IMPL-PERMISSION-POLICY-CENTRALIZATION: Consolidate repeated visibility, ownership, role, circle-membership, and quest-application permission checks into explicit policy services with named decisions.
  Goal: preserve logic across modules and prevent future feature work from duplicating subtly different authorization rules.
- [ ] IMPL-WORKFLOW-STATE-MACHINE-CATALOG: Standardize workflow transitions for quests, applications, circle requests, chat conversations, and future bookings as named state-machine contracts.
  Goal: make state transitions auditable, easier to test, and easier for Codex to extend without missing edge cases.
- [ ] IMPL-READ-QUERY-FETCH-PROFILES: Introduce named repository fetch profiles or query methods for each read surface instead of ad hoc entity loading inside services.
  Goal: reduce LazyInitializationException risk and make DTO read paths predictable.
- [ ] IMPL-BACKEND-PREPARED-UI-SECTIONS: Expand backend-prepared view DTO sections for complex screens so frontend components render state instead of deriving product logic.
  Goal: keep frontend thin and lower future token cost for UI changes.
- [ ] IMPL-CROSS-MODULE-CORE-CONCEPTS-PACKAGE: Create explicit shared backend abstractions for reusable concepts such as actor identity, circle visibility, scheduling windows, consent, and module ownership before adding more planned modules.
  Goal: avoid copy-paste domain concepts when business hub, thing sharing, car sharing, and booking features are introduced.
- [ ] IMPL-FRONTEND-APP-SHELL-CONSOLIDATION: Finish standardizing authenticated pages around one app shell, shared detail surfaces, shared dialogs, and shared action panels.
  Goal: reduce one-off UI patterns and make future frontend implementation mostly data wiring.
- [ ] IMPL-API-CONTRACT-GENERATION-PIPELINE: Move API contract generation into a first-class build/documentation pipeline with explicit ownership and freshness checks.
  Goal: prevent backend/frontend contract drift and make generated contracts trustworthy for Codex.
- [ ] IMPL-DOMAIN-EVENT-BOUNDARIES: Add lightweight domain events for cross-module side effects such as quest news, chat notifications, reviews, booking updates, and future sharing workflows.
  Goal: keep service methods smaller and reduce hidden coupling as modules grow.
- [ ] IMPL-ADMIN-SANDBOX-SEPARATION: Make production admin flows, sandbox generation flows, and synthetic data helpers structurally separate packages with clear naming and tests.
  Goal: preserve safety boundaries and make automation-related changes easier to audit.

## Intake Rules

- Record new deferred implementation or control-system work in the appropriate persistent backlog with a stable ID before closing the change that discovered it.
- Record new persistent implementation follow-ups here with a stable uppercase ID before closing the change that discovered them.
- If a deferred follow-up also needs an inline code note, use `TODO(<ID>):` or `FIXME(<ID>):` and make sure the same ID exists in an open backlog entry.
- When a backlog item is implemented, remove it from the open backlog and clear matching inline `TODO(<ID>):` or `FIXME(<ID>):` references in the same change.
- Do not keep resolved items here as historical checkboxes. Remove them once the implementing change is complete.
