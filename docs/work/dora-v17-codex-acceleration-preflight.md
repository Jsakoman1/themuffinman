# Dora v1.7 Codex acceleration preflight

## Baseline and scope

The baseline is MuffinMan commit `0a5ca21`, which pins Dora v1.6.0. The v1.5
interview/application-authoring route and v1.6 confirmed-feature route are retained
as baseline-only behavior. v1.7 changes only agent guidance, bounded context,
capability ordering, declared project conventions, related-feature composition, and
proof obligation orchestration.

## Command and safety compatibility

- Existing interview, session-create-app, agent-context, agent-next, compiled-feature
  preview, and apply commands remain their own public contracts unless an explicit
  v1.7 compatibility test proves an extension is safe.
- New commands must be read-only unless an existing explicit apply route is invoked
  with its own manifest and collision checks.
- Project convention checks and capability graphs diagnose declared data. They never
  rewrite existing consumer source, decide a business rule, or change work status.
- Related-resource composition must remain additive: new declared paths only, a new
  Flyway version only, no historic migration modification, and no regeneration over
  a manual edit.

## Evidence and approval boundaries

All planned leaf tests are local Ruby tests and fresh temporary consumer tests. The
v1.7 plan does not require package installation, network access, Docker, PostgreSQL,
browser execution, real data, credentials, backup/restore, deployment, or a release.
If a later task benefits from compile, database, browser, or external proof, it must
be added as a separate atomic task with a user approval request at execution time.

## Preparation result

The master has seven child plans, sixteen serial inventory items, exact task paths,
direct predecessors, leaf validation commands, and explicit evidence boundaries. The
first task hardens this structure; no Dora implementation may start until the verifier
records that hardening task as verified.
