# Dora control-state hygiene

The records below were reviewed on 2026-08-11. This classification preserves their
historical paths and evidence references; it does not promote, rewrite, or delete
evidence.

| Baseline Doctor signal | Record | Canonical classification or repair |
| --- | --- | --- |
| `unsupported_kind` | `docs/work/chatgpt-intent-plan-alignment-atomic-hardening-review.yaml` | Retained narrative atomic-hardening review cited by verified preflight evidence; explicit non-executable record. |
| `unsupported_kind` | `docs/work/dora-bridge-v2-1-collaboration-atomic-hardening-review.yaml` | Retained narrative atomic-hardening review cited by verified preflight evidence; explicit non-executable record. |
| `unsupported_kind` | `docs/work/dora-bridge-v2-2-collaboration-atomic-hardening-review.yaml` | Retained narrative atomic-hardening review cited by verified preflight evidence; explicit non-executable record. |
| `invalid_yaml` | `docs/work/dora-bridge-v3-local-runner-atomic-hardening-review.yaml` | Retained narrative review cited by verified preflight evidence. Its historical YAML syntax damage is preserved rather than rewritten; explicit non-executable record. |
| `unsupported_kind` | `docs/work/dora-implementation-excellence-pre-goal-inventory.yaml` | Retained non-executable planning blueprint. It does not select work or establish verification. |
| `unsupported_kind` | `docs/work/dora-project-memory-closeout-gate-atomic-hardening-review.yaml` | Retained narrative atomic-hardening review cited by verified preflight evidence; explicit non-executable record. |
| `verified-work-active-inventory` | `docs/work/dora-bridge-v1-historical-record-inventory.yaml` | Reconciled `state` from legacy `delivery_verified` to canonical `verified`; master, sole item, and cited evidence were already verified. |
| `verified-work-active-inventory` | `docs/work/dora-bridge-v3-runner-exec-contract-recovery-execution-inventory.yaml` | Reconciled stale `active` to canonical `verified`; the sole work item and its passing evidence were already terminal. |
| `verified-work-active-inventory` | `docs/work/dora-v3-claimed-handoff-restart-no-replay-execution-inventory.yaml` | Reconciled stale `active` to canonical `verified`; the sole regression item and its passing evidence were already terminal. |

`work_artifact_audit.non_executable_records` is the canonical policy boundary for the
six retained records. It is an exact-path classification, not a broad YAML exclusion:
new or unlisted malformed records under `docs/work` still produce a Doctor advisory.

The hygiene plan's own inventory is also terminally `verified`; this prevents the
closeout record itself from recreating the stale verified-work/inventory mismatch.
