---
machine_kind: plan
machine_status: unknown
machine_title: CODEX Context Gateway Core Plan
---

# CODEX Context Gateway Core Plan

Purpose: implement the repo-native gateway shell, central ContextPack model, budget composer, and stable output writer.

## Tasks

- [x] Add a dedicated Ruby gateway module under `scripts/audits/`.
- [x] Add `collect`, `explain`, and `clean` commands through one wrapper script.
- [x] Implement stable machine and human outputs under the existing generated-tooling area.
- [x] Add centralized pack mode downgrade logic: `full -> compact -> indexOnly`.
- [x] Keep the gateway dev-tool-only and separate from Spring/Vue production runtime.

## Completion Evidence

- Status: complete
- Changed files: `scripts/audits/codex_local_context_gateway.rb`, `scripts/audits/codex-context.rb`, `Makefile`, `.gitignore`
- Validation evidence: `ruby -c scripts/audits/codex_local_context_gateway.rb`, `ruby -c scripts/audits/codex-context.rb`, `make codex-context`, `make codex-context-explain`
- Residual risk: AST isolation is heuristic, not parser-backed.
