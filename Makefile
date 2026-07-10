SHELL := /bin/zsh

dev:
	$(MAKE) -C apps/themuffinman dev

dev-doctor:
	$(MAKE) -C apps/themuffinman dev-doctor

dev-storage:
	$(MAKE) -C apps/themuffinman dev-storage

dev-storage-down:
	$(MAKE) -C apps/themuffinman dev-storage-down

backend-dev:
	$(MAKE) -C apps/themuffinman backend-dev

backend-test:
	$(MAKE) -C apps/themuffinman backend-test

backend-package:
	$(MAKE) -C apps/themuffinman backend-package

backend-bootstrap-example:
	$(MAKE) -C apps/themuffinman backend-bootstrap-example

generate-frontend-contracts:
	npm --prefix apps/themuffinman/frontend run generate:contracts

validate-frontend-contracts:
	npm --prefix apps/themuffinman/frontend run validate:contracts

audit-agent-safety:
	$(MAKE) -C apps/themuffinman audit-agent-safety
	$(MAKE) audit-validation-evidence-quality
	$(MAKE) audit-validation-memory-drift
	$(MAKE) audit-todo

audit-todo:
	ruby scripts/todo-audit.rb

audit-validation-evidence-quality:
	ruby scripts/audits/audit-validation-evidence-quality.rb

audit-generated-commit-scope:
	ruby scripts/audits/audit-generated-commit-scope.rb

changeset-risk:
	ruby scripts/audits/score-changeset-risk.rb $(files)

audit-change-impact-preflight:
	ruby scripts/audits/audit-change-impact-preflight.rb $(files)

audit-read-surface-inventory:
	ruby scripts/audits/audit-read-surface-inventory.rb

audit-mapper-usage:
	ruby scripts/audits/audit-mapper-usage.rb

audit-generated-artifact-freshness:
	$(MAKE) validate-frontend-contracts
	ruby scripts/audits/audit-generated-artifact-freshness.rb files="$(files)"

audit-generated-artifact-hygiene:
	$(MAKE) validate-frontend-contracts
	ruby scripts/audits/audit-generated-artifact-freshness.rb files="$(files)"

cleanup-generated-history:
	ruby scripts/audits/cleanup-generated-history.rb

audit-api-contract-drift:
	ruby scripts/audits/audit-api-contract-drift.rb

audit-repository-fetch:
	ruby scripts/audits/audit-repository-fetch.rb

audit-endpoint-callsite-linker:
	ruby scripts/audits/audit-endpoint-callsite-linker.rb

audit-frontend-route-surfaces:
	ruby scripts/audits/audit-frontend-route-surfaces.rb

audit-frontend-stale-surfaces:
	ruby scripts/audits/audit-frontend-stale-surfaces.rb

audit-frontend-state-logic-duplication:
	ruby scripts/audits/audit-frontend-state-logic-duplication.rb

audit-duplicate-logic:
	ruby scripts/audits/audit-duplicate-logic.rb

audit-permission-rule-duplication:
	ruby scripts/audits/audit-permission-rule-duplication.rb

audit-frontend-dead-code:
	ruby scripts/audits/audit-frontend-dead-code.rb

audit-backend-dead-code:
	ruby scripts/audits/audit-backend-dead-code.rb

audit-dead-code:
	ruby scripts/audits/audit-dead-code.rb

audit-state-transition-coverage:
	ruby scripts/audits/audit-state-transition-coverage.rb

audit-docs-to-code-drift:
	ruby scripts/audits/audit-docs-to-code-drift.rb

audit-doc-staleness-scoring:
	ruby scripts/audits/audit-doc-staleness-scoring.rb

audit-architecture-drift:
	ruby scripts/audits/audit-architecture-drift.rb

architecture-decision-index:
	ruby scripts/audits/generate-architecture-decision-index.rb

audit-doc-coverage-gap:
	ruby scripts/audits/audit-doc-coverage-gap.rb

audit-automation-readiness-gap:
	ruby scripts/audits/audit-automation-readiness-gap.rb

audit-agent-model-feature-coverage:
	ruby scripts/audits/audit-agent-model-feature-coverage.rb

audit-sandbox-generation-coverage:
	ruby scripts/audits/audit-sandbox-generation-coverage.rb

audit-domain-ownership-inventory:
	ruby scripts/audits/audit-domain-ownership-inventory.rb

