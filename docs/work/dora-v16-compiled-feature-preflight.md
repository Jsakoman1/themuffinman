# Dora v1.6 compiled feature preflight

## Baseline and residual scope

The baseline is MuffinMan commit `c7dcfaa` with Dora v1.5.0 pinned to its immutable
tag. v1.5 session creation, starter creation, placeholder generation, safety
declarations, runtime profile, and work verifier are reused unchanged unless a listed
retest trigger occurs. v1.6 residual scope is only the confirmed-input-to-compilable-
feature path for `spring-vue-postgres-buildable`.

## Starter compatibility result

The target starter has:

- package `example` and Spring Boot 3.3.5 with Java 21;
- Spring Web, JDBC, Flyway, PostgreSQL, Actuator, and Spring test dependencies;
- Flyway migrations at `backend/src/main/resources/db/migration`;
- Vue 3 with Node test and build commands;
- local Docker Compose and an environment-based PostgreSQL configuration.

It does not declare JPA, an ORM, authentication, a frontend router, TypeScript, an
OpenAPI generator, browser dependencies, or a database that may be started without
approval. v1.6 therefore targets Java model plus JDBC repository, plain Vue JavaScript,
and a written API contract. It must not add an ORM, auth policy, or undeclared runtime
tooling as a generator default.

## Required boundaries

- Preview and static generator tests are local and require no external action.
- A temporary consumer compile may download Maven or npm dependencies. That one
  planned atomic task is marked `requires_external_approval: true` and cannot start
  until the user explicitly permits it.
- Docker/PostgreSQL and Playwright scenarios are separate future opt-in work; v1.6
  records only their unresolved obligations.
- Every output path is new beneath a declared empty feature namespace. Existing source,
  historic migrations, and manually changed generated files are collision failures.

## Preparation result

The master has nine child plans and fourteen strict serial inventory items. Every
implementation item has one outcome, exact paths, a direct predecessor, a leaf
validation, and an evidence boundary. The hardening task is the first item and must
be verifier-verified before generator implementation.
