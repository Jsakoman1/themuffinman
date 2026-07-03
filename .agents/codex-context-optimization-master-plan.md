---
machine_kind: master-plan
machine_status: unknown
machine_title: Codex Context Optimization Master Plan
---

# Codex Context Optimization Master Plan

Purpose: coordinate the sequential implementation of shared changeset snapshotting, parser-backed Java AST isolation, relevance scoring upgrades, doc-sync resolution, validation evidence autofill, and one canonical feature-delivery workflow document.

## Backlog IDs In Scope

- AGENT-CONTEXT-CHANGESET-SNAPSHOT
- AGENT-JAVA-PARSER-AST-DIFF
- AGENT-CONTEXT-RELEVANCE-SCORER-V2
- AGENT-DOC-SYNC-REQUIRED-SURFACE-RESOLVER
- AGENT-VALIDATION-EVIDENCE-AUTOFILL
- AGENT-FEATURE-WORKFLOW-CANONICAL-DOC

## Child Plans

- `.agents/codex-context-optimization-core-plan.md`
- `.agents/codex-context-optimization-closeout-plan.md`

## Execution Order

- [x] 01 Core gateway and local-tooling optimization: `.agents/codex-context-optimization-core-plan.md`
- [x] 02 Closeout flow, documentation workflow, and backlog cleanup: `.agents/codex-context-optimization-closeout-plan.md`

## Scope

- [x] Reuse the existing gateway and local-tooling stack instead of adding a parallel framework.
- [x] Centralize reusable changeset state for gateway providers and sibling audits.
- [x] Upgrade Java changed-symbol extraction to parser-backed output.
- [x] Improve pack relevance decisions using deterministic evidence from changes, categories, domains, and linked artifacts.
- [x] Add deterministic doc-sync required-surface output and a machine-readable report.
- [x] Add validation evidence autofill helpers that fit the manifest workflow.
- [x] Add one maintained canonical feature-delivery workflow document under `docs/`.

## Completion Check

- [x] Child plans are completed in sequence.
- [x] The gateway still produces stable machine and human outputs.
- [x] New doc-sync and validation helpers are exposed through existing local-tooling entrypoints.
- [x] Living documentation is updated to reflect the new workflow and helper tooling.
- [x] Resolved backlog IDs are removed from the persistent backlog.
- [x] Closeout audits pass for the completed plan/manifest state.

## Completion Evidence

- Status: complete
- Manifest: `.agents/feature-manifests/codex-context-optimization-manifest.yaml`
- Validation evidence:
  - `make generate-audit-registry-artifacts`
  - `make audit-doc-sync-required-surfaces`
  - `make codex-context`
  - `make codex-context-explain`
  - `make generate-agent-operating-model`
  - `make generate-agent-artifacts`
  - `make audit-agent-safety`
  - `make audit-validation-evidence-quality`
  - `make audit-todo`
  - `make audit-plan-completion plan=.agents/codex-context-optimization-master-plan.md manifest=.agents/feature-manifests/codex-context-optimization-manifest.yaml`
