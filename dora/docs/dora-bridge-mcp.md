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

Bridge V1 exposes only read-only project projections plus two bounded IDC advisory
profiles: `get_idc_envelope` returns a fixed sanitized Dora context envelope, and
`evaluate_idc_triage` classifies one bounded structured triage request. Triage returns
only `NO_IDC_NEEDED`, `IDC_OWNER_CONFIRMATION_REQUIRED`, or
`IDC_OWNER_AUTHORIZED_LOCAL_RENDER`; it never accepts source/output paths, dossier
content, commands, or arbitrary selectors. Tools are used instead of MCP resources
because all projections require an allow-listed project ID and some require a declared,
project-relative plan/task reference.

Neither IDC profile renders a dossier or invokes a local process. A Bridge response
that says local render is authorized remains advisory: the owner/Codex must separately
run the fixed local Dora command with explicit inputs. Bridge has no write, shell,
source, search, arbitrary-file, Git, network, IDC-start, or Codex-start operation.

For private ChatGPT use, Secure MCP Tunnel supplies the remote-capable authenticated
transport above this unchanged local stdio process. The bridge itself remains
transport-neutral and must never publish an unauthenticated HTTP endpoint.
