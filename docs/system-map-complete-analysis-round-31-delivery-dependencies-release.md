# Round 31: Delivery, Dependencies, and Release Provenance

Status: source-trace analysis. Reviewed: 2026-07-22.

## Conclusion

The repository provides local delivery and verification commands through root and
application Makefiles, Maven wrapper workflows, npm scripts, and a checked frontend
lockfile. The frontend contract generator is a controlled derived-artifact path.
No repository-visible CI workflow, Dockerfile, compose file, deployment manifest,
or release/rollback automation was found in this pass; external delivery behavior
must remain unknown rather than inferred.

## Delivery graph

```text
Java source + Flyway + Maven dependencies → Maven test/package
backend DTO source + generator → generated TypeScript contract → frontend validation/build
source + plan required paths → verifier evidence → verified work status
```

## Dependency and release boundaries

- Maven manages Spring Boot, JPA, security, WebSocket, Flyway, PostgreSQL, S3 and
  test dependencies; Java 21 is declared.
- npm manages Vue/Vite, TypeScript, Playwright, Axios, YAML, and editor packages;
  `package-lock.json` records the resolved frontend dependency graph.
- `npm run build` validates generated contracts before Vite build. Generated
  TypeScript must follow its backend source/generator rather than manual edits.
- Workspace Make targets coordinate local development, audits, package/build, and
  strict work-plan verification.
- CI execution, artifact publishing, image construction, environment promotion,
  release approval, rollback and dependency-vulnerability automation are not
  established by repository-visible evidence.

## Required disposition

Round 29 should treat missing CI/release provenance as an explicit drift-control
gap. It must not introduce an assumed deployment process. A future delivery plan
can add CI, SBOM/dependency scanning, release manifests, and rollback evidence.
