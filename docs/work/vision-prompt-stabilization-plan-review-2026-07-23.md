# Vision prompt stabilization — in-depth plan review

Date: 2026-07-23  
Reviewed baseline: `dcb983f4ad2b0354101caa8c1cdc47c624fb8b71`  
Reviewed plan: `docs/work/vision-prompt-stabilization-master.yaml`

## Executive assessment

The plan has the correct high-level decomposition and correctly keeps the work serial: contract first, read routing second, mutation continuity third, runtime reliability fourth, browser closeout last. It also distinguishes prior verified evidence from new residual scope.

It is not yet fully goal-pursuing-ready without a short plan-hardening pass. The main issue is not missing intent to repair; it is that several acceptance statements are broader than the paths and evidence declared for them. If started unchanged, the verifier could prove tests and a partial browser trace while still leaving important prompt families, provider boundaries, or auth/persistence defects unresolved.

Readiness: `prepared with required corrections`, not blocked.

## Cross-plan coverage matrix

| Problem family | Master | Child 01 | Child 02 | Child 03 | Child 04 | Child 05 | Assessment |
|---|---:|---:|---:|---:|---:|---:|---|
| `SEND_MESSAGE` versus `OPEN_CHAT` drift | yes | yes | no | partial | no | yes | Must include route catalog and router in required paths, not only tests. |
| `create new work` fallback/routing | yes | yes | no | no | no | yes | Covered, but provider-only versus development fallback policy must be explicit. |
| `go to circles` navigation-only | yes | yes | yes | no | no | yes | Covered, but exact navigation action/readback must be typed and browser-asserted. |
| `open my circles` visible content | yes | partial | yes | no | no | yes | Needs read-model/DTO/renderer ownership named explicitly. |
| `open quest Suitcases` resolution | yes | partial | yes | no | no | yes | Needs fixture/data precondition and separate unique/no-match/ambiguous cases. |
| Generic `Hello, test.` output | yes | partial | yes | no | no | yes | Must name response DTO/state and renderer contract, not only the renderer. |
| Mocked semantic-plan false green | yes | yes | no | partial | no | yes | Needs a real provider-chain integration fixture, not only service tests. |
| Follow-up slot continuity | yes | no | no | yes | no | yes | Needs explicit prompt cases and conversation fixtures. |
| Multi-signal precedence | yes | yes | no | yes | no | yes | Should have a machine-readable matrix, not scattered test cases. |
| Entity aliases/normalization | yes | partial | yes | yes | no | yes | Needs exact normalization owner and acceptance examples. |
| Provider failure/retry | yes | no | no | no | yes | yes | Overlaps the already pending runtime matrix and must reuse its authoritative rows. |
| Duplicate preference key | yes | no | no | no | partial | no | Child 04 lacks the actual persistence source/test paths. |
| Earlier 401 | yes | no | no | no | partial | no | Root cause is not established; acceptance currently assumes a repair before diagnosis. |
| Multilingual boundary | yes | partial | no | partial | no | yes | Must align with the OpenAI-only production contract; do not accidentally promote local English rescue. |

## Master plan review

### What is strong

- The objective covers routing, fallback, resolution, continuity, renderer states, recovery, and browser behavior.
- The residual scope is separated from previously verified route and adapter work.
- The five-child order is sensible and avoids capturing browser evidence before source contracts are stable.
- The rules correctly require backend authority, explicit confirmation, safe navigation, typed recovery, and authoritative readback.
- The closeout correctly rejects static tests as a substitute for browser evidence.

### Required corrections

