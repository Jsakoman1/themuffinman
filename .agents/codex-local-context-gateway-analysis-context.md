# CODEX-LOCAL-CONTEXT-GATEWAY Analysis Context

Purpose: capture the original task brief, a repository-grounded analysis, an implementation outline, and the executable master plan for introducing a unified local context gateway on top of the existing Codex local tooling stack.

## Original Rough Instructions

```text
Codex Task: Analyze Current Local Context Tooling and Propose Implementation Plan for CODEX-LOCAL-CONTEXT-GATEWAY
You are operating inside this IntelliJ workspace/repository.
Your task is analysis and planning only in this phase.
Do not immediately implement code unless explicitly instructed in a later prompt. First inspect the current repository state, compare it with the target architecture below, and produce a concrete implementation plan that fits the existing repo conventions.
Goal
We already have many local Codex/audit/context tools implemented. The next architectural step is to avoid creating many disconnected tools and instead introduce a unified local development tool called:
CODEX-LOCAL-CONTEXT-GATEWAY
This gateway should act as a local dev-tool / sidecar, not as production application logic.
It should collect, normalize, score, compress, and emit compact local context packs for Codex, using existing tools where possible.
The gateway should help Codex understand the repository with fewer tokens by producing small, fresh, reproducible, local context artifacts.
Important framing
This is not a new production app.
It should be a local developer tool that lives inside or next to the existing repository tooling.
Preferred mental model:
repository/
  backend/
  frontend/
  tests/
  docs/
  existing-audit-tools/
  tools/
    codex-context/
      ...
  .codex/
    context/
      latest.human.md
      latest.machine.json
      packs/

If the existing repository already has a better location for local audit/context tools, use that instead.
Follow existing project conventions.
Do not introduce unnecessary framework complexity.
Do not create a full web dashboard, auth system, database, or production deployment.
Known existing local tool stack
Assume that many or all of these tools may already exist in this workspace. Verify by inspecting the repository.
Existing context/compression/entry tools may include:
diff-summary
audit-summary-index
context-pack
repo-map
symbol-index
codebase-capsule
session-handoff
changeset-playbook

Existing audit routing/scope tools may include:
audit-change-impact-preflight
changeset-risk
audit-router
recommend-feature-slices
recommend-validation-preset
audit-manifest-decision

Existing validation/test-selection tools may include:
validation-matrix
recommend-targeted-tests
fast-check
closeout-bundle
record-validation
test-history-summary
failure-knowledge-base
audit-delta-report

Existing local graph / inventory / callsite context tools may include:
endpoint-contract-packs
audit-endpoint-callsite-linker
audit-frontend-route-surfaces
audit-frontend-usage-graph
audit-backend-dependency-graph
audit-file-relation-graph
audit-read-surface-inventory
audit-test-surface-inventory
api-contract-snapshot

Existing drift/coverage/gap audit tools may include:
audit-api-contract-drift
audit-repository-fetch
audit-migration-entity-drift
audit-contract-test-gaps
audit-mutation-safety
audit-docs-as-tests
audit-doc-template-coverage
audit-doc-sync-preflight
audit-doc-sync-duplicates
audit-doc-canonical-phrases
audit-docs-to-code-drift
audit-doc-coverage-gap
audit-doc-staleness-scoring
audit-architecture-drift
architecture-decision-index
audit-agent-model-feature-coverage
audit-sandbox-data-coverage-pack
audit-sandbox-generation-coverage
audit-feature-intro-check

Existing cleanup/hygiene tools may include:
audit-mapper-usage
audit-duplicate-logic
audit-permission-rule-duplication
audit-frontend-state-logic-duplication
audit-test-fixture-duplication
audit-frontend-stale-surfaces
audit-frontend-dead-code
audit-backend-dead-code
audit-dead-code
audit-manual-cleanup-candidate-report
audit-dormant-code
audit-naming-consistency
audit-config-sprawl
audit-style-token-usage
audit-rich-text-safety
audit-error-pattern
audit-async-mutation-flow

Existing closeout/operational-safety tools may include:
feature-closeout-audit
enforce-feature-closeout
closeout-report
audit-plan-completion
audit-validation-evidence-quality
audit-generated-artifact-freshness
audit-generated-commit-scope
generate-audit-registry-artifacts
audit-documentation
audit-make-target-index

Planned or desired next-generation tools may include:
CODEX-LOCAL-MANIFEST-PATH-RESOLVER
CODEX-LOCAL-SYMBOL-TO-TEST-LINKER
CODEX-LOCAL-DTO-TO-ENDPOINT-TO-FRONTEND-PACK
CODEX-LOCAL-WORKFLOW-SLICE-PACK
CODEX-LOCAL-PLAN-TO-CODE-MAP
CODEX-LOCAL-HOTSPOT-RANKER
CODEX-LOCAL-COMPACT-DOMAIN-PACKS
codex-prompt-distillation-engine
codex-semantic-response-cache
codex-token-budget-enforcer
codex-local-runtime-trace-packer
codex-local-git-blame-hotspotter
codex-ast-diff-isolation
codex-session-checkpoint-diff
codex-postgres-schema-snapshotter
codex-postgres-query-explain-proxy
codex-vue-hmr-runtime-bridge
codex-vue-tailwind-ast-stripper
codex-local-test-feedback-loop
codex-shadow-workspace-sandbox
codex-postgres-schema-ast-bridge
codex-local-db-seed-injector
codex-vue-active-route-contextualizer
codex-pinia-state-hydrator
codex-e2e-contract-aligner

Treat this list as an initial hypothesis, not as verified truth. Inspect the repository and determine what already exists, what is partially implemented, what is duplicated, and what is missing.
Target architecture
Design around a central Context Gateway.
High-level flow:
Codex / IntelliJ / CLI request
        |
        v
CODEX-LOCAL-CONTEXT-GATEWAY
        |
        +-- Git/Diff Provider
        +-- AST Provider
        +-- Existing Tool Provider Wrappers
        +-- Symbol/Test Provider
        +-- Endpoint/DTO/Frontend Provider
        +-- Postgres Schema Provider
        +-- Postgres Explain Provider
        +-- Vue Runtime Provider
        +-- Validation Provider
        +-- Session Provider
        +-- Cache Provider
        |
        v
Context Composer
        |
        +-- relevance scoring
        +-- token budget enforcement
        +-- deterministic distillation
        +-- safety/sanitization
        +-- provenance metadata
        |
        v
.codex/context/latest.human.md
.codex/context/latest.machine.json
.codex/context/packs/*.json

The gateway should not replace existing tools. It should orchestrate and normalize them.
Existing tools should be wrapped as providers where possible.
Core concept: ContextPack
The gateway should standardize all output through a stable ContextPack shape.
Propose the final shape after inspecting existing repository conventions, but start from this conceptual model:
type ContextPack = {
  id: string
  kind:
    | "diff"
    | "ast"
    | "symbol"
    | "test"
    | "endpoint-contract"
    | "frontend-route"
    | "frontend-usage"
    | "db-schema"
    | "db-explain"
    | "runtime-trace"
    | "validation"
    | "session"
    | "cache"
    | "domain"
    | "other"

  scope: {
    files?: string[]
    symbols?: string[]
    endpoints?: string[]
    dtoTypes?: string[]
    tables?: string[]
    routes?: string[]
    tests?: string[]
  }

  confidence: number
  relevance: number
  estimatedTokens: number

  mode?: "full" | "compact" | "indexOnly"

  fingerprint: string

  provenance: {
    generatedAt: string
    provider: string
    sourceCommands?: string[]
    sourceFiles?: string[]
    sourceHashes?: Record<string, string>
  }

  payload: unknown
}

If the project is Java-oriented, adapt this into Java records/classes.
If the project is TypeScript/Node-oriented, adapt this into TypeScript interfaces.
If the existing tools use JSON schemas already, reuse those conventions.
Required output artifacts
The gateway should eventually produce two main outputs:
.codex/context/latest.machine.json
.codex/context/latest.human.md

Additionally, it should write individual provider packs:
.codex/context/packs/git-diff.json
.codex/context/packs/ast-diff.json
.codex/context/packs/symbol-test.json
.codex/context/packs/endpoint-contract.json
.codex/context/packs/postgres-schema.json
.codex/context/packs/vue-runtime.json
.codex/context/packs/validation.json

Names can be adjusted to fit existing conventions.
latest.machine.json should be stable and machine-readable.
latest.human.md should be optimized for Codex prompts and human inspection.
Proposed CLI shape
Inspect existing scripts first. If compatible, propose something like:
codex-context collect --budget 6000 --mode implementation
codex-context collect --budget 3000 --mode bugfix
codex-context collect --budget 12000 --mode deep-audit
codex-context explain --last
codex-context clean

Or if the repo uses npm/pnpm:
pnpm codex:context collect --budget 6000 --mode implementation
pnpm codex:context explain --last

Or if the repo uses Maven/Gradle/Java CLI tooling, propose the equivalent.
Token budget behavior
The gateway should enforce token budgets centrally.
Each pack should support these modes where possible:
full      = detailed payload
compact   = signatures, relationships, key facts
indexOnly = names, paths, fingerprints, reason for inclusion

Suggested inclusion policy:
1. Always include task intent if provided.
2. Always include changed AST nodes if available.
3. Always include direct tests if known.
4. Include endpoint/DTO/frontend contract when touched.
5. Include DB schema only for touched tables and direct FK neighbors.
6. Include Vue runtime only when explicitly available or frontend route is relevant.
7. Include validation/test failure summaries only when recent and relevant.
8. If over budget, downgrade low-priority packs from full to compact.
9. If still over budget, downgrade to indexOnly.
10. If still over budget, exclude low-confidence or low-relevance packs.

Do not solve token budget independently inside each individual tool. Centralize it in the composer.
Deterministic prompt distillation
Before considering LLM-based distillation, prefer deterministic compression.
For code:
- remove irrelevant comments
- remove blank lines
- collapse imports
- collapse unchanged method bodies
- collapse generated code
- replace huge literals/snapshots with placeholders

For Vue:
template:
  keep component structure
  keep v-if / v-for / v-model / @events / :bindings / refs / ids / data-testid
  remove class/style/Tailwind visual noise unless style is the task

script:
  keep imports
  keep props/emits
  keep stores
  keep API calls
  keep computed/watch/methods
  collapse unrelated logic

For backend:
controller:
  keep route annotations
  keep method signatures
  keep DTO types
  keep auth/permission annotations
  collapse unrelated method bodies

service:
  keep changed methods
  keep direct callers/callees
  collapse unrelated methods

entity:
  keep fields, relations, indexes, constraints
  remove generated getters/setters unless custom

AST diff isolation
Plan for a provider that turns textual git diff into symbol-level context.
Desired behavior:
git diff
 -> changed files
 -> parse AST
 -> identify changed classes/methods/functions/components
 -> include minimal enclosing symbol
 -> include parent class/interface summary
 -> include direct references from symbol index
 -> emit ContextPack(kind="ast")

Do not send entire 2000-line files when one method changed.
Existing tool wrappers
Many existing tools likely already produce valuable reports.
Do not duplicate their logic.
Create a provider/wrapper pattern:
ExistingToolProvider
  - invokes or reads output from existing local tools
  - normalizes their result into ContextPack
  - attaches provenance and fingerprints
  - estimates token cost

Examples:
repo-map                  -> ContextPack(kind="symbol" or "domain")
symbol-index              -> ContextPack(kind="symbol")
recommend-targeted-tests  -> ContextPack(kind="test")
endpoint-contract-packs   -> ContextPack(kind="endpoint-contract")
audit-frontend-usage-graph -> ContextPack(kind="frontend-usage")
audit-backend-dependency-graph -> ContextPack(kind="symbol")
audit-delta-report        -> ContextPack(kind="validation" or "diff")

Postgres integration
Postgres integration should be local-only and safe.
It should never expose database credentials or real sensitive values.
First implementation should focus on schema, not data.
Desired provider:
codex-postgres-schema-snapshotter

Desired behavior:
- connect only to local/dev database
- read information_schema / pg_catalog
- output compact schema graph:
  - tables
  - columns
  - types
  - nullability
  - primary keys
  - foreign keys
  - indexes
  - unique constraints
- support focused mode:
  - only touched tables
  - plus FK neighbors with depth=1
- support schema hashing

Safety requirements:
- no production DB
- no secrets in output
- denylist columns matching:
  password
  token
  secret
  api_key
  refresh_token
  access_token
  session
  credential
- optional allowlist schemas
- no row samples in MVP

Postgres explain proxy should be validation-only:
codex-postgres-query-explain-proxy

It should not be included in every initial context pack.
Use only when a query/repository/SQL change is detected or explicitly requested.
Summarize EXPLAIN output. Do not dump huge raw JSON into Codex context.
Vue/Vite runtime integration
Do not make Vue runtime mandatory for MVP.
If implemented, it should be dev-only and sanitized.
Preferred approach:
- tiny dev-only Vue plugin
- records current route
- records matched component names
- records Pinia/Vuex store shape
- optionally records selected safe values
- sends snapshot to local gateway

Important:
- no auth tokens
- no personal data
- no full user objects
- no cookies
- no localStorage dumps
- shape-only by default

Example safe payload:
{
  "route": "/dashboard/subscriptions",
  "routeName": "subscriptions",
  "components": ["SubscriptionPage", "PlanCard", "CheckoutModal"],
  "stores": {
    "userStore": {
      "shape": {
        "id": "string",
        "role": "string",
        "subscriptionStatus": "string",
        "loading": "boolean"
      }
    }
  }
}

Semantic response cache
Treat semantic cache as later-phase.
Do not cache risky implementation advice as source-of-truth.
Safe things to cache:
- schema summaries by schemaHash
- endpoint packs by endpointHash
- route packs by routeHash
- test history summaries by testHash
- audit summaries by reportHash

Unsafe things to cache initially:
- final implementation recommendations
- business-rule decisions
- old fix suggestions without exact fingerprint match

Any cache hit must include fingerprint/provenance.
If fingerprints do not match, cache result may only be used as a hint, not as truth.
Shadow workspace and test feedback loop
Treat these as valuable but later-phase modules.
Desired long-term behavior:
- apply proposed change in shadow workspace
- run targeted tests
- run typecheck/linter
- summarize failures compactly
- feed only the relevant stack trace and affected lines back to Codex

Do not design this as the first milestone unless the repository already has most required building blocks.
What you must do now
Perform the following steps.
Step 1: Repository inventory
Inspect the repository and identify:
- where existing Codex/audit/context tools live
- language/runtime used by those tools
- how they are invoked
- where their outputs are written
- naming conventions
- config conventions
- test conventions
- whether .codex/ or similar directories already exist
- whether JSON schemas or report formats already exist

Step 2: Current-state matrix
Create a matrix with columns:
Tool / Capability
Current status: implemented / partial / missing / duplicate / unknown
Location
Input
Output
Can be wrapped as ContextPack provider? yes/no
Notes

Include at least these capability groups:
context compression
repo map / symbol index
diff summary
audit routing
test selection
endpoint contracts
frontend usage graph
backend dependency graph
audit delta / validation
session handoff
Postgres schema
Postgres explain
Vue runtime
AST diff isolation
token budget enforcement
prompt distillation
semantic cache
shadow workspace
test feedback loop

Step 3: Gap analysis
Compare the current repo with the target gateway architecture.
Identify:
- what already exists and should be reused
- what should be wrapped
- what should be refactored
- what should be left unchanged
- what is missing
- what is risky or too large for first milestone
- what duplicates existing capability

Step 4: Proposed architecture for this repo
Propose the most repo-native architecture.
Answer these questions:
- Should this be a CLI, script package, Java tool, Node tool, Python tool, Gradle/Maven task, or IntelliJ run config?
- Where should it live?
- What files/modules should be created?
- What existing modules should it call?
- What output directories should it use?
- What config file should it use?
- How should it estimate token cost?
- How should it decide pack relevance?
- How should it handle provenance/fingerprints?

Step 5: Implementation milestones
Create a phased plan.
Recommended shape:
Milestone 1: Gateway skeleton
- ContextPack schema
- Provider interface
- Composer
- Output writer
- explain command
- existing tool wrapper for 2-3 already implemented tools

Milestone 2: AST diff and token budget
- AST diff isolation
- deterministic distillation
- budget downgrade full -> compact -> indexOnly
- latest.human.md and latest.machine.json

Milestone 3: Existing context stack integration
- repo-map
- symbol-index
- recommend-targeted-tests
- endpoint-contract-packs
- frontend/backend graph tools
- audit-delta-report

Milestone 4: Postgres schema provider
- focused schema snapshot
- touched table resolver
- FK neighbor expansion
- safety filters

Milestone 5: Vue runtime provider
- dev-only bridge
- route/component snapshot
- store shape sanitizer

Milestone 6: Validation feedback
- targeted test runner integration
- compact failure summaries
- optional shadow workspace

Milestone 7: Cache and advanced optimization
- safe fingerprinted cache
- session checkpoint diff
- hotspot ranking

Adjust milestones based on actual repository state.
Step 6: MVP recommendation
After analysis, recommend the smallest useful MVP.
It should probably include:
- ContextPack schema
- collect command
- explain command
- GitDiffProvider
- wrapper around existing repo-map/context-pack/symbol-index if present
- wrapper around recommend-targeted-tests if present
- TokenBudgetComposer
- latest.human.md
- latest.machine.json

But verify against the repo.
Step 7: Implementation risk list
List risks and mitigations:
- stale context
- wrong cache reuse
- token estimation errors
- secrets in DB/runtime output
- too much runtime data
- duplicate tool logic
- fragile AST parser
- slow execution
- too many generated artifacts

Step 8: Final deliverable
Produce a markdown report.
Preferred output path:
.codex/plans/codex-local-context-gateway-plan.md

If this path does not fit the repository, choose the closest existing convention and explain why.
The report must include:
# CODEX-LOCAL-CONTEXT-GATEWAY Plan

## Executive Summary
## Current State Inventory
## Current-State Matrix
## Gap Analysis
## Recommended Architecture
## ContextPack Schema Proposal
## Provider Design
## Composer / Token Budget Strategy
## Output Artifacts
## CLI / Invocation Design
## Postgres Integration Plan
## Vue Runtime Integration Plan
## Milestones
## MVP
## Risks and Mitigations
## Open Questions
## Suggested First Implementation Prompt

The "Suggested First Implementation Prompt" should be a short follow-up prompt that I can send to you after reviewing the plan, for example:
Implement Milestone 1 only. Keep changes minimal. Reuse existing tool conventions. Do not implement Postgres or Vue runtime yet.

Constraints
Follow these constraints strictly:
- Do not implement yet unless explicitly instructed later.
- Do not modify production application behavior.
- Do not add external services.
- Do not expose secrets.
- Do not connect to production databases.
- Prefer existing tooling and conventions.
- Prefer deterministic compression before LLM-based compression.
- Prefer wrapping existing tools over duplicating logic.
- Keep generated context artifacts under .codex/ or existing equivalent.
- Make all generated artifacts reproducible and provenance-backed.
- Keep Vue runtime bridge dev-only.
- Keep Postgres data extraction schema-first, no row samples in MVP.

Expected answer from you now
First inspect the repository.
Then write the plan report.
Do not give a generic plan. Ground the report in the actual current repository state.
If you cannot verify something, mark it as unknown and explain what would be needed to verify it.
```

