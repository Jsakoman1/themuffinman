# Agent-first application guide

This guide's reviewed local-source route is for Dora development, testing, and
pre-release work. A stable consumer runtime instead vendors its own immutable Dora
release/tag and commit locally; it does not execute a workstation source path or treat
a bootstrap-source record as its runtime pin. The guided lifecycle below remains the
same for existing and greenfield projects: owner-confirmed facts and decisions are
canonical, while research and derived context remain advisory.

This is the one route for a beginner working with Codex and Dora. Dora is the operating system for disciplined delivery; it is not the authority for your product decisions.

1. Conduct the Codex idea interview in [`codex-idea-interview.md`](codex-idea-interview.md). Record only user-confirmed answers and explicit open decisions in `idea-interview.yaml`.
2. Review a local Dora source descriptor, then run `dora create-app <destination> --interview idea-interview.yaml --source bootstrap-source.yaml [--starter <starter-id>] [--codex-integrate]`. For a confirmed Java, Vue, and PostgreSQL direction, select `--starter spring-vue-postgres-buildable`. This copies a checksum-verified package, creates knowledge, project memory, a first capability package, first work, and a local Git baseline. It does not create product implementation.
3. Read the generated product brief, domain library, project memory, capability package, and agent project profile. Preserve open decisions rather than guessing them.
4. Choose a neutral architecture capability blueprint. Put business rules, permissions, validation, and state transitions in the service owner; keep the client responsible for presentation and declared interactions.
5. If voice is needed, use the voice blueprint. Audio and interpretation are inputs only; deterministic validation, review, explicit confirmation, consent, and retention rules come before execution.
6. For one confirmed capability, create a `dora_confirmed_capability_context` containing only the confirmed data-safety, workflow, permission, and technical decisions. Dora's vertical-slice generator creates a proposal for migration, backend, API, frontend, tests, runtime evidence, documentation, and one atomic work declaration. Review it with the readiness gate. If it reports a gap, ask the user; never infer the missing rule.
7. A vertical-slice proposal is not source code, SQL, a database migration, or an implementation command. Only after the proposal is ready and the product owner has reviewed it may Codex create one atomic plan item with a bounded outcome, exact changed paths, a leaf validation command, and an evidence boundary. Start it before editing, implement it, then verify it.
8. Run the declared tests and collect runtime evidence where a real user flow matters. Static analysis, automated tests, and runtime observations are different evidence types.
9. Update product and domain documentation with the implementation. Hand off only the verified evidence and unresolved decisions; do not call a product complete merely because Dora commands pass.

The PostgreSQL starter deliberately defers authentication, authorization policy,
retention, backup, domain schema, API behavior, production hosting, and release
approval. It offers technical wiring, not a safe or complete product.

Use this public command surface to give Codex only declared project context and guidance:

```text
dora agent-context <adapter-path> <work-plan-path> <task-id>
dora agent-next <adapter-path> <execution-inventory-path>
dora status <adapter-path> <execution-inventory-path>
dora impact <adapter-path> <node-id>...
dora agent-closeout <adapter-path> <work-plan-path> <task-id> <change-impact-path> <changed-path>...
```

`dora create-app` creates `AGENTS.md`, `docs/product-brief.yaml`, `docs/domain-library.yaml`, `docs/project-memory.yaml`, `docs/capability-package.yaml`, and `.dora/agent-project-profile.yaml`. Run `dora diagnose <adapter-path>` for a read-only summary of declared blockers, then `dora doctor <adapter-path>` before work; the latter fails closed if that context is missing or inconsistent.

For a large greenfield idea, a cited discovery skeleton may first summarize owner-confirmed users, problem, intended first delivery, explicit product areas, exclusions, open questions, and foundation-choice coverage. It is advisory and regenerable: it does not create a project, amend canonical product/domain/decision records, or generate product code. Select a normal `create-app` or atomic work plan only after the owner accepts the relevant framing.

An owner-selected discovery delivery handoff may then summarize one first capability, its declared backend/API/UI/test/docs obligations, journey scenario states, and one candidate next atomic task. It remains a review artifact, not a work item: it cannot activate or verify work, generate code, or replace the normal `create-app` and verifier workflow.

Before `work-start`, run `dora readiness <project-root>`. Its default response is
read-only and tells Codex whether a Git baseline exists. Only the explicit
`--initialize-git` option creates a local repository and initial baseline commit.

## Guided authoring commands

Use a resumable session when the user is still describing an idea:

```text
dora interview-start docs/idea-session.yaml --project my-app
dora interview-next docs/idea-session.yaml
dora interview-answer docs/idea-session.yaml --id target_users --value "Confirmed user answer"
dora authoring-next docs/idea-session.yaml
```

Only record `user_confirmed` answers. Keep unresolved questions explicit, and do
not convert them into permissions, workflow rules, or implementation tasks. Before
selecting a technical starter, use `dora app-readiness starter-compatibility.yaml`.
For a confirmed capability, use `dora vertical-slice context.yaml`; it returns a
proposal and readiness blockers, not source code. Use `dora decision-record` only
to append a cited proposed or accepted decision.

The optional runtime profile is deliberately separate:
`dora runtime-profile-apply runtime-proof --dry-run` previews it and `--apply`
creates it only in an empty destination. Do not install a browser or run its proof
without explicit user approval. Even a passing neutral health proof does not prove
product behavior, security, production readiness, or release approval.
