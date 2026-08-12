# IDC triage and owner-started orchestration preflight

## Purpose

This program makes the existing local IDC v0 renderer easier to use from an
owner/Codex workflow without changing Dora's canonical authority. Its only new
execution boundary is a future, owner-started local Dora wrapper around the
already-existing IDC renderer.

## Triage contract

The future evaluator consumes one explicitly supplied structured triage request.
It does not interpret repository state, search for sources, call an LLM, or infer
missing owner intent. It can return exactly one of these advisory outcomes:

| Outcome | Meaning | Permitted next action |
| --- | --- | --- |
| `NO_IDC_NEEDED` | The declared request is bounded delivery or does not request wider research/context shaping. | Use the existing Dora delivery workflow. |
| `IDC_OWNER_CONFIRMATION_REQUIRED` | IDC may help, but the current request lacks explicit authorization to create a local advisory dossier. | Ask the owner whether to authorize one local render for this request. |
| `IDC_OWNER_AUTHORIZED_LOCAL_RENDER` | The current structured request explicitly authorizes one local advisory render and selects an allowed IDC profile. | The local owner/Codex may invoke the fixed Dora wrapper with explicit inputs. |

An outcome is advisory. It is not a DecisionLog record, Master Plan, task,
evidence record, verified status, source selection, or promotion.

## Owner authorization

Authorization is valid only when the current triage request explicitly says that
the owner authorizes one local IDC render for the named request/profile. A broad
request such as “help me think about this” is not authorization. A prior dossier,
prior conversation, or a standing setting is not authorization.

The wrapper must revalidate the triage request at invocation time. It must not
trust a terminal transcript, a ChatGPT assertion, or a previously printed result.

## Local execution boundary

The future `dora idc-render` command is owner-started, foreground, and local. It
receives exactly five explicit paths: triage request, IDC request, source manifest,
dossier payload, and output destination. It may invoke only the repository's fixed
IDC Ruby entrypoint through direct argument passing.

It must reject absent/invalid authorization and unsafe/missing paths. It cannot:

- invoke a shell, Codex, Git, network, web research, or arbitrary executable;
- browse directories, select sources, read a repository, or inspect terminal state;
- write outside the declared dossier output destination;
- write Dora decisions, plans, task lifecycle, ProjectMemory, evidence, or verified status;
- promote advisory content into canonical Dora state.

The already-existing IDC renderer remains the sole dossier renderer. The wrapper
does not create a second format, store, or lifecycle.

## Bridge and ChatGPT boundary

Bridge may later evaluate a bounded triage request and return a sanitized advisory
readback for an allow-listed project. It may not receive paths, dossier content,
output destinations, commands, or arbitrary selectors. Bridge cannot invoke the
local wrapper, IDC, Codex, a shell, Git, network, a handoff, or any Dora write.

ChatGPT/Codex can recognize an IDC candidate and present the owner with the
confirmation question. Once the owner explicitly authorizes the current request,
Codex may run the local wrapper in the owner-controlled terminal. Promotion remains
the existing separate owner-approved Dora decision/plan workflow.

## Non-goals retained

No retained authorization state, background worker, hosted service, automatic
research, automatic context/source selection, personal memory, vector index, agent
loop, consumer-project access, or remote execution is introduced by this program.
