# Core Product Stabilization and Business/Rides Deep-Dive

## Decision

The master plan is appropriate as a P0 maintenance program, but it must remain a
residual-hardening plan rather than a second implementation of already verified
repair and production-completion plans.

## Findings and changes

1. Existing repair, Business landing, Business Calendar, Rides UI, and Rides
   production plans are now explicit baseline inputs. Their completion claims are
   not copied as new work.
2. The core child reconciles one product-level denominator covering authentication,
   shell, discovery, domain action, authoritative readback, notifications/activity,
   visibility, and recovery, while marking already verified standalone surfaces as
   baseline-only. New tests are limited to affected integrations and explicit retest
   triggers.
3. Business hardening is constrained to effective schedules and exceptions, timezone,
   capacity/conflict, idempotency, stale state, owner/customer action parity,
   calendar focus, audit/side-effect classification, and fresh runtime evidence.
4. Rides hardening is constrained to circle scope, viewer privacy, capacity
   concurrency, join/leave and lifecycle transitions, commute preference separation,
   stale recovery, owner/participant parity, and Web/Vision evidence.
5. The runtime child now requires a fresh runtime artifact and manual local runtime
   execution; builds and static audits cannot close it alone.
6. Core, Business, and Rides implementation tasks declare concrete regression test
   paths so a task cannot close by changing only a matrix or backlog entry.

## Final scope-control audit

The master and all six child plans now explicitly reference
`docs/plan-scope-control-standard.yaml`. Each plan declares reused verified plans,
baseline-only surfaces, residual scope, retest triggers, and a non-duplication rule.
The child scopes are narrower than the master scope and preserve the same trigger
taxonomy, so a later goal-pursuing pass can distinguish historical evidence from
new repairs and new runtime evidence.

The plan is therefore prepared for goal pursuing, with one important execution
boundary: the first child must reconcile the denominator and residual gaps before
any implementation child changes code. No standalone Login, Chat, Work, or basic
notification audit is authorized unless a declared trigger is present.

## Sequencing rationale

Core journey definition comes first because it establishes the user-level acceptance
denominator. Core reliability follows because Business and Rides depend on identity,
visibility, action routing, stale-state handling, and readback. Business and Rides
then harden independently. Runtime/regression evidence runs after both modules, and
closeout reconciles all statuses last.

## Completion boundary

The plan may close repaired and evidenced Web behavior while keeping provider,
database-failure, process-crash, production-operations, and native/device limits
explicit. It does not authorize new product features or claim that source tests prove
concurrency, privacy, or production reliability.

## Goal-pursuing readiness

The plan is ready after YAML, docs, truth, recursion, and plan coverage audits pass.
The first task is the core journey denominator; no Business or Rides implementation
should start before that matrix is verified.
