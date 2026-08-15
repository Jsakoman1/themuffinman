# Codex Fast Path

Read `AGENTS.md`, then `docs/implementation-control.md`.

Run `make help` for the compact, catalog-backed list of local entry points. The
catalog records each discovered tool's owner, preconditions, mutation class, and
expected cost; it is discovery metadata, not a completion authority.

For the compact final control pass, run `make control-check`; it also removes disposable
generated output. Use `make audit-all` only when a broad diagnostic review is useful.

For targeted repository context, use `make context-search q="phrase"`. It searches
source and canonical documentation while excluding dependencies, generated output,
runtime evidence, and build artifacts, then returns a bounded ranked context pack.
Use `mode=symbol`, `mode=callsite`, or `mode=canonical` when the investigation intent
is known. Use `make change-validation paths="path-a path-b"` to obtain an advisory,
deduplicated leaf-validation set before selecting the task's required command.
Use `make repository-map` for a quick structural check, or
`ruby scripts/repository-map.rb --query "SymbolName" --max-output 20000` for a
compact symbol result rather than the full frontend AST/backend map.
For a large dirty workspace, use `make workspace-change-report` before opening broad
diffs. It classifies current paths without editing them; a dated workspace snapshot is
an immutable historical handoff, not permission to commit, delete, or restore files.
When IDEA can resolve the exact symbol, use its call hierarchy, file inspection, or
rename support through the bounded fallback rules in `docs/intellij-mcp-fast-path.md`.
Use `make tool-self-test` after changing local tooling or its shared helpers.

For a non-trivial change, use `docs/system-map.md` to locate the canonical domain,
implementation, client, evidence, and control owners before creating or selecting a
work plan. The current System Map optimization program is
`docs/work/system-map-optimization-master.yaml`.

For local runtime evidence, start the owned stack with `make dev` and always end it
with `make dev-stop`. `make dev` records only its own process tree and refuses to
reuse occupied ports, so do not kill an unverified process by port alone.

For a small change, implement and run the relevant targeted test.

## Shared Auth Foundation adoption

Before changing an Auth Foundation consumer, run its repository-native backend
entry point (here: `make backend-test`), not a guessed root-level Maven command.
Use the supported Java runtime selected by the repository; the application wrapper
lives in `apps/themuffinman/`. For a new registration or password-reset policy,
add a focused compile-and-test check before the full suite, then run the full
repository-native validation. Keep login free of new-password policy checks so
existing password hashes remain usable. Transport DTO constraints validate request
shape; Auth Foundation validates Unicode-aware password policy at the service
boundary. Do not place Unicode minimum-length rules back in Bean Validation.

For a non-trivial change:

1. Create `docs/work/<id>.yaml` from `docs/work-plan.template.yaml`.
2. Record the baseline Git revision.
3. Implement the listed tasks.
4. Run `make work-verify plan=docs/work/<id>.yaml`.

For a strict serial program, do not bulk-verify a child plan. Start exactly one
atomic task, make its implementation change, then verify only that task:

```text
make work-start plan=docs/work/<id>.yaml task=<task-id>
ruby scripts/verify-work.rb plan=docs/work/<id>.yaml task=<task-id>
```

The program execution inventory is the required queue. Do not start a later item
or write its completion state while an earlier item remains unverified.

The work plan and verifier evidence are the only active implementation status. A strict serial program's
verifier-controlled execution inventory is the sole allowed atomic queue. Do not create duplicate checklists,
generated verification reports, or parallel plan formats.

Before starting a prepared master, confirm its preflight artifact, inventory mappings,
exact required paths, and repository-root-safe leaf validation commands. `draft` means
ready for controlled start, not active implementation.

For every broad or high-risk master, atomic-task hardening is a mandatory first slice:
split each implementation task into one independently verifiable outcome, exact paths,
dependencies, leaf validation, and evidence boundary. Verify that hardening task before
starting backend, API, frontend, Vision, documentation, or runtime implementation.

Minimal System Map path for a non-trivial change:

```text
make system-map-impact
make work-start plan=<child-plan> task=<task-id>   # serial plans only
make work-verify plan=<child-plan> task=<task-id>
make audit-truth-registry && make audit-docs
```

When autonomous continuation is authorized, treat the plan as an active batch: continue through all safe queued tasks
and close out only after the batch boundary, a real blocker, or exhausted validation recovery.