1. Add a canonical prompt coverage matrix path, for example `docs/vision-prompt-contract-matrix.yaml`. Every critical prompt family should have: examples, expected intent, capability id, required slots, provider fixture, development/test behavior, integration test, browser scenario, expected success state, no-match state, ambiguity state, and provider-failure state.
2. Resolve the architecture contradiction. The verified VisionForWeb contract says OpenAI is the only production semantic interpreter and the local parser is development/test-only. The new plan says “deterministic safe fallback” without consistently saying that it is test/dev-only. The master must explicitly prohibit promoting local emergency routing as production behavior.
3. Add the existing authoritative runtime matrix rows to the runtime child scope: `runtime-vision-execution`, `runtime-mutation-provider-failure-correlation`, `runtime-read-vision-context`, and, where relevant, `runtime-visibility-web-vision-parity`. The new plan must update those rows or explicitly state that it only creates linked evidence and does not change their status.
4. Add a plan-level requirement that the expanded browser harness must be extended before closeout. The current script only runs the five original prompts and its `visionPromptContract` scenario says backend tests are enough; that statement must not remain as acceptance evidence for prompt runtime behavior.
5. Define the exact runtime denominator. “Expanded prompt matrix” is too vague for a strict plan. The plan needs a finite list of prompt scenarios and a pass/fail rule for each.
6. Add the change-impact report to the review artifact itself, including disposition for recommendations. The master currently references an existing report but does not record which recommendations were accepted, deferred, or not applicable for this new prompt slice.

## Execution inventory review

The inventory is structurally correct: one item per child, ordered 1–5, and each child points back to the same master. `make audit-plan-coverage` and recursion validation pass.

The inventory is still too coarse for a high-risk prompt program. Each child contains one large task spanning multiple behavior families. This is acceptable only if the task remains atomic at the verifier level. In practice, Child 01 combines contract drift, aliases, fallback status, and matrix generation; Child 04 combines provider failure, retry, auth, persistence, and seed reliability. A failure in one sub-area would block the whole child, but the evidence would not reveal which sub-area was actually completed.

Recommended hardening: keep the five serial phases, but add explicit subtasks or acceptance sub-items with one evidence owner each. Do not create a second execution inventory; keep the existing inventory authoritative.

## Child 01 — contract and fallback

### Intended responsibility

This is the foundation slice. It should define the difference between `SEND_MESSAGE` and `OPEN_CHAT`, add product language aliases, reconcile the semantic route catalog, and make fallback/provider status explicit.

### Covered well

- It names router, signal support, prompt semantics, audit tests, and regression catalog.
- It makes the requested `send message` behavior an explicit acceptance rule.
- It recognizes that fallback behavior needs a typed distinction rather than a generic unsupported result.

### Gaps

- `VisionSemanticRouteCatalogService.java` is listed under `paths` but not under `required_paths`. A route-contract repair could therefore pass without changing the route catalog.
- `VisionSemanticContractSanitizer.java`, `VisionSemanticResponseValidator.java`, and `VisionPromptUnderstandingService.java` are not declared even though provider output and fallback status are part of the acceptance criteria.
- The plan does not say whether `create new work` and `go to circles` are production aliases or development/test fixture aliases. This matters because the existing architecture explicitly prohibits local production classification.
- The “provider unavailable” state cannot be implemented only in `VisionIntentRouter` and `PromptSemanticsSupport`; it likely requires the understanding result, conversation response DTO, and renderer state contract.
- The prompt matrix is described in prose but no matrix artifact is declared.

### Recommended acceptance additions

- `send message to Josip` → `SEND_MESSAGE`; `open chat with Josip` → `OPEN_CHAT`; `show chat` → `VIEW_CHAT_WORKSPACE` across provider fixture and dev-only local fixture.
- `create new work` → `CREATE_QUEST` or the canonical product intent selected by the route catalog; no mutation before confirmation.
- `go to circles` → navigation-only action; `open my circles` → viewer-scoped read action.
- OpenAI unavailable → `PROVIDER_UNAVAILABLE`, never local production classification and never `UNSUPPORTED`.

## Child 02 — read routing and resolution

### Intended responsibility

This slice should turn prompt recognition into visible, typed results for quest and circle reads, including no-match and ambiguity behavior.

### Covered well

- It keeps entity resolution and backend read ownership in scope.
- It explicitly separates visible success from no-match/ambiguity.
- It includes the renderer, which is necessary because the current runtime only proves the shell.

