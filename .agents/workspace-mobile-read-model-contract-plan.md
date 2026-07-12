---
machine_kind: plan
machine_status: draft
machine_closeout_contract: 2
machine_baseline_ref: 0cf598d
machine_delivery_mode: implementation
machine_title: Workspace And Mobile Read Model Contract Plan
machine_goal: Provide viewer-specific backend read models and actions that let workspace, phone, and Watch clients render correct state without frontend business logic.
---

# Workspace And Mobile Read Model Contract Plan

## Status

Draft. API work begins only from concrete gaps discovered by Plans 1, 4, and 5.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: prioritized viewer-specific DTOs, sections, actions, pagination, public business discovery, availability, booking, and generated client contracts.
- Out of scope: a repository-wide DTO cleanup and broad domain-service rewrite.
- Manifest decision: required
- Manifest path: .agents/feature-manifests/workspace-mobile-read-model-contract-manifest.yaml
- Master plan: .agents/vision-adaptive-mobile-first-master-plan.md
- Depends on: Plans 1, 4, and 5

## Current Findings

- API drift audit scans 211 DTOs and reports 14 missing contracts; the plan must prioritize only route-required contracts.
- Existing workmarket and business services already assemble presentation DTOs, including owner calendar/booking data.
- Vision runtime context is backend assembled and is the correct pattern for client-ready state.

## Implementation Checkboxes

- [ ] Create a route-to-read-model gap matrix from Plan 5 and a device-state matrix from Plan 4.
- [ ] Add viewer-specific sections/actions for My Work, Find Work, chat, profile, business owner, public business, booking, and calendar only where existing DTOs are insufficient.
- [ ] Define cursor/pagination, loading/error/empty semantics, visibility, consent, and action availability in backend DTOs.
- [ ] Implement public-business discovery, offering, availability, guest booking, status, and coordination contracts with service-level permissions and validations.
- [ ] Regenerate frontend contracts and add API/domain tests for every added contract; remove any duplicated frontend derivation.

## Local Tools And Validation

- Read: business, chat, quest, dashboard, and Vision endpoint packs; presentation assemblers; controllers; generated frontend contracts.
- Run: `make audit-api-contract-drift`, `make audit-endpoint-callsite-linker`, `make audit-repository-fetch`, focused `./mvnw test`, `npm run validate:contracts`, `npm run type-check`, and `npm run build`.
- Acceptance: each migrated client surface renders backend-prepared sections/actions; mutations and permissions stay in services; public booking has no owner-only leakage.

## Risks And Boundaries

- Avoid “mobile DTOs” that duplicate the same viewer state with divergent rules; use one use-case DTO per viewer role.
- Audit sibling mapper/repository read surfaces whenever a lazy mapping or DTO assembly defect is fixed.
- Add schema migrations forward-only with JUnit coverage when the contract requires persisted state.

## Completion Evidence

- Status: draft
- Baseline ref: `0cf598d`
- Implemented code paths: TBD
- Changed files: TBD
- Validation evidence: TBD
- Doc delta summary: TBD
- Backlog update: TBD
- Residual risk: TBD
