# Round 23: Workflow, State, Permission, and Invariant Matrix

Status: source-trace analysis. Reviewed: 2026-07-22.

## Conclusion

Six workflow declarations exist in `workflow-state-machines.yaml`. Four are backed
by named current owners, one is a derived chat access model, and the booking entry
still declares `future_module` despite the implemented Business booking services.
This is a documentation ownership mismatch, not evidence that booking behavior is
absent. It must be reconciled before the workflow registry can be complete.

## Coverage classification

| Workflow | Declared owner | Implementation evidence | Classification |
|---|---|---|---|
| Quest lifecycle | `QuestStateTransitionService` | explicit state transitions | current declared workflow |
| Quest application | `QuestApplicationWorkflowSupport` | application status rules | current declared workflow |
| Circle relation | `CircleRelationService` | derived relationship state | current declared workflow |
| Chat access | `ChatService` | derived access based on membership/relations | derived access lifecycle |
| Thing borrow request | `ThingSharingService` | status-based sharing flow | current declared workflow |
| Booking lifecycle | `future_module` | booking service, offering lock, idempotency key, audit model | declared-owner drift |

## Concurrency and replay observations

- Business booking has offering-row locking and idempotency-key lookups.
- Ride offers use locking and optimistic versioning; repeated join confirmation is
  intentionally idempotent.
- Vision records a last client request ID and has replay-candidate logic for turns.
- These controls are local to selected flows. They do not establish one universal
  cross-module idempotency or correlation contract.

## Required disposition

The booking workflow owner needs reconciliation in the owning state-machine source
or a stable backlog item. Later rounds link workflows to endpoint consumers,
side-effects, tests, and runtime proof. This analysis does not promote any derived
access rule or local concurrency mechanism to a universal guarantee.
