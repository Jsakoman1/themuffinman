# Dora v1.7 Codex acceleration analysis

## Baseline

Dora v1.6 already provides a confirmed interview route, project creation, a
Spring/Vue/PostgreSQL/Flyway technical starter, deterministic compiled-feature
preview and apply mechanics, generated-output inspection, and independent static
and compile proof. It deliberately does not infer product rules, generate a real
authentication system, run external services, or treat generated code as accepted
product behavior.

## Residual friction for a Codex agent

1. The first route still exposes several artifacts and commands before an agent can
   confidently tell a beginner what to do next.
2. Confirmed domain knowledge is not yet compiled into an ordered dependency graph
   of capabilities, decisions, and implementation work.
3. v1.6 generates one new feature only. It needs a controlled route for relating a
   new feature to existing product conventions and previously generated features.
4. Generated source obligations are visible, but a Codex agent does not yet receive
   one compact, machine-readable implementation and proof packet for the exact task.
5. Runtime/browser evidence is intentionally generic; a product still has to invent
   how a declared capability becomes a reviewable runtime scenario.

## Decision

v1.7 should improve the agent workflow and safe feature evolution before adding
security, production, or deployment automation. This gives Codex less context to
assemble manually while preserving the rule that a product owner confirms meaning
and a consumer project owns real behavior and evidence.

## Deferred work

Real authentication/provider setup, backup/restore execution, production deployment,
offline synchronization, and hosted-environment adapters remain later opt-in work.
They affect credentials, real data, external systems, or security policy and must not
be bundled into a convenience generator.
