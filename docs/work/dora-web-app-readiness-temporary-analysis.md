# Temporary analysis: Dora web-application readiness

Status: temporary analysis only. Superseded as planning input by `dora-v13-web-app-foundation-master.yaml`; it is not implementation authorization or a release claim.

## Question

What is the smallest rational Dora expansion that lets Codex take a reviewed product idea and build a new Spring Boot, Vue, and PostgreSQL web application through a controlled path?

The example product is a household emergency-stock application. Its domain must remain outside Dora. Dora should supply reusable delivery mechanics only.

## Current verified baseline

Dora v1.2.0 already provides:

- user-provenance idea interviews and explicit open decisions;
- `create-app` from a declared interview and reviewed local Dora source;
- generated product brief, domain library, agent profile, project memory, first work declaration, and first capability package;
- neutral `blank`, `spring-vue`, and buildable `spring-vue-buildable` starters;
- capability-package validation, domain contradiction reporting, implementation-readiness reporting, and explicit evidence gaps;
- bounded Codex commands for integration, explanation, next action, diagnosis, and evidence explanation;
- strict serial work plans, atomic work verification, capability/revision evidence traces, and independent-consumer proofs;
- declared plugin roots and an explicit absence-of-sandbox boundary.

The current buildable Spring/Vue starter intentionally excludes database schema, authentication, API resources, and product behavior. The agent-first guide and README still foreground older `new --answers` material, so v1.2's interview-based `create-app` route needs documentation consolidation.

## Desired end-to-end route

```text
plain-language product idea
  -> Codex interview
  -> confirmed product/domain/permission/workflow decisions
  -> Dora project creation with one selected technical starter
  -> first vertical capability package
  -> generated implementation skeleton and atomic tasks
  -> backend, database, API, frontend, tests, runtime evidence, documentation
  -> reviewed release decision
```

Codex remains the implementer. Dora remains the deterministic control, context, validation, and evidence layer. Dora must not claim that an unreviewed generated application is production-ready.

## Recommended first expansion

### 1. Codex interview workflow

Add a compact Codex-facing guide/command contract that turns a natural-language conversation into the existing `dora_idea_interview` artifact. It should ask only for:

- user groups and ownership;
- first problem and first capability;
- core domain concepts;
- permissions and prohibited outcomes;
- workflow states and transitions;
- data sensitivity, retention, offline requirement, and external integrations;
- unresolved decisions requiring user confirmation.

It must preserve answer provenance and never fabricate an answer. This is the largest usability gain for a beginner because the existing interview contract already exists.

### 2. First-party Spring Boot + Vue + PostgreSQL starter

Add one opt-in neutral starter, for example `spring-vue-postgres-buildable`, containing only technical setup:

- Spring Boot backend with typed configuration and health endpoint;
- Vue frontend with a shared application shell;
- PostgreSQL service through local Docker Compose;
- Flyway configured for new migrations only;
- local setup, test, build, and database-reset-safe commands;
- environment example files and no credentials;
- no entity, business rule, role, API resource, feature screen, or seed business data.

The starter must remain smaller than a full framework template. It should prove that the declared commands build and test from a fresh independent consumer.

### 3. Capability-to-vertical-slice generator

Given a confirmed capability package, create a proposal, not silently applied code, for:

- entity and ownership boundary;
- Flyway migration outline;
- DTO, service, controller, repository, and mapper paths;
- API operation contract;
- Vue module, route, API client, list/detail surface paths;
- backend test, frontend test, and runtime scenario;
- living documentation updates;
- a serial atomic-task inventory.

The proposal must stop when the package lacks a decision. Applying generated source should be a separately approved, bounded Codex task, not a side effect of project creation.

### 4. Generic security and data-safety baseline

Provide optional, declared profiles instead of hidden defaults:

- authentication and role/ownership model;
- password/secret configuration boundary;
- audit-log requirement;
- data export and backup/restore test declaration;
- retention decision record;
- local/demo data explicitly separated from production data.

For the emergency-stock example, these mechanisms are useful, but terms such as stock item, expiry date, or storage location must not enter Dora itself.

### 5. Runtime evidence starter

Provide a neutral browser/runtime harness integration that can create a first scenario from a capability package. It should record only declared evidence paths and never imply acceptance from a passing static test.

## Explicitly deferred

- automatic production deployment;
- automatic database deletion/reset;
- generated product-specific UI design;
- offline-first synchronization implementation;
- automatic security certification;
- autonomous creation of business rules from ambiguous wording.

These are either product decisions, high-risk operations, or too broad for a first reusable slice.

## Suggested sequencing

1. Consolidate v1.2 README and agent-first documentation around interview-based `create-app`.
2. Add the Postgres buildable starter with an independent-consumer proof.
3. Add the capability-to-vertical-slice proposal contract and readiness gate.
4. Add generic auth/data-safety decision profiles.
5. Add runtime-harness bootstrap and a release-readiness extension.

This sequence produces immediate value after step 2 while keeping code generation and production concerns explicitly controlled.

## Decision required before a master plan

Confirm whether the next Dora version should target:

1. a narrow first slice: interview UX + Spring/Vue/Postgres starter + one vertical capability proposal; or
2. a broader "application factory" scope that also includes auth/data-safety and runtime-harness setup.

The recommended choice is the narrow first slice. It is enough to begin DoomsDayStorage responsibly and will reveal which generic abstractions are actually reusable.
