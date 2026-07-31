# Developer tooling and IntelliJ MCP optimization analysis

Date: 2026-07-31  
Baseline: `a18b811e65001f68d174b42d1574f6c0f2ffc18d`

## Decision

Keep repository-owned tools as the deterministic validation and evidence layer, and
use IntelliJ MCP as the semantic inspection layer. The two layers solve different
problems: local scripts must remain reproducible in a terminal and in the work-plan
verifier, while IDEA can resolve symbols, call relationships, inspections,
refactorings, and debugger state more precisely than text search.

The optimization target is a small hybrid workflow:

1. bounded local context discovery;
2. IDEA semantic inspection when a symbol or executable path is known;
3. changed-path validation routing;
4. repository-owned leaf validation and verifier evidence.

MCP availability is an optional accelerator, never a completion authority or a
required dependency of repository validation.

## Observed baseline

- The local catalog passes with 65 tools in five categories and zero unreferenced
  tools.
- `make context-search` already bounds files, lines, generated output, and overall
  output characters. It is the correct first-pass discovery tool.
- `repository-map.rb` builds a useful frontend AST, Java symbol inventory,
  capability map, and import graph. Its query mode still serializes the complete
  repository map before appending matches.
- A single query for `WorkspaceNavigationService` emitted approximately 1.04 MB of
  output even though the useful result was one backend match. This is the largest
  measured token-efficiency defect in the current workflow.
- `make tool-self-test` currently reaches the frontend product validators and fails
  on `offer work route is discoverable`. This is a real contract failure in the
  dirty working tree, but the current output does not distinguish tool-mechanics
  health from product-contract health.
- IntelliJ MCP exposes 55 tools covering symbols, call hierarchy, inspections,
  build/run configurations, refactoring, debugger, terminal, Git, and database
  access.
- IDEA correctly recognized the project and Java module. No IDE run configurations
  are currently defined.
- IDEA symbol search returned no result for the known
  `WorkspaceNavigationService` type. Semantic tools therefore need a documented
  fallback rather than being assumed universally available while indexing or
  project configuration is incomplete.

## Existing strengths to preserve

- `scripts/verify-work.rb` remains the sole implementation completion mechanism.
- `make control-check` remains the compact control closeout.
- System Map, truth, capability, runtime, and work-plan states remain separate.
- Local tools remain usable without IntelliJ, MCP, a GUI, or external services.
- Broad audits stay optional diagnostics; they are not moved into every small task.
- Existing verified System Map optimization evidence is reused as baseline and is
  not counted as new completion.

## Residual problems

### 1. Query output is structurally unbounded

`repository-map.rb --query` calculates a targeted match set but prints the entire
map. The plan must add a compact query result that includes only the query,
high-value summary counts, exact matches, and bounded relationship context. Full
map output remains available only through an explicit full/write mode.

### 2. Context search does not distinguish query intent

The current fixed-string ranking is useful for prose and exact names but cannot
prioritize definitions, callsites, API boundaries, or canonical documentation by
intent. A small explicit mode contract can improve ranking without attempting to
rebuild an IDE index in Ruby.

### 3. Validation selection is manual

The repository contains precise leaf commands but lacks one read-only command that
maps changed paths to the smallest safe validation set. The router must be advisory,
deterministic, explain each recommendation, and escalate shared/control changes to
broader checks. It must never set plan or capability status.

### 4. IDEA usage has no repository-owned routing contract

Agents can see the available IDEA tools, but the repository does not define when a
semantic tool should replace text search, when to fall back, or which local command
still supplies final evidence. This increases repeated discovery and makes accuracy
dependent on individual agent habits.

### 5. Tool health and product drift are conflated

The self-test is valuable, but its stages need labeled output and a clear boundary:
tool syntax/index/search smoke checks versus product contract validators. The plan
does not weaken either class; it makes failures attributable and preserves the
existing product checks.

## Target routing model

| Need | Primary path | Fallback | Completion evidence |
| --- | --- | --- | --- |
| Locate bounded context | local context search | `rg` with bounded paths | none; discovery only |
| Find a known symbol | IDEA symbol search/info | compact repository map | none; discovery only |
| Find real callers/callees | IDEA call hierarchy | local AST/callsite audits | relevant leaf test |
| Rename a resolved symbol | IDEA rename refactoring | bounded manual edit after usage audit | compile/type-check/test |
| Inspect edited files | IDEA file problems/lint | compiler/type-check | leaf validation |
| Debug runtime behavior | IDEA debugger/logpoints | owned runtime harness | runtime artifact when required |
| Select validation | changed-path validation router | System Map impact review | verifier-executed commands |

## Quantitative acceptance targets

