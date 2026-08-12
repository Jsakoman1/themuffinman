# Capability documentation v1 atomic hardening review

Reviewed master: `dora-machine-readable-capability-documentation-v1`
Reviewed inventory: `dora-machine-readable-capability-documentation-v1-execution-inventory.yaml`

## Result

The nine inventory items form one strict serial chain. Each item maps exactly to
one child-plan task, has one direct predecessor except the first item, names
exact required paths, declares one leaf validation command, and has a boundary
that prevents it from acquiring a second source of truth or unapproved runtime
authority.

## Atomic review

| Item | Single observable outcome | Boundary confirmed |
| --- | --- | --- |
| `capability-documentation-hardening` | Record this independent plan-integrity review. | No product, runtime, consumer, release, PC or CPPE change. |
| `capability-documentation-contract` | Define one portable capability/documentation contract. | Inventory links evidence; it does not create decisions or verification. |
| `capability-documentation-controls` | Validate explicitly adopted controls while preserving legacy compatibility. | Doctor validates declarations; it does not infer capabilities or migrate projects. |
| `capability-documentation-scaffold` | Generate neutral controls for a new Dora project. | A generated skeleton declares no product fact or delivery result. |
| `dora-idc-self-adoption` | Publish Dora/IDC current capability and boundary documentation. | IDC remains local, advisory-only, and gains no process or write authority. |
| `agent-request-routing-policy` | Make the existing IDC and Master Plan route durable and machine-readable. | Triage is advisory; rendering, promotion and goal pursuing remain owner-gated. |
| `capability-documentation-release` | Prepare one reviewed Dora release contract. | No tag, push or consumer update occurs without later approval. |
| `external-adoption-contract` | Publish a project-local external adoption guide/template. | The Dora worktree neither reads nor mutates an external repository. |
| `capability-documentation-closeout` | Record Dora-core completion and external follow-on gates. | Closeout cannot claim external adoption or PC/CPPE implementation. |

## Authority and rollout checks

- Dora remains the only authority for its decisions, plans, task lifecycle and
  evidence. The capability inventory is a cited current-state view, never a
  delivery lifecycle.
- IDC remains bundled with Dora but local, advisory-only and explicitly
  owner-authorized per render. The routing policy reuses the verified triage
  contract; it does not add a natural-language classifier or autonomous agent.
- `ai-system`, DoomsDayStorage and TheMuffinMan are named only as future,
  owner-approved, project-local adoption follow-ons. They are not tasks in this
  Dora execution inventory.
- The proposed v1.12.0 release and every external adoption remain deferred
  owner gates. Their presence in the master grants neither publication nor
  cross-repository mutation.

## Review conclusion

The program is eligible to enter its contract slice after this review is
verifier-checked. The review is deliberately Markdown rather than a YAML work
artifact: it is narrative plan evidence, not executable work state and therefore
does not create a Doctor artifact-policy advisory.
