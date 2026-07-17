# Interaction and action-system child analysis

The shared `AppCard` and `AppActionMenu` primitives exist, but adoption is incomplete. The main gap is data: metrics and rows often have no `to`, while the template still presents them as cards or action-bearing rows. The implementation must define an explicit actionable/informational contract and remove duplicate card/title/Open activation. Nested controls must stop parent activation and retain keyboard semantics.
