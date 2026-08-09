# Dora Bridge MCP adapter

`dora/bridge/bin/dora-bridge-mcp` is a small, local stdio MCP server. It delegates
all project semantics to `Dora::ProjectReadModel`; it does not parse Dora CLI text,
run shell commands, discover repositories, or write project state.

Start it only with a trusted server-side allow-list:

```sh
ruby dora/bridge/bin/dora-bridge-mcp /absolute/path/to/bridge-projects.yaml
```

The registry is `dora_bridge_projects` version 1. Each entry has a public `id`, an
optional display `name`, and a trusted server-side `adapter_path`. Client requests
can provide only an `id`; unknown IDs fail before an adapter path is opened.
The read model canonicalizes every artifact it opens, so a project-relative request
cannot follow a symlink outside the configured project root.

Bridge V1 exposes only the read-only tools `list_projects`, `get_project_summary`,
`get_project_health`, `get_current_delivery`, `get_next_task`, `get_open_decisions`,
`get_plan`, and `get_task_evidence`. Tools are used instead of MCP resources because
all projections require an allow-listed project ID and some require a declared,
project-relative plan/task reference. There are no write, shell, source, search, or
arbitrary-file operations.

The adapter is stdio-only in this task. A future private remote transport must retain
this registry and read-model boundary, add authentication outside Dora core, and must
not publish an unauthenticated endpoint.
