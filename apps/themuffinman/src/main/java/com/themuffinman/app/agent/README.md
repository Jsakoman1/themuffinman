# Agent Backend Capsule

## Responsibility

Owns admin-facing agent playground and dry-run surfaces used to inspect planner/simulation behavior and automation safety outputs.

## Main Entry Points

- Controller: `controller/`
- Production admin planning services: `service/`
- Sandbox and synthetic-data planning helpers: `sandbox/`
- DTOs: `dto/`

## Tests

- Add focused controller/service tests when agent response contracts or safety fields change.
- Keep agent-operating-model validation tests passing when automation contracts change.
- Keep the OpenAI-backed summary path aligned with the default-mini / creative-medium model split when model-routing rules change.

## Living Docs

- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml`
- `docs/domain-technical.md`
- `docs/agent-improvement-backlog.md`

## Forbidden Shortcuts

- Do not let sandbox or simulation behavior mutate production data.
- Do not place synthetic-data generation heuristics in production admin services when they can live in `sandbox/`.
- Do not paraphrase protected agent-operating-model phrases.
- Do not expose unsafe automation capabilities without updating safety docs and validation.
