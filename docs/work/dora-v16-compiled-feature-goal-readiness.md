# Dora v1.6 compiled feature goal readiness

## Ready to pursue

- v1.5.0 is the pinned, verified baseline.
- The target stack is constrained to the existing Spring JDBC, Vue, PostgreSQL, and
  Flyway starter rather than a guessed generic stack.
- The master, its child plans, and one strict serial inventory are prepared.
- The first task is atomic-plan hardening, with no generator implementation before its
  verifier evidence exists.
- Source generation is explicitly bounded to confirmed inputs, preview, collision
  rejection, and Git-based rollback.

## Deferred approval gates

- The independent compile task may require Maven/npm downloads and must obtain user
  approval at that time.
- Database, Docker, browser, real data, backup, deployment, and release actions are
  not v1.6 goal prerequisites and remain separately approved actions.

## Goal completion evidence

The goal may complete only after every inventory item is verifier-marked verified,
the master verifies its children, and the independent consumer demonstrates generated
source layout plus compile evidence. Compile evidence alone remains insufficient for
runtime, security, accessibility, or business acceptance claims.
