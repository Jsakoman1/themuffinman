# Developer tooling quality next-pass analysis

Date: 2026-07-31
Baseline: a18b811e65001f68d174b42d1574f6c0f2ffc18d

## Decision

Run one bounded follow-up program on the verified IntelliJ/local-tool optimization.
The new program repairs current health and freshness defects, adds two missing
deterministic audits, pilots a shared browser-evidence harness, and makes IntelliJ
run configurations repository-visible. It does not create another control system.

## Verified baseline reused

- docs/work/developer-tooling-intellij-mcp-master.yaml is verified.
- The local catalog contains 67 referenced tools and no unreferenced tools.
- Tool-mechanics self-test passes.
- Context search, compact repository-map queries, changed-path validation routing,
  and IntelliJ/local fallback routing are already verified.
- Work-plan verifier, System Map ownership, capability state, and runtime state stay
  unchanged and are not re-proven by this program.

## Finding 1: product-contract health is red because the assertion is stale

The full tool self-test reaches the modern-surface product stage and fails only
offer-work discoverability. The router already contains work/offer and the shell
definition exposes Post a SideJob at that route. The validator still searches for
the retired label Offer work.

Repair only the validator assertion to match the current SideJobs language. Do not
change the route, shell action, product behavior, or previously verified UI evidence.

## Finding 2: agent templates are useful but stale

Five documentation templates point to docs/agent-operating-model.md, which does not
exist; the canonical source is docs/agent-operating-model.yaml and its sections. The
validation-evidence template points to a retired docs/generated audit path.

Add a template-freshness audit that parses repository path references, rejects
known-retired canonical paths, and verifies the current work-plan/verifier language.
Repair all affected templates in the same atomic task.

The changed-path router currently rejects .agents paths. The template task must add
that repository-owned prefix and classify it as documentation/control tooling so the
new audit is discoverable through the normal validation-selection path.

## Finding 3: self-test output is correct but noisy

Tooling-only health prints one identical ruby_syntax line per Ruby file and one
frontend_script_syntax line per frontend script. This is useful on failure but wastes
context on success.

Aggregate successful repeated stages into one count. Preserve exact failing command,
file, class, and stage. Keep verbose output as an explicit option, and add a
machine-readable JSON summary only if it can reuse the same execution result.

## Finding 4: work artifacts need category-aware schema checks

The work directory contains legitimate non-plan artifacts, but also multiple spellings
of execution-inventory kinds and several status vocabularies. A universal status enum
would be wrong. Add a machine-readable schema contract keyed by artifact kind and an
audit that distinguishes work plans, masters, inventories, analysis registries, and
domain contracts.

The first pass reports and rejects only structurally unsafe ambiguity: missing kind,
unknown plan status, invalid inventory spelling for new files, or task/inventory shape
errors. It must grandfather explicitly listed historical variants rather than rewrite
verified evidence.

## Finding 5: runtime evidence repeats infrastructure code

Multiple Playwright scripts repeat browser launch, base URLs, seeded authentication,
localStorage setup, page-error collection, viewport definitions, overflow checks,
JSON writing, and browser cleanup. A shared runtime harness has high value, but a
wholesale migration would be risky.

Create a side-effect-light helper with pure configuration and serialization functions,
plus browser/session helpers. Verify pure behavior without a running stack. Then migrate
only brand-header-runtime.mjs as the pilot and capture fresh desktop/mobile evidence.
Other runtime scripts remain baseline-only and become follow-up candidates after the
pilot proves equivalence.

## Finding 6: IntelliJ MCP breadth is high but project readiness is low

IDEA exposes 55 tools but has no shared run configurations, and symbol search currently
does not resolve WorkspaceNavigationService. Repository code cannot repair an IDE
index deterministically.

Add shared .run configurations for backend start/test and frontend dev/type-check/build,
plus XML/routing audit coverage. Re-probe the known symbol and run-configuration list
at closeout. Record success or fallback; do not claim the index is repaired when the
IDE still returns no symbol.

The changed-path router also rejects .run paths. The IntelliJ task must add that prefix
and route it to the IntelliJ routing audit without broad product validation.

## Skill decision

Do not create a new skill in this program. The proposed themuffinman-runtime-evidence
skill only becomes useful after the shared harness pilot is verified. The installed
review-agent skill is not exposed in the current session; that is an environment/plugin
availability issue, not a repository implementation task.

## Ranked delivery sequence

1. verifier-harden the master inventory;
2. repair the stale modern-surface assertion;
3. repair templates and add freshness enforcement;
4. compact self-test success output;
5. add category-aware work-artifact schema enforcement;
6. add the shared runtime harness;
7. migrate and runtime-verify the brand-header pilot;
8. add shared IntelliJ run configurations and audit them;
9. reconcile measurements, follow-ups, and final control evidence.

## Explicit exclusions

- no product route, permission, API, schema, or domain change;
- no rewrite of verified plans or historical evidence;
- no migration of all Playwright scripts;
- no replacement of local AST/audits with MCP-only behavior;
- no claim that IntelliJ indexing is fixed without an observed IDE result;
- no new skill or plugin installation;
- no commit, push, deployment, database mutation, or external site action.

## Plan-by-plan readiness review

### Health plan

The validator repair is one-file and preserves the current route/action implementation.
The template task owns every stale template, its new audit, Make exposure, self-test
integration, and .agents changed-path classification. The self-test compaction follows
separately so success-output changes cannot hide template-audit behavior.

Selected health paths are already modified in the working tree. Every task must read
the then-current content after verifier start and apply a narrow merge.

### Control plan

The work-artifact schema is category-aware and explicitly separates executable plans
from registries and contracts. It may recognize documented historical variants but may
not rewrite verified evidence or infer completion from a normalized status.

### Runtime plan

Pure harness behavior is verified before browser integration. Only the brand-header
script migrates in this program. Its existing screenshots and JSON are baseline files;
the serial task requires all three to change after its own start snapshot through an
owned browser run.

### IntelliJ plan

Shared run XML is source configuration, not execution proof. The task validates XML,
expected commands, working directories, and .run changed-path routing locally. Closeout
records the independent IDEA discovery and symbol-resolution observation.

### Closeout plan

Closeout changes only the dated artifact, this analysis, and the persistent backlog.
The backlog is already modified, so the task must append stable follow-up IDs without
reformatting or replacing unrelated entries.

## Dependency and leaf-command review

- All nine items form one contiguous dependency chain.
- Every task maps once to its child plan and inventory item.
- Every required path is exact; future files first appear in their owning task.
- No leaf command invokes the work verifier recursively.
- The runtime task alone requires browser/visual evidence.
- .agents and .run paths are explicitly added to changed-path routing by their owning tasks.
- Full tool health is required only after the stale product assertion is repaired.

## Closeout measurements

The completed follow-up pass restores the full self-test: 16 stages pass, including
four separately labeled product-contract stages. The tooling-only mode now emits 13
lines with aggregate `files=` counts, while `--verbose` preserves individual commands.

The new audits pass for seven agent templates and 180 work artifacts. The brand-header
pilot produced fresh Chromium desktop/mobile screenshots and JSON evidence; both
viewports have visible header/logo, no horizontal overflow, and no page errors.

IntelliJ MCP independently discovered all five shared run configurations. Its symbol
probe for `WorkspaceNavigationService` still returned no matches, so the documented
repository-map/context-search fallback remains mandatory rather than claiming an IDE
index repair. Future runtime-script migrations and a conditional runtime-evidence
skill are recorded as stable backlog items.