## Repository-Grounded Analysis

### Executive Summary

The repository already contains a large Ruby-based local tooling platform under `scripts/audits/`, invoked through root `Makefile` targets and writing deterministic JSON/Markdown reports under `docs/generated/local-tooling/`.

The right architecture for this repo is not a new service, Java module, or Node package. It is a Ruby orchestration layer that reuses the existing audit registry, report formats, and generation helpers.

The implemented gateway therefore sits as a thin sidecar over the existing tooling stack:

- it adds a new Ruby gateway module and wrapper CLI
- it reuses existing generated outputs by default for static packs
- it refreshes scoped/dynamic packs when reuse would be misleading
- it emits centralized machine/human context artifacts in the existing generated-tooling area because `.codex/` could not be written from this execution context

### Current State Inventory

- Existing tooling location:
  - `scripts/audits/*.rb`
  - `scripts/local_tooling_common.rb`
  - `docs/generated/local-tooling/**`
  - `docs/tooling/codex-local-audits.yml`
- Language/runtime:
  - Ruby for local tooling
  - root `Makefile` as primary invocation surface
  - Java/Spring and Vue/Vite are application runtimes, not the local tooling runtime
- Invocation style:
  - `make <target>`
  - `ruby scripts/audits/<script>.rb`
- Output conventions:
  - JSON + Markdown summary pairs
  - archival history under `docs/generated/local-tooling/.history/`
  - cache metadata under `docs/generated/local-tooling/.cache/audit-inputs.json`
