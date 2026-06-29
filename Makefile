SHELL := /bin/zsh

.PHONY: dev backend-dev backend-test backend-package backend-bootstrap-example generate-frontend-contracts validate-frontend-contracts audit-agent-safety audit-todo audit-validation-evidence-quality audit-plan-completion audit-generated-commit-scope changeset-risk audit-change-impact-preflight audit-read-surface-inventory audit-mapper-usage audit-generated-artifact-freshness audit-api-contract-drift audit-repository-fetch audit-endpoint-callsite-linker audit-frontend-route-surfaces audit-frontend-stale-surfaces audit-frontend-state-logic-duplication audit-duplicate-logic audit-permission-rule-duplication audit-frontend-dead-code audit-backend-dead-code audit-dead-code audit-state-transition-coverage audit-docs-to-code-drift audit-doc-staleness-scoring audit-architecture-drift architecture-decision-index audit-doc-coverage-gap audit-automation-readiness-gap audit-agent-model-feature-coverage audit-sandbox-generation-coverage audit-domain-ownership-inventory audit-config-sprawl audit-naming-consistency audit-dormant-code audit-manual-cleanup-candidate-report audit-file-relation-graph audit-test-surface-inventory audit-test-fixture-duplication audit-mutation-safety audit-docs-as-tests audit-error-pattern audit-rich-text-safety audit-async-mutation-flow audit-style-token-usage audit-feature-intro-check audit-make-target-index audit-documentation codex-context codex-context-explain codex-context-clean context-pack recommend-feature-slices recommend-targeted-tests audit-router repo-map symbol-index endpoint-contract-packs validation-matrix changeset-playbook resolve-manifest-path link-symbol-to-tests dto-usage-pack workflow-slice-pack plan-code-map rank-changeset-hotspots domain-pack recommend-validation-preset audit-delta-report audit-doc-sync-preflight audit-doc-sync-required-surfaces audit-doc-template-coverage audit-doc-sync-duplicates audit-migration-entity-drift audit-test-gap-recommendations audit-contract-test-gaps audit-frontend-usage-graph audit-backend-dependency-graph diff-summary session-handoff audit-summary-index generate-audit-registry-artifacts fast-check api-contract-snapshot audit-doc-canonical-phrases audit-sandbox-data-coverage-pack smoke-local-authenticated smoke-local-dashboard closeout-bundle closeout-report autofill-feature-closeout post-merge-retrospective failure-knowledge-base test-history-summary codebase-capsule diagnose-backend-test diagnose-frontend-type-check diagnose-frontend-build audit-local-tooling audit-local-tooling-incremental bootstrap-feature-work feature-closeout-audit enforce-feature-closeout record-validation audit-manifest-decision clean-text-noise generate-agent-operating-model generate-agent-artifacts

dev:
	$(MAKE) -C apps/themuffinman dev

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
	$(MAKE) audit-todo

audit-todo:
	ruby scripts/todo-audit.rb

audit-validation-evidence-quality:
	ruby scripts/audits/audit-validation-evidence-quality.rb

audit-plan-completion:
	@if [ -z "$(plan)" ]; then echo "usage: make audit-plan-completion plan=<plan-file> [manifest=<manifest-file>]"; exit 1; fi
	ruby scripts/audits/audit-plan-completion.rb plan="$(plan)" $(if $(manifest),manifest="$(manifest)",)

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
	ruby scripts/audits/audit-generated-artifact-freshness.rb

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

plan-code-map:
	@if [ -z "$(plan)" ]; then echo "usage: make plan-code-map plan=<plan-file>"; exit 1; fi
	ruby scripts/audits/generate-plan-code-map.rb plan="$(plan)"

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

audit-sandbox-data-coverage-pack:
	ruby scripts/audits/audit-sandbox-data-coverage-pack.rb

smoke-local-authenticated:
	ruby scripts/audits/smoke-local-authenticated.rb

smoke-local-dashboard:
	ruby scripts/audits/smoke-local-dashboard.rb

closeout-bundle:
	ruby scripts/audits/generate-closeout-bundle.rb manifest="$(manifest)" files="$(files)"

closeout-report:
	@if [ -z "$(manifest)" ]; then echo "usage: make closeout-report manifest=<manifest-file>"; exit 1; fi
	ruby scripts/audits/generate-closeout-report.rb manifest="$(manifest)"

autofill-feature-closeout:
	@if [ -z "$(manifest)" ]; then echo "usage: make autofill-feature-closeout manifest=<manifest-file> [files=<csv>] [generated=<csv>] [docs=<csv>] [ready=true]"; exit 1; fi
	ruby scripts/audits/autofill-feature-closeout.rb manifest="$(manifest)" files="$(files)" generated="$(generated)" docs="$(docs)" $(if $(ready),ready="$(ready)",)

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