audit-config-sprawl:
	ruby scripts/audits/audit-config-sprawl.rb

audit-naming-consistency:
	ruby scripts/audits/audit-naming-consistency.rb

audit-dormant-code:
	ruby scripts/audits/audit-dormant-code.rb

audit-manual-cleanup-candidate-report:
	ruby scripts/audits/audit-manual-cleanup-candidate-report.rb

audit-file-relation-graph:
	ruby scripts/audits/audit-file-relation-graph.rb

audit-test-surface-inventory:
	ruby scripts/audits/audit-test-surface-inventory.rb

audit-test-fixture-duplication:
	ruby scripts/audits/audit-test-fixture-duplication.rb

audit-mutation-safety:
	ruby scripts/audits/audit-mutation-safety.rb

audit-docs-as-tests:
	ruby scripts/audits/audit-docs-as-tests.rb

audit-error-pattern:
	ruby scripts/audits/audit-error-pattern.rb

audit-rich-text-safety:
	ruby scripts/audits/audit-rich-text-safety.rb

audit-async-mutation-flow:
	ruby scripts/audits/audit-async-mutation-flow.rb

audit-style-token-usage:
	ruby scripts/audits/audit-style-token-usage.rb

audit-feature-intro-check:
	ruby scripts/audits/audit-feature-intro-check.rb

audit-make-target-index:
	ruby scripts/audits/audit-make-target-index.rb

audit-documentation:
	ruby scripts/audits/audit-documentation.rb

codex-context:
	ruby scripts/audits/codex-context.rb collect mode="$(if $(mode),$(mode),implementation)" budget="$(if $(budget),$(budget),6000)" topic="$(topic)" files="$(files)" intent="$(intent)" $(if $(refresh),refresh="$(refresh)",) $(if $(include_generated),include_generated="$(include_generated)",) $(if $(include_agents),include_agents="$(include_agents)",)

codex-context-explain:
	ruby scripts/audits/codex-context.rb explain

codex-context-clean:
	ruby scripts/audits/codex-context.rb clean

context-pack:
	ruby scripts/audits/generate-context-pack.rb topic="$(topic)" files="$(files)" budget="$(budget)"

recommend-feature-slices:
	@if [ -z "$(topic)" ]; then echo "usage: make recommend-feature-slices topic=<topic> [files=<csv>]"; exit 1; fi
	ruby scripts/audits/recommend-feature-slices.rb topic="$(topic)" files="$(files)"

recommend-targeted-tests:
	ruby scripts/audits/recommend-targeted-tests.rb files="$(files)"

audit-router:
	ruby scripts/audits/audit-router.rb $(files)

repo-map:
	ruby scripts/audits/generate-repo-map.rb

symbol-index:
	ruby scripts/audits/generate-symbol-index.rb

endpoint-contract-packs:
	ruby scripts/audits/generate-endpoint-contract-packs.rb

validation-matrix:
	ruby scripts/audits/generate-validation-matrix.rb

changeset-playbook:
	ruby scripts/audits/generate-changeset-playbook.rb files="$(files)"

resolve-manifest-path:
	ruby scripts/audits/resolve-manifest-path.rb files="$(files)"

link-symbol-to-tests:
	@if [ -z "$(symbol)" ]; then echo "usage: make link-symbol-to-tests symbol=<symbol-name>"; exit 1; fi
	ruby scripts/audits/link-symbol-to-tests.rb symbol="$(symbol)"

dto-usage-pack:
	@if [ -z "$(dto)" ]; then echo "usage: make dto-usage-pack dto=<dto-name>"; exit 1; fi
	ruby scripts/audits/generate-dto-usage-pack.rb dto="$(dto)"

workflow-slice-pack:
	@if [ -z "$(workflow)" ]; then echo "usage: make workflow-slice-pack workflow=<workflow-id>"; exit 1; fi
	ruby scripts/audits/generate-workflow-slice-pack.rb workflow="$(workflow)"

rank-changeset-hotspots:
	ruby scripts/audits/rank-changeset-hotspots.rb files="$(files)"

domain-pack:
	@if [ -z "$(domain)" ]; then echo "usage: make domain-pack domain=<domain-id>"; exit 1; fi
	ruby scripts/audits/generate-domain-pack.rb domain="$(domain)"