- Config conventions:
  - hard-coded defaults in Ruby helpers
  - no existing dedicated gateway config file
- Test/validation conventions:
  - Ruby syntax checks for scripts
  - representative command execution for generated reports
  - backend JUnit and frontend type/build validations selected by audit/router reports
- Existing `.codex/` directory:
  - not present and not writable from this execution path
- Existing schemas/report formats:
  - multiple JSON outputs already exist
  - repo already tracks JSON schemas in `docs/`

### Current-State Matrix

| Tool / Capability | Status | Location | Input | Output | Wrap as Provider | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| context compression | implemented | `generate-context-pack.rb`, `generate-codebase-capsule.rb`, `generate-session-handoff.rb` | topic/files/budget | `docs/generated/local-tooling/context-packs`, handoffs, capsule | yes | already does lightweight summarization but not centralized |
| repo map / symbol index | implemented | `generate-repo-map.rb`, `generate-symbol-index.rb` | repo scan | JSON + summary | yes | stable static inventories |
| diff summary | implemented | `generate-diff-summary.rb` | git status/diff | JSON + summary | yes | complements direct git provider |
| audit routing | implemented | `audit-router.rb`, `audit-change-impact-preflight.rb`, `recommend-validation-preset.rb` | files/current change | JSON + summary | yes | routing logic already exists |
| test selection | implemented | `recommend-targeted-tests.rb`, `link-symbol-to-tests.rb`, `generate-test-history-summary.rb` | files/symbol | JSON + summary | yes | strong reuse surface |
| endpoint contracts | implemented | `generate-endpoint-contract-packs.rb`, `generate-api-contract-snapshot.rb`, `generate-dto-usage-pack.rb` | controllers/DTOs | JSON + summary | yes | contract packs already compact |
| frontend usage graph | implemented | `audit-frontend-usage-graph.rb`, `audit-frontend-route-surfaces.rb` | frontend scan | JSON + summary | yes | static graph available |
| backend dependency graph | implemented | `audit-backend-dependency-graph.rb` | backend scan | JSON + summary | yes | static graph available |
| audit delta / validation | implemented | `audit-delta-report.rb`, `generate-validation-matrix.rb`, `generate-fast-check-report.rb` | audit id/files | JSON + summary | yes | validation guidance exists |
| session handoff | implemented | `generate-session-handoff.rb` | topic/files/budget | JSON + summary | yes | good session provider candidate |
| Postgres schema | missing | none verified | local DB | none | later | runtime DB deps intentionally absent from current tooling |
| Postgres explain | missing | none verified | query/SQL | none | later | no local explain proxy exists |
| Vue runtime | missing | none verified | live app state | none | later | no dev bridge/plugin exists |
| AST diff isolation | partial | new gateway heuristic provider | git diff + source files | context pack | yes | heuristic symbol extraction only, not parser-backed |
| token budget enforcement | partial | legacy `context-pack` file count budget, new gateway token budget | files/packs | pack mode downgrade | yes | centralized token budget now added in gateway |
| prompt distillation | partial | legacy summaries + new gateway compact/index variants | report payloads | compact/index payloads | yes | deterministic but generic |
| semantic cache | missing | none verified | fingerprints | none | later | existing `.cache/audit-inputs.json` is freshness metadata, not semantic cache |
| shadow workspace | missing | none verified | repo clone/worktree | none | later | no shadow execution layer exists |
| test feedback loop | partial | diagnostics + failure knowledge base | failing command output | JSON + summary | yes | no tight collect-run-feedback loop yet |