### Gaps

- `VisionConversationReadModelAssembler.java`, `VisionConversationTurnResponseDTO.java`, and any generated frontend contract are missing from the declared paths, although typed visible outcomes are part of the acceptance criteria.
- `VisionCapabilityPreviewService` and the quest/circle read services are not named, even though they own the actual target/read model behavior.
- “Suitcases” requires a known visible fixture. The plan needs a fixture setup or deterministic seed requirement; otherwise a no-match result could be correct while the requested success scenario remains unproven.
- It does not explicitly test unauthorized or stale quest targets, which are essential because title resolution must not leak inaccessible data.
- `go to circles` and `open my circles` may share `/circles`, but the acceptance must assert different backend action types, not only the final URL.

### Recommended acceptance additions

- Unique visible quest title → detail preview/action with authorized target id.
- Missing title → typed no-match with retry/alternative action.
- Multiple title matches → clarification with privacy-safe candidate labels.
- Unauthorized/stale title → no-match or safe unavailable result, never leaked detail.
- Navigation-only circles → no circles content query or mutation; viewer-scoped circles → content query and readback.

## Child 03 — mutation and continuity

### Intended responsibility

This slice should complete the direct-message flow and make multi-turn slot collection reliable across Vision mutation families.

### Covered well

- It reuses the existing chat execution boundary instead of introducing a second chat API.
- It requires recipient/body separation, review, confirmation, and final readback.
- It includes sanitizer coverage and conversation tests.

### Gaps

- The root semantic selection defect is handled in Child 01, but Child 03 does not explicitly depend on the changed route/router contract in its required paths. The master order implies dependency, but acceptance should state that it consumes Child 01’s verified `SEND_MESSAGE` contract.
- `VisionCanvasAssembler` and `VisionCanvasRenderer` are absent even though the review UI and “Confirm and send” action are part of the product flow.
- The plan does not require idempotency/replay coverage for a repeated confirmation or browser retry.
- It does not list recipient-resolution variants: exact username, display name, duplicate candidates, inaccessible user, and unknown user.
- It does not include “send message to Nikolina” with message body already present versus body supplied in a second turn.

### Recommended acceptance additions

- `send message to Nikolina` asks for body and never sends immediately.
- `send message to Nikolina saying ...` extracts both slots, shows review, and sends only after confirmation.
- Duplicate Nikolina candidates clarify without leaking private contact data.
- Unknown/inaccessible recipient produces recoverable no-match.
- Duplicate confirm/retry produces one message and one authoritative readback.

## Child 04 — runtime recovery and auth

### Intended responsibility

This slice should capture real runtime evidence for provider failure/retry and investigate the earlier preference-uniqueness and 401 findings.

### Covered well

- It uses the owned `make dev`/`make dev-stop` lifecycle.
- It declares a fresh runtime evidence path.
- It requires provider outcome, browser/API status, and authoritative final state.

### Gaps and risk

- It declares only the runtime harness and documentation paths. It has no backend source or test paths for the preference upsert and no auth/session source paths for the 401 investigation. It can record a failure but cannot implement a repair under strict required-path rules.
- The acceptance assumes the duplicate preference and 401 must be repaired, but the prior evidence does not establish whether the 401 was caused by auth, a query error, or an expired disposable account. Diagnosis must precede the repair acceptance.
- The runtime command is a shell compound command containing `make dev`, node execution, and `make dev-stop`; the closeout behavior if the node process exits non-zero should be explicitly defined so the owned stack is always stopped.
- The child overlaps the already verified runtime-pending program. Without explicit row ownership, two plans could claim the same provider-failure evidence.
- It does not include database observation or server log capture needed to prove preference uniqueness behavior.

### Recommended acceptance additions

