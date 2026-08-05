# Agent-first application guide

This is the one route for a beginner working with Codex and Dora. Dora is the operating system for disciplined delivery; it is not the authority for your product decisions.

1. Complete the explicit `project-new.yaml` answers: product brief, domain library, agent profile, and first bounded work. Then run `dora new <destination> --answers <project-new.yaml>`. This creates knowledge and project memory, not product implementation.
2. Read the generated product brief, domain library, project memory, and agent project profile. They tell Codex which sources are canonical, which commands are allowed, what evidence is required, and which external actions require approval.
3. Preserve open decisions in product memory rather than guessing them. Dora treats them as unresolved product-owned questions.
4. Choose a neutral architecture capability blueprint. Put business rules, permissions, validation, and state transitions in the service owner; keep the client responsible for presentation and declared interactions.
5. If voice is needed, use the voice blueprint. Audio and interpretation are inputs only; deterministic validation, review, explicit confirmation, consent, and retention rules come before execution.
6. Create one atomic plan item with a bounded outcome, exact changed paths, a leaf validation command, and an evidence boundary. Start it before editing, implement it, then verify it.
7. Run the declared tests and collect runtime evidence where a real user flow matters. Static analysis, automated tests, and runtime observations are different evidence types.
8. Update product and domain documentation with the implementation. Hand off only the verified evidence and unresolved decisions; do not call a product complete merely because Dora commands pass.

Use this public command surface to give Codex only declared project context and guidance:

```text
dora agent-context <adapter-path> <work-plan-path> <task-id>
dora agent-next <adapter-path> <execution-inventory-path>
dora status <adapter-path> <execution-inventory-path>
dora impact <adapter-path> <node-id>...
dora agent-closeout <adapter-path> <work-plan-path> <task-id> <change-impact-path> <changed-path>...
```

`dora new` creates `AGENTS.md`, `docs/product-brief.yaml`, `docs/domain-library.yaml`, `docs/project-memory.yaml`, and `.dora/agent-project-profile.yaml`. Run `dora doctor <adapter-path>` before work; it fails closed if that context is missing or inconsistent.