### Gap Analysis

- Reuse as-is:
  - audit registry and `Makefile` target model
  - JSON + summary output pattern
  - repo map, symbol index, targeted tests, endpoint packs, graphs, delta report, session handoff
- Wrap, do not rewrite:
  - any script already generating compact deterministic JSON
  - hotspot ranking, plan-code-map, symbol-to-test, dto-usage-pack, workflow-slice-pack
- Refactor/add centrally:
  - token budgeting across heterogeneous packs
  - one stable ContextPack shape
  - one collect/explain entrypoint
  - consistent provenance/fingerprint metadata
- Leave unchanged:
  - production backend/frontend code
  - existing static audit implementations
- Missing for first milestone:
  - parser-backed AST provider
  - Postgres schema/explain providers
  - Vue runtime provider
  - semantic cache
  - shadow workspace / automated feedback loop
- Too risky/large for this pass:
  - live DB integration
  - runtime browser/plugin capture
  - caching decision-recommendation outputs as truth
- Duplicate capability avoided:
  - gateway does not replace existing tools
  - it normalizes them and fills only the orchestration gap

### Recommended Architecture

- Tool shape:
  - Ruby CLI wrapper plus Ruby module under `scripts/audits/`
- Placement:
  - implementation in `scripts/audits/codex_local_context_gateway.rb`
  - wrapper in `scripts/audits/codex-context.rb`
  - root `Makefile` targets for `codex-context`, `codex-context-explain`, `codex-context-clean`
