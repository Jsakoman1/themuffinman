# Dora v1.3 web-app foundation preflight

Status: prepared for goal pursuing after atomic hardening verification.

## Baseline

- MuffinMan baseline: `90f1e3f513e3a0a8941b2a5720d8bd8395f54962`.
- Dora baseline: published and pinned v1.2.0.
- Existing Spring/Vue starter is intentionally database-free.

## Execution assumptions

- Ruby runs Dora contracts and plan verification.
- Java, Maven, Node, and npm are needed by the fresh buildable starter proof.
- Docker Compose and PostgreSQL are optional local prerequisites for the database service configuration; no task may silently install, start, destroy, or reset them.
- The independent-consumer test must use temporary directories and no MuffinMan services or data.

## Boundaries confirmed

- Dora owns starter mechanics, proposal contracts, project guidance, and reusable tests.
- A consuming project owns business entities, database field choices, permissions, workflows, product UI, production configuration, and runtime authority.
- No remote release, deployment, secret change, or consumer pin is in the execution inventory.

## Gaps intentionally retained

- Authentication/data-safety profiles are deferred to a separate plan.
- Offline synchronization is deferred.
- A proposal will not write generated application source.