recommend-validation-preset:
	ruby scripts/audits/recommend-validation-preset.rb files="$(files)"

audit-delta-report:
	@if [ -z "$(audit)" ]; then echo "usage: make audit-delta-report audit=<audit-id>"; exit 1; fi
	ruby scripts/audits/audit-delta-report.rb audit="$(audit)"

audit-doc-sync-preflight:
	ruby scripts/audits/audit-doc-sync-preflight.rb $(files)

audit-doc-sync-required-surfaces:
	ruby scripts/audits/audit-doc-sync-required-surfaces.rb files="$(files)" $(if $(include_generated),include_generated="$(include_generated)",) $(if $(include_agents),include_agents="$(include_agents)",)

audit-doc-template-coverage:
	ruby scripts/audits/audit-doc-template-coverage.rb files="$(files)"

audit-doc-sync-duplicates:
	ruby scripts/audits/audit-doc-sync-duplicates.rb

audit-manifest-decision:
	ruby scripts/audits/audit-manifest-decision.rb files="$(files)"

clean-text-noise:
	ruby scripts/audits/clean-text-noise.rb max_lines="$(if $(max_lines),$(max_lines),80)" aggressive="$(if $(aggressive),$(aggressive),false)"

audit-migration-entity-drift:
	ruby scripts/audits/audit-migration-entity-drift.rb

audit-test-gap-recommendations:
	ruby scripts/audits/audit-test-gap-recommendations.rb $(files)

audit-contract-test-gaps:
	ruby scripts/audits/audit-contract-test-gaps.rb

audit-frontend-usage-graph:
	ruby scripts/audits/audit-frontend-usage-graph.rb

audit-backend-dependency-graph:
	ruby scripts/audits/audit-backend-dependency-graph.rb

diff-summary:
	ruby scripts/audits/generate-diff-summary.rb

session-handoff:
	ruby scripts/audits/generate-session-handoff.rb topic="$(topic)" budget="$(budget)"

audit-summary-index:
	ruby scripts/audits/generate-audit-summary-index.rb

generate-audit-registry-artifacts:
	ruby scripts/audits/generate-audit-registry-artifacts.rb

fast-check:
	ruby scripts/audits/generate-fast-check-report.rb files="$(files)"

api-contract-snapshot:
	ruby scripts/audits/generate-api-contract-snapshot.rb

audit-doc-canonical-phrases:
	ruby scripts/audits/audit-doc-canonical-phrases.rb

audit-validation-memory-drift:
	ruby scripts/audits/audit-validation-memory-drift.rb

validation-memory-closeout-card:
	ruby scripts/audits/generate-validation-memory-closeout-card.rb

audit-sandbox-data-coverage-pack:
	ruby scripts/audits/audit-sandbox-data-coverage-pack.rb

smoke-local-authenticated:
	ruby scripts/audits/smoke-local-authenticated.rb

smoke-local-dashboard:
	ruby scripts/audits/smoke-local-dashboard.rb

post-merge-retrospective:
	ruby scripts/audits/generate-post-merge-retrospective.rb topic="$(topic)" files="$(files)"

failure-knowledge-base:
	ruby scripts/audits/update-failure-knowledge-base.rb source="$(source)"

test-history-summary:
	ruby scripts/audits/generate-test-history-summary.rb

codebase-capsule:
	ruby scripts/audits/generate-codebase-capsule.rb

diagnose-backend-test:
	ruby scripts/audits/diagnose-backend-test.rb

diagnose-frontend-type-check:
	ruby scripts/audits/diagnose-frontend-type-check.rb

diagnose-frontend-build:
	ruby scripts/audits/diagnose-frontend-build.rb

record-validation:
	@if [ -z "$(manifest)" ] || [ -z "$(command)" ]; then echo "usage: make record-validation manifest=<manifest-file> command='<command>'"; exit 1; fi
	ruby scripts/audits/record-validation-evidence.rb manifest="$(manifest)" command="$(command)"

generate-agent-operating-model:
	ruby scripts/generate-agent-operating-model.rb

generate-agent-artifacts:
	ruby scripts/generate-backend-audit-inventory.rb
	ruby scripts/generate-agent-endpoint-inventory.rb
	ruby scripts/generate-automation-read-model-inventory.rb
	ruby scripts/generate-source-of-truth-audit.rb
