# Example Scenario Library

This file gives compact implementation patterns for common changes. It is not a replacement for the living docs or templates; use it to avoid rediscovering the expected sequence from many prior changes.

## Add An Endpoint

1. Add or update the backend DTO first, keeping validation annotations and service-side validation in the backend.
2. Add the controller method as a thin adapter and route behavior through the owning service.
3. Add or update service tests for permissions, validation, and the happy path.
4. Regenerate endpoint or frontend contract artifacts when the endpoint is agent-visible or frontend-consumed.
5. Update `docs/domain-technical.md`; update `docs/business-logic.md` when behavior changes; update `docs/agent-operating-model.md` when automation can call or reason about the endpoint.

## Change A Workflow Transition

1. Locate the service that owns the state transition and update the rule there before frontend surfaces.
2. Add positive and negative tests for the old and new transition conditions.
3. Review sibling read surfaces that expose actions, state labels, notification destinations, or visibility flags.
4. Update workflow/state documentation, permission notes, generated artifacts, and agent-safety rules in the same change.
5. Record a doc delta summary that names behavior changed, docs updated, and intentionally unchanged related surfaces.

## Add A DTO Contract

1. Define the backend DTO fields, nullability, allowed values, and viewer-dependent visibility.
2. Assemble the DTO in one service-level path per viewer role or use case instead of duplicating mapper logic.
3. Add mapper or service tests for required fields and redaction rules.
4. Regenerate frontend contracts when the DTO crosses the frontend boundary.
5. Register automation-relevant DTOs in source-of-truth and documentation coverage before treating the change as complete.

## Add A Schema Migration

1. Add a new Flyway migration; do not edit old migrations.
2. Update the entity, repository query, service validation, and tests in the same slice.
3. Document new constraints, defaults, data compatibility assumptions, and domain meaning.
4. Regenerate backend audit or source-of-truth artifacts when schema-visible code surfaces change.
5. Run backend tests and migration-adjacent validation before closing the plan.

## Update Documentation

1. Start with the behavior or contract that changed; do not add aspirational notes.
2. Update `docs/business-logic.md` first when user-facing meaning changes.
3. Mirror technical details in `docs/domain-technical.md`, then update agent artifacts if automation behavior changed.
4. Use the matching `.agents/templates/docs/` template for new workflows, endpoints, DTO contracts, modules, permission rules, or schema migrations.
5. Run `ruby scripts/todo-audit.rb`, `make audit-documentation`, and any affected generated-artifact validation before closeout.
