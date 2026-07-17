# Codex Fast Path

Read `AGENTS.md`, then `docs/implementation-control.md`.

For a small change, implement and run the relevant targeted test.

For a non-trivial change:

1. Create `docs/work/<id>.yaml` from `docs/work-plan.template.yaml`.
2. Record the baseline Git revision.
3. Implement the listed tasks.
4. Run `make work-verify plan=docs/work/<id>.yaml`.

The work plan and verifier evidence are the only active implementation status. Do not create work plans, duplicate
checklists, generated verification reports, or parallel plan formats.

When autonomous continuation is authorized, treat the plan as an active batch: continue through all safe queued tasks
and close out only after the batch boundary, a real blocker, or exhausted validation recovery.