- Output location:
  - `docs/generated/local-tooling/codex-context/`
  - reason: closest existing repo-native generated-artifact area and writable in this environment
- Config:
  - optional JSON config at `docs/generated/local-tooling/codex-context/config.json` supported by code defaults
- Token estimation:
  - deterministic `JSON.generate(payload).length / 4`
- Relevance:
  - hard-coded provider relevance by touched surface
  - always prioritize git diff, AST diff, and targeted tests
- Provenance/fingerprints:
  - SHA-256 fingerprints
  - provider name, generation timestamp, source commands, source files, source hashes

### ContextPack Schema Proposal

The implemented schema stays close to the requested conceptual model and uses JSON because the repo already relies heavily on JSON local-audit outputs:

- `id`
- `kind`
- `scope`
- `confidence`
- `relevance`
- `estimatedTokens`
- `mode`
- `fingerprint`
- `provenance`
- `payload`

Additional top-level gateway payload fields:

- `schemaVersion`
- `generatedAt`
- `topic`
- `budgetTokens`
- `includedPackCount`
- `excludedPacks`
- `providerFailures`

### Provider Design

- Direct providers implemented now:
  - `git-diff`
  - `ast-diff` heuristic symbol isolation
- Existing-tool wrapper providers implemented now:
  - `recommend-targeted-tests`
  - `endpoint-contract-packs`
  - `audit-frontend-usage-graph`
  - `audit-backend-dependency-graph`
  - `validation-matrix`
  - `repo-map`
  - `symbol-index`
  - `codebase-capsule`
  - `session-handoff`
  - `link-symbol-to-tests`
  - `dto-usage-pack`
  - `workflow-slice-pack`
  - `rank-changeset-hotspots`
  - `plan-code-map`
  - `audit-delta-report`

