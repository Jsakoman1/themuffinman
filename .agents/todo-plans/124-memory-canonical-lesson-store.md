# Memory Canonical Lesson Store

Purpose: define one canonical lesson-memory layer for learned product, tooling, and control-system lessons.

## Goal

Capture stable lessons, recurring patterns, and proven logic in a dedicated memory document so future work can reuse them without reopening old context.

## Scope

- `docs/product-memory.md`
- `docs/failure-knowledge-base.md` or the generated failure knowledge base pipeline
- `docs/agent-improvement-backlog.md`
- `docs/codex-local-tooling-todo.md`
- `docs/business-logic.md`
- `docs/domain-technical.md`

## Checklist

- [x] Decide the canonical file and shape for lesson memory.
- [x] Define what belongs in lesson memory versus active backlog versus generated failure knowledge.
- [x] Specify how lessons are appended, reviewed, and retired.
- [x] Keep the lesson store separate from aspirational product vision.

## Validation

- `ruby scripts/todo-audit.rb`
- `make failure-knowledge-base`
- `make audit-documentation`

## Completion Evidence

- Status: complete
- Execution status: complete
- Persistent backlog item: none
- Validation evidence:
  - `make failure-knowledge-base`
  - `make audit-documentation`
  - `ruby scripts/todo-audit.rb`
