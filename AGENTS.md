# Repository Guidelines

## Product

`TheMuffinMan` is a multi-module product. `SideQuest` is one module inside it.

Planned modules:

1. work marketplace
2. business mini websites and appointment booking
3. thing sharing / lending
4. voluntary car sharing for selected circles
5. shared chat across modules

## Structure

- Product modules live in `apps/`.
- Shared backend capabilities live in `services/`.
- `apps/themuffinman` contains the current Spring Boot app and frontend.
- Backend code lives in `apps/themuffinman/src/main/java/com/themuffinman/app`.
- Keep Spring code organized by domain and layer: `controller`, `service`, `repository`, `model`, `dto`, `mapper`,
  `config`.
- Flyway migrations live in `apps/themuffinman/src/main/resources/db/migration`.
- Frontend lives in `apps/themuffinman/frontend/`.

## Commands

- Backend: `./mvnw spring-boot:run`, `./mvnw test`, `./mvnw package`
- Frontend: `npm run dev`, `npm run type-check`, `npm run build`
- Full local dev: `make dev`

## Architecture

- Prefer backend-centric domain design.
- Put business rules, permissions, validations, workflow rules, and state transitions in backend services.
- Keep frontend code thin so the same logic can later support mobile clients.
- Prefer backend-prepared DTOs and sections over frontend-derived product logic.
- When logic moves to backend, remove duplicated frontend logic unless there is a clear offline need.
- Reuse core concepts carefully across modules: users, circles, scheduling, bookings, visibility, consent, messaging.

## Config

- Put operational backend config in typed central config objects under `config/`, using `@ConfigurationProperties`.
- This applies to retention, cleanup jobs, cache settings, TTLs, rate limits, polling intervals, timeouts, and similar
  runtime behavior.
- Avoid scattering those settings across many `@Value` fields when they belong to one operational area.
- Keep environment variable mapping in `application.properties`.
- Keep shared local defaults in `.env.backend.dev` and developer-specific overrides in `.env.backend.dev.local`.
- Add new operational config to those central classes first, then wire it into services or jobs.

## Working Style

- Treat the user as a junior developer.
- Prefer simple, readable, incremental implementations over speculative abstractions.
- Optimize for standardization, simplicity, and maintainability over visual flourish.
- Default to minimal UI logic.
- Standardize authenticated pages around one shared app shell.
- Prefer shared components, shared tokens, and shared layout patterns over one-off implementations.
- Standardize dialogs and detail surfaces.
- For dialogs with enough content, default to two columns: main content left, utility/actions right.
- Prefer inline editing on an already open detail surface over opening a second nested edit popup.
- Keep controllers thin.
- Add a new Flyway migration for schema changes; do not edit old migrations.
- Add or extend JUnit tests whenever backend behavior changes.
- Keep `./mvnw test` passing for backend changes.
- Run `npm run type-check` and `npm run build` for frontend changes.

## Collaboration

- Do not commit or push changes unless explicitly asked.
- Keep code, comments, docs, API text, and user-facing copy in English.
- The user allows routine file creation and file edits inside the workspace, including temporary planning files under
  `.agents/`, without per-file confirmation.
- Ask only when a command needs sandbox escalation, external side effects, or some other higher-risk approval outside
  normal workspace editing.

## Living Documentation

- Maintain two living documentation files in `docs/`:
    - `docs/business-logic.md` for product behavior, rules, and FAQ-style explanations
    - `docs/domain-technical.md` for entities, relations, validations, permissions, workflows, and invariants
- Maintain the agent-operation documentation set in `docs/`:
    - `docs/agent-operating-model.md` for human review of agent-safe workflows
    - `docs/agent-operating-model.yaml` for machine-readable workflow rules, dependencies, defaults, enums, and endpoint
      mappings
    - `docs/agent-operating-model.schema.json` for validating the YAML structure
    - `docs/agent-improvement-backlog.md` for persistent forward-looking agent/control-system tightening work that
      should survive across Codex sessions
    - `docs/feature-completion-manifest.schema.json` for validating machine-readable per-feature completion manifests
      under `.agents/feature-manifests/`
    - `docs/documentation-sync-policy.md` for change-propagation requirements across code, docs, and agent-safety
      artifacts
    - `docs/change-completion-checklist.md` for the fast operational completion checklist layered on top of the policy
      and operating-model rules
- For multi-file, multi-layer, or high-risk logical changes, create a temporary implementation plan in `.agents/` before
  substantial edits.
- Prefer the filename pattern `.agents/<short-feature-topic>-plan.md`.
- If a requested change is too large for one safe pass, split it into sequential implementation phases instead of
  forcing one oversized batch.
- Reusable templates for temporary plans and feature completion manifests live under `.agents/templates/`.
- When business rules, domain models, permissions, validations, workflows, endpoint contracts, or automation assumptions
  change, update all affected living docs in the same change unless the edit is purely cosmetic.
- When a new feature or logical expansion changes what entities, workflows, validations, or states exist, also review
  and extend affected admin or sandbox generation flows in the agent-operating docs instead of leaving generation
  capabilities stale.
- Treat admin-generation or sandbox-generation coverage for entities and workflows as part of the same maintenance
  surface as backend logic and agent-safety rules.
- synthetic admin-generation flows must be kept current with newly introduced feature logic, validations, and edge cases
- A logic change is not complete when only code and tests are updated.
- Treat logical backend and frontend behavior changes as documentation changes by default until proven cosmetic.
- Treat sandbox or synthetic-data behavior as high-risk logic that must stay explicitly separated from production
  behavior in docs and agent artifacts.
- Prefer concise, factual entries that reflect the current codebase rather than aspirational design notes.
- If a change affects user-facing meaning, update the business document first, then mirror the technical source of
  truth.
- Treat `docs/agent-operating-model.yaml` as an operational safety artifact for future automation and voice agents. Keep
  it deterministic, explicit, and free of guessed behavior.
- Keep `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java` passing
  whenever the agent-operating docs or referenced workflow contracts change.
- Keep `docs/documentation-sync-policy.md` aligned with the actual propagation process and protected rules.
- Keep `docs/change-completion-checklist.md` aligned with the actual completion workflow and do not let it drift away
  from the stronger source rules.
- Keep machine-readable feature completion manifests aligned with the actual implementation state when that workflow is
  used for a change.
- No logic-only change is complete until the affected docs, agent artifacts, and validation tests are updated together.
- When adding or changing protected documentation phrases in `agent-operating-model.yaml`, copy the canonical wording
  directly into the target docs instead of paraphrasing.
- Treat case, punctuation, and markdown formatting as non-semantic, but treat wording changes as semantic unless the
  YAML rule is updated too.
- Before finishing documentation or agent-safety edits, run the validation test and fix wording drift at the source
  instead of layering more near-duplicate phrases.