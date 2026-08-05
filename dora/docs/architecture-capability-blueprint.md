# Architecture and capability blueprint

Use one blueprint for one product capability before implementing it. It is a design and verification contract, not code generation and not proof that a feature is complete.

The service owner holds business rules, permission decisions, deterministic validation, and state transitions. The API boundary publishes typed commands and queries. Clients render prepared data, collect declared interactions, and do not recreate business decisions.

Record three different evidence types: static checks can inspect declared structure, tests can prove deterministic behavior, and runtime evidence can show a real user flow. None of those should be silently replaced by the others.

Keep product vocabulary in the consuming project's domain library. This Dora template intentionally uses neutral terms so it can guide a new application without copying MuffinMan concepts.