### Composer / Token Budget Strategy

- Centralized in the gateway
- Starts every pack in `full`
- Downgrades non-required packs in this order:
  - `full -> compact`
  - `compact -> indexOnly`
  - exclude low-priority pack only if still over budget
- Required packs:
  - `git-diff`
  - `ast-diff`
  - `targeted-tests`
  - `intent` when provided

### Output Artifacts

- `docs/generated/local-tooling/codex-context/latest.machine.json`
- `docs/generated/local-tooling/codex-context/latest.human.md`
- `docs/generated/local-tooling/codex-context/latest.explain.md`
- `docs/generated/local-tooling/codex-context/packs/*.json`

### CLI / Invocation Design

- `make codex-context`
- `make codex-context mode=bugfix budget=3000 topic=<topic> files=<csv>`
- `make codex-context-explain`
- `make codex-context-clean`

Equivalent direct Ruby calls:

- `ruby scripts/audits/codex-context.rb collect ...`
- `ruby scripts/audits/codex-context.rb explain`
- `ruby scripts/audits/codex-context.rb clean`

### Postgres Integration Plan

Not implemented in this pass.

Follow-up shape:

- local-only schema snapshotter
- allowlist schema support
- denylist sensitive column names
- no row sampling
- focus touched tables plus FK depth 1

