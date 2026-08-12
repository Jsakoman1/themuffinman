# Dora current capabilities

Dora is the canonical local delivery-control layer. It owns owner-confirmed
decisions, bounded work plans, task lifecycle and verification evidence; it does
not replace a consumer project's product/domain truth.

IDC v0 is bundled with Dora but is separate in responsibility: it produces local,
owner-approved advisory dossiers from explicit inputs. It cannot write Dora,
Git or product code, start Codex, use a shell, browse a repository or network, or
turn a recommendation into a decision.

The Bridge is a sanitized read-only projection. A Bridge response can advise that
IDC may be useful, but only the existing current-request owner authorization can
permit the fixed local renderer. Private Context and CPPE are planned architecture
components, not current Dora or IDC capabilities.

The machine-readable [capability inventory](capability-inventory.yaml) is a
current-state summary with links to existing documentation and evidence. It never
creates verification or lifecycle state.
