# Dora v0.8 goal-readiness review

## Decision

Ready to create an implementation goal and start only the `dora-v08-harden` item.
The master remains draft; no release, pin, product migration, or Voice runtime work
is authorized by this preparation artifact.

## Requirement-to-evidence map

| Requirement | Planned proof | Boundary |
| --- | --- | --- |
| Remaining reusable code leaves MuffinMan | Built-in runner, parser fixtures, physical-extraction test, ownership audit | Product-owned rules and report locations remain local. |
| Codex has reliable project context | Product brief, domain library, agent profile, knowledge-aware doctor | The profile declares knowledge; it cannot invent missing business rules. |
| New app can begin from an idea | Independent agent-first consumer fixture | It proves a deterministic contract and workflow, not LLM answer quality. |
| Voice AI is reusable and safe | Voice blueprint schema, fixture, and safety test | No provider credentials, raw durable transcripts, or unconfirmed execution. |
| MuffinMan becomes product-first | Reproved catalog and compatibility audit | Local adapter, handoff, and product-specific audits legitimately remain. |
| Release remains safe | Separate v0.8 publish and pin tasks | Both require fresh external approval. |

## Readiness findings

1. The v0.7 wrapper layer is not accepted as a complete extraction: v0.8 explicitly
   names all eleven wrappers and both parser sources for retirement or redirection.
2. The Java index can use standard JDK APIs; the TypeScript/Vue index must declare
   its Node dependency and execution environment instead of relying on MuffinMan's
   package installation.
3. Voice patterns are eligible for abstraction only as capability contracts and
   evaluation fixtures. MuffinMan's Vision service, intents, entity resolvers,
   permissions, UI, provider configuration, and runtime evidence remain product-owned.
4. The generated agent entrypoint must tell Codex where canonical knowledge lives,
   what it may modify, which evidence is required, and when to stop for human
   approval. It must not grant implicit release or production authority.

## First safe implementation action

Run `make work-start plan=docs/work/dora-v08-agent-first-operating-system-atomic-hardening.yaml task=harden-agent-first-program` only after the user explicitly asks to set this master as the active goal.
