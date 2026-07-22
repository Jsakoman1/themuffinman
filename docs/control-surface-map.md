# Control Surface Map

For the cross-system architecture and documentation index, start with [`docs/system-map.md`](system-map.md). This file remains intentionally scoped to implementation control.

There is one active implementation control surface:

- `docs/work/*.yaml` — current work and master plans;
- `docs/work-plan.template.yaml` — plan template;
- `docs/master-plan.template.yaml` — master-plan template;
- `scripts/verify-work.rb` — executable verifier and evidence writer;
- `docs/audits.md` — catalog of optional diagnostic audits;
- `docs/implementation-control.md` — human-readable workflow;
- `docs/agent-operating-model.yaml` — general agent safety rules.

Generated audit reports are disposable diagnostics. Only the active YAML plans and verifier evidence determine completion.
