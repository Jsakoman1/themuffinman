# System Standardization Audit Plan

Purpose: build a complete inventory of the current backend, frontend, docs, and generated artifacts so we can convert the findings into a `todo standardisation` backlog without losing file or method coverage.

## Scope

- [x] Audit all product modules under `apps/themuffinman/src/main/java/com/themuffinman/app/`
- [x] Audit all frontend modules under `apps/themuffinman/frontend/src/modules/`
- [x] Audit shared frontend shell/components under `apps/themuffinman/frontend/src/components/`
- [x] Audit living docs and workflow contracts under `docs/`
- [x] Audit generated local-tooling reports that already summarize the repository
- [x] Convert the results into a grouped standardization backlog with concrete file and method references

## Search Map

Use these fixed entry points so the review stays deterministic:

- Backend controllers: `rg -n "class .*Controller|@RequestMapping|@GetMapping|@PostMapping|@PutMapping|@PatchMapping|@DeleteMapping" apps/themuffinman/src/main/java/com/themuffinman/app`
- Backend services: `rg -n "class .*Service|class .*UseCase|class .*Assembler|class .*Factory|class .*Support" apps/themuffinman/src/main/java/com/themuffinman/app`
- Backend repositories: `rg -n "interface .*Repository" apps/themuffinman/src/main/java/com/themuffinman/app`
- Backend DTOs: `rg -n "class .*DTO|record .*DTO|enum .*DTO" apps/themuffinman/src/main/java/com/themuffinman/app`
- Backend mappers: `rg -n "class .*Mgr|class .*Mapper" apps/themuffinman/src/main/java/com/themuffinman/app`
- Backend models/enums: `rg -n "class .*\\{|enum .*" apps/themuffinman/src/main/java/com/themuffinman/app/*/model`
- Frontend pages/views: `rg --files apps/themuffinman/frontend/src/modules | rg '/(pages|views)/.*\\.vue$'`
- Frontend composables: `rg --files apps/themuffinman/frontend/src/modules | rg '/composables/.*\\.(ts|js)$'`
- Frontend API clients/contracts: `rg --files apps/themuffinman/frontend/src/modules | rg '/api/|contracts\\.ts$'`
- Shared frontend shell/components: `rg --files apps/themuffinman/frontend/src/components`
- Docs and contracts: `rg --files docs apps/themuffinman/src/test/java | rg 'AGENTS|business-logic|domain-technical|workflow-state-machines|agent-operating-model|documentation-sync-policy|change-completion-checklist|README|Validation|Contract|Inventory|Audit'`
- Generated summaries: `rg --files docs/generated/local-tooling | rg '(context|domain-packs|workflow-slices|targeted-tests|closeout|validation-evidence|plan-completion)'`

## Method

1. Start with repository-wide inventories, then narrow to module boundaries.
2. For each module, collect:
   - main entry files
   - public methods and endpoint signatures
   - DTOs and request/response contracts
   - repositories and fetch paths
   - state machines, permissions, and workflow transitions
   - frontend pages, views, composables, and API clients
3. Cross-check the inventories against docs and generated reports.
4. Record standardization gaps as concrete tasks, grouped by surface and by source file.
5. Prefer method-level references over file-only references when a class contains multiple responsibilities.

## Output Shape

- Backend standardization backlog
- Frontend standardization backlog
- API and contract consistency backlog
- Docs and agent-artifact consistency backlog
- Validation and test-surface backlog

## Working Notes

- Do not stop at a few representative files; cover each domain and each layer.
- Treat shared cross-module utilities separately from module-owned code.
- Capture repeated patterns even when they are technically correct, because standardization work is about reducing variance.
- Use existing generated reports where they already provide trustworthy inventory data, but verify the actual source files before turning a finding into a backlog item.

## Completion Check

- [x] All main modules have been inventoried.
- [x] The result list has concrete file and method locations.
- [x] The findings are grouped into todo-standardisation buckets.
- [x] The plan reflects the actual completed coverage before closeout.

## Completion Evidence

- Status: complete
- Output artifact: `.agents/system-standardization-audit-findings.md`
- Backlog handoff: `docs/implementation-backlog.md`
- Residual risk: later drift can be handled by the standardization master plans and the persistent backlog items they spawn.
