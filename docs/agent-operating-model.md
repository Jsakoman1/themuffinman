# Agent Operating Model

This file is the human-review layer for agent-safe product operations.

It exists to reduce the risk of future automation, batch agents, or voice agents performing invalid actions from partial understanding.

The machine-readable source of truth is:
- `docs/agent-operating-model.yaml`
- validated by `docs/agent-operating-model.schema.json`
- enforced by `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`

## Safety Goal

- Do not let an automation layer invent missing business logic.
- Do not let a voice agent skip required prerequisites.
- Do not let stale documentation silently drift away from backend rules.

## Operating Rules

- Treat backend code as the final execution authority.
- Treat `agent-operating-model.yaml` as the machine-operational contract for high-impact workflows.
- Keep workflows procedural, explicit, and dependency-ordered.
- Prefer hard failure over implicit fallback when the spec does not define a safe next step.
- Every workflow step should point to concrete source files or concrete endpoint contracts.
- A logic change is not complete when only code and tests are updated.

## Current Scope

Initial agent-safe workflows:
- `create_user`
- `set_profile_location`
- `create_circle_connection`
- `accept_circle_connection`
- `create_circle`
- `assign_circle_members`
- `create_quest`
- `apply_to_quest`
- `approve_application`
- `decline_application`
- `start_quest`
- `complete_quest`
- `confirm_quest_term_change`
- `reject_quest_term_change`
- `request_owner_term_change`
- `create_review`
- `create_user_with_quests`
- `create_circle_only_quest_for_selected_people`
- `prepare_circle_only_quest_flow_to_start`
- `voice_prepare_scheduled_circle_only_quest_for_selected_people`

Initial machine policies:
- `uniqueness_policy`
- `edge_case_policy`

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

update:
- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml`
- `docs/documentation-sync-policy.md`

and keep the validation test passing.

update all affected living docs in the same change

No logic-only change is complete until the affected docs, agent artifacts, and validation tests are updated together.

## Current Voice-Agent Boundary

Covered execution actions:
- complete quest
- confirm term change
- reject term change

Still intentionally not modeled for autonomous execution:
- delete quest
- admin-only quest correction flows not represented in normal owner or worker automation

Composite execution boundary:
- A voice agent may prepare a circle-only quest flow up to `ASSIGNED` or `STARTED` only if each actor-side step has real authenticated context.
- It must not invent applications, connection acceptances, or term confirmations on behalf of another user.
- If the flow requires another actor and that actor context is missing, the safe outcome is to stop.

## Sandbox Boundary

- Internal sandbox flow is a separate capability from production-like voice flow.
- Sandbox flow must stay separate from real-life product flow.
- sandbox behavior must stay separate from production mutation semantics
- Sandbox flow may generate synthetic multi-actor data for testing and demo preparation.
- Sandbox flow must be limited to admin or developer-controlled environments.
- Sandbox flow must never reuse production intents in a way that hides synthetic behavior from the caller.
- Synthetic flows should eventually write identifiable markers on created data once backend support exists.

## Modeling Rule

- Do not encode one-off example counts such as `10` as canonical workflow behavior.
- Model reusable capabilities instead:
  - single-item intents
  - batch intents with caller-supplied counts
  - uniqueness rules
  - partial-failure rules
  - stop conditions

## New Workmarket Execution Notes

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
