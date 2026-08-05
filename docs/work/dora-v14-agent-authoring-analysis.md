# Dora v1.4 agent authoring analysis

## Objective

Make the existing Dora knowledge and control primitives directly operable by Codex
without turning Dora into a product-rule inference engine or an unreviewed source
generator.

## Existing reusable baseline

- `IdeaInterview` validates a complete user-confirmed interview, but has no guided
  command session for an agent to use while talking to a beginner.
- `CreateApp` creates a project from an interview and reviewed Dora source, but does
  not expose a first vertical-slice proposal command.
- `VerticalSliceGenerator` and `VerticalSliceReadiness` generate and gate a proposal,
  but are Ruby APIs rather than a documented public CLI surface.
- `CapabilityPackage` and `DomainCompiler` already link declared domain rules,
  permissions, workflows, tests, and runtime evidence. v1.4 must reuse them rather
  than create a second domain model.
- `DecisionLog` is already a structured ADR-like record. It needs a project-safe
  creation and validation route, not a new decision format.
- The runtime-harness plugin declares runtime trace contracts, but Dora does not yet
  offer an opt-in neutral Playwright starter for a new web application.

## Chosen scope

1. Add a guided, provenance-preserving interview session surface for Codex.
2. Add a public proposal/readiness command that consumes confirmed context only.
3. Add a project-safe ADR command backed by the existing decision log.
4. Add an opt-in neutral Playwright runtime-proof profile, including an independent
   consumer proof.
5. Update agent-first guidance and independently prove the complete authoring route.

## Additions after plan review

- A starter compatibility contract and `app-readiness` command must detect missing
  Java, Maven, Node, npm, Docker, Compose, and declared technical requirements before
  a consumer starts work.
- A provenance trace must connect confirmed interview answers, a proposal, readiness
  gaps, and cited decisions without restating inferred product facts.
- Runtime profiles need a dry-run, non-overwriting profile-apply operation before
  they can be copied into an existing consumer project.
- Static profile proof is insufficient for a browser-proof profile. The plan includes
  one opt-in, temporary Playwright execution task with explicit browser-install
  approval and generated runtime evidence.
- `authoring-next` must select one declared next action across interview, readiness,
  and decision state so Codex does not guess which command to use.

## Deliberately deferred

- Authentication, roles, ownership, and audit-trail generation require confirmed
  product security rules; they belong in a separate auth/permission profile program.
- Database entity, DTO, controller, and UI source generation remain out of scope.
  v1.4 may prepare reviewable work but must not invent fields, roles, states, or APIs.
- Backup/restore, production readiness, seed data, UI blueprints, offline/sync, and
  deployment adapters are separate opt-in profiles after their explicit decisions and
  safety boundaries are defined.

## Success boundary

v1.4 can help Codex turn a confirmed conversation into a project-owned, reviewable
first capability path and a browser-proof starter. It does not create a product,
authenticate users, deploy software, or prove production readiness.
