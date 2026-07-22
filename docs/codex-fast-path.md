# Codex Fast Path

Read `AGENTS.md`, then `docs/implementation-control.md`.

For a non-trivial change, use `docs/system-map.md` to locate the canonical domain,
implementation, client, evidence, and control owners before creating or selecting a
work plan. The current System Map optimization program is
`docs/work/system-map-optimization-master.yaml`.

For local runtime evidence, start the owned stack with `make dev` and always end it
with `make dev-stop`. `make dev` records only its own process tree and refuses to
reuse occupied ports, so do not kill an unverified process by port alone.

For a small change, implement and run the relevant targeted test.

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

Minimal System Map path for a non-trivial change:

```text
make system-map-impact
make work-start plan=<child-plan> task=<task-id>   # serial plans only
make work-verify plan=<child-plan> task=<task-id>
make audit-truth-registry && make audit-docs
```

When autonomous continuation is authorized, treat the plan as an active batch: continue through all safe queued tasks
and close out only after the batch boundary, a real blocker, or exhausted validation recovery.