### Vue Runtime Integration Plan

Not implemented in this pass.

Follow-up shape:

- dev-only bridge/plugin
- route and component snapshot only
- store shape sanitization
- no auth tokens, user objects, cookies, or storage dumps

### Risks And Mitigations

- Stale wrapped outputs:
  - mitigate by reusing only static packs by default and refreshing scoped packs
- Token estimate drift:
  - mitigate by deterministic byte-length heuristic and mode downgrade
- Secrets in future runtime/DB providers:
  - mitigate by deferring those providers and documenting denylist requirements now
- Duplicate logic:
  - mitigate by wrapping existing scripts instead of cloning them
- Fragile AST parsing:
  - mitigate by labelling current implementation as heuristic and recording parser-backed follow-up work
- Generated artifact churn:
  - mitigate by keeping gateway outputs in one dedicated generated subtree and ignoring them in git
- Wrong scope when no changed files:
  - mitigate by fallback topic inference and clear notes in git-diff pack

## Implementation Outline

### Recommended MVP

Smallest useful repo-native MVP, now implemented:

- central ContextPack JSON shape
- `collect`, `explain`, `clean`
- direct git diff pack
- heuristic AST diff pack
- wrappers around existing repo map, symbol index, targeted tests, validation matrix, endpoint packs, graphs, session handoff, hotspots, plan-code-map, and delta report
- centralized token budget composer
- machine + human outputs in one location