- A repository-map symbol query returns no full frontend AST or global import-edge
  payload and stays under 20,000 characters for the standard fixture queries.
- Context modes produce deterministic bounded output under the caller-provided
  budget and identify why each result class is included.
- The validation router returns a deduplicated command set and rationale for backend,
  frontend, documentation/control, migration, and mixed fixtures.
- Tool self-test reports the failing stage and classifies it as tool mechanics or
  product contract without suppressing the failure.
- The IDEA routing contract documents semantic-first use, indexing/unavailable
  fallback, required project path, bounded result parameters, and local evidence
  authority.
- Closeout records before/after bytes, elapsed time, result count, fallback outcome,
  and validation-router fixtures. Token estimates may be reported as estimates only.

## Explicit exclusions

- No product feature, route, permission, API, schema, or runtime capability change.
- No attempt to invoke MCP tools from repository scripts.
- No replacement of Maven, npm, audit, or verifier commands with IDE success.
- No broad rewrite of existing audits into IntelliJ-dependent implementations.
- No claim that IDEA symbol resolution is complete until measured after indexing and
  project configuration are available.
- No repair of the current `offer work route` product-contract failure in this
  tooling program; it remains an external retest boundary unless the owning product
  change explicitly brings it into scope.

## Recommended sequence

1. verifier-harden the plan and inventory;
2. compact repository-map query output;
3. add intent-aware bounded context modes;
4. add the changed-path validation router;
5. define and audit the IntelliJ MCP routing contract;
6. make tool-self-test stages attributable;
7. update the compact workflow documentation;
8. measure the resulting workflow and close only on new evidence.

## Plan-by-plan readiness review

### Atomic hardening

The hardening plan owns planning readiness only. Its nine required paths are the
complete master package, and its leaf command validates the one-to-one inventory
mapping. It must be verifier-verified before any tooling source changes.

### Context optimization

The first context task changes only repository-map query serialization and its
self-test assertion. The second changes only context-search modes and the matching
self-test assertion. Their shared `scripts/tool-self-test.rb` path is intentional:
each serial task adds its own independently testable stage after a fresh start
snapshot. Neither task changes the generated AST meaning or canonical sources.

### Validation routing

The validation task creates one new advisory script and exposes it through the
Makefile. Its fixture mode is the leaf validation; it must not execute recommended
commands, inspect unbounded external state, or write plan status. Mixed fixtures
must deduplicate commands while retaining every rationale.

### IntelliJ adoption

The routing-contract task creates documentation, machine-readable routing data, and
one audit. The following self-test task consumes that audit and labels existing test
stages without removing product validators. The documentation task updates the two
existing workflow entry points only after implementation behavior exists.

`docs/implementation-control.md` is already modified in the working tree. Its later
task has a preserve-and-merge requirement: read the then-current file, add only the
hybrid workflow rule, and retain all unrelated user changes.

### Closeout

Closeout changes only the analysis and a new dated machine-readable artifact. It
measures new tooling behavior and records environment-dependent IDEA results. It
cannot promote product, capability, runtime, or release status.

## Dependency and command review

- All eight inventory items form one linear dependency chain with no skipped or
  parallel implementation item.
- Every implementation task has exact bounded required paths and a repository-root
  leaf command.
- Four required paths intentionally do not exist yet; each is created by its owning
  task and is not needed by an earlier validation.
- No validation invokes `work-verify` or `scripts/verify-work.rb` recursively.
- Shell pipelines in leaf validations inspect bounded generated output; they do not
  mutate product state.
- The final tooling-only self-test is distinct from the known failing product-contract
  stage, which stays visible in the closeout record.

## Measured outcome

The `WorkspaceNavigationService` repository-map fixture fell from approximately
1,041,493 bytes in the prior full-map query output to 921 bytes in the compact query
envelope: a 99.91% reduction. The compact response contains one backend match and no
global frontend AST or import graph. Its measured execution time was 1.817 seconds.

The `symbol` context fixture returned exactly its 2,048-byte budget in 0.038 seconds
and reports the selected mode and ranking rationale. The changed-path router passed
six deterministic fixtures without executing the recommended commands.

`ruby scripts/tool-self-test.rb --tooling-only` passed in 7.633 seconds. The complete
self-test now labels every stage; it remains non-zero at the distinct
`product_contract/modern_surface_contract` stage because the current workspace fails
the offer-work discoverability assertion. This program did not alter that product
contract.

IDEA `search_symbol` still returned no item for the known `WorkspaceNavigationService`.
The compact local fallback returned one backend match in 921 bytes, confirming that
the documented fallback path works when IDE indexing is unavailable or incomplete.
