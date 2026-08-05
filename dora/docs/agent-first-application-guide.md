# Agent-first application guide

This is the one route for a beginner working with Codex and Dora. Dora is the operating system for disciplined delivery; it is not the authority for your product decisions.

1. Write the idea in a product brief: user problem, users, intended outcomes, non-goals, assumptions, risks, and open decisions.
2. Write the domain library: vocabulary, entities, invariants, permissions, workflows, and acceptance scenarios.
3. Read and complete the agent project profile. It tells Codex which sources are canonical, which commands are allowed, what evidence is required, and which external actions require approval.
4. Choose a neutral architecture capability blueprint. Put business rules, permissions, validation, and state transitions in the service owner; keep the client responsible for presentation and declared interactions.
5. If voice is needed, use the voice blueprint. Audio and interpretation are inputs only; deterministic validation, review, explicit confirmation, consent, and retention rules come before execution.
6. Create one atomic plan item with a bounded outcome, exact changed paths, a leaf validation command, and an evidence boundary. Start it before editing, implement it, then verify it.
7. Run the declared tests and collect runtime evidence where a real user flow matters. Static analysis, automated tests, and runtime observations are different evidence types.
8. Update product and domain documentation with the implementation. Hand off only the verified evidence and unresolved decisions; do not call a product complete merely because Dora commands pass.

Bootstrap creates `AGENTS.md`, `docs/product-brief.yaml`, `docs/domain-library.yaml`, and `.dora/agent-project-profile.yaml`. Run `./bin/dora doctor .dora/project.yaml` before work; it fails closed if that context is missing or inconsistent.
