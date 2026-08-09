# Dora Bridge V1 private ChatGPT connection

## Verified integration assumption

This runbook was checked against the OpenAI Secure MCP Tunnel documentation on 2026-08-09. Secure MCP Tunnel is the
Bridge V1 transport. It keeps the local Dora Bridge private: `tunnel-client` makes an outbound HTTPS connection to
OpenAI, then forwards queued MCP JSON-RPC requests to an approved local server. The local server may be reached through
stdio, so the existing `dora-bridge-mcp` command is used unchanged. No HTTP wrapper, Apps SDK UI, plugin package, public
endpoint, or a second Dora query implementation is required for this developer-mode proof.

The trust path is:

```text
ChatGPT developer-mode app
  -> OpenAI-hosted Secure MCP Tunnel endpoint
  -> tunnel-client on this Mac (outbound HTTPS only)
  -> dora-bridge-mcp stdio command
  -> Dora::ProjectReadModel
```

The bridge command exposes only the existing read-only tools. It holds the explicit
`doomsday-storage` and `dora` allow-list in a private local registry. The tunnel runtime API key is used only by `tunnel-client`;
neither it nor a tunnel ID belongs in this repository, Dora evidence, or any project artifact.

## Required account and network access

The operator needs a Platform organization with Tunnels Read + Manage to create or edit the tunnel, and Tunnels Read +
Use to run/select it. The target ChatGPT workspace must be associated with that tunnel. ChatGPT developer mode is a
separate workspace permission; on Enterprise/Edu, an administrator grants it before the user enables it in **Settings →
Security and login**.

This Mac needs outbound HTTPS access to `api.openai.com:443` and local access to the Dora Bridge command. It requires no
inbound port, public DNS name, public HTTPS endpoint, or third-party tunnel.

## One-owner setup

1. In Platform tunnel settings, create a tunnel and associate it with the target ChatGPT workspace. Record its
   `tunnel_id` privately.
2. Obtain the runtime API key for `tunnel-client` privately. Do not paste it into a shell history, repository file, Dora
   plan, or ChatGPT conversation.
3. Create a private local registry outside the repository with permissions limited to the local owner. Its only entries
   must be:

   ```yaml
   kind: dora_bridge_projects
   version: 1
   projects:
     - id: doomsday-storage
       name: DoomsDayStorage
       adapter_path: /Users/jsakoman/Desktop/DoomsDayStorage/.dora/project.yaml
     - id: dora
       name: Dora
       adapter_path: /Users/jsakoman/Desktop/themuffinman/dora/.dora/project.yaml
   ```

4. Download the latest `tunnel-client` from Platform tunnel settings, then initialize a named local stdio profile.
   Substitute the private registry path and tunnel ID:

   ```sh
   export CONTROL_PLANE_API_KEY="..."
   tunnel-client init \
     --sample sample_mcp_stdio_local \
     --profile dora-bridge-v1 \
     --tunnel-id "tunnel_..." \
     --mcp-command "ruby /Users/jsakoman/Desktop/themuffinman/dora/bridge/bin/dora-bridge-mcp /private/local/dora-bridge-projects.yaml"
   tunnel-client doctor --profile dora-bridge-v1 --explain
   tunnel-client run --profile dora-bridge-v1
   ```

   Keep `tunnel-client run` active. Confirm its loopback-only `/ui` is healthy, ready, and connected before continuing.
5. In ChatGPT, open **Plugins**, select **+** to create a developer-mode app, choose **Tunnel** under Connection, then
   select the associated tunnel or paste the private `tunnel_id`. The Dora tools should appear as a read-only
   developer-mode capability. If the tunnel is absent, verify the workspace association and the operator's Tunnels
   Read + Use permission.

## Required proof

In ChatGPT, without pasting filesystem paths or terminal output, ask:

1. What is the current state of DoomsDayStorage?
2. What did Codex finish most recently?
3. Are there open product decisions?
4. What task is next?
5. What evidence verified the latest delivery?
6. What is the current state of Dora?
7. Is Dora Bridge V1 complete, and what is Dora's next task?

Record only the high-level answers and confirmation that the server returned no absolute paths, credentials, raw output,
source code, or write capability. The reconciled DoomsDayStorage projection is `HEALTHY`, has no active or next task,
and identifies V21 evidence corrections as its latest verified delivery. Dora is `HEALTHY`, has no next task, and cites
the integrated private read-only Bridge V1 provenance record. Do not change either project merely to make this proof
look healthy.

## Completion boundary

Do not run Dora `work-verify` for this task until an authorized ChatGPT user has completed the five questions through
the tunnel. If developer mode, tunnel access, or workspace association is unavailable, stop here: do not expose an
unauthenticated endpoint or substitute a public tunnel.

## Official sources

- OpenAI Secure MCP Tunnel guide: <https://developers.openai.com/api/docs/guides/secure-mcp-tunnels>
- OpenAI MCP and Connectors guide: <https://developers.openai.com/api/docs/guides/tools-connectors-mcp>

## Recorded external proof — 2026-08-09

An authorized ChatGPT developer-mode app connected through Secure MCP Tunnel and
used this Bridge without supplied filesystem paths or pasted terminal output. It
discovered the sole allowed project, `doomsday-storage`, then successfully answered:

1. Current project state: doctor checks passed, while the projection honestly
   reported `INVALID` because project memory is inconsistent and exposed the stale
   active V17 inventory rather than guessing.
2. Latest completed delivery: V21 evidence corrections.
3. Open decisions: the declared unresolved/deferred product decision.
4. Next task: the currently resolved V17 active task, including its known stale-state
   implication.
5. Latest delivery evidence: a sanitized V21 task result containing passed status,
   verification timestamp, revision, validation type, and exit code.

The connected client received no credentials, environment values, source code, raw
terminal/verifier output, manual filesystem browser, write capability, or project
path input. The private tunnel remained the sole external transport.

## Two-project refresh checkpoint

The private local registry now contains only `doomsday-storage` and `dora`. Restart
the local `tunnel-client run --profile dora-bridge-v1` process if it was already
running when the registry changed. In ChatGPT, reopen the developer-mode Dora app or
start a new conversation so tool discovery refreshes, then confirm `list_projects`
shows exactly those two IDs. This final user-side checkpoint does not add any write
authority.

## Final adversarial review

The MCP adapter delegates project semantics exclusively to `Dora::ProjectReadModel`;
it does not duplicate delivery, decision, evidence, or task resolution. Project IDs
are resolved from the trusted allow-list before an adapter path is opened. The read
model canonicalizes opened artifact paths and rejects path or symlink escape.

All exposed tools carry read-only intent and the server implements no write, shell,
source, arbitrary-file, work-start, work-verify, commit, push, or Codex operation.
Its projection removes absolute paths and raw evidence output server-side. Secure MCP
Tunnel supplies the private outbound-only transport and control-plane authentication;
no public bridge endpoint, second state store, or Handoff model was introduced.