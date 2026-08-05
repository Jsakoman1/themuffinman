# Dora v1.5 preflight

The v1.5 baseline is MuffinMan commit `97aff932f0fd55207b5eceb549ebd8d0415a9a96`,
which pins Dora v1.4.0. v1.5 may reuse v1.4 interview, starter, proposal, decision,
work verification, and runtime-proof primitives, but it must not silently change
their existing consumer contracts.

All source generation must have dry-run, empty-destination or explicit-target,
non-overwrite, and review boundaries. Package downloads, database startup, browser
execution, backup/restore, deployment, and external identity-provider setup require
explicit user approval at the specific task that needs them.

The execution inventory is the only queue. The hardening task is first and must be
verifier-verified before any application-authoring implementation begins.
