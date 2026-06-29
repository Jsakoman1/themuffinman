# Manifest Path Resolution

- Decision: `review`
- Manifest Path: ``
- Feature Id: ``
- Title: ``
- Confidence: `low`
## `reasons`

- `More than one manifest candidate matched the current files. Review the candidate list before closeout.`

## `candidates`

- `{:manifest_path: ".agents/feature-manifests/agent-control-phase-two-manifest.yaml", :feature_id: "agent_control_phase_two", :title: "Agent Control Phase Two", :plan_file: ".agents/agent-control-phase-two-plan.md", :score: 32, :reasons: ["Manifest file itself is in the current changeset.", "Referenced plan file is in the current changeset.", "Changed files overlap manifest artifact lists (scripts/feature-closeout-audit.sh, scripts/generate-source-of-truth-audit.rb, scripts/generate-agent-operating-model.rb, Makefile, apps/themuffinman/frontend/package.json).", "Changed paths include feature/plan tokens: agent, control, phase, two, manifest, plan."]}`
- `{:manifest_path: ".agents/feature-manifests/agent-operating-refactor-manifest.yaml", :feature_id: "agent_operating_refactor", :title: "Agent Operating Refactor", :plan_file: ".agents/agent-operating-refactor-plan.md", :score: 32, :reasons: ["Manifest file itself is in the current changeset.", "Referenced plan file is in the current changeset.", "Changed files overlap manifest artifact lists (apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestService.java, apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestExecutionPrimitiveService.java, scripts/generate-agent-operating-model.rb, docs/agent-operating-model.md, docs/agent-operating-model.yaml).", "Changed paths include feature/plan tokens: agent, manifest, operating, refactor, plan."]}`
- `{:manifest_path: ".agents/feature-manifests/agent-safety-enforcement-round2-manifest.yaml", :feature_id: "agent_safety_enforcement_round2", :title: "Agent safety enforcement round 2", :plan_file: ".agents/agent-safety-enforcement-round2-plan.md", :score: 32, :reasons: ["Manifest file itself is in the current changeset.", "Referenced plan file is in the current changeset.", "Changed files overlap manifest artifact lists (Makefile, docs/agent-operating-model.md, docs/agent-operating-model.yaml, docs/agent-operating-model.schema.json, docs/domain-technical.md).", "Changed paths include feature/plan tokens: agent, manifest, safety, enforcement, round, round2."]}`
- `{:manifest_path: ".agents/feature-manifests/agent-safety-upgrade-manifest.yaml", :feature_id: "agent_safety_upgrade", :title: "Agent safety upgrade", :plan_file: ".agents/agent-safety-upgrade-plan.md", :score: 32, :reasons: ["Manifest file itself is in the current changeset.", "Referenced plan file is in the current changeset.", "Changed files overlap manifest artifact lists (Makefile, apps/themuffinman/frontend/package.json, docs/agent-operating-model.md, docs/agent-operating-model.yaml, docs/agent-operating-model.schema.json).", "Changed paths include feature/plan tokens: agent, manifest, safety, upgrade, plan."]}`
- `{:manifest_path: ".agents/feature-manifests/backend-audit-domain-tagging-manifest.yaml", :feature_id: "backend_audit_domain_tagging", :title: "Backend audit domain tagging", :plan_file: ".agents/backend-audit-domain-tagging-plan.md", :score: 32, :reasons: ["Manifest file itself is in the current changeset.", "Referenced plan file is in the current changeset.", "Changed files overlap manifest artifact lists (docs/agent-operating-model.md, docs/agent-operating-model.yaml, docs/agent-operating-model/sections/backend_audit_coverage.yaml, docs/agent-operating-model/sections/source_of_truth.yaml, docs/agent-operating-model/sections/documentation_coverage.yaml).", "Changed paths include feature/plan tokens: manifest, backend, audit, domain, tagging, plan."]}`
- `{:manifest_path: ".agents/feature-manifests/backend-audit-identity-dto-tightening-manifest.yaml", :feature_id: "backend_audit_identity_dto_tightening", :title: "Backend audit identity dto tightening", :plan_file: ".agents/backend-audit-identity-dto-tightening-plan.md", :score: 32, :reasons: ["Manifest file itself is in the current changeset.", "Referenced plan file is in the current changeset.", "Changed files overlap manifest artifact lists (docs/agent-operating-model.md, docs/agent-operating-model.yaml, docs/agent-operating-model/sections/backend_audit_coverage.yaml, docs/agent-operating-model/sections/source_of_truth.yaml, docs/agent-operating-model/sections/documentation_coverage.yaml).", "Changed paths include feature/plan tokens: manifest, backend, audit, identity, dto, tightening."]}`
- `{:manifest_path: ".agents/feature-manifests/backend-audit-location-dto-tightening-manifest.yaml", :feature_id: "backend_audit_location_dto_tightening", :title: "Backend audit location dto tightening", :plan_file: ".agents/backend-audit-location-dto-tightening-plan.md", :score: 32, :reasons: ["Manifest file itself is in the current changeset.", "Referenced plan file is in the current changeset.", "Changed files overlap manifest artifact lists (docs/agent-operating-model.md, docs/agent-operating-model.yaml, docs/agent-operating-model/sections/backend_audit_coverage.yaml, docs/agent-operating-model/sections/source_of_truth.yaml, docs/agent-operating-model/sections/documentation_coverage.yaml).", "Changed paths include feature/plan tokens: manifest, backend, audit, dto, tightening, location."]}`
- `{:manifest_path: ".agents/feature-manifests/backend-audit-manifest-cleanup-manifest.yaml", :feature_id: "backend_audit_manifest_cleanup", :title: "Backend audit manifest cleanup", :plan_file: ".agents/backend-audit-manifest-cleanup-plan.md", :score: 32, :reasons: ["Manifest file itself is in the current changeset.", "Referenced plan file is in the current changeset.", "Changed files overlap manifest artifact lists (docs/agent-operating-model.md, docs/agent-operating-model.yaml, .agents/backend-audit-manifest-cleanup-plan.md, .agents/feature-manifests/backend-audit-tiering-manifest.yaml, .agents/feature-manifests/backend-audit-tightening-manifest.yaml).", "Changed paths include feature/plan tokens: manifest, backend, audit, cleanup, plan."]}`
- `{:manifest_path: ".agents/feature-manifests/backend-audit-tiering-manifest.yaml", :feature_id: "backend_audit_tiering", :title: "Backend audit tiering", :plan_file: ".agents/backend-audit-tiering-plan.md", :score: 32, :reasons: ["Manifest file itself is in the current changeset.", "Referenced plan file is in the current changeset.", "Changed files overlap manifest artifact lists (scripts/generate-agent-operating-model.rb, scripts/generate-source-of-truth-audit.rb, Makefile, docs/agent-operating-model.md, docs/agent-operating-model.yaml).", "Changed paths include feature/plan tokens: manifest, backend, audit, tiering, plan."]}`
- `{:manifest_path: ".agents/feature-manifests/backend-audit-tightening-manifest.yaml", :feature_id: "backend_audit_tightening", :title: "Backend audit tightening", :plan_file: ".agents/backend-audit-tightening-plan.md", :score: 32, :reasons: ["Manifest file itself is in the current changeset.", "Referenced plan file is in the current changeset.", "Changed files overlap manifest artifact lists (docs/agent-operating-model.md, docs/agent-operating-model.yaml, docs/agent-operating-model/sections/backend_audit_coverage.yaml, docs/agent-operating-model/sections/source_of_truth.yaml, docs/agent-operating-model/sections/documentation_coverage.yaml).", "Changed paths include feature/plan tokens: manifest, backend, audit, tightening, plan."]}`

