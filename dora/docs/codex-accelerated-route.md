# Codex-accelerated Dora route

Use this route when a Codex agent helps build a new application or one new feature.
It is designed to reduce repeated context assembly, not to replace product decisions.

The default decision rule is machine-readable in
[`agent-request-routing-policy.yaml`](agent-request-routing-policy.yaml): classify
the request before planning. Bounded delivery stays on Dora's direct route; wide
research and greenfield discovery are IDC candidates. Classification is advisory
and never authorizes rendering or implementation.

0. For a broad research or greenfield request, Codex may prepare one bounded
   `dora_idc_triage_request` and use the read-only triage route. A
   `IDC_OWNER_CONFIRMATION_REQUIRED` result means ask the owner before rendering.
   Only an explicit current-request `IDC_OWNER_AUTHORIZED_LOCAL_RENDER` result allows
   the owner-controlled local `dora idc-render` command, with explicit triage, request,
   manifest, dossier, and output paths. The command renders advisory material only;
   it does not create Dora decisions, plans, work, evidence, or verified status.
   Bridge never runs this command, and Codex must not infer or retain authorization.
1. Run `dora guided-next guided-agent-entrypoint.yaml`. If it returns
   `ask_one_confirmed_question`, ask the user only that question and record the
   answer through the existing interview route. If it returns
   `review_create_app_handoff`, review the handoff before the existing create-app
   route is used.
2. Build a `dora_codex_context_packet` for one bounded task, then run
   `dora codex-context <packet>`. Read only its cited confirmed rules, allowed
   paths, dependencies, validation, proof obligations, and explicit omissions.
   A missing citation or unresolved required context is a blocker, not an invitation
   to inspect arbitrary files or infer product behavior.
3. Run `dora capability-graph <graph>` to see declared dependency cycles, open
   decisions, unconfirmed capabilities, and one `next_safe_capability`. The result
   is planning guidance; consumer-project implementation evidence is still required
   before a dependent capability is considered available.
4. Before a compiled feature is applied, declare and confirm a project convention
   profile. Run `dora convention-check <profile> <manifest>` to reject incompatible
   source roots, Java package paths, migration directories, API locations, frontend
   feature locations, test locations, and documentation roots.
5. For a related resource, declare the relation, confirmed foreign key, query/index
   decision, UI states, and convention profile. Dora may render a deterministic
   additive packet and relation trace. It may not overwrite existing source, edit a
   historic Flyway migration, or regenerate a manual change.
6. Declare assertions and obligations in a capability proof matrix. Run
   `dora proof-packet <matrix>` to see required local proof and approval-gated
   browser/runtime, accessibility, security, or acceptance proof. The packet never
   executes those actions or records a pass.
7. Create one project-owned atomic work item. Implement only after the task is
   started through the project work control, then run its declared leaf validation
   and project work verification. Database, browser, real-data, credential,
   deployment, publication, and release work need their own explicit approval.

## Ownership boundary

Dora owns neutral schemas, commands, templates, diagnostics, and bounded tests.
The consumer project owns product-specific business rules, user-facing copy, generated-code review,
source implementation, tests, runtime evidence, real data, security policy, and
release decisions. Generated output, context packets, graph reports, convention
reports, and proof packets are not implementation, acceptance, or release evidence.
Dora does not create product meaning, credentials, real data, or an approved release.
