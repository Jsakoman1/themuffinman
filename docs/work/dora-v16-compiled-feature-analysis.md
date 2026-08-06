# Dora v1.6 compiled feature analysis

## Objective

Move Dora from a v1.5 reviewable feature proposal to a controlled, compilable first
vertical feature for the declared `spring-vue-postgres-buildable` stack. The output
must remain a starting implementation that a Codex agent reviews, tests, and changes
through normal project-owned work; it must never become an unsupported claim that a
product is complete.

## Baseline

v1.5 validates confirmed capability inputs and can propose every required path. Its
safe apply writes review-required placeholders and rejects path traversal and
collisions. The official technical starter provides a neutral Spring Boot, Vue,
PostgreSQL, Flyway, Docker Compose, and command baseline. This means v1.6 has a
bounded first target stack and does not need to guess a framework or package layout.

## Gap to close

The current proposal does not yet produce a buildable migration, Java entity/DTO/
service/controller, API contract, Vue module/API client, or executable test harness.
It does not record which generated files may later be safely regenerated, and its
static safety checks currently consume declarations rather than a generated-project
manifest plus real generated paths.

## Product boundary

The generator may translate only explicit, confirmed input into technical structure.
It must reject rather than invent all of the following:

- database type, nullability, default, index, unique constraint, foreign key, or
  cascade behavior not explicitly declared;
- permission behavior, role policy, ownership query, workflow transition, endpoint
  request/response shape, error response, or UI copy not explicitly declared;
- external service, secret, real data, browser, database, package installation,
  deployment, or production operation.

## v1.6 approach

1. Define a stack-specific compiled-feature input that extends the v1.5 confirmed
   skeleton with explicit technical mappings and selected project locations.
2. Produce a deterministic generation manifest and read-only preview before writing.
3. Generate only into an empty, declared feature namespace; reject all collisions,
   traversal, unknown templates, and attempts to alter historic migrations.
4. Render a coherent Spring/Flyway/API/Vue/test/documentation vertical slice from
   those declarations. Generated tests initially prove compilation and declared
   contract wiring, not business acceptance.
5. Add generated-output inspection and consumer evidence. Database and browser
   execution remain opt-in tasks with separate approval gates.
6. Record every generated file, source declaration, generator version, and validation
   obligation so Codex can distinguish generated context from verified behavior.

## Explicit deferrals

- A policy-complete login/JWT/OIDC implementation and role enforcement generator.
- Generic generation for every framework, mobile client, or database.
- Automated backup/restore, real seed data, deployment, cloud adapter, or secret
  provisioner.
- Automatic regeneration over manual edits. v1.6 rejects a collision and requires
  an explicit future reconciliation workflow.
- A claim that generated source is production-ready, accessible, secure, or accepted
  merely because it compiles.
