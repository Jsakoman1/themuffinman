# Dora v1.4 preflight

The plan reuses Dora's current `IdeaInterview`, `VerticalSliceGenerator`,
`VerticalSliceReadiness`, `DecisionLog`, and runtime-trace contracts. It does not
replace or migrate a consumer package.

The only planned local side effects are explicit session or decision-log writes below
the selected consumer root and opt-in profile template copying. Browser installation,
browser execution, endpoint access, source generation, deployment, release, and
secrets remain outside the default v1.4 execution boundary.

Before execution, the hardening task must confirm exact paths and sequential
dependencies. Runtime profile consumer proof validates declarations only; a later
runtime task must explicitly opt in to browser execution and attach its own evidence.

The added starter-readiness command is read-only and must report observed tool gaps
without installing Java, Node, Docker, Playwright, a browser, or any application
dependency. The later neutral Playwright task may run only after a user explicitly
approves browser installation and a temporary local server; it must retain fresh,
scoped runtime evidence and cleanup its own resources.
