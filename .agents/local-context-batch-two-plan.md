---
machine_kind: plan
machine_status: unknown
machine_title: Local Context Batch Two Plan
---

# Local Context Batch Two Plan

Goal: strengthen Codex local-context generation and reduce prompt overhead by implementing the next six deferred local-tooling helpers in one coordinated batch.

Scope source:
- `docs/codex-local-tooling-todo.md`

Execution rules:
- Implement child plans in dependency order where earlier tools feed later reports.
- Shared surfaces (`Makefile`, `scripts/audits/local_tooling_extended_tools.rb`, `docs/codex-local-tooling-todo.md`, registry artifacts, generated local-tooling outputs) close only after all touched child plans are internally consistent.
- Remove completed backlog items from the persistent backlog source in the same change that completes them.
- Close this master plan only after each completed child plan has completion evidence and the shared validation/audit pass succeeds.

Up-front approval assessment:
- No approval is currently required for workspace file edits, local Ruby execution, registry regeneration, or local validation commands used by this tooling batch.

Child plan inventory:
- [x] 86 `CODEX-LOCAL-MANIFEST-PATH-RESOLVER` (local-tooling, medium risk): `.agents/todo-plans/86-codex-local-manifest-path-resolver.md`
- [x] 87 `CODEX-LOCAL-SYMBOL-TO-TEST-LINKER` (local-tooling, medium risk): `.agents/todo-plans/87-codex-local-symbol-to-test-linker.md`
- [x] 88 `CODEX-LOCAL-DTO-TO-ENDPOINT-TO-FRONTEND-PACK` (local-tooling, medium risk): `.agents/todo-plans/88-codex-local-dto-to-endpoint-to-frontend-pack.md`
- [x] 89 `CODEX-LOCAL-WORKFLOW-SLICE-PACK` (local-tooling, medium risk): `.agents/todo-plans/89-codex-local-workflow-slice-pack.md`
- [x] 90 `CODEX-LOCAL-PLAN-TO-CODE-MAP` (local-tooling, medium risk): `.agents/todo-plans/90-codex-local-plan-to-code-map.md`
- [x] 91 `CODEX-LOCAL-HOTSPOT-RANKER` (local-tooling, medium risk): `.agents/todo-plans/91-codex-local-hotspot-ranker.md`
- [x] 92 `CODEX-LOCAL-COMPACT-DOMAIN-PACKS` (local-tooling, medium risk): `.agents/todo-plans/92-codex-local-compact-domain-packs.md`

Execution phases:
- [x] Phase 1: create child plans and inspect shared local-tooling extension points.
- [x] Phase 2: implement manifest-path resolver and integrate it into changeset playbook and validation preset flow.
- [x] Phase 3: implement symbol/test, DTO usage, and workflow slice context packs.
- [x] Phase 4: implement plan/code map, hotspot ranker, and compact domain packs.
- [x] Phase 5: regenerate registry artifacts and run batch validation plus plan/todo closeout audits.

## Completion Evidence

- Status: completed