- Generated At: `2026-06-29T12:47:29Z`
- Original File Count: `373`
- Filtered File Count: `195`
- Excluded File Count: `195`
## `excluded_files_sample`

- `{:path: ".agents/agent-control-phase-two-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/agent-operating-refactor-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/agent-safety-enforcement-round2-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/agent-safety-upgrade-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/backend-audit-domain-tagging-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/backend-audit-identity-dto-tightening-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/backend-audit-location-dto-tightening-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/backend-audit-manifest-cleanup-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/backend-audit-tiering-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/backend-audit-tightening-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/executor-readiness-program-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/persistent-backlog-system-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: "apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/agent-endpoint-inventory.json", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/automation-read-model-inventory.json", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/backend-audit-inventory.json", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/dead-code-audit/backend-unused-summary.md", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/dead-code-audit/backend-unused.json", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/dead-code-audit/dead-code-summary-summary.md", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/dead-code-audit/dead-code-summary.json", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/dead-code-audit/frontend-unused-summary.md", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/dead-code-audit/frontend-unused.json", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/local-tooling/.cache/audit-inputs.json", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/local-tooling/agent-model-feature-coverage-audit-summary.md", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/local-tooling/agent-model-feature-coverage-audit.json", :excluded: true, :reasons: ["generated"]}`

## `files_considered`

- `.agents/feature-manifests/agent-control-phase-two-manifest.yaml`
- `.agents/feature-manifests/agent-operating-refactor-manifest.yaml`
- `.agents/feature-manifests/agent-safety-enforcement-round2-manifest.yaml`
- `.agents/feature-manifests/agent-safety-upgrade-manifest.yaml`
- `.agents/feature-manifests/backend-audit-domain-tagging-manifest.yaml`
- `.agents/feature-manifests/backend-audit-identity-dto-tightening-manifest.yaml`
- `.agents/feature-manifests/backend-audit-location-dto-tightening-manifest.yaml`
- `.agents/feature-manifests/backend-audit-manifest-cleanup-manifest.yaml`
- `.agents/feature-manifests/backend-audit-tiering-manifest.yaml`
- `.agents/feature-manifests/backend-audit-tightening-manifest.yaml`
- `.agents/feature-manifests/executor-readiness-program-manifest.yaml`
- `.agents/feature-manifests/persistent-backlog-system-manifest.yaml`
- `.agents/templates/feature-completion-manifest.template.yaml`
- `.agents/templates/feature-implementation-plan.template.md`
- `.gitignore`
- `Makefile`
- `apps/themuffinman/frontend/README.md`
- `apps/themuffinman/frontend/package.json`
- `apps/themuffinman/frontend/scripts/generate-workmarket-contracts.mjs`
- `apps/themuffinman/frontend/scripts/validate-admin-agent-ui-scenarios.mjs`
- `apps/themuffinman/frontend/src/components/ui/UiDashboardPage.vue`
- `apps/themuffinman/frontend/src/modules/moduleRegistry.ts`
- `apps/themuffinman/frontend/src/modules/social/pages/AdminCirclesPage.vue`
- `apps/themuffinman/frontend/src/modules/social/views/CirclesView.vue`
- `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/createDashboardSelectors.ts`

