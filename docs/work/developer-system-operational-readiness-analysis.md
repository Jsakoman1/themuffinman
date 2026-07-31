# Developer system operational-readiness analysis

Date: 2026-07-31
Baseline: a18b811e65001f68d174b42d1574f6c0f2ffc18d

## Decision

Run one bounded operational-readiness program after the verified developer-tooling
quality pass. The repository already has strong local validation, verifier-controlled
goal execution, IntelliJ routing, and one shared runtime-harness pilot. The remaining
gap is not another planning framework; it is making the current system easier to
review, continuously validate, retain, and reuse.

## Measured baseline

- The full tool self-test passes all 16 stages.
- The local catalog contains 71 referenced tools in five categories and no reported
  unreferenced tools.
- IntelliJ exposes 55 MCP tools and discovers five shared run configurations, while
  `WorkspaceNavigationService` still requires the documented local fallback.
- Twelve Chromium/runtime scripts pass runtime-tool preflight; only the brand-header
  pilot currently consumes the shared runtime harness.
- `docs/work` contains 180 YAML artifacts, including 150 with verified status.
- The current worktree contains 456 changed paths: 150 modified, 3 added, 2 deleted,
  and 301 untracked.
- CI, deployment, rollback, backup/restore, and incident response remain explicitly
  external-unknown in the release registry.
- No repository-specific skill is currently exposed; only generic system skills are
  available.

## Root-cause analysis

### Workspace state is hard to review

Strict start fingerprints protect new atomic tasks from pre-existing changes, but the
large worktree still makes ownership, review, checkpointing, and rollback difficult.
The safe first improvement is an immutable classified snapshot plus a repeatable
reporting tool. Commit, push, deletion, or branch rewriting remain outside this master
unless separately authorized.

### Work-plan retention is documented but not enforced

Implementation control says completed plans should normally be removed after durable
state is retained, yet 150 verified artifacts remain. Immediate bulk deletion is too
risky because references must be reconciled first. Add an explicit retention policy,
reference-aware candidate report, and audit; actual pruning must consume an approved
exact manifest in a later bounded cleanup.

### Local validation lacks a repository-visible CI baseline

Local validation is strong, but there is no `.github/workflows` source and the release
registry correctly marks CI as external-unknown. Add a minimal workflow for backend
tests, frontend type-check/build, generated-contract freshness, and tooling health.
The workflow file proves repository configuration only; CI remains unobserved until a
runner result exists.

### Tool discoverability trails tool capability

The current catalog infers references from text occurrence and the Makefile has no
single help surface. Replace implicit discovery with a machine-readable catalog that
records command, category, ownership, preconditions, mutation class, and expected cost,
then generate compact help from that source.

### Runtime harness needs two more independent pilots

The brand-header pilot proves the shared primitives work for one visual scenario. The
next two bounded migrations are business-style visual parity and SideJobs human-first.
They cover multi-route screenshots and multi-route semantic assertions without taking
on the much larger human-first-web evidence set.

### A repository skill is not ready yet

Skill creation is deferred until both new runtime pilots pass. Afterward, record a
machine-readable create/defer decision. If creation is recommended, use the system
`skill-creator` in a separate user-authorized task because the installed skill location
is outside this repository and cannot become verifier-owned completion state.

## Post-pilot decision

The business-style and SideJobs migrations completed after the initial brand-header
pilot. Together they verify shared browser lifecycle, seeded authentication,
browser-error collection, overflow inspection, and JSON evidence writing across three
independent scenario shapes: visual, multi-route parity, and semantic assertions.
The decision is now **create**, but only as a separate explicitly authorized skill-
creation task. The skill must preserve scenario-specific assertions and cannot replace
the repository work verifier as completion authority. The machine-readable decision is
recorded in `docs/runtime-evidence-skill-decision-2026-07-31.yaml`.

## Ranked execution sequence

1. verifier-harden the entire inventory;
2. capture and enforce a classified workspace-change snapshot;
3. establish reference-aware work-artifact retention policy and candidates;
4. add a repository-visible CI validation baseline;
5. formalize the local-tool catalog and generated help;
6. migrate business-style visual parity to the shared runtime harness;
7. migrate SideJobs human-first runtime to the shared harness;
8. record the runtime-evidence skill decision;
9. close out with measured before/after evidence and stable residual boundaries.

## Explicit exclusions

- no commit, push, merge, branch rewrite, or destructive worktree cleanup;
- no bulk deletion of verified work artifacts;
- no claim that a workflow file proves a successful external CI run;
- no product route, permission, API, schema, or domain behavior change;
- no migration of the remaining runtime scripts;
- no repository script calling IntelliJ MCP;
- no skill installation outside the workspace during this master;
- no second completion or capability-status authority.

## Expected improvement

If verified, the program should raise developer-system operational readiness from
approximately 7.4/10 to about 8.3/10. The remaining ceiling will be external CI
execution, clean logical commits, release/recovery evidence, and broader runtime
coverage rather than missing local tooling.

## Verified closeout measurement

The complete local closeout passed on 2026-07-31. The immutable workspace baseline
contains 468 classified paths; retention reviewed 189 work artifacts and reported 54
review candidates without deleting anything. The explicit catalog now covers 74 local
tools in five categories and provides 12 generated help commands. The local self-test
now has 17 passing stages.

The shared runtime harness has three verified pilots: the original brand-header
scenario plus fresh desktop/mobile business-style and SideJobs evidence. Business-style
covered six routes per viewport; SideJobs covered three semantic scenarios per viewport;
both reported no browser errors. The repository-visible CI workflow remains correctly
classified as unobserved until an external runner produces evidence. The program's
operational-readiness estimate is therefore 8.3/10 locally, with the remaining gap
limited to external execution, clean commits, recovery evidence, and broader runtime
coverage.

## Verifier readiness review

The review reconciled the originally measured 456-path worktree with the 11 planning
artifacts added by this package: goal pursuit must capture the then-current Git state,
not treat the earlier count as a permanent invariant. The workspace snapshot is an
immutable classification baseline; later program edits are expected and do not make
that historical snapshot stale.

All nine inventory items have one predecessor, one observable outcome, exact required
paths, a leaf validation, and a distinct evidence boundary. Future files first appear
in the task that owns them. Repeated shared paths (`Makefile`, implementation control,
analysis, backlog, and release registry) are intentionally changed by separate serial
tasks and therefore receive a fresh fingerprint at each start.

The initial plan missed `.github/` changed-path routing. The CI item now owns
`scripts/change-validation.rb` and must add a fixture-backed `.github/workflows/`
classification. This prevents the new workflow from becoming a repository path that
the normal validation router rejects.

The System Map impact report was generated from the ten existing implementation
surfaces available during planning. It recommends review of truth/drift ownership,
release operations, System Map, runtime acceptance, and regression scenarios. Their
dispositions are recorded on the master; runtime registries are reviewed again after
fresh pilot evidence rather than promoted during planning.
