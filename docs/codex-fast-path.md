# Codex Fast Path

Read `AGENTS.md`, then `docs/implementation-control.md`.

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

When autonomous continuation is authorized, treat the plan as an active batch: continue through all safe queued tasks
and close out only after the batch boundary, a real blocker, or exhausted validation recovery.
