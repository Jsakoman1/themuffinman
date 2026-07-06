# Generated Artifact Scope

Generated artifacts are split by review value. The machine-readable policy is `docs/generated/artifact-policy.yaml`.

Machine-readable source files remain the primary control input. Generated markdown summaries are review aids for the
same state and should stay compact enough to inspect quickly without replacing the underlying machine-readable files.

Source-of-truth generated artifacts stay tracked when they describe backend contracts, agent safety, route/API ownership, DTO drift, documentation coverage, or frontend contract shapes. These include:

- `apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts`
- `docs/agent-operating-model.yaml`
- `docs/generated/README.md`
- `docs/generated/artifact-policy.yaml`
- `docs/generated/agent-endpoint-inventory.json`
- `docs/generated/automation-read-model-inventory.json`
- `docs/generated/backend-audit-inventory.json`
- `docs/generated/source-of-truth-audit.json`

The frontend contract generator namespaces enum exports by backend domain when two Java enums share the same simple name, so colliding workmarket and vision types stay distinct in the generated contract.

Tracked review context may stay tracked when it is concise, reviewer-useful, and directly refreshed by the task. This
includes stable local-tooling summaries, local-tooling JSON inventories, endpoint contract packs, and dead-code audit
summaries.

The active plan index is one of those stable local-tooling review aids:

- `docs/generated/local-tooling/plan-index.json`
- `docs/generated/local-tooling/plan-index-summary.md`

The one-shot control-system start surface is also tracked as stable review context:

- `docs/generated/local-tooling/control-start.json`
- `docs/generated/local-tooling/control-start-summary.md`

Disposable local run outputs stay untracked by default when they are machine caches, smoke-test latest files, session handoffs, ad-hoc context packs, diagnostic captures, per-change closeout reports, or post-merge retrospectives. A disposable output should be committed only when a change explicitly needs it as evidence and the reviewer can use it without rerunning the local environment.

Ignored disposable paths:

- `docs/generated/local-tooling/.cache/`
- `docs/generated/local-tooling/context-packs/`
- `docs/generated/local-tooling/session-handoffs/`
- `docs/generated/local-tooling/smoke/`
- `docs/generated/local-tooling/diagnostics/`
- `docs/generated/local-tooling/feature-slices/`
- `docs/generated/local-tooling/closeout-reports/`
- `docs/generated/local-tooling/plan-completion/`
- `docs/generated/local-tooling/post-merge-retrospectives/`

Stable generated outputs should stay concise enough for review. If a generated file is noisy but still useful, prefer
adding or improving a summary artifact before committing broad raw-output diffs.
