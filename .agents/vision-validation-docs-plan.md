# Vision Validation And Documentation Plan

## Status

Planning only. Do not implement until explicitly requested.

## Purpose

Keep the new vision architecture testable, documented, and aligned across backend code, API contracts, frontend behavior, and agent safety artifacts.

## Documentation Surfaces

Update when behavior changes:
- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/product-vision.md` only for stable direction changes
- `docs/product-memory.md` only for proven lessons
- `docs/implementation-backlog.md` for open work
- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml`
- `docs/agent-operating-model/sections/*`
- domain README files under backend package roots

## Agent Model Updates

Required when adding:
- new `/vision` endpoints
- new intents
- new execution capability
- new clarification workflow
- new safety class
- new DTO that participates in executor or canvas behavior

The agent model must state:
- endpoint ID
- handler method
- DTOs
- source-of-truth files
- intent ID
- risk group
- target resolution requirements
- destructive or multi-actor policies
- whether execution is planning-only or mutation-capable

## Test Layers

Backend:
- service tests for orchestration
- adapter tests for each executor
- controller tests for auth and validation
- agent operating model validation
- domain use case tests where execution behavior changes

Frontend:
- generated contract freshness
- type-check
- build
- later: component tests for block rendering
- later: screenshot checks for key canvas states

Generated artifacts:
- frontend contracts
- endpoint inventory
- endpoint contract packs
- repo map and symbol index when frontend surfaces are renamed or removed
- agent operating model sections when machine-operational rules change

## Required Commands By Change Type

Planning-only docs:
- no runtime validation required unless docs reference protected agent-operating rules

Backend orchestration without endpoint:
- `./mvnw test -Dtest=<new service tests>`
- `./mvnw test` when shared agent services change

New endpoint or DTO:
- `./mvnw test`
- `npm run generate:contracts`
- `npm run type-check`
- `npm run build`
- regenerate endpoint artifacts

Frontend canvas change:
- `npm run type-check`
- `npm run build`

Agent safety or executor change:
- `./mvnw test`
- regenerate agent operating model artifacts
- run agent operating model validation

## Feature Manifest Guidance

The future vision executor work should usually be manifest-backed because it will touch:
- backend behavior
- API contract
- frontend rendering
- agent safety rules
- generated artifacts
- docs

Use a manifest for:
- first conversation orchestrator
- new `/vision` API
- first real executor
- any destructive/multi-actor capability
- broad frontend canvas rewrite

## Closeout Criteria

A future implementation phase is not complete unless:
- tests pass for the changed layer
- generated contracts are fresh
- docs reflect behavior
- agent model reflects intents and safety rules
- frontend does not infer backend workflow rules
- execution remains fail-closed for missing/ambiguous data

## Long-Term Governance

Every new vision capability should answer:
- What can the user ask?
- What intent does it map to?
- What slots are required?
- What can be asked one step at a time?
- What confirms execution?
- What blocks execution?
- Which domain use case performs the mutation?
- What canvas blocks should the frontend render?
- Which tests prove this?