audit-local-tooling:
	$(MAKE) audit-change-impact-preflight
	$(MAKE) changeset-risk
	$(MAKE) audit-read-surface-inventory
	$(MAKE) audit-mapper-usage
	$(MAKE) audit-generated-artifact-freshness
	$(MAKE) audit-generated-commit-scope
	$(MAKE) audit-api-contract-drift
	$(MAKE) audit-repository-fetch
	$(MAKE) audit-endpoint-callsite-linker
	$(MAKE) audit-frontend-route-surfaces
	$(MAKE) audit-frontend-stale-surfaces
	$(MAKE) audit-frontend-state-logic-duplication
	$(MAKE) audit-duplicate-logic
	$(MAKE) audit-permission-rule-duplication
	$(MAKE) audit-frontend-dead-code
	$(MAKE) audit-backend-dead-code
	$(MAKE) audit-dead-code
	$(MAKE) audit-state-transition-coverage
	$(MAKE) audit-docs-to-code-drift
	$(MAKE) audit-doc-staleness-scoring
	$(MAKE) audit-architecture-drift
	$(MAKE) architecture-decision-index
	$(MAKE) audit-doc-coverage-gap
	$(MAKE) audit-automation-readiness-gap
	$(MAKE) audit-agent-model-feature-coverage
	$(MAKE) audit-sandbox-generation-coverage
	$(MAKE) audit-domain-ownership-inventory
	$(MAKE) audit-config-sprawl
	$(MAKE) audit-naming-consistency
	$(MAKE) audit-dormant-code
	$(MAKE) audit-manual-cleanup-candidate-report
	$(MAKE) audit-file-relation-graph
	$(MAKE) audit-test-surface-inventory
	$(MAKE) audit-test-fixture-duplication
	$(MAKE) audit-mutation-safety
	$(MAKE) audit-docs-as-tests
	$(MAKE) audit-error-pattern
	$(MAKE) audit-rich-text-safety
	$(MAKE) audit-async-mutation-flow
	$(MAKE) audit-style-token-usage
	$(MAKE) audit-feature-intro-check
	$(MAKE) audit-make-target-index
	$(MAKE) audit-documentation
	$(MAKE) repo-map
	$(MAKE) symbol-index
	$(MAKE) endpoint-contract-packs
	$(MAKE) validation-matrix
	$(MAKE) changeset-playbook
	$(MAKE) resolve-manifest-path
	$(MAKE) link-symbol-to-tests symbol=QuestService
	$(MAKE) dto-usage-pack dto=DashboardSectionsDTO
	$(MAKE) workflow-slice-pack workflow=quest-application
	$(MAKE) plan-code-map plan=.agents/todo-plans/86-codex-local-manifest-path-resolver.md
	$(MAKE) rank-changeset-hotspots
	$(MAKE) domain-pack domain=workmarket
	$(MAKE) recommend-validation-preset
	$(MAKE) recommend-targeted-tests
	$(MAKE) audit-doc-sync-preflight
	$(MAKE) audit-doc-template-coverage
	$(MAKE) audit-doc-sync-duplicates
	$(MAKE) audit-manifest-decision
	$(MAKE) audit-migration-entity-drift
	$(MAKE) audit-test-gap-recommendations
	$(MAKE) audit-contract-test-gaps
	$(MAKE) audit-frontend-usage-graph
	$(MAKE) audit-backend-dependency-graph
	$(MAKE) diff-summary
	$(MAKE) audit-delta-report audit=diff-summary
	$(MAKE) audit-doc-canonical-phrases
	$(MAKE) audit-sandbox-data-coverage-pack
	$(MAKE) api-contract-snapshot
	$(MAKE) failure-knowledge-base
	$(MAKE) test-history-summary
	$(MAKE) codebase-capsule
	$(MAKE) audit-summary-index

audit-local-tooling-incremental:
	$(MAKE) audit-local-tooling

bootstrap-feature-work:
	@if [ -z "$(topic)" ]; then echo "usage: make bootstrap-feature-work topic=<short-feature-topic> [risk=<tier>] [mode=<tiny|normal|feature|agent-workflow|small-change|major-change>] [impact=<cosmetic|contract-neutral-refactor|logic-drift>] [profiles=<csv>] [tier=<workflow-tier|auto>] [manifest=<required|optional|resolver_review|auto>]"; exit 1; fi
	/bin/zsh scripts/bootstrap-feature-work.sh "$(topic)" "$(if $(risk),$(risk),medium)" "$(if $(mode),$(mode),normal)" "$(if $(impact),$(impact),logic-drift)" "$(if $(profiles),$(profiles),backend-logic)" "$(if $(tier),$(tier),auto)" "$(if $(manifest),$(manifest),auto)"
	@if [ "$(discover)" = "true" ]; then ruby scripts/audits/generate-plan-scaffold-discovery.rb topic="$(topic)"; fi

feature-closeout-audit:
	@if [ -z "$(manifest)" ]; then echo "usage: make feature-closeout-audit manifest=<manifest-file>"; exit 1; fi
	/bin/zsh scripts/feature-closeout-audit.sh "$(manifest)"

enforce-feature-closeout:
	@if [ -z "$(manifest)" ]; then echo "usage: make enforce-feature-closeout manifest=<manifest-file>"; exit 1; fi
	ruby scripts/audits/enforce-feature-closeout.rb manifest="$(manifest)"

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