### Deferred Milestones

- Milestone 2:
  - parser-backed AST provider
  - richer code distillation per Java/Vue symbol type
- Milestone 3:
  - Postgres schema provider
  - Postgres explain provider
- Milestone 4:
  - Vue runtime bridge
  - targeted validation feedback loop
- Milestone 5:
  - safe fingerprinted semantic cache
  - session checkpoint diff

## Master Plan

### Child Plans

- [x] Core: `.agents/codex-context-gateway-core-plan.md`
- [x] Integration: `.agents/codex-context-gateway-integration-plan.md`
- [x] Closeout: `.agents/codex-context-gateway-closeout-plan.md`

### Execution Sequence

1. Build the gateway shell and central composer.
2. Integrate existing local audits through provider wrappers.
3. Refresh registry/docs, generate gateway outputs, and record deferred follow-up work.

## Completion Check

- [x] Analysis context file contains original instructions, analysis, implementation outline, and master-plan references.
- [x] Gateway implementation is completed through sequenced child plans.
- [x] Generated context outputs are reproducible and provenance-backed.
- [x] Repo docs and tooling inventory are updated if conventions or capabilities changed.
- [x] Validation evidence is recorded in this plan before closeout.

## Completion Evidence

- Status: complete
- Changed files:
  - `.agents/codex-local-context-gateway-analysis-context.md`
  - `.agents/codex-local-context-gateway-master-plan.md`
  - `.agents/codex-context-gateway-core-plan.md`
  - `.agents/codex-context-gateway-integration-plan.md`
  - `.agents/codex-context-gateway-closeout-plan.md`
  - `scripts/audits/codex_local_context_gateway.rb`
  - `scripts/audits/codex-context.rb`
  - `scripts/audits/local_tooling_extended_tools.rb`
  - `scripts/local_tooling_common.rb`
  - `Makefile`
  - `.gitignore`
  - `docs/tooling/codex-local-audits.yml`
  - `docs/codex-local-tooling-todo.md`
- Validation evidence:
  - `ruby -c scripts/audits/codex_local_context_gateway.rb`
  - `ruby -c scripts/audits/codex-context.rb`
  - `ruby -c scripts/local_tooling_common.rb`
  - `ruby -c scripts/audits/local_tooling_extended_tools.rb`
  - `make codex-context`
  - `make codex-context-explain`
  - `make generate-audit-registry-artifacts`
  - `make audit-summary-index`
- Doc delta summary:
  - Added one repo-native gateway over the existing audit stack instead of creating a parallel toolchain.
  - Recorded the writable-output fallback from `.codex/` to `docs/generated/local-tooling/codex-context/`.
  - Updated the audit registry and tooling backlog to reflect the new capability and deferred next phases.
- Backlog update:
  - Added `CODEX-LOCAL-CONTEXT-GATEWAY-MVP`
  - Added deferred follow-up items for parser-backed AST, Postgres schema, and Vue runtime providers
- Residual risk:
  - The current AST pack is heuristic.
  - Wrapped static packs can still become stale if their source audits have not been regenerated recently.

## Deferred Follow-Up IDs

- `AGENT-FEATURE-WORKFLOW-CANONICAL-DOC`
- `AGENT-CONTEXT-CHANGESET-SNAPSHOT`
- `AGENT-JAVA-PARSER-AST-DIFF`
- `AGENT-CONTEXT-RELEVANCE-SCORER-V2`
- `AGENT-DOC-SYNC-REQUIRED-SURFACE-RESOLVER`
- `AGENT-VALIDATION-EVIDENCE-AUTOFILL`
