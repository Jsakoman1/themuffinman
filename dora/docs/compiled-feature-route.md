# Compiled feature route for Codex

Use this route only after the product interview, domain decisions, permissions,
workflow, API shape, database mappings, and UI states are explicitly confirmed.

1. Create or select a project with the `spring-vue-postgres-buildable` starter.
2. Build one `dora_compiled_feature` document. It must declare package and project
   locations, migration version, every Java/SQL field mapping, nullable/default/
   unique/index/foreign-key choice, API request and response fields, service or
   controller permission location, workflow transitions, and UI states.
3. Run `dora compiled-feature-preview <input> --format yaml`. Review exact paths,
   template digests, input digest, and unresolved obligations. A missing decision is
   a blocker, not a request for Dora to infer a default.
4. Render the feature with the explicit Dora renderer API and call
   `Dora::CompiledFeatureApply` with the manifest and the exact rendered-file map.
   Apply writes only new declared paths and rejects traversal, collisions, changed
   historic migrations, and a file map that differs from the manifest.
5. Run generated safety inspection against the written files and compare their
   content hashes plus DTO/API/workflow/permission trace to the confirmed model.
6. Treat static and compile evidence separately. Compile work may download Maven or
   npm dependencies and requires explicit approval. Docker/PostgreSQL and Playwright
   are separate explicit approval gates. None of them is business acceptance evidence.
7. Create project-owned work items, tests, runtime scenarios, and acceptance evidence
   before considering the feature complete or releasable.

## Boundaries

The v1.6 generator targets one neutral Spring JDBC and Vue starter. It does not
create users, authentication policy, credentials, secrets, real data, backup jobs,
deployments, cloud resources, browser proof, or a product release. It never overwrites
an existing or manually changed generated file; reconcile such a change through
normal project-owned work and Git history.