- Link this child to the authoritative runtime matrix rows and define whether it updates status or only produces diagnostic evidence.
- Add `VisionLearningService.java`, `VisionLearningServiceTest.java`, `VisionUserPreferenceRepository.java`, and the relevant auth/session or runtime fixture owner after root-cause diagnosis.
- Add a concurrent/upsert regression test if the duplicate key is a race; do not change the unique constraint without proving the domain invariant.
- Capture request id/correlation id, provider failure control, HTTP response, conversation id, retry idempotency, DB preference rows, and final conversation state.

## Child 05 — browser closeout

### Intended responsibility

This is the final acceptance slice: fresh Chromium evidence for the five requested prompts plus the expanded matrix, followed by documentation closeout.

### Covered well

- It requires fresh runtime evidence and preserves prior evidence as baseline.
- It includes the runtime script, evidence artifact, backlog, ledger, and regression catalog.
- It includes frontend type-check/build and runtime audits.

### Gaps

- The current script runs only five exact prompts plus route reachability. It does not yet implement the promised expanded matrix.
- The script classifies most non-chat/non-unsupported results as `captured`, which is not equivalent to a semantic success. It must assert intent, action type, visible state, route, target label/id safety, and final readback.
- The script does not capture API response bodies/statuses, correlation IDs, provider status, conversation ids, or database/readback evidence.
- The current `visionPromptContract` scenario explicitly treats backend tests as prompt confirmation; this must be removed or renamed as source-only evidence.
- The browser script does not assert that `send message` sends a message; it only detects a `/chat` handoff.
- There is no cleanup/fixture ownership for created conversations, messages, quests, or preference rows.

### Recommended expanded browser denominator

- Exact requested prompts: `open quest Suitcases`, `create new work`, `open my circles`, `go to circles`, `send message to Nikolina`.
- Read success/no-match/ambiguity: visible quest, missing quest, duplicate quest; circles success; inaccessible target.
- Chat: open chat, send with inline body, send with second-turn body, unknown recipient, ambiguous recipient, duplicate confirmation.
- Provider/runtime: provider unavailable, retry success, retry after partial failure, no duplicate mutation.
- Fallback boundary: development fixture labeled as synthetic; production-like provider path never claims local parser behavior.

## Dependency and sequencing review

The sequence is directionally correct, but the dependencies are implicit rather than machine-readable. At minimum:

- Child 02 depends on Child 01 for intent names, route action types, and fallback state.
- Child 03 depends on Child 01 for `SEND_MESSAGE` selection and on Child 02 for typed no-match/target resolution conventions.
- Child 04 depends on the response/recovery contract from Children 01–03.
- Child 05 depends on all previous children and must not begin with the current five-prompt harness unchanged.

The master should add these dependencies or a `depends_on` field to each child task. The execution inventory remains serial, but explicit dependency declarations make the plan auditable and prevent a future parallel runner from starting a child too early.

## Documentation and registry impact

The plans currently update backlog, ledger, and regression catalog, which is good. They also need to account for:

- `docs/runtime-acceptance-matrix.yaml` for authoritative runtime rows.
- `docs/capability-evidence-registry.yaml` for capability evidence reconciliation.
- `docs/vision-architecture-patterns.md` if the provider/fallback boundary changes.
- `docs/business-logic.md` if user-visible distinctions between unsupported, unavailable, no-match, and clarification change.
- `docs/domain-technical.md` if new intent, slot, state, or persistence invariants are introduced.
- `docs/agent-operating-model.yaml` only if synthetic/admin generation or automation-facing Vision behavior changes.

## Final recommendation

The plan should remain `draft` until the following are added:

1. A finite machine-readable prompt matrix and expanded browser denominator.
2. Explicit OpenAI-only production versus development-only deterministic fixture behavior.
3. Source paths and tests for preference/auth diagnosis in Child 04.
4. Typed response/DTO/read-model paths in Child 02.
5. Renderer and idempotency/readback coverage in Child 03.
6. Authoritative runtime matrix ownership and non-duplication rules.
7. Explicit child dependencies and a safer browser harness that asserts semantic outcomes, not just route capture.

After those corrections, the master is suitable for goal pursuing. No implementation should be marked complete from the current partial Chromium artifact alone.
