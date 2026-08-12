# IDC owner-started orchestration plan analysis

## Conclusion

The master is ready for a future owner-approved goal-pursuing run. It deliberately
extends IDC v0 only at the local owner-start boundary. It does not convert Bridge
into an executor and does not add retained authorization, a background runtime, or
a second IDC renderer.

## Baseline reconciled

- IDC v0 is already verifier-verified in `dora-idc-operational-v0-master.yaml`.
  It validates owner-supplied advisory material, renders a deterministic dossier,
  exports a sanitized Dora envelope, and exposes a fixed read-only Bridge envelope.
- The verified ChatGPT Intent Plan alignment capability is reusable only as a model
  for transient, sanitized Bridge evaluation. It is not authority to create or run
  an IDC dossier.
- The existing Bridge boundary forbids shell, filesystem, source retrieval, Git,
  Codex invocation, and generic Dora writes. The new plan preserves that boundary.

## Hardening findings

1. Triage must not claim that a request has execution permission merely because it
   is broad or complex. The only permitted transition is an explicit current-request
   owner authorization in a validated triage request.
2. A retained or standing authorization would be a new state/permission model and
   is out of scope. This master uses per-request authorization only.
3. The local wrapper needs a distinct explicit triage-request input, in addition to
   request, manifest, dossier, and output paths. It must revalidate that input rather
   than trust a prior terminal response.
4. The wrapper may use direct Ruby argument passing to the fixed repository IDC
   entrypoint only. Shell interpolation, arbitrary executables, Codex, Git, network,
   repository search, and Dora writes are excluded.
5. An atomic-hardening YAML record would be scanned as an unsupported work artifact
   by the current Doctor policy. The first slice therefore produces a markdown review,
   keeping the known existing advisory from multiplying.
6. Bridge triage can return a transient recommendation but cannot accept paths,
   dossier payloads, destinations, or commands. It must not call the local wrapper.

## Serial eligibility

| Slice | Eligible only when | New proof |
| --- | --- | --- |
| 01 preflight | Owner approves this master | Authority contract and atomic hardening only |
| 02 triage | IDC-01 verifier evidence | Pure evaluator fails closed without authorization |
| 03 local render | IDC-02 verifier evidence | Fixed local wrapper invokes existing renderer only |
| 04 Bridge readback | IDC-03 verifier evidence | Bridge remains advisory and non-executing |

## Required owner gate before implementation

The owner must approve this exact master, confirm `/Users/jsakoman/Desktop/dora`
as the sole target, and accept per-request authorization rather than a retained
permission. A later product decision is needed before any standing preference or
automatic background behavior can be considered.
