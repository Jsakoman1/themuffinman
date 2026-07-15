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
- When fixing a `LazyInitializationException` or DTO-mapping bug in one backend read path, audit sibling read surfaces that use the same entity mapper or repository fetch pattern before closing the change.
- Prefer one service-level DTO assembly path per viewer role or use case instead of duplicating mapper-plus-action wiring across multiple read services.
- If a repeated inspection task can be made reliable with a small local script, keep it focused and document its
  command in `docs/implementation-control.md`.

## Collaboration

- Do not commit or push changes unless explicitly asked.
- Keep code, comments, docs, API text, and user-facing copy in English.
- The user allows routine file creation and file edits inside the workspace without per-file confirmation.
- Ask only when a command needs sandbox escalation, external side effects, or some other higher-risk approval outside
  normal workspace editing.
- If the user asks for a push/merge workflow in the style of "napravi push i merge na main" or "create branch push and merge on main", treat it as a known shortcut: gather the current relevant changes, commit if needed, push to `origin`, and merge to `main` when that is the active release path, without extra questions unless there is a real blocker, conflict, or approval need.
- When the user explicitly assigns a broad autonomous work session, says they will be away, or asks Codex to work through
  a large TODO/backlog batch, front-load any required approval requests that can be identified safely, then continue
  through all safe implementation, documentation, generated-artifact, and validation phases without waiting for
  additional user signals.
- During autonomous implementation, when Codex notices a likely improvement, repeated failure pattern, or safe follow-up slice that should not interrupt the current batch, record it in the appropriate backlog surface during the current slice and continue implementing it automatically after the current slice closes unless the user narrows scope, approval is required, or a real blocker appears.
- When the user asks for a broad safe batch, such as many improvements or an entire workstream, assemble the full safe slice list up front and execute it in order without asking after each slice, unless a real blocker, scope change, or required approval appears.
- Keep implementation batches direct and linear: make the change, validate it, update docs, and stop only for a real blocker or required approval.
- Even during autonomous sessions, do not commit or push unless the user explicitly includes commit or push in the task.

## Living Documentation

- Maintain two living documentation files in `docs/`:
    - `docs/business-logic.md` for product behavior, rules, and FAQ-style explanations
    - `docs/domain-technical.md` for entities, relations, validations, permissions, workflows, and invariants
- For active operational state, validation state, and automation-facing rules, prefer machine-readable
  sources first:
    - `docs/work/*.yaml`
    - `docs/implementation-control.md`
    - `docs/agent-operating-model.yaml`
    - `docs/regression-scenario-catalog.yaml`
- Use human-readable docs as canonical only when they define product meaning, domain meaning, or the curated narrative
  around that state; do not let markdown summaries become the only durable record of active control facts.
- Maintain the agent-operation documentation set in `docs/`:
    - `docs/implementation-control.md` for the canonical implementation, evidence, and closeout workflow
    - `docs/codex-fast-path.md` for the compact startup guide
    - `docs/agent-operating-model.yaml` for machine-readable agent safety rules
    - `docs/implementation-backlog.md` for persistent product-delivery work
  and operating-model rules
- For product-direction, UX, interaction-design, or Social Useful Network vision work, treat `docs/product-memory.md`
  and `docs/product-vision.md` as the first canonical reference points before expanding into broader business or
  technical docs.
- For `/vision` implementation work, also read `docs/vision-architecture-patterns.md` before changing backend
  orchestration, API contracts, frontend canvas rendering, prompt handling, or execution behavior.
- Read `AGENTS.md` first for every task.
- For feature implementation, use `docs/codex-fast-path.md` as the default compact workflow entrypoint.
- `docs/codex-fast-path.md` is the compact execution entrypoint for most feature work.
- Use `docs/implementation-control.md` for every non-trivial implementation change.
- For broad work, use one master YAML work plan that references child YAML work plans.
- When `AGENTS.md` records a standing autonomous continuation preference, do not stop only to ask which safe offered follow-up slice should run next; continue with the best sequenced slice unless scope changes, approval is required, or a real blocker appears.
- In a safe active batch, do not ask the user whether to continue between slices, phases, or follow-up passes; continue automatically through the full planned sequence and only stop for a real blocker, scope change, or required approval.
- During a safe batch, do not stop after one or two phases just to ask whether to continue; carry the batch through all planned phases in sequence, record any safe follow-up items in the appropriate backlog during the same batch, and close the batch only after the final closeout pass.
- During broad implementation work, review the product, control-system, and implementation-workflow layers before substantial edits, and capture the review in a temporary analysis artifact when the batch is broad or high-risk.
- Never mark work verified unless `make work-verify plan=<path>` has executed every required validation and recorded passing evidence.
- Use the work-plan verifier as the default control path. Product-specific audits are optional diagnostics, not status.
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
  whenever the agent-operating model changes.
- Record new deferred implementation or control-system work in the appropriate persistent backlog with a stable ID
  before closing the change that discovered it.
- If inline `TODO/FIXME` notes are needed, use `TODO(<ID>):` or `FIXME(<ID>):` and keep the same ID open in one
  persistent backlog file.
- When a backlog item is implemented, remove it from the open backlog and clear matching inline `TODO(<ID>):` or
  `FIXME(<ID>):` references in the same change.
- No logic-only change is complete until the affected docs, agent artifacts, and validation tests are updated together.
