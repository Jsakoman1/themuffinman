#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "digest"
require "json"
require "open3"
require "set"
require "time"
require "yaml"
require_relative "../local_tooling_common"

module LocalToolingExtendedTools
  extend self

  REPO_ROOT = LocalToolingCommon::REPO_ROOT
  OUT = "docs/generated/local-tooling"
  AUDIT_REGISTRY = [
    ["audit-change-impact-preflight", "scripts/audits/audit-change-impact-preflight.rb", "docs/generated/local-tooling/change-impact-preflight-summary.md"],
    ["changeset-risk", "scripts/audits/score-changeset-risk.rb", "docs/generated/local-tooling/changeset-risk-summary.md"],
    ["audit-router", "scripts/audits/audit-router.rb", "docs/generated/local-tooling/audit-router-summary.md"],
    ["codex-context", "scripts/audits/codex-context.rb", "docs/generated/local-tooling/codex-context/latest.review.md"],
    ["codex-context-explain", "scripts/audits/codex-context.rb", "docs/generated/local-tooling/codex-context/latest.explain.md"],
    ["codex-context-clean", "scripts/audits/codex-context.rb", "docs/generated/local-tooling/codex-context/"],
    ["clean-text-noise", "scripts/audits/clean-text-noise.rb", "docs/generated/local-tooling/clean-text-noise-summary.md"],
    ["context-pack", "scripts/audits/generate-context-pack.rb", "docs/generated/local-tooling/context-packs"],
    ["recommend-feature-slices", "scripts/audits/recommend-feature-slices.rb", "docs/generated/local-tooling/feature-slices/<topic>-summary.md"],
    ["recommend-targeted-tests", "scripts/audits/recommend-targeted-tests.rb", "docs/generated/local-tooling/targeted-tests-summary.md"],
    ["repo-map", "scripts/audits/generate-repo-map.rb", "docs/generated/local-tooling/repo-map-summary.md"],
    ["symbol-index", "scripts/audits/generate-symbol-index.rb", "docs/generated/local-tooling/symbol-index-summary.md"],
    ["endpoint-contract-packs", "scripts/audits/generate-endpoint-contract-packs.rb", "docs/generated/local-tooling/endpoint-contract-packs"],
    ["validation-matrix", "scripts/audits/generate-validation-matrix.rb", "docs/generated/local-tooling/validation-matrix-summary.md"],
    ["changeset-playbook", "scripts/audits/generate-changeset-playbook.rb", "docs/generated/local-tooling/changeset-playbook-summary.md"],
    ["resolve-manifest-path", "scripts/audits/resolve-manifest-path.rb", "docs/generated/local-tooling/manifest-path-resolution-summary.md"],
    ["link-symbol-to-tests", "scripts/audits/link-symbol-to-tests.rb", "docs/generated/local-tooling/symbol-test-links/<symbol-name>-summary.md"],
    ["dto-usage-pack", "scripts/audits/generate-dto-usage-pack.rb", "docs/generated/local-tooling/dto-usage-packs/<dto-name>-summary.md"],
    ["workflow-slice-pack", "scripts/audits/generate-workflow-slice-pack.rb", "docs/generated/local-tooling/workflow-slices/<workflow-id>-summary.md"],
    ["plan-code-map", "scripts/audits/generate-plan-code-map.rb", "docs/generated/local-tooling/plan-code-maps/<plan-id>-summary.md"],
    ["rank-changeset-hotspots", "scripts/audits/rank-changeset-hotspots.rb", "docs/generated/local-tooling/hotspots-summary.md"],
    ["domain-pack", "scripts/audits/generate-domain-pack.rb", "docs/generated/local-tooling/domain-packs/<domain-id>-summary.md"],
    ["audit-doc-sync-preflight", "scripts/audits/audit-doc-sync-preflight.rb", "docs/generated/local-tooling/doc-sync-preflight-summary.md"],
    ["audit-doc-sync-required-surfaces", "scripts/audits/audit-doc-sync-required-surfaces.rb", "docs/generated/local-tooling/doc-sync-required-surfaces-summary.md"],
    ["audit-doc-template-coverage", "scripts/audits/audit-doc-template-coverage.rb", "docs/generated/local-tooling/doc-template-coverage-summary.md"],
    ["audit-doc-sync-duplicates", "scripts/audits/audit-doc-sync-duplicates.rb", "docs/generated/local-tooling/doc-sync-duplicates-summary.md"],
    ["audit-manifest-decision", "scripts/audits/audit-manifest-decision.rb", "docs/generated/local-tooling/manifest-decision-summary.md"],
    ["recommend-validation-preset", "scripts/audits/recommend-validation-preset.rb", "docs/generated/local-tooling/validation-preset-summary.md"],
    ["audit-doc-staleness-scoring", "scripts/audits/audit-doc-staleness-scoring.rb", "docs/generated/local-tooling/doc-staleness-scoring-summary.md"],
    ["audit-architecture-drift", "scripts/audits/audit-architecture-drift.rb", "docs/generated/local-tooling/architecture-drift-summary.md"],
    ["architecture-decision-index", "scripts/audits/generate-architecture-decision-index.rb", "docs/generated/local-tooling/architecture-decision-index-summary.md"],
    ["audit-generated-commit-scope", "scripts/audits/audit-generated-commit-scope.rb", "docs/generated/local-tooling/generated-commit-scope-summary.md"],
    ["audit-migration-entity-drift", "scripts/audits/audit-migration-entity-drift.rb", "docs/generated/local-tooling/migration-entity-drift-audit-summary.md"],
    ["audit-test-gap-recommendations", "scripts/audits/audit-test-gap-recommendations.rb", "docs/generated/local-tooling/test-gap-recommendations-summary.md"],
    ["audit-contract-test-gaps", "scripts/audits/audit-contract-test-gaps.rb", "docs/generated/local-tooling/contract-test-gaps-summary.md"],
    ["audit-mutation-safety", "scripts/audits/audit-mutation-safety.rb", "docs/generated/local-tooling/mutation-safety-summary.md"],
    ["audit-docs-as-tests", "scripts/audits/audit-docs-as-tests.rb", "docs/generated/local-tooling/docs-as-tests-summary.md"],
    ["audit-frontend-usage-graph", "scripts/audits/audit-frontend-usage-graph.rb", "docs/generated/local-tooling/frontend-usage-graph-summary.md"],
    ["audit-backend-dependency-graph", "scripts/audits/audit-backend-dependency-graph.rb", "docs/generated/local-tooling/backend-dependency-graph-summary.md"],
    ["audit-test-fixture-duplication", "scripts/audits/audit-test-fixture-duplication.rb", "docs/generated/local-tooling/test-fixture-duplication-summary.md"],
    ["diff-summary", "scripts/audits/generate-diff-summary.rb", "docs/generated/local-tooling/diff-summary.md"],
    ["session-handoff", "scripts/audits/generate-session-handoff.rb", "docs/generated/local-tooling/session-handoffs"],
    ["plan-scaffold-discovery", "scripts/audits/generate-plan-scaffold-discovery.rb", ".agents/<topic>-plan.md"],
    ["plan-index", "scripts/audits/generate-plan-index.rb", "docs/generated/local-tooling/plan-index-summary.md"],
    ["control-start", "scripts/audits/generate-control-start.rb", "docs/generated/local-tooling/control-start-summary.md"],
    ["audit-summary-index", "scripts/audits/generate-audit-summary-index.rb", "docs/generated/local-tooling/audit-summary-index.md"],
    ["generate-audit-registry-artifacts", "scripts/audits/generate-audit-registry-artifacts.rb", "docs/tooling/codex-local-audits.yml"],
    ["fast-check", "scripts/audits/generate-fast-check-report.rb", "docs/generated/local-tooling/fast-check-report-summary.md"],
    ["diagnose-backend-test", "scripts/audits/diagnose-backend-test.rb", "docs/generated/local-tooling/diagnostics/backend-test-latest-summary.md"],
    ["diagnose-frontend-type-check", "scripts/audits/diagnose-frontend-type-check.rb", "docs/generated/local-tooling/diagnostics/frontend-type-check-latest-summary.md"],
    ["diagnose-frontend-build", "scripts/audits/diagnose-frontend-build.rb", "docs/generated/local-tooling/diagnostics/frontend-build-latest-summary.md"],
    ["test-history-summary", "scripts/audits/generate-test-history-summary.rb", "docs/generated/local-tooling/test-history-summary.md"],
    ["failure-knowledge-base", "scripts/audits/update-failure-knowledge-base.rb", "docs/generated/local-tooling/failure-knowledge-base-summary.md"],
    ["codebase-capsule", "scripts/audits/generate-codebase-capsule.rb", "docs/generated/local-tooling/codebase-capsule.md"],
    ["record-validation", "scripts/audits/record-validation-evidence.rb", "docs/generated/local-tooling/validation-evidence/<feature-id>.json"],
    ["api-contract-snapshot", "scripts/audits/generate-api-contract-snapshot.rb", "docs/generated/local-tooling/api-contract-snapshot-summary.md"],
    ["audit-doc-canonical-phrases", "scripts/audits/audit-doc-canonical-phrases.rb", "docs/generated/local-tooling/doc-canonical-phrases-summary.md"],
    ["audit-validation-memory-drift", "scripts/audits/audit-validation-memory-drift.rb", "docs/generated/local-tooling/validation-memory-drift-summary.md"],
    ["validation-memory-closeout-card", "scripts/audits/generate-validation-memory-closeout-card.rb", "docs/generated/local-tooling/validation-memory-closeout-card-summary.md"],
    ["audit-sandbox-data-coverage-pack", "scripts/audits/audit-sandbox-data-coverage-pack.rb", "docs/generated/local-tooling/sandbox-data-coverage-pack-summary.md"],
    ["smoke-local-authenticated", "scripts/audits/smoke-local-authenticated.rb", "docs/generated/local-tooling/smoke/local-authenticated-latest.json"],
    ["smoke-local-dashboard", "scripts/audits/smoke-local-dashboard.rb", "docs/generated/local-tooling/smoke/local-dashboard-latest.json"],
    ["closeout-bundle", "scripts/audits/generate-closeout-bundle.rb", "docs/generated/local-tooling/closeout-bundle-summary.md"],
    ["closeout-report", "scripts/audits/generate-closeout-report.rb", "docs/generated/local-tooling/closeout-reports/<feature-id>-summary.md"],
    ["autofill-feature-closeout", "scripts/audits/autofill-feature-closeout.rb", "docs/generated/local-tooling/closeout-autofill/<feature-id>-summary.md"],
    ["enforce-feature-closeout", "scripts/audits/enforce-feature-closeout.rb", "docs/generated/local-tooling/closeout-enforcement/<feature-id>-summary.md"],
    ["post-merge-retrospective", "scripts/audits/generate-post-merge-retrospective.rb", "docs/generated/local-tooling/post-merge-retrospectives/latest-summary.md"],
    ["audit-plan-completion", "scripts/audits/audit-plan-completion.rb", "docs/generated/local-tooling/plan-completion/<plan-id>-summary.md"],
    ["audit-delta-report", "scripts/audits/audit-delta-report.rb", "docs/generated/local-tooling/audit-deltas/<audit-id>-summary.md"]
  ].freeze

  DOCS_BY_DOMAIN = {
    "workmarket" => %w[docs/business-logic.md docs/domain-technical.md docs/agent-operating-model.md docs/agent-operating-model.yaml],
    "social" => %w[docs/business-logic.md docs/domain-technical.md docs/agent-operating-model.md docs/agent-operating-model.yaml],
    "identity" => %w[docs/business-logic.md docs/domain-technical.md docs/agent-operating-model.md],
    "location" => %w[docs/business-logic.md docs/domain-technical.md docs/location-services.md docs/agent-operating-model.md docs/agent-operating-model.yaml],
    "chat" => %w[docs/business-logic.md docs/domain-technical.md docs/agent-operating-model.md docs/agent-operating-model.yaml],
    "business" => %w[docs/business-logic.md docs/domain-technical.md docs/agent-operating-model.md],
    "things" => %w[docs/business-logic.md docs/domain-technical.md docs/agent-operating-model.md],
    "rides" => %w[docs/business-logic.md docs/domain-technical.md docs/agent-operating-model.md],
    "agent" => %w[docs/agent-operating-model.md docs/agent-operating-model.yaml docs/domain-technical.md]
  }.freeze

  def changeset_snapshot(files = nil, include_generated: false, include_agents: false)
    selected_files = Array(files).compact.map(&:to_s).reject(&:empty?).uniq
    key = [selected_files.sort, include_generated, include_agents]
    @changeset_snapshot_cache ||= {}
    return deep_copy(@changeset_snapshot_cache[key]) if @changeset_snapshot_cache.key?(key)

    status_entries = LocalToolingCommon.git_status_entries
    all_paths = status_entries.map { |entry| entry[:path] }.select { |path| File.exist?(abs(path)) }.uniq
    filtered = LocalToolingCommon.filter_file_list(
      selected_files.empty? ? all_paths : selected_files,
      include_generated: include_generated,
      include_agents: include_agents
    )
    included = filtered[:included]
    status_map = status_entries.each_with_object({}) do |entry, rows|
      rows[entry[:path]] = entry[:status]
    end
    snapshot = {
      generated_at: now,
      all_changed_files: all_paths,
      included_files: included,
      excluded_files: filtered[:excluded],
      original_file_count: filtered[:original_file_count],
      filtered_file_count: filtered[:filtered_file_count],
      excluded_file_count: filtered[:excluded_file_count],
      status_entries: status_entries.select { |entry| included.include?(entry[:path]) || all_paths.include?(entry[:path]) },
      status_by_path: status_map,
      changed_line_map: included.each_with_object({}) do |path, rows|
        rows[path] = LocalToolingCommon.changed_line_numbers(path, status_map)
      end,
      diff_stats: included.each_with_object({}) do |path, rows|
        rows[path] = LocalToolingCommon.diff_stat_for(path, status_map)
      end,
      source_hashes: included.each_with_object({}) do |path, rows|
        digest = LocalToolingCommon.sha256_for(path)
        rows[path] = digest if digest
      end,
      categories: included.each_with_object({}) do |path, rows|
        rows[path] = category_for(path)
      end,
      domains: included.each_with_object({}) do |path, rows|
        rows[path] = domain_for(path)
      end
    }
    @changeset_snapshot_cache[key] = snapshot
    deep_copy(snapshot)
  end

  def run(mode, argv = ARGV)
    public_send("run_#{mode.tr('-', '_')}", argv)
  end

  def run_context_pack(argv)
    options = parse_key_values(argv)
    topic = slug(options["topic"] || argv.first || "current-change")
    topic = "current-change" if topic.empty?
    files = option_files(options, argv)
    files = infer_topic_files(topic) if files.empty?
    files = changed_files if files.empty?
    filter = filter_files(files, options)
    files = filter[:included]
    budget = context_budget(options["budget"])
    omitted_sections = []
    if files.size > budget[:file_limit]
      omitted_sections << "files truncated from #{files.size} to #{budget[:file_limit]} by #{budget[:id]} budget"
      files = files.first(budget[:file_limit])
    end
    domains = files.map { |path| domain_for(path) }.uniq.sort
    audits = relevant_audit_summaries(files, domains)
    report = {
      generated_at: now,
      topic: topic,
      budget: budget[:id],
      omitted_sections: omitted_sections,
      read_next: budget_read_next(topic, files, omitted_sections),
      original_file_count: filter[:original_file_count],
      filtered_file_count: filter[:filtered_file_count],
      excluded_file_count: filter[:excluded_file_count],
      excluded_files_sample: filter[:excluded].first(25),
      file_count: files.size,
      domains: domains,
      files: files.map { |path| file_entry(path) },
      related_tests: related_tests(files),
      related_docs: domains.flat_map { |domain| DOCS_BY_DOMAIN.fetch(domain, []) }.uniq.sort,
      related_migrations: rel_glob("apps/themuffinman/src/main/resources/db/migration/*.sql").last(10),
      relevant_audits: audits
    }
    write_report("context-packs/#{topic}", "Context Pack #{topic}", report)
  end

  def run_audit_router(argv)
    options = parse_key_values(argv)
    files = argv.empty? || argv.include?("--changed") ? changed_files : option_files(options, argv)
    filter = filter_files(files, options)
    files = filter[:included].uniq
    matrix = validation_rules
    categories = files.map { |path| category_for(path) }.uniq.sort
    audits = router_audits_for(files)
    commands = categories.flat_map { |category| matrix.fetch(category, []) }.uniq
    report = {
      generated_at: now,
      original_file_count: filter[:original_file_count],
      filtered_file_count: filter[:filtered_file_count],
      excluded_file_count: filter[:excluded_file_count],
      excluded_files_sample: filter[:excluded].first(25),
      files: files,
      categories: categories,
      recommended_audits: audits,
      recommended_commands: commands,
      notes: files.empty? ? ["No changed files detected; pass files=<csv> or path args for focused routing."] : []
    }
    write_report("audit-router", "Audit Router", report)
  end

  def run_repo_map(_argv)
    files = rel_glob("apps/themuffinman/src/main/java/**/*.java", "apps/themuffinman/frontend/src/**/*.{ts,vue}", "docs/**/*.md", "scripts/**/*.rb")
    groups = files.group_by { |path| [domain_for(path), category_for(path)] }.map do |(domain, category), paths|
      {domain: domain, category: category, count: paths.size, sample_paths: paths.first(8)}
    end.sort_by { |row| [row[:domain], row[:category]] }
    report = {
      generated_at: now,
      total_files: files.size,
      groups: groups,
      read_first: {
        repository_rules: "AGENTS.md",
        local_tooling: "docs/codex-local-tooling-todo.md",
        business_rules: "docs/business-logic.md",
        technical_rules: "docs/domain-technical.md",
        automation_rules: "docs/agent-operating-model.md"
      }
    }
    write_report("repo-map", "Repo Map", report)
  end

  def run_recommend_feature_slices(argv)
    options = parse_key_values(argv)
    topic = slug(options["topic"] || argv.first || "feature")
    files = option_files(options, argv)
    files = infer_topic_files(topic) if files.empty?
    files = changed_files if files.empty?
    filter = filter_files(files, options)
    files = filter[:included]
    domains = files.map { |path| domain_for(path) }.uniq.sort
    categories = files.map { |path| category_for(path) }.uniq.sort
    backend = files.select { |path| category_for(path).start_with?("backend_") }
    frontend = files.select { |path| category_for(path).start_with?("frontend_") }
    docs = files.select { |path| category_for(path) == "docs" }
    generated = files.select { |path| LocalToolingCommon.generated_path?(path) }
    slices = []
    slices << feature_slice("backend", backend, domains, categories) if backend.any?
    slices << feature_slice("frontend", frontend, domains, categories) if frontend.any?
    slices << {
      id: "docs-and-artifacts",
      purpose: "Update living docs and generated artifacts that move with the implementation.",
      files: (docs + generated + domains.flat_map { |domain| DOCS_BY_DOMAIN.fetch(domain, []) }).uniq.first(30),
      validation: ["make audit-documentation", "make audit-generated-artifact-freshness"]
    }
    slices << {
      id: "final-validation",
      purpose: "Run focused and broad validation after implementation slices are complete.",
      files: [],
      validation: files.flat_map { |path| validation_rules.fetch(category_for(path), []) }.uniq.first(12)
    }
    report = {
      generated_at: now,
      topic: topic,
      domains: domains,
      categories: categories,
      original_file_count: filter[:original_file_count],
      filtered_file_count: filter[:filtered_file_count],
      files_considered: files.first(80),
      slices: slices,
      read_next: [
        "Run `make context-pack topic=#{topic}` before editing if more file context is needed.",
        "Run `make audit-router files=<csv>` after the first implementation slice.",
        "Keep slices sequential; avoid mixing backend, frontend, generated artifacts, and final validation in one edit pass unless the change is tiny."
      ]
    }
    write_report("feature-slices/#{topic}", "Feature Slices #{topic}", report)
  end

  def run_recommend_targeted_tests(argv)
    options = parse_key_values(argv)
    files = option_files(options, argv)
    files = changed_files if files.empty?
    filter = filter_files(files, options)
    files = filter[:included]
    domains = files.map { |path| domain_for(path) }.uniq.sort
    categories = files.map { |path| category_for(path) }.uniq.sort
    direct_tests = related_tests(files).first(12)
    commands = targeted_backend_commands(files, direct_tests)
    commands += targeted_frontend_commands(files, categories)
    commands += targeted_docs_commands(files, categories)
    commands += targeted_generated_commands(files)
    commands += targeted_scenario_commands(domains)
    commands = dedupe_command_rows(commands).first(14)
    report = {
      generated_at: now,
      original_file_count: filter[:original_file_count],
      filtered_file_count: filter[:filtered_file_count],
      excluded_file_count: filter[:excluded_file_count],
      files_considered: files.first(80),
      domains: domains,
      categories: categories,
      direct_tests: direct_tests,
      recommended_commands: commands,
      residual_risk: targeted_residual_risk(files, domains, categories, commands),
      notes: [
        "This is a targeted recommendation report, not a replacement for full validation.",
        "Use full `cd apps/themuffinman && ./mvnw test` for high-risk backend behavior, schema, or broad cross-domain changes."
      ]
    }
    write_report("targeted-tests", "Targeted Tests", report)
  end

  def run_symbol_index(_argv)
    corpus = read_all("apps/themuffinman/src/main/java") + "\n" + read_all("apps/themuffinman/frontend/src")
    java = rel_glob("apps/themuffinman/src/main/java/**/*.java").map do |path|
      content = read(path)
      class_name = content[/\b(class|enum|interface|record)\s+([A-Z][A-Za-z0-9_]*)/, 2] || File.basename(path, ".java")
      methods = content.scan(/(?:public|protected|private)\s+(?:static\s+)?[A-Za-z0-9_<>, ?\[\]]+\s+([a-z][A-Za-z0-9_]*)\s*\(/).flatten.uniq.first(40)
      {symbol: class_name, kind: "java", file: path, category: category_for(path), domain: domain_for(path), methods: methods, refs: ref_count(class_name, corpus)}
    end
    frontend = rel_glob("apps/themuffinman/frontend/src/**/*.{ts,vue}").flat_map do |path|
      content = read(path)
      names = content.scan(/export\s+(?:default\s+)?(?:function|const|class|interface|type)\s+([A-Za-z0-9_]+)/).flatten
      names << File.basename(path, ".*") if path.end_with?(".vue")
      names.uniq.map { |name| {symbol: name, kind: "frontend", file: path, category: category_for(path), domain: domain_for(path), refs: ref_count(name, corpus)} }
    end
    report = {generated_at: now, symbol_count: java.size + frontend.size, symbols: (java + frontend).sort_by { |row| [row[:domain], row[:symbol]] }}
    write_report("symbol-index", "Symbol Index", report)
  end

  def run_endpoint_contract_packs(_argv)
    endpoints = endpoint_entries
    by_family = endpoints.group_by { |entry| endpoint_family(entry[:path]) }
    written = by_family.map do |family, rows|
      dto_names = rows.flat_map { |row| row[:dtos] }.uniq
      payload = {
        generated_at: now,
        endpoint_family: family,
        endpoints: rows,
        dto_files: dto_names.flat_map { |dto| rel_glob("apps/themuffinman/src/main/java/**/*#{dto}.java") }.uniq.sort,
        frontend_callsites: frontend_callsites_for(rows.map { |row| row[:path] }),
        docs: DOCS_BY_DOMAIN.fetch(family, [])
      }
      write_report("endpoint-contract-packs/#{family}", "Endpoint Contract Pack #{family}", payload, terminal: false)
      "docs/generated/local-tooling/endpoint-contract-packs/#{family}.json"
    end
    write_report("endpoint-contract-packs/index", "Endpoint Contract Packs", {generated_at: now, pack_count: written.size, packs: written})
  end

  def run_validation_matrix(_argv)
    report = {generated_at: now, categories: validation_rules.map { |category, commands| {category: category, commands: commands} }}
    write_report("validation-matrix", "Validation Matrix", report)
  end

  def run_changeset_playbook(argv)
    options = parse_key_values(argv)
    files = option_files(options, argv)
    files = changed_files if files.empty?
    filter = filter_files(files, options)
    files = filter[:included]
    manifest = manifest_decision_for(files)
    manifest_resolution = resolve_manifest_path_for(files)
    preset = validation_preset_for(files, manifest, manifest_resolution)
    docs = doc_sync_rows_for(files)
    report = {
      generated_at: now,
      original_file_count: filter[:original_file_count],
      filtered_file_count: filter[:filtered_file_count],
      excluded_file_count: filter[:excluded_file_count],
      excluded_files_sample: filter[:excluded].first(25),
      changed_file_count: files.size,
      categories: files.map { |path| category_for(path) }.uniq.sort,
      domains: files.map { |path| domain_for(path) }.uniq.sort,
      files: files,
      manifest_decision: manifest,
      manifest_resolution: manifest_resolution,
      validation_preset: preset,
      doc_targets: docs.flat_map { |row| row[:likely_docs] }.uniq.sort,
      source_reports: [
        "docs/generated/local-tooling/diff-summary.md",
        "docs/generated/local-tooling/audit-router-summary.md",
        "docs/generated/local-tooling/doc-sync-preflight-summary.md",
        "docs/generated/local-tooling/doc-sync-required-surfaces-summary.md",
        "docs/generated/local-tooling/manifest-decision-summary.md",
        "docs/generated/local-tooling/manifest-path-resolution-summary.md",
        "docs/generated/local-tooling/validation-preset-summary.md"
      ],
      ordered_actions: changeset_playbook_steps(files, manifest, preset, docs)
    }
    write_report("changeset-playbook", "Changeset Playbook", report)
  end

  def run_resolve_manifest_path(argv)
    options = parse_key_values(argv)
    files = option_files(options, argv)
    files = changed_files if files.empty?
    filter = filter_files(files, options)
    files = filter[:included]
    report = resolve_manifest_path_for(files).merge(
      generated_at: now,
      original_file_count: filter[:original_file_count],
      filtered_file_count: filter[:filtered_file_count],
      excluded_file_count: filter[:excluded_file_count],
      excluded_files_sample: filter[:excluded].first(25),
      files_considered: files.first(120)
    )
    write_report("manifest-path-resolution", "Manifest Path Resolution", report)
  end

  def run_doc_sync_preflight(argv)
    files = argv.empty? ? changed_files : argv
    rows = doc_sync_rows_for(files)
    report = {generated_at: now, files: rows, unique_docs: rows.flat_map { |row| row[:likely_docs] }.uniq.sort}
    write_report("doc-sync-preflight", "Doc Sync Preflight", report)
  end

  def run_doc_sync_required_surfaces(argv)
    options = parse_key_values(argv)
    files = option_files(options, argv)
    snapshot =
      if files.empty?
        changeset_snapshot(nil, include_generated: truthy?(options["include_generated"]), include_agents: truthy?(options["include_agents"]))
      else
        changeset_snapshot(files, include_generated: truthy?(options["include_generated"]), include_agents: truthy?(options["include_agents"]))
      end
    files = snapshot[:included_files]
    manifest = manifest_decision_for(files)
    manifest_resolution = resolve_manifest_path_for(files)
    preset = validation_preset_for(files, manifest, manifest_resolution)
    rows = doc_sync_rows_for(files)
    report = {
      generated_at: now,
      original_file_count: snapshot[:original_file_count],
      filtered_file_count: snapshot[:filtered_file_count],
      excluded_file_count: snapshot[:excluded_file_count],
      excluded_files_sample: snapshot[:excluded_files].first(25),
      changed_file_count: files.size,
      files: rows,
      required_docs: rows.flat_map { |row| row[:likely_docs] }.uniq.sort,
      required_generated_artifacts: rows.flat_map { |row| Array(row[:generated_artifacts]) }.uniq.sort,
      required_validation_commands: rows.flat_map { |row| Array(row[:validation_commands]) }.uniq.sort,
      recommended_audits: router_audits_for(files),
      manifest_decision: manifest,
      manifest_resolution: manifest_resolution,
      validation_preset: preset,
      residual_risk: rows.flat_map { |row| Array(row[:residual_risk]) }.uniq.sort
    }
    write_report("doc-sync-required-surfaces", "Doc Sync Required Surfaces", report)
  end

  def run_validation_preset(argv)
    options = parse_key_values(argv)
    files = option_files(options, argv)
    files = changed_files if files.empty?
    filter = filter_files(files, options)
    files = filter[:included]
    manifest = manifest_decision_for(files)
    manifest_resolution = resolve_manifest_path_for(files)
    report = validation_preset_for(files, manifest, manifest_resolution).merge(
      generated_at: now,
      original_file_count: filter[:original_file_count],
      filtered_file_count: filter[:filtered_file_count],
      excluded_file_count: filter[:excluded_file_count],
      excluded_files_sample: filter[:excluded].first(25),
      files_considered: files.first(120)
    )
    write_report("validation-preset", "Validation Preset", report)
  end

  def run_link_symbol_to_tests(argv)
    options = parse_key_values(argv)
    symbol = (options["symbol"] || argv.reject { |arg| arg.include?("=") || arg.start_with?("--") }.first).to_s.strip
    raise "usage: link-symbol-to-tests symbol=<symbol-name>" if symbol.empty?

    symbol_files = symbol_definition_files(symbol)
    direct_tests = direct_symbol_tests(symbol)
    nearby_tests = related_tests(symbol_files).reject { |path| direct_tests.include?(path) }.first(20)
    domains = symbol_files.map { |path| domain_for(path) }.uniq
    scenario_hits = regression_scenarios.select do |scenario|
      scenario_files = Array(scenario["test_files"])
      (scenario_files & (direct_tests + nearby_tests)).any? || domains.include?(scenario["domain"])
    end
    report = {
      generated_at: now,
      symbol: symbol,
      symbol_files: symbol_files.map { |path| file_entry(path) },
      direct_tests: direct_tests,
      nearby_tests: nearby_tests,
      scenario_hits: scenario_hits.map do |scenario|
        {
          id: scenario["id"],
          domain: scenario["domain"],
          risk: scenario["risk"],
          scenario: scenario["scenario"],
          test_files: Array(scenario["test_files"]),
          commands: Array(scenario["commands"])
        }
      end,
      recommended_commands: symbol_test_commands(symbol_files, direct_tests, nearby_tests, scenario_hits),
      residual_risk: symbol_test_residual_risk(symbol_files, direct_tests, nearby_tests, scenario_hits)
    }
    write_report("symbol-test-links/#{slug(symbol)}", "Symbol Test Links #{symbol}", report)
  end

  def run_dto_usage_pack(argv)
    options = parse_key_values(argv)
    dto = (options["dto"] || argv.reject { |arg| arg.include?("=") || arg.start_with?("--") }.first).to_s.strip
    raise "usage: dto-usage-pack dto=<dto-name>" if dto.empty?

    dto_files = rel_glob("apps/themuffinman/src/main/java/**/*#{dto}.java")
    backend_refs = rel_glob("apps/themuffinman/src/main/java/**/*.java").select { |path| read(path).match?(/\b#{Regexp.escape(dto)}\b/) }
    frontend_refs = rel_glob("apps/themuffinman/frontend/src/**/*.{ts,vue}").select { |path| read(path).match?(/\b#{Regexp.escape(dto)}\b/) }
    controller_methods = endpoint_entries.select { |entry| Array(entry[:dtos]).include?(dto) }
    tests = rel_glob("apps/themuffinman/src/test/java/**/*.java").select { |path| read(path).match?(/\b#{Regexp.escape(dto)}\b/) }.first(30)
    docs_refs = rel_glob("docs/**/*.md", "docs/**/*.yaml").select { |path| read(path).match?(/\b#{Regexp.escape(dto)}\b/) }.first(20)
    generated_contract_refs = rel_glob("apps/themuffinman/frontend/src/contracts/**/*.ts", "docs/generated/**/*.json").select { |path| read(path).match?(/\b#{Regexp.escape(dto)}\b/) }.first(20)
    report = {
      generated_at: now,
      dto: dto,
      dto_files: dto_files.map { |path| file_entry(path) },
      controller_methods: controller_methods,
      backend_refs: backend_refs.first(30).map { |path| file_entry(path) },
      frontend_refs: frontend_refs.first(30).map { |path| file_entry(path) },
      tests: tests,
      docs_refs: docs_refs,
      generated_contract_refs: generated_contract_refs,
      recommended_commands: dto_usage_commands(controller_methods, frontend_refs, tests),
      residual_risk: dto_usage_residual_risk(dto_files, controller_methods, frontend_refs, tests)
    }
    write_report("dto-usage-packs/#{slug(dto)}", "DTO Usage Pack #{dto}", report)
  end

  def run_workflow_slice_pack(argv)
    options = parse_key_values(argv)
    workflow = (options["workflow"] || argv.reject { |arg| arg.include?("=") || arg.start_with?("--") }.first).to_s.strip
    raise "usage: workflow-slice-pack workflow=<workflow-id>" if workflow.empty?

    machine = workflow_machine_for(workflow)
    service_files = workflow_service_files(machine)
    tests = workflow_related_tests(machine, workflow)
    frontend_actions = workflow_frontend_actions(machine, workflow)
    docs_refs = workflow_doc_refs(machine, workflow)
    report = {
      generated_at: now,
      workflow: workflow,
      state_machine: machine,
      service_files: service_files.map { |path| file_entry(path) },
      tests: tests,
      frontend_actions: frontend_actions,
      docs_refs: docs_refs,
      recommended_commands: workflow_commands(tests, machine),
      residual_risk: workflow_residual_risk(machine, service_files, tests)
    }
    write_report("workflow-slices/#{slug(workflow)}", "Workflow Slice #{workflow}", report)
  end

  def run_plan_code_map(argv)
    options = parse_key_values(argv)
    plan = (options["plan"] || argv.reject { |arg| arg.include?("=") || arg.start_with?("--") }.first).to_s.strip
    raise "usage: plan-code-map plan=<plan-file>" if plan.empty?
    raise "plan file not found: #{plan}" unless File.file?(abs(plan))

    plan_text = read(plan)
    plan_id = File.basename(plan, ".md")
    files = plan_candidate_files(plan, plan_text)
    categories = files.map { |path| category_for(path) }.uniq.sort
    domains = files.map { |path| domain_for(path) }.uniq.sort
    manifest_resolution = resolve_manifest_path_for(files)
    report = {
      generated_at: now,
      plan: plan,
      plan_id: plan_id,
      categories: categories,
      domains: domains,
      mapped_files: files.map { |path| plan_file_map_entry(path, plan_text) },
      likely_docs: doc_sync_rows_for(files).flat_map { |row| row[:likely_docs] }.uniq.sort,
      related_generated_artifacts: plan_related_generated_artifacts(plan_text, files),
      related_audits: router_audits_for(files),
      recommended_commands: validation_preset_for(files, manifest_decision_for(files), manifest_resolution)[:commands],
      related_manifests: plan_related_manifests(plan, plan_text, files, manifest_resolution),
      residual_risk: plan_map_residual_risk(files, manifest_resolution)
    }
    write_report("plan-code-maps/#{slug(plan_id)}", "Plan Code Map #{plan_id}", report)
  end

  def run_rank_changeset_hotspots(argv)
    options = parse_key_values(argv)
    files = option_files(options, argv)
    files = changed_files if files.empty?
    filter = filter_files(files, options)
    files = filter[:included]
    report = rank_changeset_hotspots_for(files).merge(
      generated_at: now,
      original_file_count: filter[:original_file_count],
      filtered_file_count: filter[:filtered_file_count],
      excluded_file_count: filter[:excluded_file_count],
      excluded_files_sample: filter[:excluded].first(25)
    )
    write_report("hotspots", "Changeset Hotspots", report)
  end

  def run_domain_pack(argv)
    options = parse_key_values(argv)
    domain = (options["domain"] || argv.reject { |arg| arg.include?("=") || arg.start_with?("--") }.first).to_s.strip
    raise "usage: domain-pack domain=<domain-id>" if domain.empty?
    raise "unknown domain: #{domain}" unless known_domains.include?(domain)

    runtime_files = domain_runtime_files(domain)
    categories = runtime_files.map { |path| category_for(path) }.uniq.sort
    workflows = workflow_state_machines.select { |machine| machine["domain"].to_s == domain }
    scenario_rows = regression_scenarios.select { |scenario| scenario["domain"].to_s == domain }
    tests = domain_related_tests(domain, runtime_files, workflows, scenario_rows)
    report = {
      generated_at: now,
      domain: domain,
      file_count: runtime_files.size,
      categories: categories,
      key_services: domain_key_backend_files(domain, runtime_files, "service"),
      key_controllers: domain_key_backend_files(domain, runtime_files, "controller"),
      key_repositories: domain_key_backend_files(domain, runtime_files, "repository"),
      key_models: domain_key_backend_files(domain, runtime_files, "model"),
      dto_groups: domain_dto_groups(domain, runtime_files),
      frontend_surfaces: domain_frontend_surfaces(domain, runtime_files),
      workflows: workflows.map do |machine|
        {
          id: machine["id"],
          owner: machine["owner"],
          states: Array(machine["states"]).size,
          transitions: Array(machine["transitions"]).size
        }
      end,
      docs: DOCS_BY_DOMAIN.fetch(domain, default_domain_docs(domain)),
      tests: tests.first(20),
      scenario_catalog: scenario_rows.map do |scenario|
        {
          id: scenario["id"],
          risk: scenario["risk"],
          scenario: scenario["scenario"],
          test_files: Array(scenario["test_files"]).first(6),
          commands: Array(scenario["commands"]).first(2)
        }
      end,
      first_commands: domain_first_commands(domain, runtime_files, tests, scenario_rows),
      residual_risk: domain_pack_residual_risk(domain, runtime_files, workflows, tests)
    }
    write_report("domain-packs/#{slug(domain)}", "Domain Pack #{domain}", report)
  end

  def run_migration_entity_drift(_argv)
    entities = rel_glob("apps/themuffinman/src/main/java/**/*/*.java").select { |path| read(path).include?("@Entity") }
    migrations = rel_glob("apps/themuffinman/src/main/resources/db/migration/*.sql")
    migration_text = migrations.map { |path| read(path).downcase }.join("\n")
    rows = entities.map do |path|
      content = read(path)
      class_name = File.basename(path, ".java")
      table = content[/@Table\(name\s*=\s*"([^"]+)"/, 1] || camel_to_snake(class_name)
      fields = content.scan(/(?:private|protected)\s+[A-Za-z0-9_<>, ?]+\s+([a-z][A-Za-z0-9_]*)\s*[;=]/).flatten.uniq
      missing_fields = fields.reject { |field| migration_text.include?(camel_to_snake(field)) || %w[id createdAt updatedAt].include?(field) }
      {entity: class_name, file: path, table: table, table_in_migration: migration_text.include?(table.downcase), fields_without_obvious_migration_match: missing_fields.first(20)}
    end
    report = {generated_at: now, migration_count: migrations.size, entities: rows, review_needed: rows.select { |row| !row[:table_in_migration] || row[:fields_without_obvious_migration_match].any? }}
    write_report("migration-entity-drift-audit", "Migration Entity Drift Audit", report)
  end

  def run_test_gap_recommendations(argv)
    files = argv.empty? ? changed_files : argv
    tests = rel_glob("apps/themuffinman/src/test/java/**/*.java")
    rows = files.map do |path|
      base = File.basename(path, ".*").sub(/(Service|Controller|Mapper|Mgr)\z/, "")
      nearest = tests.select { |test| test.downcase.include?(base.downcase) || test.downcase.include?(domain_for(path)) }.first(12)
      {file: path, category: category_for(path), domain: domain_for(path), nearest_tests: nearest, suggested_commands: validation_rules.fetch(category_for(path), [])}
    end
    report = {generated_at: now, files: rows, missing_direct_tests: rows.select { |row| row[:nearest_tests].empty? }}
    write_report("test-gap-recommendations", "Test Gap Recommendations", report)
  end

  def run_frontend_usage_graph(_argv)
    files = rel_glob("apps/themuffinman/frontend/src/**/*.{ts,vue}")
    nodes = files.map { |path| {file: path, category: category_for(path), domain: domain_for(path)} }
    edges = files.flat_map do |path|
      read(path).scan(/from\s+['"]([^'"]+)['"]/).flatten.map { |target| {from: path, to: target} }
    end
    report = {generated_at: now, node_count: nodes.size, edge_count: edges.size, nodes: nodes, edges: edges.first(1000)}
    write_report("frontend-usage-graph", "Frontend Usage Graph", report)
  end

  def run_backend_dependency_graph(_argv)
    files = rel_glob("apps/themuffinman/src/main/java/**/*.java")
    nodes = files.map { |path| {file: path, class_name: File.basename(path, ".java"), category: category_for(path), domain: domain_for(path)} }
    edges = files.flat_map do |path|
      content = read(path)
      injected = content.scan(/(?:private\s+final|private)\s+([A-Z][A-Za-z0-9_]*(?:Service|Repository|Mgr|Mapper|Client|UseCase|Assembler|Validator|Properties))\s+[a-z]/).flatten
      used = content.scan(/\b([A-Z][A-Za-z0-9_]*(?:DTO|Entity|Status|Type|Role))\b/).flatten
      (injected + used).uniq.map { |target| {from: File.basename(path, ".java"), to: target, file: path} }
    end
    report = {generated_at: now, node_count: nodes.size, edge_count: edges.size, nodes: nodes, edges: edges.first(1500)}
    write_report("backend-dependency-graph", "Backend Dependency Graph", report)
  end

  def run_diff_summary(_argv)
    options = parse_key_values(_argv)
    snapshot = changeset_snapshot(nil, include_generated: truthy?(options["include_generated"]), include_agents: truthy?(options["include_agents"]))
    entries = snapshot[:status_entries]
    included = snapshot[:included_files].to_set
    entries = entries.select { |entry| included.include?(entry[:path]) }
    report = {
      generated_at: now,
      original_file_count: snapshot[:original_file_count],
      filtered_file_count: snapshot[:filtered_file_count],
      excluded_file_count: snapshot[:excluded_file_count],
      excluded_files_sample: snapshot[:excluded_files].first(25),
      changed_file_count: entries.size,
      groups: entries.group_by { |entry| [domain_for(entry[:path]), category_for(entry[:path])] }.map { |(domain, category), rows| {domain: domain, category: category, count: rows.size, files: rows} },
      recommended_audits: router_audits_for(entries.map { |entry| entry[:path] })
    }
    write_report("diff-summary", "Diff Summary", report, summary_path: "#{OUT}/diff-summary.md")
  end

  def run_session_handoff(argv)
    options = parse_key_values(argv)
    topic = slug(options["topic"] || argv.first || "current-work")
    files = changed_files
    filter = filter_files(files, options)
    files = filter[:included]
    budget = context_budget(options["budget"])
    omitted_sections = []
    if files.size > budget[:file_limit]
      omitted_sections << "changed files truncated from #{files.size} to #{budget[:file_limit]} by #{budget[:id]} budget"
      files = files.first(budget[:file_limit])
    end
    report = {
      generated_at: now,
      topic: topic,
      budget: budget[:id],
      omitted_sections: omitted_sections,
      read_next: budget_read_next(topic, files, omitted_sections),
      original_file_count: filter[:original_file_count],
      filtered_file_count: filter[:filtered_file_count],
      excluded_file_count: filter[:excluded_file_count],
      excluded_files_sample: filter[:excluded].first(25),
      changed_files: files,
      plans: rel_glob(".agents/*#{topic}*plan.md"),
      manifests: rel_glob(".agents/feature-manifests/*#{topic}*.yaml"),
      recommended_audits: router_audits_for(files),
      recommended_commands: files.flat_map { |path| validation_rules.fetch(category_for(path), []) }.uniq
    }
    write_report("session-handoffs/#{topic}", "Session Handoff #{topic}", report)
  end

  def run_plan_scaffold_discovery(argv)
    options = parse_key_values(argv)
    topic = slug(options["topic"] || argv.first || "feature")
    plan = ".agents/#{topic}-plan.md"
    run_context_pack(["topic=#{topic}"])
    return puts "Plan not found: #{plan}" unless File.exist?(abs(plan))

    append = [
      "",
      "## Local Discovery",
      "",
      "- Context pack: `docs/generated/local-tooling/context-packs/#{topic}-summary.md`",
      "- Suggested first pass: run `make audit-router` after the first implementation slice.",
      "- Suggested closeout: run `make closeout-bundle manifest=.agents/feature-manifests/#{topic}-manifest.yaml` when the feature is ready for final validation.",
      ""
    ].join("\n")
    File.write(abs(plan), File.read(abs(plan)) + append)
    puts "Updated #{plan} with local discovery notes"
  end

  def run_audit_summary_index(_argv)
    summaries = rel_glob("docs/generated/local-tooling/**/*summary.md", "docs/generated/dead-code-audit/**/*summary.md").map do |path|
      {path: path, mtime: File.exist?(abs(path)) ? File.mtime(abs(path)).utc.iso8601 : nil, bytes: File.exist?(abs(path)) ? File.size(abs(path)) : 0}
    end
    registry = AUDIT_REGISTRY.map do |target, script, output|
      {target: target, script: script, output: output, output_exists: File.exist?(abs(output))}
    end
    payload = {generated_at: now, registry: registry, summaries: summaries}
    write_report("audit-summary-index", "Audit Summary Index", payload, summary_path: "#{OUT}/audit-summary-index.md")
    LocalToolingCommon.write_text("#{OUT}/audit-summary-index.md", audit_summary_index_markdown(payload))
  end

  def run_plan_index(_argv)
    entries = plan_index_entries
    open_entries = entries.select { |entry| entry[:status] != "complete" }
    open_master_plans = open_entries.select { |entry| entry[:kind] == "master-plan" }
    open_plans = open_entries.reject { |entry| entry[:kind] == "master-plan" }
    payload = {
      generated_at: now,
      total_count: entries.size,
      open_count: open_entries.size,
      master_plan_count: entries.count { |entry| entry[:kind] == "master-plan" },
      god_plan_count: entries.count { |entry| entry[:kind] == "god-plan" },
      open_master_plans: open_master_plans,
      open_plans: open_plans,
      entries: open_entries
    }
    json_path = "#{OUT}/plan-index.json"
    summary_path = "#{OUT}/plan-index-summary.md"
    LocalToolingCommon.write_json(json_path, payload)
    LocalToolingCommon.write_text(summary_path, plan_index_markdown(payload))
    update_audit_cache("plan-index", json_path, payload)
    puts terminal_line("Plan Index", payload, json_path, summary_path)
  end

  def run_control_start(_argv)
    plan_index = read_json_if_present("#{OUT}/plan-index.json") || {}
    audit_summary_index = read_json_if_present("#{OUT}/audit-summary-index.json") || {}
    codex_context_review = File.exist?(abs("#{OUT}/codex-context/latest.review.md")) ? "#{OUT}/codex-context/latest.review.md" : nil

    payload = {
      generated_at: now,
      plan_index: {
        path: "#{OUT}/plan-index.json",
        summary_path: "#{OUT}/plan-index-summary.md",
        total_count: plan_index["total_count"],
        open_count: plan_index["open_count"],
        open_master_plans: Array(plan_index["open_master_plans"]).first(5),
        open_plans: Array(plan_index["open_plans"]).first(10)
      },
      audit_summary_index: {
        path: "#{OUT}/audit-summary-index.json",
        summary_path: "#{OUT}/audit-summary-index.md",
        registry_entries: audit_summary_index["registry_entries"],
        tracked_outputs: audit_summary_index["tracked_outputs"],
        missing_outputs: audit_summary_index["missing_outputs"]
      },
      codex_context_review: codex_context_review,
      next_action: "Use `make codex-context topic=<topic> intent='<intent>'` for topic-specific broad work once this control snapshot is fresh."
    }

    json_path = "#{OUT}/control-start.json"
    summary_path = "#{OUT}/control-start-summary.md"
    LocalToolingCommon.write_json(json_path, payload)
    LocalToolingCommon.write_text(summary_path, control_start_markdown(payload))
    update_audit_cache("control-start", json_path, payload)
    puts terminal_line("Control Start", payload, json_path, summary_path)
  end

  def plan_index_entries
    markdown_plans = rel_glob(".agents/*-plan.md").map { |path| plan_index_entry_for_markdown(path) }.compact
    god_plans = rel_glob(".agents/god-plans/*.yaml").map { |path| plan_index_entry_for_god_plan(path) }.compact
    (markdown_plans + god_plans).sort_by { |entry| [status_sort_key(entry[:status]), entry[:kind], entry[:path]] }
  end

  def plan_index_entry_for_markdown(path)
    content = File.read(abs(path))
    frontmatter = LocalToolingCommon.markdown_frontmatter(content)
    status = plan_index_status_from_markdown(content)
    {
      path: path,
      kind: path.include?("/god-plans/") ? "god-plan" : (File.basename(path).include?("master-plan") ? "master-plan" : "plan"),
      status: status,
      title: frontmatter["machine_title"].to_s.strip.empty? ? plan_index_title_from_markdown(content) : frontmatter["machine_title"].to_s.strip,
      goal: frontmatter["machine_goal"].to_s.strip.empty? ? plan_index_section_first_line(content, "Goal") : frontmatter["machine_goal"].to_s.strip,
      child_plans: plan_index_section_count(content, "Child Plans"),
      open_tasks: content.scan(/^\s*-\s*\[\s\]/).size
    }
  rescue StandardError
    nil
  end

  def plan_index_entry_for_god_plan(path)
    markdown_path = path.sub(/\.yaml\z/, ".md")
    if File.exist?(abs(markdown_path))
      content = File.read(abs(markdown_path))
      frontmatter = LocalToolingCommon.markdown_frontmatter(content)
      status = plan_index_status_from_markdown(content)
      return {
        path: path,
        kind: "god-plan",
        status: status,
        title: frontmatter["machine_title"].to_s.strip.empty? ? plan_index_title_from_markdown(content) : frontmatter["machine_title"].to_s.strip,
        goal: frontmatter["machine_goal"].to_s.strip.empty? ? (plan_index_section_first_line(content, "Purpose") || plan_index_section_first_line(content, "Goal")) : frontmatter["machine_goal"].to_s.strip,
        child_plans: plan_index_section_count(content, "Master Plans"),
        open_tasks: content.scan(/^\s*-\s\[[ xX]\]/).size
      }
    end

    data = YAML.load_file(abs(path))
    return nil unless data.is_a?(Hash)

    status = data["status"].to_s.downcase
    {
      path: path,
      kind: "god-plan",
      status: status.empty? ? "unknown" : status,
      title: data["title"].to_s,
      goal: data["objective"].to_s,
      child_plans: Array(data["masterPlans"]).size,
      open_tasks: 0
    }
  rescue StandardError
    nil
  end

  def plan_index_status_from_markdown(content)
    frontmatter_status = LocalToolingCommon.markdown_frontmatter_value(content, "machine_status")
    normalized = frontmatter_status.to_s.strip.downcase
    return "complete" if %w[complete completed done closed].include?(normalized)
    return "active" if %w[active in-progress in_progress draft pending].include?(normalized)

    status = content[/^## Status\s*$([\s\S]*?)(?=^## |\z)/, 1]
    value = status.to_s.lines.map(&:strip).reject(&:empty?).first.to_s
    normalized = value.sub(/\.$/, "").downcase
    return "complete" if %w[complete completed done closed].include?(normalized)
    return "active" if %w[active in-progress in_progress draft pending].include?(normalized)

    normalized.empty? ? "unknown" : normalized
  end

  def plan_index_title_from_markdown(content)
    content[/^#\s+(.+)$/, 1].to_s.strip
  end

  def plan_index_section_first_line(content, section_name)
    section = content[/^## #{Regexp.escape(section_name)}\s*$([\s\S]*?)(?=^## |\z)/, 1]
    return nil unless section

    section.lines.map(&:strip).reject(&:empty?).first
  end

  def plan_index_section_count(content, section_name)
    section = content[/^## #{Regexp.escape(section_name)}\s*$([\s\S]*?)(?=^## |\z)/, 1]
    return 0 unless section

    section.lines.count { |line| line.match?(/^\s*\d+\.\s+/) || line.match?(/^\s*-\s+/) }
  end

  def status_sort_key(status)
    case status.to_s
    when "active", "open" then 0
    when "draft", "pending", "in-progress", "in_progress" then 1
    when "complete", "completed", "done", "closed" then 2
    else 3
    end
  end

  def plan_index_markdown(payload)
    lines = []
    lines << "# Plan Index"
    lines << ""
    lines << "- Total entries: #{payload[:total_count]}"
    lines << "- Open entries: #{payload[:open_count]}"
    lines << "- Open master plans: #{payload[:open_master_plans].size}"
    lines << "- Open regular plans: #{payload[:open_plans].size}"
    lines << ""
    lines << "## Open Master Plans"
    lines << ""
    payload[:open_master_plans].first(20).each do |entry|
      lines << "- `#{entry[:path]}` | `#{entry[:status]}` | #{entry[:goal] || entry[:title]}"
    end
    lines << "- ... and #{payload[:open_master_plans].size - 20} more" if payload[:open_master_plans].size > 20
    lines << ""
    lines << "## Open Plans"
    lines << ""
    payload[:open_plans].first(20).each do |entry|
      lines << "- `#{entry[:path]}` | `#{entry[:status]}` | #{entry[:goal] || entry[:title]}"
    end
    lines << "- ... and #{payload[:open_plans].size - 20} more" if payload[:open_plans].size > 20
    lines << ""
    lines << "_Routing aid only. Use the underlying plan file or plan-completion report for final status._"
    lines.join("\n")
  end

  def control_start_markdown(payload)
    lines = []
    lines << "# Control Start"
    lines << ""
    lines << "- Plan index: `#{payload.dig(:plan_index, :summary_path)}`"
    lines << "- Audit summary index: `#{payload.dig(:audit_summary_index, :summary_path)}`"
    lines << "- Plan count: `#{payload.dig(:plan_index, :total_count)}`"
    lines << "- Open count: `#{payload.dig(:plan_index, :open_count)}`"
    lines << "- Open master plans: `#{Array(payload.dig(:plan_index, :open_master_plans)).size}`"
    lines << "- Open plans: `#{Array(payload.dig(:plan_index, :open_plans)).size}`"
    lines << "- Codex context review: `#{payload[:codex_context_review] || 'none'}`"
    lines << ""
    lines << "## Open Master Plans"
    lines << ""
    Array(payload.dig(:plan_index, :open_master_plans)).each do |entry|
      lines << "- `#{entry["path"] || entry[:path]}` | `#{entry["status"] || entry[:status]}` | #{entry["goal"] || entry[:goal] || entry["title"] || entry[:title]}"
    end
    lines << ""
    lines << "## Open Plans"
    lines << ""
    Array(payload.dig(:plan_index, :open_plans)).each do |entry|
      lines << "- `#{entry["path"] || entry[:path]}` | `#{entry["status"] || entry[:status]}` | #{entry["goal"] || entry[:goal] || entry["title"] || entry[:title]}"
    end
    lines << ""
    lines.join("\n")
  end

  def audit_summary_index_markdown(payload)
    registry = Array(payload[:registry])
    summaries = Array(payload[:summaries])
    tracked = registry.count { |entry| entry[:output_exists] }
    missing = registry.count - tracked
    lines = []
    lines << "# Audit Summary Index"
    lines << ""
    lines << "- Registry entries: #{registry.size}"
    lines << "- Tracked outputs: #{tracked}"
    lines << "- Missing outputs: #{missing}"
    lines << "- Summary files: #{summaries.size}"
    lines << ""
    lines << "## Registry"
    lines << ""
    registry.first(12).each do |entry|
      status = entry[:output_exists] ? "tracked" : "missing"
      lines << "- `#{entry[:target]}` -> `#{entry[:output]}` (`#{status}`)"
    end
    lines << "- ... and #{registry.size - 12} more" if registry.size > 12
    lines << ""
    lines << "## Summary Files"
    lines << ""
    summaries.first(12).each do |entry|
      lines << "- `#{entry[:path]}` | #{entry[:mtime]} | #{entry[:bytes]} bytes"
    end
    lines << "- ... and #{summaries.size - 12} more" if summaries.size > 12
    lines << ""
    lines << "_Routing aid only. Use the underlying generated report or source file for current state._"
    lines.join("\n")
  end

  def run_audit_registry_artifacts(_argv)
    yaml = ["# Generated local audit registry", "audits:"]
    AUDIT_REGISTRY.each do |target, script, output|
      yaml << "- target: #{target}"
      yaml << "  script: #{script}"
      yaml << "  primaryOutput: #{output}"
    end
    LocalToolingCommon.write_text("docs/tooling/codex-local-audits.yml", yaml.join("\n") + "\n")
    run_audit_summary_index([])
    write_report("audit-registry-artifacts", "Audit Registry Artifacts", {generated_at: now, audit_count: AUDIT_REGISTRY.size, registry_path: "docs/tooling/codex-local-audits.yml"})
  end

  def run_fast_check_report(argv)
    options = parse_key_values(argv)
    files = option_files(options, argv)
    files = changed_files if files.empty?
    filter = filter_files(files, options)
    files = filter[:included]
    audits = router_audits_for(files).first(10)
    commands = files.flat_map { |path| validation_rules.fetch(category_for(path), []) }.uniq.first(8)
    report = {
      generated_at: now,
      original_file_count: filter[:original_file_count],
      filtered_file_count: filter[:filtered_file_count],
      excluded_file_count: filter[:excluded_file_count],
      excluded_files_sample: filter[:excluded].first(25),
      files: files,
      audits_to_run: audits,
      commands_to_run: commands,
      note: "Advisory fast-check plan only; run full validation for behavior changes."
    }
    write_report("fast-check-report", "Fast Check Report", report)
  end

  def run_api_contract_snapshot(_argv)
    report = {generated_at: now, endpoint_count: endpoint_entries.size, endpoints: endpoint_entries}
    write_report("api-contract-snapshot", "API Contract Snapshot", report)
  end

  def run_failure_knowledge_base(argv)
    options = parse_key_values(argv)
    sources = option_source_reports(options)
    observations = sources.flat_map { |path| diagnostic_observations(path) }
    entries = failure_patterns.map do |pattern|
      matches = observations.select { |observation| observation[:line].match?(pattern[:regex]) }
      pattern.merge(
        regex: pattern[:regex].source,
        source_reports: matches.map { |match| match[:source_report] }.uniq.sort,
        matched_examples: matches.map { |match| match[:line] }.uniq.first(5),
        observed: matches.any?
      )
    end
    unmatched = observations.reject do |observation|
      failure_patterns.any? { |pattern| observation[:line].match?(pattern[:regex]) }
    end
    report = {
      generated_at: now,
      source_reports: sources,
      entry_count: entries.size,
      observed_entry_count: entries.count { |entry| entry[:observed] },
      entries: entries.map { |entry| entry.reject { |key, _| key == :regex } },
      review_needed: unmatched.first(25),
      notes: [
        "This index stores compact failure patterns and fixes, not full logs.",
        "Use diagnostic reports as the source of truth for exact command output."
      ]
    }
    write_report("failure-knowledge-base", "Failure Knowledge Base", report)
  end

  def run_test_history_summary(_argv)
    history = test_history_runs
    report = test_history_report(history)
    write_test_history_files(report)
    puts terminal_line("Test History", {rows: report[:runs]}, "#{OUT}/test-history.json", "#{OUT}/test-history-summary.md")
  end

  def run_codebase_capsule(_argv)
    report = {
      generated_at: now,
      purpose: "Very small read-first context for a new Codex session before broad repository discovery.",
      repo_layout: [
        "apps/themuffinman: current Spring Boot app and Vue frontend",
        "apps/themuffinman/src/main/java/com/themuffinman/app: backend domains and layers",
        "apps/themuffinman/src/main/resources/db/migration: Flyway migrations",
        "apps/themuffinman/frontend: frontend app",
        "services: planned shared backend capabilities",
        "docs: living documentation and agent operating artifacts",
        ".agents: temporary plans, manifests, and validation evidence"
      ],
      active_conventions: [
        "Keep business rules, permissions, validations, workflows, and state transitions in backend services.",
        "Keep controllers thin and frontend logic minimal.",
        "Use new Flyway migrations for schema changes; do not edit old migrations.",
        "Update living docs and generated agent artifacts with logic or contract changes.",
        "Use `.agents/*-plan.md` for multi-file, multi-layer, high-risk, or broad autonomous work.",
        "Do not commit or push unless the user explicitly asks."
      ],
      open_backlog_ids: open_backlog_ids,
      open_master_plan_items: open_master_plan_items.first(12),
      preferred_first_commands: [
        "ruby scripts/todo-audit.rb",
        "make diff-summary",
        "make audit-summary-index"
      ],
      read_next: [
        "AGENTS.md",
        "docs/codex-local-tooling-todo.md",
        "docs/domain-technical.md"
      ]
    }
    LocalToolingCommon.write_json("#{OUT}/codebase-capsule.json", report)
    LocalToolingCommon.write_text("#{OUT}/codebase-capsule.md", codebase_capsule_markdown(report))
    update_audit_cache("codebase-capsule", "#{OUT}/codebase-capsule.json", report)
    puts terminal_line("Codebase Capsule", {rows: report[:repo_layout]}, "#{OUT}/codebase-capsule.json", "#{OUT}/codebase-capsule.md")
  end

  def run_doc_canonical_phrases(_argv)
    yaml = read("docs/agent-operating-model.yaml")
    docs = rel_glob("docs/**/*.md")
    phrases = yaml.scan(/must_contain_all:\s*\n((?:\s+- .+\n)+)/).flat_map do |block|
      block.first.lines.map { |line| line.sub(/^\s+-\s*/, "").strip.delete_prefix('"').delete_suffix('"') }
    end.uniq
    rows = phrases.map do |phrase|
      {phrase: phrase, present_in: docs.select { |path| read(path).include?(phrase) }}
    end
    write_report("doc-canonical-phrases", "Doc Canonical Phrases", {generated_at: now, phrase_count: rows.size, phrases: rows, missing: rows.select { |row| row[:present_in].empty? }})
  end

  def run_sandbox_data_coverage_pack(_argv)
    code = read_all("apps/themuffinman/src/main/java")
    docs = read_all("docs") + read_all(".agents")
    entities = rel_glob("apps/themuffinman/src/main/java/**/*DTO.java", "apps/themuffinman/src/main/java/**/*Status.java").map { |path| File.basename(path, ".java") }.uniq.sort
    rows = entities.map do |name|
      {entity_or_contract: name, code_refs: code.scan(/\b#{Regexp.escape(name)}\b/).size, sandbox_docs_refs: docs.scan(/\b#{Regexp.escape(name)}\b/i).size}
    end
    write_report("sandbox-data-coverage-pack", "Sandbox Data Coverage Pack", {generated_at: now, rows: rows, review_needed: rows.select { |row| row[:sandbox_docs_refs].zero? }.first(100)})
  end

  def run_smoke_local_authenticated(argv)
    write_smoke_placeholder("local-authenticated", argv, "Opt-in smoke target placeholder. Configure local auth token/cookie before turning this into live HTTP checks.")
  end

  def run_smoke_local_dashboard(argv)
    write_smoke_placeholder("local-dashboard", argv, "Opt-in dashboard smoke target placeholder. Intended endpoint: http://localhost:8080/dashboard/me.")
  end

  def run_closeout_bundle(argv)
    options = parse_key_values(argv)
    files = option_files(options, argv)
    files = changed_files if files.empty?
    report = {
      generated_at: now,
      manifest: options["manifest"],
      files: files,
      audits: router_audits_for(files),
      commands: files.flat_map { |path| validation_rules.fetch(category_for(path), []) }.uniq,
      final_checks: ["make audit-summary-index", "make validation-memory-closeout-card", options["manifest"] ? "make feature-closeout-audit manifest=#{options['manifest']}" : nil].compact,
      compact_memory: {
        closeout_card: "docs/generated/local-tooling/validation-memory-closeout-card-summary.md",
        drift_audit: "docs/generated/local-tooling/validation-memory-drift-summary.md"
      }
    }
    write_report("closeout-bundle", "Closeout Bundle", report)
  end

  def run_autofill_feature_closeout(argv)
    options = parse_key_values(argv)
    manifest_path = (options["manifest"] || argv.reject { |arg| arg.include?("=") || arg.start_with?("--") }.first).to_s.strip
    raise "usage: autofill-feature-closeout manifest=<manifest-file> [files=<csv>] [generated=<csv>] [docs=<csv>] [ready=true]" if manifest_path.empty?
    raise "manifest not found: #{manifest_path}" unless File.file?(abs(manifest_path))

    manifest = YAML.load_file(abs(manifest_path)) || {}
    files = option_files(options, argv)
    snapshot =
      if files.empty?
        changeset_snapshot(nil, include_generated: true, include_agents: true)
      else
        changeset_snapshot(files, include_generated: true, include_agents: true)
      end
    changed_files_for_closeout = snapshot[:all_changed_files]
    doc_rows = doc_sync_rows_for(changed_files_for_closeout)
    generated_paths = csv_list(options["generated"]) + changed_files_for_closeout.select { |path| LocalToolingCommon.generated_path?(path) }
    doc_paths = csv_list(options["docs"]) + changed_files_for_closeout.select do |path|
      path.start_with?("docs/") || path.start_with?(".agents/") || path == "AGENTS.md"
    end
    plan_file = manifest["planFile"].to_s
    plan_open_tasks = plan_file.empty? || !File.exist?(abs(plan_file)) ? 0 : File.readlines(abs(plan_file)).count { |line| line.start_with?("- [ ]") }
    command_results = Array(manifest.dig("validationEvidence", "commands"))
    passed_commands = command_results.select { |entry| entry.is_a?(Hash) && entry["result"] == "passed" }.map { |entry| entry["command"].to_s }

    manifest["docDelta"] ||= {}
    manifest["docDelta"]["docsUpdated"] = (Array(manifest.dig("docDelta", "docsUpdated")) + doc_paths).uniq.sort
    manifest["docDelta"]["intentionallyUnchanged"] ||= []
    manifest["generatedArtifacts"] ||= {}
    manifest["generatedArtifacts"]["refreshedPaths"] = (Array(manifest.dig("generatedArtifacts", "refreshedPaths")) + generated_paths).uniq.sort
    manifest["generatedArtifacts"]["notApplicableReason"] =
      if manifest["generatedArtifacts"]["refreshedPaths"].empty?
        "No generated artifacts were refreshed by this change."
      else
        "Generated artifacts are listed in refreshedPaths."
      end

    manifest["artifacts"] ||= {}
    manifest["artifacts"]["docPaths"] = (Array(manifest.dig("artifacts", "docPaths")) + doc_paths).uniq.sort
    manifest["artifacts"]["codePaths"] ||= []
    manifest["artifacts"]["testPaths"] ||= []
    manifest["artifacts"]["generatorCommands"] ||= []
    manifest["artifacts"]["auditCommands"] ||= []

    manifest["planCompletion"] ||= {}
    manifest["planCompletion"]["reviewed"] = plan_open_tasks.zero?
    manifest["planCompletion"]["openTasks"] = plan_open_tasks
    manifest["planCompletion"]["summary"] = plan_open_tasks.zero? ? "Plan has no open checkbox tasks." : "Plan still has #{plan_open_tasks} open checkbox task(s)."

    manifest["checklist"] ||= {}
    manifest["checklist"]["tempPlanCreated"] = true
    manifest["checklist"]["codeImplemented"] = changed_files_for_closeout.any? { |path| path.start_with?("scripts/") || path.include?("/src/") || path.end_with?(".java") || path.end_with?(".rb") || path.end_with?(".mjs") }
    manifest["checklist"]["docsSynced"] = manifest["artifacts"]["docPaths"].any?
    manifest["checklist"]["agentModelSynced"] = (Array(manifest["artifacts"]["docPaths"]) & %w[docs/agent-operating-model.md docs/agent-operating-model.yaml]).any?
    manifest["checklist"]["backendTestsPassed"] = passed_commands.any? { |command| command.match?(%r{mvnw .*test|./mvnw test|make audit-agent-safety}) }
    manifest["checklist"]["frontendValidationPassed"] = passed_commands.any? { |command| command.include?("npm --prefix apps/themuffinman/frontend run type-check") } &&
      passed_commands.any? { |command| command.include?("npm --prefix apps/themuffinman/frontend run build") }
    manifest["checklist"]["destructivePolicyChecked"] = manifest.dig("checklist", "destructivePolicyChecked") != false
    manifest["checklist"]["multilingualCoverageChecked"] = manifest.dig("checklist", "multilingualCoverageChecked") != false

    manifest["backlog"] ||= {}
    manifest["backlog"]["reviewed"] = manifest.dig("backlog", "reviewed") == true
    manifest["backlog"]["createdIds"] ||= []
    manifest["backlog"]["resolvedIds"] ||= []

    if truthy?(options["ready"])
      manifest["closeoutDecision"] ||= {}
      manifest["closeoutDecision"]["status"] = "ready"
      manifest["closeoutDecision"]["reason"] = "Autofill prepared the manifest and the caller marked it ready for final audit."
    else
      manifest["closeoutDecision"] ||= {}
      manifest["closeoutDecision"]["status"] ||= "deferred"
      manifest["closeoutDecision"]["reason"] ||= "Autofill updated evidence and artifact paths; review before final closeout."
    end

    LocalToolingCommon.write_text(manifest_path, YAML.dump(manifest).sub(/\A---\n/, ""))
    feature_id = manifest["featureId"].to_s.empty? ? File.basename(manifest_path, ".yaml").sub(/-manifest\z/, "") : manifest["featureId"].to_s
    report = {
      generated_at: now,
      manifest: manifest_path,
      feature_id: feature_id,
      changed_file_count: changed_files_for_closeout.size,
      changed_files: changed_files_for_closeout.first(80),
      required_docs: doc_rows.flat_map { |row| row[:likely_docs] }.uniq.sort,
      required_generated_artifacts: doc_rows.flat_map { |row| Array(row[:generated_artifacts]) }.uniq.sort,
      plan_open_tasks: plan_open_tasks,
      closeout_status: manifest.dig("closeoutDecision", "status"),
      validation_command_count: command_results.size
    }
    write_report("closeout-autofill/#{slug(feature_id)}", "Closeout Autofill #{feature_id}", report)
  end

  def run_post_merge_retrospective(argv)
    options = parse_key_values(argv)
    topic = slug(options["topic"] || argv.first || "latest")
    files = option_files(options, argv)
    files = changed_files if files.empty?
    audit_data = {
      change_impact: read_json_if_present("#{OUT}/change-impact-preflight.json"),
      architecture_drift: read_json_if_present("#{OUT}/architecture-drift.json"),
      doc_coverage_gap: read_json_if_present("#{OUT}/doc-coverage-gap-audit.json"),
      doc_staleness: read_json_if_present("#{OUT}/doc-staleness-scoring.json"),
      automation_readiness_gap: read_json_if_present("#{OUT}/automation-readiness-gap-audit.json"),
      test_gap_recommendations: read_json_if_present("#{OUT}/test-gap-recommendations.json")
    }
    report = {
      generated_at: now,
      topic: topic,
      changed_file_count: files.size,
      changed_file_groups: files.group_by { |path| [domain_for(path), category_for(path)] }.map do |(domain, category), rows|
        {domain: domain, category: category, count: rows.size}
      end.sort_by { |row| [row[:domain], row[:category]] },
      failure_points: retrospective_failure_points(audit_data),
      missing_tools: retrospective_missing_tools,
      docs_gaps: retrospective_docs_gaps(audit_data),
      reusable_patterns: retrospective_reusable_patterns(files, audit_data),
      next_commands: ["make audit-change-impact-preflight", "make audit-summary-index", "make closeout-bundle", "make post-merge-retrospective topic=#{topic}"]
    }
    write_retrospective_report(topic, report)
  end

  def run_audit_delta_report(argv)
    options = parse_key_values(argv)
    audit_id = (options["audit"] || argv.first).to_s.strip
    raise "usage: audit-delta-report audit=<audit-id>" if audit_id.empty?

    current_output = audit_json_output_for(audit_id)
    current = read_json_if_present(current_output)
    previous_snapshot = latest_history_snapshot(audit_id)
    previous = previous_snapshot && read_json_if_present(previous_snapshot[:path])
    report = {
      generated_at: now,
      audit: audit_id,
      current_output: current_output,
      current_exists: !current.nil?,
      previous_output: previous_snapshot && previous_snapshot[:path],
      previous_exists: !previous.nil?,
      history_entries: history_snapshots_for(audit_id).last(10).map { |entry| entry[:path] },
      changed: current && previous ? Digest::SHA256.hexdigest(JSON.generate(current)) != Digest::SHA256.hexdigest(JSON.generate(previous)) : nil,
      current_generated_at: current && (current["generated_at"] || current[:generated_at]),
      previous_generated_at: previous && (previous["generated_at"] || previous[:generated_at]),
      introduced_risks: current && previous ? (risk_signals_from_report(current) - risk_signals_from_report(previous)).first(30) : [],
      fixed_risks: current && previous ? (risk_signals_from_report(previous) - risk_signals_from_report(current)).first(30) : [],
      count_deltas: count_deltas(previous, current),
      field_deltas: top_level_field_deltas(previous, current),
      notes: audit_delta_notes(current_output, current, previous)
    }
    write_report("audit-deltas/#{audit_id}", "Audit Delta #{audit_id}", report)
  end

  def run_diagnostic(argv, command_name)
    command = diagnostic_command(command_name)
    started_at = Time.now
    started = started_at.utc.iso8601
    stdout, stderr, status = Open3.capture3(*command, chdir: command_workdir(command_name))
    duration_seconds = (Time.now - started_at).round(3)
    failures = (stdout + "\n" + stderr).lines.select { |line| line.match?(/FAIL|ERROR|Exception|Cannot find|TS\d+|BUILD FAILED/i) }.first(80)
    report = {generated_at: started, command: command.join(" "), success: status.success?, exit_status: status.exitstatus, duration_seconds: duration_seconds, failure_lines: failures, changed_files: changed_files}
    name = command_name.tr("_", "-")
    write_report("diagnostics/#{name}-latest", "Diagnostic #{name}", report)
    record_test_history(command_name, report)
    exit(status.exitstatus || 1) unless status.success?
  end

  private

  def read_json_if_present(path)
    absolute = abs(path)
    return nil unless File.exist?(absolute)

    JSON.parse(File.read(absolute))
  rescue JSON::ParserError
    nil
  end

  def write_retrospective_report(topic, payload)
    base = "#{OUT}/post-merge-retrospectives/#{topic}"
    json_path = "#{base}.json"
    summary_path = "#{base}-summary.md"
    LocalToolingCommon.write_json(json_path, payload)
    LocalToolingCommon.write_text(summary_path, retrospective_markdown(payload))
    update_audit_cache("post-merge-retrospective/#{topic}", json_path, payload)
    puts terminal_line("Post Merge Retrospective", {changed_file_count: payload[:changed_file_count]}, json_path, summary_path)
  end

  def retrospective_failure_points(audit_data)
    points = []
    guardrails = audit_data.dig(:change_impact, "scope_guardrail_warnings") || []
    points += guardrails.map { |warning| "Scope guardrail: #{warning["id"] || warning[:id] || warning}" }.first(5)
    architecture = audit_data[:architecture_drift] || {}
    total_findings = architecture["total_findings"] || architecture["findings"]&.size
    points << "Architecture drift candidates: #{total_findings}" if total_findings.to_i.positive?
    stale = read_json_if_present("#{OUT}/generated-artifact-freshness.json")
    stale_count = stale && (stale["stale"] || stale["stale_count"])
    points << "Generated artifacts stale: #{stale_count}" if stale_count.to_i.positive?
    points = diagnostic_failure_points if points.empty?
    points.empty? ? ["No failed validation or guardrail signal was found in available local reports."] : points.uniq.first(8)
  end

  def diagnostic_failure_points
    rel_glob("docs/generated/local-tooling/diagnostics/*latest.json").flat_map do |path|
      report = read_json_if_present(path)
      next [] unless report && report["success"] == false

      failures = report["failure_lines"] || []
      cleaned = LocalToolingCommon.clean_text_output(failures.join("\n"), max_lines: 3, aggressive: true)
      cleaned.lines.first(3).map { |line| "#{File.basename(path, ".json")}: #{line.strip}" }
    end
  end

  def retrospective_missing_tools
    local = read("docs/codex-local-tooling-todo.md")
    agent = read("docs/agent-improvement-backlog.md")
    ids = (local + "\n" + agent).scan(/- \[ \] ([A-Z0-9-]+):/).flatten
    ids.first(10).map { |id| "Open improvement/tooling backlog: #{id}" }
  rescue StandardError
    ["Open improvement/tooling backlog could not be read."]
  end

  def retrospective_docs_gaps(audit_data)
    gaps = []
    doc_gap = audit_data[:doc_coverage_gap] || {}
    gap_count = doc_gap["gaps"]&.size || doc_gap["gap_count"] || doc_gap["gaps"]
    gaps << "Doc coverage gap candidates: #{gap_count}" if gap_count.to_i.positive?
    staleness = audit_data[:doc_staleness] || {}
    candidates = staleness["candidates"]&.size || staleness["candidate_count"] || staleness["candidates"]
    gaps << "Doc staleness candidates: #{candidates}" if candidates.to_i.positive?
    gaps.empty? ? ["No documentation gap signal was found in available local reports."] : gaps.uniq.first(8)
  end

  def retrospective_reusable_patterns(files, audit_data)
    patterns = []
    categories = files.map { |path| category_for(path) }.uniq
    patterns << "Run router-selected audits before full validation for #{categories.sort.join(', ')} changes." if categories.any?
    patterns << "Use report-first architecture drift findings to plan refactors separately from feature work." if audit_data[:architecture_drift]
    patterns << "Keep generated local reports refreshed through `make audit-local-tooling-incremental` after adding audit targets." if files.any? { |path| path.start_with?("scripts/") || path == "Makefile" }
    patterns << "Record backlog removal and plan completion in the same change that implements a persistent item." if files.any? { |path| path.start_with?(".agents/") || path.include?("backlog") }
    patterns.uniq.first(8)
  end

  def retrospective_markdown(payload)
    lines = ["# Post Merge Retrospective", ""]
    lines << "- Topic: `#{payload[:topic]}`"
    lines << "- Changed files: `#{payload[:changed_file_count]}`"
    lines << ""
    {
      "Failure Points" => payload[:failure_points],
      "Missing Tools" => payload[:missing_tools],
      "Docs Gaps" => payload[:docs_gaps],
      "Reusable Patterns" => payload[:reusable_patterns],
      "Next Commands" => payload[:next_commands]
    }.each do |heading, values|
      lines << "## #{heading}"
      lines << ""
      values.first(10).each { |value| lines << "- #{value}" }
      lines << ""
    end
    lines.join("\n")
  end

  def option_source_reports(options)
    source = options["source"]
    reports = source.to_s.empty? ? rel_glob("docs/generated/local-tooling/diagnostics/*latest.json") : [source]
    reports.select { |path| File.exist?(abs(path)) }.uniq.sort
  end

  def record_test_history(command_name, diagnostic_report)
    runs = test_history_runs
    runs << {
      "recorded_at" => now,
      "diagnostic" => command_name.tr("_", "-"),
      "command" => diagnostic_report[:command],
      "success" => diagnostic_report[:success],
      "exit_status" => diagnostic_report[:exit_status],
      "duration_seconds" => diagnostic_report[:duration_seconds],
      "failing_tests" => failing_tests_from_lines(diagnostic_report[:failure_lines]),
      "top_error_patterns" => error_patterns_from_lines(diagnostic_report[:failure_lines]),
      "changed_file_count" => Array(diagnostic_report[:changed_files]).size
    }
    report = test_history_report(runs.last(100))
    write_test_history_files(report)
  rescue StandardError
    nil
  end

  def test_history_runs
    report = read_json_if_present("#{OUT}/test-history.json")
    Array(report && report["runs"])
  end

  def test_history_report(runs)
    runs = runs.last(100)
    failures = runs.reject { |run| run["success"] }
    {
      generated_at: now,
      run_count: runs.size,
      failure_count: failures.size,
      success_count: runs.count { |run| run["success"] },
      slowest_runs: runs.sort_by { |run| -run["duration_seconds"].to_f }.first(10),
      repeated_failures: repeated_values(failures.flat_map { |run| Array(run["top_error_patterns"]) }),
      repeated_failing_tests: repeated_values(failures.flat_map { |run| Array(run["failing_tests"]) }),
      runs: runs
    }
  end

  def write_test_history_files(report)
    LocalToolingCommon.write_json("#{OUT}/test-history.json", report)
    LocalToolingCommon.write_text("#{OUT}/test-history-summary.md", test_history_markdown(report))
    update_audit_cache("test-history-summary", "#{OUT}/test-history.json", report)
  end

  def failing_tests_from_lines(lines)
    Array(lines).flat_map do |line|
      line.scan(/\b([A-Z][A-Za-z0-9_]*(?:Test|IT))\b/).flatten
    end.uniq.first(20)
  end

  def error_patterns_from_lines(lines)
    Array(lines).map do |line|
      normalized = line.to_s.strip
      normalized = normalized.gsub(%r{/Users/[^ ]+}, "<path>")
      normalized = normalized.gsub(/\b\d+\b/, "<n>")
      normalized[0, 180]
    end.reject(&:empty?).uniq.first(20)
  end

  def repeated_values(values)
    values.each_with_object(Hash.new(0)) { |value, counts| counts[value] += 1 }
          .select { |_, count| count > 1 }
          .sort_by { |value, count| [-count, value] }
          .first(20)
          .map { |value, count| {value: value, count: count} }
  end

  def test_history_markdown(report)
    lines = ["# Test History", ""]
    lines << "- Decision: `#{report[:failure_count].to_i.zero? ? "healthy" : "investigate"}`"
    lines << "- Why: failures=#{report[:failure_count]}, successes=#{report[:success_count]}"
    lines << "- Next action: `make audit-validation-evidence-quality`" if report[:failure_count].to_i.positive?
    lines << "- Evidence: runs=#{report[:run_count]}"
    lines << ""
    lines << "## Slowest Runs"
    lines << ""
    report[:slowest_runs].first(3).each { |run| lines << "- `#{run['diagnostic']}` #{run['duration_seconds']}s success=#{run['success']}" }
    lines << ""
    lines << "## Recent Runs"
    lines << ""
    report[:runs].last(3).reverse.each { |run| lines << "- `#{run['diagnostic']}` success=#{run['success']} duration=#{run['duration_seconds']}s" }
    lines.join("\n") + "\n"
  end

  def open_backlog_ids
    files = ["docs/codex-local-tooling-todo.md", "docs/implementation-backlog.md", "docs/agent-improvement-backlog.md"]
    files.flat_map do |path|
      read(path).scan(/- \[ \] ([A-Z0-9-]+):/).flatten
    end.uniq.sort
  end

  def open_master_plan_items
    read(".agents/todo-master-plan.md").lines.map do |line|
      next unless line.start_with?("- [ ] ")

      line.sub("- [ ] ", "").strip
    end.compact
  end

  def codebase_capsule_markdown(report)
    lines = ["# Codebase Capsule", ""]
    lines << "- Decision: use this capsule before broad repo discovery"
    lines << "- Why: #{report[:open_backlog_ids].empty? ? "no open backlog ids" : "#{report[:open_backlog_ids].size} open backlog ids"}"
    lines << "- Next action: #{report[:preferred_first_commands].first(3).map { |command| "`#{command}`" }.join(", ")}"
    lines << "- Evidence: repo_layout=#{report[:repo_layout].size}, conventions=#{report[:active_conventions].size}, backlog=#{report[:open_backlog_ids].size}"
    lines << ""
    lines << "## Read Next"
    lines << ""
    report[:read_next].first(4).each { |value| lines << "- #{value}" }
    lines.join("\n") + "\n"
  end

  def diagnostic_observations(path)
    report = read_json_if_present(path)
    return [] unless report

    lines = report["failure_lines"] || report[:failure_lines] || []
    lines.map do |line|
      {
        source_report: path,
        command: report["command"] || report[:command],
        exit_status: report["exit_status"] || report[:exit_status],
        line: line.to_s.strip
      }
    end.reject { |row| row[:line].empty? }
  end

  def failure_patterns
    [
      {
        id: "backend-java-compilation",
        pattern: "Maven or javac reports a Java compilation error.",
        owning_surface: "backend Java",
        likely_cause: "A Java contract, import, symbol, method signature, or generated type changed without updating all call sites.",
        verified_fix: "Update the affected Java references, then rerun `./mvnw test` from `apps/themuffinman`.",
        regex: /COMPILATION ERROR|cannot find symbol|package .* does not exist|Failed to execute goal .*maven-compiler-plugin/i
      },
      {
        id: "frontend-typescript-contract",
        pattern: "TypeScript or vue-tsc reports a TS diagnostic.",
        owning_surface: "frontend TypeScript/Vue",
        likely_cause: "Frontend props, API DTO types, generated contracts, or composable return values are out of sync.",
        verified_fix: "Align the frontend type or generated contract usage, then rerun `npm run type-check` from `apps/themuffinman/frontend`.",
        regex: /TS\d{4}|vue-tsc|Type .* is not assignable|Property .* does not exist/i
      },
      {
        id: "frontend-build-contract",
        pattern: "Frontend build reports a Vite, Rollup, or module-resolution failure.",
        owning_surface: "frontend build",
        likely_cause: "A module path, export, asset reference, or build-time type dependency changed without matching build wiring.",
        verified_fix: "Fix the import/export or asset path, then rerun `npm run build` from `apps/themuffinman/frontend`.",
        regex: /vite|rollup|Cannot find module|Could not resolve|BUILD FAILED/i
      },
      {
        id: "flyway-schema-drift",
        pattern: "Flyway, SQL, or schema validation reports a migration drift.",
        owning_surface: "backend persistence",
        likely_cause: "Entity or repository behavior expects schema that is missing from forward-only Flyway migrations.",
        verified_fix: "Add a new Flyway migration instead of editing old migrations, then rerun `./mvnw test`.",
        regex: /Flyway|Migration|schema-validation|relation .* does not exist|column .* does not exist|SQLSyntaxError/i
      },
      {
        id: "lazy-dto-mapping",
        pattern: "Hibernate reports a lazy loading failure during DTO mapping.",
        owning_surface: "backend read DTO assembly",
        likely_cause: "A read path maps associations after the persistence session closed or uses a repository fetch pattern that omits required associations.",
        verified_fix: "Fix the service-level DTO assembly or repository fetch path, audit sibling read surfaces using the same mapper, then rerun backend tests.",
        regex: /LazyInitializationException|could not initialize proxy|failed to lazily initialize/i
      },
      {
        id: "documentation-sync-canonical-phrase",
        pattern: "Documentation or agent-safety validation reports protected wording drift.",
        owning_surface: "living docs and agent operating artifacts",
        likely_cause: "A protected documentation-sync phrase was paraphrased, omitted, or changed outside the canonical YAML rule.",
        verified_fix: "Copy the canonical sentence exactly into required targets, then rerun `make audit-documentation` and `make audit-agent-safety`.",
        regex: /canonical|protected phrase|must_contain_all|documentation sync|AgentOperatingModelValidationTest/i
      }
    ]
  end

  def write_smoke_placeholder(name, argv, note)
    payload = {generated_at: now, smoke: name, status: "not_run", args: argv, note: note}
    LocalToolingCommon.write_json("#{OUT}/smoke/#{name}-latest.json", payload)
    LocalToolingCommon.write_text("#{OUT}/smoke/#{name}-latest-summary.md", "# Smoke #{name}\n\n- Status: `not_run`\n- Note: #{note}\n")
    puts "Smoke #{name}: not_run"
  end

  def write_report(base_name, title, payload, terminal: true, summary_path: nil)
    json_path = "#{OUT}/#{base_name}.json"
    md_path = summary_path || "#{OUT}/#{base_name}-summary.md"
    if report_cache_hit?(base_name, json_path, md_path, payload)
      puts terminal_line(title, payload, json_path, md_path) if terminal
      return
    end

    archive_previous_report(base_name, json_path, payload)
    LocalToolingCommon.write_json(json_path, payload)
    LocalToolingCommon.write_text(md_path, summary_markdown(title, payload))
    update_audit_cache(base_name, json_path, payload)
    puts terminal_line(title, payload, json_path, md_path) if terminal
  end

  def archive_previous_report(base_name, json_path, payload)
    absolute_json = abs(json_path)
    return unless File.exist?(absolute_json)

    current_content = File.read(absolute_json)
    next_content = JSON.pretty_generate(payload) + "\n"
    return if Digest::SHA256.hexdigest(current_content) == Digest::SHA256.hexdigest(next_content)

    timestamp = now.gsub(":", "-")
    LocalToolingCommon.write_text("#{OUT}/.history/#{base_name}/#{timestamp}.json", current_content)
  rescue StandardError
    nil
  end

  def update_audit_cache(base_name, json_path, payload)
    @audit_cache_mutex ||= Mutex.new
    @audit_cache_mutex.synchronize do
      cache_path = "#{OUT}/.cache/audit-inputs.json"
      cache = File.exist?(abs(cache_path)) ? JSON.parse(File.read(abs(cache_path))) : {}
      cache[base_name] = {
        "updated_at" => now,
        "output" => json_path,
        "cache_status" => "refreshed",
        "payload_checksum" => Digest::SHA256.hexdigest(JSON.generate(payload)),
        "semantic_checksum" => semantic_payload_checksum(payload)
      }
      LocalToolingCommon.write_json(cache_path, cache)
    end
  rescue StandardError
    nil
  end

  def report_cache_hit?(base_name, json_path, md_path, payload)
    absolute_json = abs(json_path)
    absolute_md = abs(md_path)
    return false unless File.exist?(absolute_json) && File.exist?(absolute_md)

    cache = read_audit_cache(base_name)
    return false unless cache

    semantic_checksum = semantic_payload_checksum(payload)
    cache["semantic_checksum"] == semantic_checksum && semantic_json_checksum(absolute_json) == semantic_checksum
  rescue StandardError
    false
  end

  def read_audit_cache(base_name)
    cache_path = "#{OUT}/.cache/audit-inputs.json"
    return nil unless File.exist?(abs(cache_path))

    JSON.parse(File.read(abs(cache_path)))[base_name]
  rescue StandardError
    nil
  end

  def semantic_json_checksum(path)
    semantic_payload_checksum(JSON.parse(File.read(path)))
  rescue StandardError
    nil
  end

  def semantic_payload_checksum(payload)
    Digest::SHA256.hexdigest(JSON.generate(normalize_cache_payload(payload)))
  end

  def normalize_cache_payload(value)
    case value
    when Hash
      value.each_with_object({}) do |(key, child), result|
        next if cache_metadata_key?(key.to_s)

        result[key.to_s] = normalize_cache_payload(child)
      end.sort_by { |key, _| key }.to_h
    when Array
      value.map { |entry| normalize_cache_payload(entry) }
    else
      value
    end
  end

  def cache_metadata_key?(key)
    key.match?(/\A(?:generated_at|generatedAt|updated_at|updatedAt|timestamp|checked_at|cached_at)\z/)
  end

  def summary_markdown(title, payload)
    return generic_summary_markdown(title, payload) unless compact_summary_payload?(payload)

    lines = ["# #{title}", ""]
    lines << "- Decision: `#{summary_decision(payload)}`" if summary_decision(payload)
    why = summary_why(payload)
    lines << "- Why: #{why}" if why
    next_action = summary_next_action(payload)
    lines << "- Next action: #{next_action}" if next_action
    evidence = summary_evidence(payload)
    lines << "- Evidence: #{evidence}" if evidence
    details = summary_details(payload)
    unless details.empty?
      lines << ""
      lines << "## Details"
      lines << ""
      details.each { |line| lines << "- #{line}" }
    end
    lines.join("\n") + "\n"
  end

  def generic_summary_markdown(title, payload)
    lines = ["# #{title}", ""]
    payload.each do |key, value|
      case value
      when Array
        lines << "## `#{key}`"
        lines << ""
        sample = value.first(8)
        sample.each do |entry|
          cleaned_entry = LocalToolingCommon.clean_text_output(entry.to_s, max_lines: 1, aggressive: true)
          next if cleaned_entry.empty?

          lines << "- `#{cleaned_entry}`".gsub("=>", ": ")
        end
        remaining = value.length - sample.length
        lines << "- `... #{remaining} more`" if remaining.positive?
        lines << ""
      when Hash
        lines << "- `#{key}`: `#{value.size}` entries"
      else
        cleaned_value = LocalToolingCommon.clean_text_output(value.to_s, max_lines: 1, aggressive: true)
        lines << "- #{LocalToolingCommon.titleize_slug(key.to_s)}: `#{cleaned_value}`"
      end
    end
    lines.join("\n") + "\n"
  end

  def compact_summary_payload?(payload)
    keys = payload.keys.map(&:to_s)
    (keys & %w[decision risk_tier stale_count status score summary recommended_commands regenerate_command commands next_commands suggested_commands read_first read_surfaces repository_methods endpoints routes artifacts required_docs required_generated_artifacts required_validation_commands recommended_audits candidates hotspots repeated_failures slowest_runs groups packs files items issues warnings notes registry summaries dto_groups edges nodes mapped_files related_audits related_generated_artifacts related_manifests backend_refs controller_methods docs_refs frontend_refs generated_contract_refs direct_tests nearby_tests scenario_hits missing_direct_tests mapper_usages]).any?
  end

  def summary_decision(payload)
    return payload["decision"] || payload[:decision] if payload.key?("decision") || payload.key?(:decision)
    return payload["risk_tier"] || payload[:risk_tier] if payload.key?("risk_tier") || payload.key?(:risk_tier)
    return "stale" if payload.key?("stale_count") && payload["stale_count"].to_i.positive?
    return "fresh" if payload.key?("stale_count")
    return payload["status"] || payload[:status] if payload.key?("status") || payload.key?(:status)
    return payload["score"].to_s if payload.key?("score") || payload.key?(:score)

    nil
  end

  def summary_why(payload)
    reasons = []
    reasons += Array(payload["notes"] || payload[:notes])
    reasons += Array(payload["reason"] || payload[:reason])
    reasons += Array(payload["summary"] || payload[:summary]).map { |value| value.is_a?(Hash) ? "#{value[:status] || value['status']}: #{value[:count] || value['count']}" : value }
    reasons += Array(payload["factors"] || payload[:factors]).first(3).map do |factor|
      if factor.is_a?(Hash)
        "#{factor[:id] || factor['id']} +#{factor[:weight] || factor['weight']}"
      else
        factor.to_s
      end
    end
    reasons += Array(payload["scope_guardrails"] || payload[:scope_guardrails]).select { |entry| entry.is_a?(Hash) && (entry[:status].to_s == "warn" || entry["status"].to_s == "warn") }.first(3).map do |guardrail|
      "#{guardrail[:id] || guardrail['id']}: #{guardrail[:message] || guardrail['message']}"
    end
    reasons = reasons.map { |value| LocalToolingCommon.clean_text_output(value.to_s, max_lines: 1, aggressive: true) }.reject(&:empty?)
    return nil if reasons.empty?

    reasons.first(3).join("; ")
  end

  def summary_next_action(payload)
    commands = Array(payload["recommended_commands"] || payload[:recommended_commands])
    commands += Array(payload["commands"] || payload[:commands])
    commands += Array(payload["next_commands"] || payload[:next_commands])
    commands += Array(payload["suggested_commands"] || payload[:suggested_commands])
    commands << payload["regenerate_command"] if payload["regenerate_command"]
    commands << payload[:regenerate_command] if payload[:regenerate_command]
    commands = commands.compact.map do |value|
      if value.is_a?(Hash)
        value[:command] || value["command"] || value[:regenerate_command] || value["regenerate_command"] || value[:summary] || value["summary"]
      else
        value.to_s
      end
    end.map(&:to_s).map(&:strip).reject(&:empty?).uniq
    return nil if commands.empty?

    commands.first(3).map { |command| "`#{command}`" }.join(", ")
  end

  def summary_evidence(payload)
    evidence = []
    evidence << "artifacts=#{payload["artifact_count"]}" if payload["artifact_count"]
    evidence << "stale=#{payload["stale_count"]}" if payload["stale_count"]
    evidence << "runs=#{payload["run_count"]}" if payload["run_count"]
    evidence << "files=#{payload["changed_file_count"]}" if payload["changed_file_count"]
    evidence << "generated=#{payload["generated_changes"]&.size}" if payload["generated_changes"].is_a?(Array)
    evidence << "source=#{payload["source_changes"]&.size}" if payload["source_changes"].is_a?(Array)
    evidence << "packs=#{payload["pack_count"] || payload["includedPackCount"]}" if payload["pack_count"] || payload["includedPackCount"]
    evidence << "rows=#{payload["rows"]&.size}" if payload["rows"].is_a?(Array)
    evidence << "entries=#{payload["entries"]&.size}" if payload["entries"].is_a?(Array)
    evidence << "phrases=#{payload["phrase_count"]}" if payload["phrase_count"]
    evidence = evidence.compact.reject(&:empty?)
    return nil if evidence.empty?

    evidence.join(", ")
  end

  def summary_details(payload)
    details = []
    summary_scalar_detail(details, payload, "read_surface_count")
    summary_scalar_detail(details, payload, "service_count")
    summary_scalar_detail(details, payload, "missing_read_only_transaction_count")
    summary_scalar_detail(details, payload, "repository_count")
    summary_scalar_detail(details, payload, "repository_method_count")
    summary_scalar_detail(details, payload, "high_risk_count")
    summary_scalar_detail(details, payload, "medium_risk_count")
    summary_scalar_detail(details, payload, "endpoint_count")
    summary_scalar_detail(details, payload, "frontend_client_method_count")
    summary_scalar_detail(details, payload, "linked_endpoint_count")
    summary_scalar_detail(details, payload, "unlinked_endpoint_count")
    summary_scalar_detail(details, payload, "route_count")
    summary_scalar_detail(details, payload, "surface_route_count")
    summary_scalar_detail(details, payload, "placeholder_route_count")
    summary_scalar_detail(details, payload, "redirect_route_count")
    summary_scalar_detail(details, payload, "artifact_count")
    summary_scalar_detail(details, payload, "stale_count")
    summary_scalar_detail(details, payload, "changed_file_count")
    summary_scalar_detail(details, payload, "original_file_count")
    summary_scalar_detail(details, payload, "filtered_file_count")
    summary_scalar_detail(details, payload, "excluded_file_count")
    summary_scalar_detail(details, payload, "pack_count")
    summary_scalar_detail(details, payload, "run_count")
    summary_scalar_detail(details, payload, "success_count")
    summary_scalar_detail(details, payload, "failure_count")
    summary_scalar_detail(details, payload, "decision_count")
    summary_scalar_detail(details, payload, "score")
    summary_scalar_detail(details, payload, "count")
    summary_scalar_detail(details, payload, "status")
    summary_array_detail(details, payload, "summary", "Summary")
    summary_array_detail(details, payload, "artifacts", "Artifacts")
    summary_array_detail(details, payload, "generated_changes", "Generated changes")
    summary_array_detail(details, payload, "scope_guardrails", "Scope guardrails", limit: 3)
    summary_array_detail(details, payload, "read_first", "Read first", limit: 5)
    summary_array_detail(details, payload, "read_surfaces", "Read surfaces", limit: 5)
    summary_array_detail(details, payload, "repository_methods", "Repository methods", limit: 5)
    summary_array_detail(details, payload, "endpoints", "Endpoints", limit: 5)
    summary_array_detail(details, payload, "routes", "Routes", limit: 5)
    summary_array_detail(details, payload, "required_docs", "Required docs", limit: 5)
    summary_array_detail(details, payload, "required_generated_artifacts", "Required generated artifacts", limit: 5)
    summary_array_detail(details, payload, "required_validation_commands", "Required validation commands", limit: 5)
    summary_array_detail(details, payload, "recommended_audits", "Recommended audits", limit: 5)
    summary_array_detail(details, payload, "recommended_commands", "Recommended commands", limit: 5)
    summary_array_detail(details, payload, "candidates", "Candidates", limit: 3)
    summary_array_detail(details, payload, "hotspots", "Hotspots", limit: 3)
    summary_array_detail(details, payload, "repeated_failures", "Repeated failures", limit: 3)
    summary_array_detail(details, payload, "slowest_runs", "Slowest runs", limit: 3)
    summary_array_detail(details, payload, "groups", "Groups", limit: 3)
    summary_array_detail(details, payload, "packs", "Packs", limit: 3)
    summary_array_detail(details, payload, "dto_groups", "DTO groups", limit: 3)
    summary_array_detail(details, payload, "edges", "Edges", limit: 3)
    summary_array_detail(details, payload, "nodes", "Nodes", limit: 3)
    summary_array_detail(details, payload, "mapped_files", "Mapped files", limit: 3)
    summary_array_detail(details, payload, "related_audits", "Related audits", limit: 3)
    summary_array_detail(details, payload, "related_generated_artifacts", "Related generated artifacts", limit: 3)
    summary_array_detail(details, payload, "related_manifests", "Related manifests", limit: 3)
    summary_array_detail(details, payload, "backend_refs", "Backend refs", limit: 3)
    summary_array_detail(details, payload, "controller_methods", "Controller methods", limit: 3)
    summary_array_detail(details, payload, "docs_refs", "Docs refs", limit: 3)
    summary_array_detail(details, payload, "frontend_refs", "Frontend refs", limit: 3)
    summary_array_detail(details, payload, "generated_contract_refs", "Generated contract refs", limit: 3)
    summary_array_detail(details, payload, "direct_tests", "Direct tests", limit: 3)
    summary_array_detail(details, payload, "nearby_tests", "Nearby tests", limit: 3)
    summary_array_detail(details, payload, "scenario_hits", "Scenario hits", limit: 3)
    summary_array_detail(details, payload, "missing_direct_tests", "Missing direct tests", limit: 3)
    summary_array_detail(details, payload, "mapper_usages", "Mapper usages", limit: 3)
    summary_array_detail(details, payload, "files", "Files", limit: 2)
    summary_array_detail(details, payload, "items", "Items", limit: 2)
    summary_array_detail(details, payload, "issues", "Issues", limit: 5)
    summary_array_detail(details, payload, "warnings", "Warnings", limit: 5)
    summary_array_detail(details, payload, "notes", "Notes", limit: 5)
    summary_array_detail(details, payload, "registry", "Registry", limit: 3)
    summary_array_detail(details, payload, "summaries", "Summaries", limit: 3)
    Array(payload["generated_changes"] || payload[:generated_changes]).first(3).each do |entry|
      details << entry_summary(entry)
    end
    Array(payload["scope_guardrails"] || payload[:scope_guardrails]).select { |entry| entry_is_warn?(entry) }.first(3).each do |entry|
      details << entry_summary(entry)
    end
    details.compact.reject(&:empty?)
  end

  def summary_scalar_detail(details, payload, key)
    value = payload[key] || payload[key.to_sym]
    return if value.nil? || (value.respond_to?(:empty?) && value.empty?)

    details << "#{LocalToolingCommon.titleize_slug(key.to_s)}: #{value}"
  end

  def summary_array_detail(details, payload, key, label, limit: 5)
    value = payload[key] || payload[key.to_sym]
    return unless value.is_a?(Array) && value.any?

    sample = value.first(limit).map do |entry|
      cleaned = entry_summary(entry) || entry.to_s
      LocalToolingCommon.clean_text_output(cleaned.to_s, max_lines: 1, aggressive: true)
    end.reject(&:empty?)
    return if sample.empty?

    details << "#{label}: #{sample.join(' | ')}"
    remaining = value.length - sample.length
    details << "#{label} more: #{remaining}" if remaining.positive?
  end

  def entry_summary(entry)
    case entry
    when Hash
      values = entry.each_with_object([]) do |(key, value), parts|
        next if %w[generated_at updated_at recordedAt].include?(key.to_s)
        next if value.nil? || (value.respond_to?(:empty?) && value.empty?)
        parts << "#{key}: #{value.is_a?(Array) ? value.first(3).join(", ") : value}"
      end
      values.empty? ? nil : values.join(" | ")
    else
      entry.to_s
    end
  end

  def entry_is_warn?(entry)
    return false unless entry.is_a?(Hash)

    entry[:status].to_s == "warn" || entry["status"].to_s == "warn"
  end

  def terminal_line(title, payload, json_path, md_path)
    count = payload[:file_count] || payload[:changed_file_count] || payload[:endpoint_count] || payload[:symbol_count] || payload[:node_count] || payload[:audit_count] || payload[:pack_count] || payload[:rows]&.size
    "#{title}\n  count: #{count || 'n/a'}\n  json: #{json_path}\n  summary: #{md_path}"
  end

  def validation_rules
    {
      "backend_controller" => ["./mvnw test", "make generate-agent-artifacts", "make audit-api-contract-drift"],
      "backend_service" => ["./mvnw test", "make audit-read-surface-inventory", "make audit-repository-fetch"],
      "backend_repository" => ["./mvnw test", "make audit-repository-fetch"],
      "backend_mapper" => ["./mvnw test", "make audit-mapper-usage"],
      "backend_dto" => ["./mvnw test", "npm run type-check", "npm run build", "make generate-agent-artifacts"],
      "backend_model" => ["./mvnw test", "make audit-migration-entity-drift"],
      "frontend_api" => ["npm run type-check", "npm run build", "make audit-api-contract-drift"],
      "frontend_view" => ["npm run type-check", "npm run build", "make audit-frontend-route-surfaces"],
      "frontend_composable" => ["npm run type-check", "npm run build", "make audit-async-mutation-flow"],
      "frontend_contract" => ["npm run type-check", "npm run build"],
      "docs" => ["./mvnw test", "make audit-documentation", "make audit-doc-canonical-phrases"],
      "script" => ["ruby -c <script>", "make audit-summary-index"]
    }
  end

  def validation_preset_for(files, manifest = nil, manifest_resolution = nil)
    categories = files.map { |path| category_for(path) }.uniq.sort
    domains = files.map { |path| domain_for(path) }.uniq.sort
    runtime_files = files.reject do |path|
      path.start_with?("docs/") || path.start_with?(".agents/") || path.start_with?("scripts/") ||
        path == "Makefile" || LocalToolingCommon.generated_path?(path)
    end
    manifest ||= manifest_decision_for(files)
    manifest_resolution ||= resolve_manifest_path_for(files)
    preset =
      if manifest[:decision] == "required"
        "manifest-required"
      elsif files.empty?
        "review"
      elsif files.size <= 3 && categories.all? { |category| %w[docs script other].include?(category) }
        "fast"
      elsif categories.all? { |category| category.start_with?("frontend_") || %w[docs other].include?(category) } &&
            categories.any? { |category| category.start_with?("frontend_") }
        "standard-frontend"
      elsif categories.any? { |category| category.start_with?("backend_") } &&
            categories.none? { |category| category.start_with?("frontend_") } &&
            domains.size <= 1 &&
            (categories & %w[backend_controller backend_dto backend_model]).empty?
        "standard-backend"
      elsif (runtime_files.any? && domains.size > 1) ||
            (categories.any? { |category| category.start_with?("backend_") } &&
             categories.any? { |category| category.start_with?("frontend_") })
        "full-closeout"
      else
        "fast"
      end

    base_commands = validation_preset_commands(preset, files, categories, manifest_resolution)
    canonical_commands = validation_memory_overlay_commands(files, categories, manifest)

    {
      preset: preset,
      reasons: validation_preset_reasons(preset, files, categories, domains, manifest),
      commands: (base_commands + canonical_commands).uniq,
      supporting_reports: validation_preset_supporting_reports(preset),
      manifest_decision: manifest[:decision],
      manifest_resolution: manifest_resolution,
      categories: categories,
      domains: domains,
      changed_file_count: files.size
    }
  end

  def validation_memory_overlay_commands(files, categories, manifest)
    memory = validation_memory_config
    return [] if memory.empty?

    commands = []
    commands += Array(memory.dig("canonicalCommands", "frontendContract")) if categories.any? { |category| category.start_with?("frontend_") }
    commands += Array(memory.dig("canonicalCommands", "backendLogic")) if categories.any? { |category| category.start_with?("backend_") }
    commands += Array(memory.dig("canonicalCommands", "agentContract")) if files.any? { |path| agent_contract_path?(path) }
    commands += Array(memory.dig("canonicalCommands", "workflowExpansion")) if workflow_expansion_files?(files)
    commands += Array(memory.dig("canonicalCommands", "closeout")) if manifest && manifest[:manifest_required]
    commands.uniq
  end

  def validation_memory_config
    @validation_memory_config ||= begin
      path = File.join(REPO_ROOT, "docs/validation-memory.json")
      File.exist?(path) ? JSON.parse(File.read(path)) : {}
    rescue JSON::ParserError
      {}
    end
  end

  def agent_contract_path?(path)
    path == "AGENTS.md" ||
      path == "docs/codex-fast-path.md" ||
      path == "docs/feature-delivery-workflow.md" ||
      path == "docs/documentation-sync-policy.md" ||
      path == "docs/change-completion-checklist.md" ||
      path.start_with?("docs/agent-operating-model") ||
      path.start_with?("scripts/") ||
      path == "Makefile" ||
      path.include?("agent/")
  end

  def workflow_expansion_files?(files)
    files.any? do |path|
      read(path).match?(/\b(workflow|transition|state machine|ScenarioTest|UseCaseContractTest)\b/i)
    rescue StandardError
      false
    end
  end

  def router_audits_for(files)
    categories = files.map { |path| category_for(path) }
    audits = []
    audits += ["make audit-read-surface-inventory", "make audit-repository-fetch", "make audit-mapper-usage"] if categories.any? { |category| category.start_with?("backend_") }
    audits += ["make audit-api-contract-drift", "make audit-endpoint-callsite-linker", "make endpoint-contract-packs"] if categories.include?("backend_controller") || categories.include?("frontend_api")
    audits += ["make audit-frontend-route-surfaces", "make audit-async-mutation-flow", "make audit-frontend-usage-graph"] if categories.any? { |category| category.start_with?("frontend_") }
    audits += ["make audit-doc-sync-preflight", "make audit-doc-sync-required-surfaces", "make audit-documentation", "make audit-doc-canonical-phrases"] if categories.include?("docs") || files.any? { |path| path.start_with?("scripts/") || path.start_with?(".agents/") }
    audits += ["make audit-migration-entity-drift"] if categories.include?("backend_model")
    audits += ["make audit-test-gap-recommendations", "make audit-summary-index"]
    audits.uniq
  end

  def manifest_decision_for(files)
    categories = files.group_by { |path| category_for(path) }
    domains = files.map { |path| domain_for(path) }.reject { |value| %w[shared common].include?(value) }.uniq.sort
    runtime_files = files.reject do |path|
      LocalToolingCommon.generated_path?(path) || path.start_with?(".agents/") || path.start_with?("docs/") ||
        path.start_with?("scripts/") || path == "Makefile"
    end
    schema_files = files.select { |path| path.include?("/db/migration/") || path.end_with?(".schema.json") }
    invoice_critical_files = files.select { |path| path.match?(/invoice/i) || read(path).match?(/\binvoice\b/i) }
    agent_contract_files = files.select do |path|
      path == "AGENTS.md" ||
        path == "docs/codex-fast-path.md" ||
        path == "docs/feature-delivery-workflow.md" ||
        path == "docs/documentation-sync-policy.md" ||
        path == "docs/change-completion-checklist.md" ||
        path.start_with?("docs/agent-operating-model") ||
        path.include?("AdminAgent") ||
        path.include?("agent/")
    end
    frontend_contract_files = files.select do |path|
      path.include?("/frontend/src/contracts/") ||
        path.include?("/frontend/scripts/generate") ||
        path.include?("/frontend/src/modules/") && read(path).include?("api")
    end
    generated_files = files.select { |path| LocalToolingCommon.generated_path?(path) }
    automation_safety_files = files.select do |path|
      path.start_with?("scripts/") || path == "Makefile" || path == "AGENTS.md" ||
        path == "docs/codex-fast-path.md" || path == "docs/feature-delivery-workflow.md" ||
        path == "docs/documentation-sync-policy.md" || path == "docs/change-completion-checklist.md" ||
        path.start_with?("docs/agent-operating-model") ||
        path.include?("bootstrap-feature-work") ||
        path.include?("feature-closeout") ||
        path.include?("validation-evidence") || path.include?("todo-audit") || path.include?("documentation-sync")
    end
    workflow_files = files.select { |path| read(path).match?(/\b(workflow|transition|state machine|ScenarioTest|UseCaseContractTest)\b/i) }
    meaningful_surfaces = files.map { |path| manifest_meaningful_surface(path) }.reject { |surface| %w[other tests].include?(surface) }.uniq.sort
    rules = []
    rules << manifest_rule("multi_surface_change", files, "Changes touching three or more meaningful surfaces need manifest-backed closeout traceability.") if meaningful_surfaces.size >= 3
    rules << manifest_rule("invoice_critical_change", invoice_critical_files, "Invoice-critical behavior needs explicit scope, validation, and residual-risk evidence.")
    rules << manifest_rule("agent_contract_change", agent_contract_files, "Agent-facing contracts and operating-model surfaces require manifest tracking.")
    rules << manifest_rule("workflow_expansion_change", workflow_files, "Workflow or scenario surfaces require scenario evidence and docs synchronization.")
    rules << manifest_rule("frontend_contract_change", frontend_contract_files, "Frontend contract or API surfaces require frontend validation evidence.")
    rules << manifest_rule("schema_or_generated_artifact_change", (schema_files + generated_files).uniq, "Schema and generated-artifact changes require explicit generated-artifact evidence.")
    rules << manifest_rule("agent_tooling_change", automation_safety_files, "Agent, tooling, startup-routing, or closeout workflow changes need explicit evidence.")
    rules << manifest_rule("mixed_product_domains", files, "Multiple product domains in one changeset require explicit scope and residual-risk review.") if runtime_files.any? && domains.size > 1
    rules.compact!
    allow_skip = files.size <= 1 &&
      rules.empty? &&
      files.all? { |path| %w[docs script test other].include?(category_for(path)) || path.end_with?(".md") || path.end_with?(".java") }
    decision =
      if rules.any?
        "required"
      elsif allow_skip
        "not_required"
      else
        "review"
      end
    {
      decision: decision,
      manifest_required: decision == "required",
      skip_allowed: decision == "not_required",
      skip_reason: decision == "not_required" ? "Cosmetic or single-surface contract-neutral change; no manifest required if the closeout records the reason." : nil,
      rules: rules,
      changed_file_count: files.size,
      categories: categories.keys.sort,
      domains: domains,
      meaningful_surfaces: meaningful_surfaces
    }
  end

  def manifest_rule(id, files, reason)
    return nil if files.empty?

    {id: id, file_count: files.size, files: files.first(25), reason: reason}
  end

  def manifest_meaningful_surface(path)
    return "db_migration" if path.include?("/db/migration/")
    return "agent_tooling" if path == "AGENTS.md" || path.start_with?("scripts/") || path == "Makefile"
    return "agent_tooling" if path == "docs/codex-fast-path.md" || path == "docs/feature-delivery-workflow.md" ||
      path == "docs/documentation-sync-policy.md" || path == "docs/change-completion-checklist.md" ||
      path.start_with?("docs/agent-operating-model")
    return "generated_artifact" if LocalToolingCommon.generated_path?(path) || path.start_with?("docs/generated/") || path.end_with?(".schema.json")
    return "frontend_contract" if path.include?("/frontend/src/contracts/") || path.include?("/frontend/scripts/generate")
    return "frontend" if path.include?("/frontend/")
    return "tests" if path.include?("/src/test/")
    return "backend_runtime" if path.match?(%r{/(service|controller|model|dto|repository|mapper|config)/})
    return "docs" if path.start_with?("docs/")

    "other"
  end

  def endpoint_entries
    rel_glob("apps/themuffinman/src/main/java/**/*Controller.java").flat_map do |path|
      content = read(path)
      root = content[/@RequestMapping\("([^"]+)"\)/, 1] || ""
      content.scan(/@(GetMapping|PostMapping|PutMapping|PatchMapping|DeleteMapping)(?:\("([^"]*)"\))?/).map do |verb, mapped|
        method_block = content[content.index("@#{verb}") || 0, 900] || ""
        {
          method: verb.sub("Mapping", "").upcase,
          path: [root, mapped].compact.join,
          controller: path,
          dtos: method_block.scan(/\b([A-Z][A-Za-z0-9_]*DTO|[A-Z][A-Za-z0-9_]*Request|[A-Z][A-Za-z0-9_]*Response)\b/).flatten.uniq
        }
      end
    end.uniq.sort_by { |row| [row[:path], row[:method]] }
  end

  def frontend_callsites_for(paths)
    needles = paths.flat_map { |path| [path, path.sub(%r{^/api}, ""), path.split("/").last] }.uniq.reject(&:empty?)
    rel_glob("apps/themuffinman/frontend/src/**/*.{ts,vue}").flat_map do |file|
      content = read(file)
      next [] unless needles.any? { |needle| content.include?(needle) }
      [{file: file, matched_paths: needles.select { |needle| content.include?(needle) }.first(8)}]
    end
  end

  def related_tests(files)
    tests = rel_glob("apps/themuffinman/src/test/java/**/*.java")
    files.flat_map do |path|
      base = File.basename(path, ".*").sub(/(Service|Controller|DTO|Mgr|Mapper)\z/, "")
      tests.select { |test| test.downcase.include?(base.downcase) || test.downcase.include?(domain_for(path)) }
    end.uniq.sort.first(40)
  end

  def relevant_audit_summaries(files, domains)
    summaries = rel_glob("docs/generated/local-tooling/*summary.md", "docs/generated/dead-code-audit/*summary.md")
    terms = (domains + files.map { |path| File.basename(path, ".*") }).map(&:downcase)
    summaries.map do |summary|
      text = read(summary).downcase
      score = terms.count { |term| text.include?(term) }
      {summary: summary, score: score}
    end.select { |row| row[:score].positive? }.sort_by { |row| [-row[:score], row[:summary]] }.first(10)
  end

  def git_status_entries
    LocalToolingCommon.git_status_entries
  end

  def changed_files
    changeset_snapshot[:included_files]
  end

  def changed_files_all
    changeset_snapshot[:all_changed_files]
  end

  def infer_topic_files(topic)
    term = topic.tr("-", "").downcase
    rel_glob("apps/themuffinman/src/main/java/**/*.java", "apps/themuffinman/frontend/src/**/*.{ts,vue}", "docs/**/*.md").reject do |path|
      path.start_with?("docs/generated/")
    end.select do |path|
      compact = path.tr("-_", "").downcase
      compact.include?(term) || read(path).downcase.include?(topic.tr("-", " "))
    end.first(80)
  end

  def file_entry(path)
    {path: path, category: category_for(path), domain: domain_for(path), lines: File.exist?(abs(path)) ? File.readlines(abs(path)).size : 0}
  end

  def category_for(path)
    LocalToolingCommon.path_category(path)
  end

  def domain_for(path)
    return "workmarket" if path.include?("/workmarket/") || path.include?("workmarket")
    return "social" if path.include?("/social/") || path.include?("circle")
    return "identity" if path.include?("/identity/") || path.include?("user")
    return "location" if path.include?("/location/")
    return "chat" if path.include?("/chat/")
    return "business" if path.include?("/business/")
    return "things" if path.include?("/things/")
    return "rides" if path.include?("/rides/")
    return "common" if path.include?("/common/") || path.include?("/config/")
    return "agent" if path.include?("/agent") || path.include?("agent-")

    "shared"
  end

  def endpoint_family(path)
    %w[workmarket social identity location chat agent].find { |domain| path.include?(domain) } || path.split("/").reject(&:empty?).first || "shared"
  end

  def ref_count(token, corpus = nil)
    return 0 if token.nil? || token.empty?
    (corpus || read_all("apps/themuffinman")).scan(/\b#{Regexp.escape(token)}\b/).size
  end

  def rel_glob(*patterns)
    LocalToolingCommon.repo_glob(*patterns).map { |path| LocalToolingCommon.relative_path(path) }
  end

  def truthy?(value)
    LocalToolingCommon.truthy?(value)
  end

  def read(path)
    normalize_text(File.binread(abs(path)))
  rescue Errno::ENOENT, Errno::EISDIR
    ""
  end

  def read_all(relative_root)
    Dir.glob(File.join(REPO_ROOT, relative_root, "**", "*")).select { |path| File.file?(path) }.map { |path| normalize_text(File.binread(path)) }.join("\n")
  rescue Errno::ENOENT
    ""
  end

  def normalize_text(content)
    content.to_s.encode("UTF-8", invalid: :replace, undef: :replace, replace: "")
  end

  def abs(path)
    File.join(REPO_ROOT, path)
  end

  def now
    Time.now.utc.iso8601
  end

  def slug(value)
    value.to_s.downcase.gsub(/[^a-z0-9_-]+/, "-").gsub(/-+/, "-").sub(/^-/, "").sub(/-$/, "")
  end

  def deep_copy(value)
    JSON.parse(JSON.generate(value), symbolize_names: true)
  end

  def camel_to_snake(value)
    value.gsub(/([a-z0-9])([A-Z])/, "\\1_\\2").downcase
  end

  def parse_key_values(argv)
    argv.each_with_object({}) do |arg, options|
      key, value = arg.split("=", 2)
      options[key] = value if value
    end
  end

  def option_files(options, argv)
    raw = options["files"]
    files = raw ? raw.split(",") : argv.reject { |arg| arg.include?("=") || arg.start_with?("--") }
    files.map(&:strip).reject(&:empty?).select { |path| File.file?(abs(path)) }.uniq
  end

  def csv_list(value)
    value.to_s.split(",").map(&:strip).reject(&:empty?)
  end

  def symbol_definition_files(symbol)
    escaped = Regexp.escape(symbol)
    rel_glob("apps/themuffinman/src/main/java/**/*.java", "apps/themuffinman/frontend/src/**/*.{ts,vue}").select do |path|
      basename = File.basename(path, File.extname(path))
      next true if basename == symbol

      text = read(path)
      text.match?(/\b(class|interface|enum|record)\s+#{escaped}\b/) ||
        text.match?(/\bexport\s+(?:default\s+)?(?:function|const|class|interface|type)\s+#{escaped}\b/) ||
        (path.end_with?(".vue") && text.match?(/\b#{escaped}\b/))
    end.first(20)
  end

  def direct_symbol_tests(symbol)
    escaped = Regexp.escape(symbol)
    rel_glob("apps/themuffinman/src/test/java/**/*.java").select do |path|
      read(path).match?(/\b#{escaped}\b/)
    end.first(30)
  end

  def symbol_test_commands(symbol_files, direct_tests, nearby_tests, scenario_hits)
    commands = []
    test_names = (direct_tests + nearby_tests).map { |path| File.basename(path, ".java") }.uniq
    if test_names.any?
      commands << "cd apps/themuffinman && ./mvnw test -Dtest=#{test_names.first(8).join(',')}"
    elsif symbol_files.any? { |path| category_for(path).start_with?("frontend_") }
      commands << "npm --prefix apps/themuffinman/frontend run type-check"
      commands << "npm --prefix apps/themuffinman/frontend run build"
    end
    commands += scenario_hits.flat_map { |scenario| Array(scenario["commands"]).first(1) }
    commands.uniq.first(10)
  end

  def symbol_test_residual_risk(symbol_files, direct_tests, nearby_tests, scenario_hits)
    risks = []
    risks << "Symbol definition file could not be resolved deterministically." if symbol_files.empty?
    risks << "No direct test references were found for the symbol." if direct_tests.empty?
    risks << "Only nearby tests were found; symbol-level coverage may still be indirect." if direct_tests.empty? && nearby_tests.any?
    risks << "No regression scenario catalog entry matched the symbol domain." if scenario_hits.empty?
    risks.uniq
  end

  def dto_usage_commands(controller_methods, frontend_refs, tests)
    commands = []
    test_names = tests.map { |path| File.basename(path, ".java") }.uniq
    commands << "cd apps/themuffinman && ./mvnw test -Dtest=#{test_names.first(8).join(',')}" if test_names.any?
    if controller_methods.any?
      commands << "make audit-api-contract-drift"
      commands << "make endpoint-contract-packs"
    end
    if frontend_refs.any?
      commands << "npm --prefix apps/themuffinman/frontend run type-check"
      commands << "npm --prefix apps/themuffinman/frontend run build"
    end
    commands.uniq.first(10)
  end

  def dto_usage_residual_risk(dto_files, controller_methods, frontend_refs, tests)
    risks = []
    risks << "DTO definition file was not found." if dto_files.empty?
    risks << "No controller method was found using this DTO directly." if controller_methods.empty?
    risks << "No frontend usage was found for this DTO; generated contract-only usage may still exist." if frontend_refs.empty?
    risks << "No test references were found for this DTO." if tests.empty?
    risks.uniq
  end

  def workflow_state_machines
    data = YAML.load_file(abs("docs/workflow-state-machines.yaml"))
    Array(data["stateMachines"])
  rescue StandardError
    []
  end

  def known_domains
    %w[workmarket social identity location chat business things rides agent]
  end

  def domain_runtime_files(domain)
    rel_glob(
      "apps/themuffinman/src/main/java/**/*.java",
      "apps/themuffinman/frontend/src/**/*.{ts,vue}"
    ).select { |path| domain_for(path) == domain }.uniq.sort
  end

  def domain_related_tests(domain, runtime_files, workflows, scenario_rows)
    owners = workflows.map { |machine| machine["owner"].to_s }.reject(&:empty?)
    dto_names = runtime_files.select { |path| path.include?("/dto/") }.map { |path| File.basename(path, ".java") }
    rel_glob("apps/themuffinman/src/test/java/**/*.java").select do |path|
      text = read(path)
      domain_for(path) == domain ||
        path.downcase.include?(domain.downcase) ||
        owners.any? { |owner| text.include?(owner) } ||
        dto_names.any? { |dto| text.include?(dto) } ||
        Array(scenario_rows).flat_map { |row| Array(row["test_files"]) }.include?(path)
    end.uniq.sort
  end

  def domain_key_backend_files(domain, files, layer)
    files.select do |path|
      domain_for(path) == domain && path.include?("/#{layer}/")
    end.first(8).map { |path| file_entry(path) }
  end

  def domain_dto_groups(domain, files)
    dto_files = files.select do |path|
      domain_for(path) == domain && path.include?("/dto/") && path.end_with?(".java")
    end
    dto_files.group_by do |path|
      base = File.basename(path, ".java")
      case base
      when /Request|Response/ then "request-response"
      when /Detail|Summary|List/ then "read-models"
      when /Option|Status|Type/ then "enum-like"
      else "other"
      end
    end.map do |group, paths|
      {group: group, count: paths.size, files: paths.first(10)}
    end.sort_by { |row| row[:group] }
  end

  def domain_frontend_surfaces(domain, files)
    files.select do |path|
      domain_for(path) == domain && category_for(path).start_with?("frontend_")
    end.first(12).map { |path| file_entry(path) }
  end

  def default_domain_docs(domain)
    docs = %w[docs/business-logic.md docs/domain-technical.md]
    docs << "docs/location-services.md" if domain == "location"
    docs << "docs/agent-operating-model.md" if %w[agent business things rides].include?(domain)
    docs
  end

  def domain_first_commands(domain, files, tests, scenario_rows)
    categories = files.map { |path| category_for(path) }.uniq
    commands = []
    commands.concat(validation_rules.fetch("backend_service", [])) if categories.any? { |category| category.start_with?("backend_") }
    commands.concat(targeted_frontend_commands(files, categories).map { |row| row[:command] }) if categories.any? { |category| category.start_with?("frontend_") }
    test_names = tests.map { |path| File.basename(path, ".java") }.uniq.first(6)
    commands << "cd apps/themuffinman && ./mvnw test -Dtest=#{test_names.join(',')}" if test_names.any?
    commands.concat(scenario_rows.flat_map { |row| Array(row["commands"]).first(1) })
    commands << "make audit-doc-sync-preflight files=#{files.first(8).join(',')}" if files.any?
    commands.uniq.first(8)
  end

  def domain_pack_residual_risk(domain, files, workflows, tests)
    risks = []
    risks << "No runtime files resolved for domain `#{domain}`." if files.empty?
    risks << "No documented workflow state machine exists for domain `#{domain}`." if workflows.empty? && %w[workmarket social chat things].include?(domain)
    risks << "No related tests resolved for domain `#{domain}`." if tests.empty?
    risks.uniq
  end

  def workflow_machine_for(workflow)
    key = workflow.to_s.downcase.tr("_", "-")
    workflow_state_machines.find do |machine|
      machine_id = machine["id"].to_s.tr("_", "-")
      owner = machine["owner"].to_s.downcase
      machine_id.include?(key) || key.include?(machine_id) || owner.include?(workflow.to_s.downcase)
    end || {}
  end

  def workflow_service_files(machine)
    owner = machine["owner"].to_s
    files = []
    files += rel_glob("apps/themuffinman/src/main/java/**/*#{owner}.java") unless owner.empty?
    transition_ids = Array(machine["transitions"]).map { |transition| transition["intentId"].to_s }.reject(&:empty?)
    files += rel_glob("apps/themuffinman/src/main/java/**/*.java").select do |path|
      text = read(path)
      transition_ids.any? { |intent| text.include?(intent) }
    end
    files.uniq.first(30)
  end

  def workflow_related_tests(machine, workflow)
    owner = machine["owner"].to_s
    transition_ids = Array(machine["transitions"]).map { |transition| transition["intentId"].to_s }.reject(&:empty?)
    scenario_tests = regression_scenarios.select do |scenario|
      scenario["domain"] == machine["domain"] || scenario["id"].to_s.include?(workflow.to_s.tr("_", "-"))
    end.flat_map { |scenario| Array(scenario["test_files"]) }
    owner_tests = rel_glob("apps/themuffinman/src/test/java/**/*.java").select do |path|
      text = read(path)
      text.include?(owner) || transition_ids.any? { |intent| text.include?(intent) } || path.downcase.include?(workflow.to_s.downcase)
    end
    (scenario_tests + owner_tests).uniq.first(30)
  end

  def workflow_frontend_actions(machine, workflow)
    transition_ids = Array(machine["transitions"]).map { |transition| transition["intentId"].to_s }.reject(&:empty?)
    rel_glob("apps/themuffinman/frontend/src/**/*.{ts,vue}").select do |path|
      text = read(path)
      text.downcase.include?(workflow.to_s.downcase) || transition_ids.any? { |intent| text.include?(intent) }
    end.first(30).map { |path| file_entry(path) }
  end

  def workflow_doc_refs(machine, workflow)
    refs = rel_glob("docs/**/*.md", "docs/**/*.yaml").select do |path|
      text = read(path)
      text.include?(machine["id"].to_s) || text.include?(machine["owner"].to_s) || text.downcase.include?(workflow.to_s.downcase)
    end
    refs.first(20)
  end

  def workflow_commands(tests, machine)
    commands = []
    test_names = tests.map { |path| File.basename(path, ".java") }.uniq
    commands << "cd apps/themuffinman && ./mvnw test -Dtest=#{test_names.first(8).join(',')}" if test_names.any?
    commands += regression_scenarios.select { |scenario| scenario["domain"] == machine["domain"] }.flat_map { |scenario| Array(scenario["commands"]).first(1) }
    commands.uniq.first(10)
  end

  def workflow_residual_risk(machine, service_files, tests)
    risks = []
    risks << "Workflow did not resolve to a documented state machine." if machine.empty?
    risks << "No owning service or supporting files were resolved from the state machine owner." if service_files.empty?
    risks << "No matching tests were found for the workflow slice." if tests.empty?
    risks.uniq
  end

  def plan_candidate_files(plan, plan_text)
    tokens = plan_map_tokens(plan, plan_text)
    candidates = rel_glob(
      "apps/themuffinman/src/main/java/**/*.java",
      "apps/themuffinman/src/test/java/**/*.java",
      "apps/themuffinman/frontend/src/**/*.{ts,vue}",
      "docs/**/*.md",
      "docs/**/*.yaml",
      "docs/**/*.json",
      "scripts/**/*.rb",
      ".agents/**/*.md",
      ".agents/feature-manifests/*.yaml",
      "Makefile"
    ).reject { |path| path.start_with?("docs/generated/local-tooling/.history/") }

    scored = candidates.map do |path|
      score, reasons = plan_candidate_score(path, plan_text, tokens)
      next if score.zero?

      {path: path, score: score, reasons: reasons}
    end.compact
    scored.sort_by { |row| [-row[:score], row[:path]] }.first(40).map { |row| row[:path] }
  end

  def plan_map_tokens(plan, plan_text)
    text = [plan, plan_text].join("\n")
    tokens = text.scan(/\b[A-Z][A-Za-z0-9]+(?:DTO|Service|Controller|Repository|Mapper|Test|IT)?\b/).uniq
    tokens += text.scan(/\b[a-z0-9]+(?:-[a-z0-9]+){1,}\b/).uniq
    tokens += text.scan(%r{\b(?:apps|docs|scripts|\.agents)/[A-Za-z0-9_./-]+\b}).map { |path| File.basename(path, File.extname(path)) }
    tokens.map { |token| token.to_s.strip }.reject { |token| token.size < 3 }.uniq
  end

  def plan_candidate_score(path, plan_text, tokens)
    reasons = []
    score = 0
    content = read(path)
    basename = File.basename(path, File.extname(path))
    token_hits = tokens.select do |token|
      path.downcase.include?(token.downcase) || basename.casecmp?(token) || content.match?(/\b#{Regexp.escape(token)}\b/i)
    end.uniq
    if token_hits.any?
      score += [token_hits.size * 2, 8].min
      reasons << "Matched plan tokens: #{token_hits.first(6).join(', ')}."
    end
    if plan_text.include?(path)
      score += 10
      reasons << "Plan references the file path directly."
    end
    if path.start_with?(".agents/feature-manifests/") && plan_text.match?(/\bmanifest\b/i)
      score += 4
      reasons << "Plan mentions manifests and this file is a feature manifest."
    end
    if LocalToolingCommon.generated_path?(path) && plan_text.match?(/\b(generated|artifact|summary)\b/i)
      score += 3
      reasons << "Plan mentions generated artifacts."
    end
    [score, reasons]
  end

  def plan_file_map_entry(path, plan_text)
    {
      path: path,
      category: category_for(path),
      domain: domain_for(path),
      lines: File.exist?(abs(path)) ? File.readlines(abs(path)).size : 0,
      reasons: plan_candidate_score(path, plan_text, plan_map_tokens(path, plan_text)).last
    }
  end

  def plan_related_generated_artifacts(plan_text, files)
    names = files.map { |path| File.basename(path, File.extname(path)).downcase }.uniq
    rel_glob("docs/generated/**/*.md", "docs/generated/**/*.json").select do |path|
      downcased = path.downcase
      names.any? { |name| downcased.include?(name) } ||
        (plan_text.match?(/\b(summary|generated|artifact|report)\b/i) && path.include?("local-tooling"))
    end.first(25)
  end

  def plan_related_manifests(plan, plan_text, files, manifest_resolution)
    manifests = manifest_inventory.select do |manifest|
      manifest[:plan_file] == plan ||
        files.include?(manifest[:manifest_path]) ||
        files.any? { |path| (manifest[:code_paths] + manifest[:doc_paths] + manifest[:test_paths]).include?(path) }
    end
    manifests = manifest_inventory.select { |manifest| manifest[:manifest_path] == manifest_resolution[:manifest_path] } if manifests.empty? && manifest_resolution[:manifest_path]
    manifests.map do |manifest|
      {
        manifest_path: manifest[:manifest_path],
        feature_id: manifest[:feature_id],
        title: manifest[:title],
        plan_file: manifest[:plan_file]
      }
    end.first(10)
  end

  def plan_map_residual_risk(files, manifest_resolution)
    risks = []
    risks << "Plan did not map to any likely code or documentation file." if files.empty?
    risks << "Manifest could not be resolved deterministically from the mapped files." if manifest_resolution[:decision] != "resolved"
    risks.uniq
  end

  def resolve_manifest_path_for(files)
    manifests = manifest_inventory
    analysis_files = (files + changed_files_all.select { |path| path.start_with?(".agents/") }).uniq
    candidates = manifests.map do |manifest|
      score_manifest_candidate(manifest, analysis_files)
    end.reject { |candidate| candidate[:score].zero? }
      .sort_by { |candidate| [-candidate[:score], candidate[:manifest_path]] }
    top = candidates.first
    second = candidates[1]
    decision =
      if top.nil?
        "no_match"
      elsif top[:score] >= 8 && (second.nil? || top[:score] - second[:score] >= 3)
        "resolved"
      else
        "review"
      end
    {
      decision: decision,
      manifest_path: decision == "resolved" ? top[:manifest_path] : nil,
      feature_id: decision == "resolved" ? top[:feature_id] : nil,
      title: decision == "resolved" ? top[:title] : nil,
      confidence: top ? manifest_resolution_confidence(decision, top, second) : "none",
      reasons: decision == "resolved" ? top[:reasons] : manifest_resolution_notes(decision, candidates),
      candidates: candidates.first(10)
    }
  end

  def rank_changeset_hotspots_for(files)
    corpus = hotspot_corpus
    hotspots = files.map do |path|
      hotspot_entry_for(path, corpus)
    end.sort_by { |row| [-row[:score], row[:path]] }
    categories = hotspots.map { |row| row[:category] }.uniq.sort
    {
      changed_file_count: files.size,
      categories: categories,
      hotspots: hotspots.first(25),
      read_first: hotspots.first(8).map { |row| row[:path] },
      recommended_commands: validation_preset_for(files)[:commands],
      residual_risk: hotspot_residual_risk(files, hotspots)
    }
  end

  def hotspot_entry_for(path, corpus = nil)
    content = read(path)
    category = category_for(path)
    basename = File.basename(path, File.extname(path))
    domains = [domain_for(path)]
    linked_tests = related_tests([path]).first(8)
    fanout = ref_count(basename, corpus)
    dependency_centrality = hotspot_dependency_centrality(path, content)
    workflow_sensitivity = hotspot_workflow_sensitivity(path, content)
    category_weight = hotspot_category_weight(category, path, content)
    test_signal = [linked_tests.size * 2, 6].min
    score = category_weight + [fanout, 8].min + dependency_centrality + workflow_sensitivity + test_signal
    {
      path: path,
      category: category,
      domain: domains.first,
      score: score,
      score_breakdown: {
        category: category_weight,
        fanout: [fanout, 8].min,
        dependency_centrality: dependency_centrality,
        workflow_sensitivity: workflow_sensitivity,
        test_links: test_signal
      },
      linked_tests: linked_tests,
      related_docs: DOCS_BY_DOMAIN.fetch(domains.first, []).first(6),
      recommended_commands: validation_rules.fetch(category, []),
      reasons: hotspot_reasons(path, content, linked_tests, fanout, dependency_centrality, workflow_sensitivity)
    }
  end

  def hotspot_corpus
    @hotspot_corpus ||= [
      read_all("apps/themuffinman/src/main/java"),
      read_all("apps/themuffinman/src/test/java"),
      read_all("apps/themuffinman/frontend/src"),
      read_all("docs")
    ].join("\n")
  end

  def hotspot_category_weight(category, path, content)
    return 8 if %w[backend_controller backend_service backend_repository backend_model].include?(category)
    return 7 if %w[backend_dto backend_mapper frontend_api frontend_view frontend_composable].include?(category)
    return 5 if category == "docs"
    return 4 if category == "script" || path == "Makefile"
    return 6 if content.match?(/\b(workflow|transition|permission|validation|state)\b/i)

    3
  end

  def hotspot_dependency_centrality(path, content)
    return [content.scan(/\b(?:private\s+final|private)\s+[A-Z][A-Za-z0-9_<>?, ]+\s+[a-z][A-Za-z0-9_]*\b/).size, 6].min if path.end_with?(".java")
    return [content.scan(/from\s+['"][^'"]+['"]/).size, 6].min if path.end_with?(".ts") || path.end_with?(".vue")

    1
  end

  def hotspot_workflow_sensitivity(path, content)
    score = 0
    score += 4 if content.match?(/\b(workflow|transition|state machine|permission|ScenarioTest|UseCaseContractTest)\b/i)
    score += 3 if path.include?("/db/migration/") || path.include?("agent-operating-model")
    score
  end

  def hotspot_reasons(path, content, linked_tests, fanout, dependency_centrality, workflow_sensitivity)
    reasons = []
    reasons << "High repo fanout for `#{File.basename(path)}` (#{fanout} references)." if fanout >= 4
    reasons << "Multiple dependency edges detected." if dependency_centrality >= 4
    reasons << "Workflow or permission-sensitive logic detected." if workflow_sensitivity >= 4
    reasons << "Direct or nearby tests exist: #{linked_tests.first(4).join(', ')}." if linked_tests.any?
    reasons << "Generated or tooling surface influences local automation." if path.start_with?("scripts/") || path == "Makefile"
    reasons << "Documentation or agent-safety surface can force sync work." if content.match?(/\b(canonical|manifest|documentation sync|agent)\b/i)
    reasons.uniq
  end

  def hotspot_residual_risk(files, hotspots)
    risks = []
    risks << "No files were available for hotspot ranking." if files.empty?
    risks << "Hotspot ranking is heuristic and does not replace feature-level domain judgment."
    risks << "No file reached a strong score threshold; read the diff summary first." if hotspots.none? { |row| row[:score] >= 12 }
    risks.uniq
  end

  def manifest_inventory
    rel_glob(".agents/feature-manifests/*.yaml").map do |path|
      data = YAML.load_file(abs(path)) || {}
      {
        manifest_path: path,
        feature_id: data["featureId"].to_s,
        title: data["title"].to_s,
        plan_file: data["planFile"].to_s,
        code_paths: Array(data.dig("artifacts", "codePaths")),
        doc_paths: Array(data.dig("artifacts", "docPaths")),
        test_paths: Array(data.dig("artifacts", "testPaths"))
      }
    rescue StandardError
      nil
    end.compact
  end

  def score_manifest_candidate(manifest, files)
    reasons = []
    score = 0
    normalized_paths = files.map { |path| path.to_s.downcase }
    manifest_tokens = slug_tokens(manifest[:feature_id]) +
      slug_tokens(manifest[:title]) +
      slug_tokens(File.basename(manifest[:plan_file].to_s, ".md")) +
      slug_tokens(File.basename(manifest[:manifest_path].to_s, ".yaml"))
    manifest_tokens.uniq!

    if files.include?(manifest[:manifest_path])
      score += 10
      reasons << "Manifest file itself is in the current changeset."
    end

    if manifest[:plan_file] && files.include?(manifest[:plan_file])
      score += 8
      reasons << "Referenced plan file is in the current changeset."
    end

    artifact_hits = (manifest[:code_paths] + manifest[:doc_paths] + manifest[:test_paths]).uniq & files
    if artifact_hits.any?
      score += [artifact_hits.size, 8].min
      reasons << "Changed files overlap manifest artifact lists (#{artifact_hits.first(5).join(', ')})."
    end

    token_hits = normalized_paths.flat_map { |path| manifest_tokens.select { |token| !token.empty? && path.include?(token) } }.uniq
    if token_hits.any?
      score += [token_hits.size * 2, 6].min
      reasons << "Changed paths include feature/plan tokens: #{token_hits.first(6).join(', ')}."
    end

    {
      manifest_path: manifest[:manifest_path],
      feature_id: manifest[:feature_id],
      title: manifest[:title],
      plan_file: manifest[:plan_file],
      score: score,
      reasons: reasons
    }
  end

  def slug_tokens(value)
    slug(value).split("-").reject { |token| token.size < 3 }
  end

  def manifest_resolution_confidence(decision, top, second)
    return "none" if decision == "no_match"
    return "high" if decision == "resolved" && (second.nil? || top[:score] - second[:score] >= 5)
    return "medium" if decision == "resolved"

    "low"
  end

  def manifest_resolution_notes(decision, candidates)
    return ["No manifest candidate matched the current files strongly enough."] if decision == "no_match"
    return ["More than one manifest candidate matched the current files. Review the candidate list before closeout."] if candidates.size > 1

    ["One manifest candidate matched weakly; review before using it for closeout."]
  end

  def doc_sync_rows_for(files)
    files.map do |path|
      category = category_for(path)
      domain = domain_for(path)
      docs = DOCS_BY_DOMAIN.fetch(domain, []).dup
      docs << "docs/codex-local-tooling-todo.md" if path.start_with?("scripts/")
      docs << "docs/feature-delivery-workflow.md" if path.start_with?("scripts/") || path.start_with?(".agents/") || path.include?("codex")
      docs << "docs/agent-operating-model.yaml" if category.start_with?("backend_") || category == "docs"
      docs.uniq!
      {
        file: path,
        category: category,
        domain: domain,
        likely_docs: docs,
        generated_artifacts: required_generated_artifacts_for(path, category),
        validation_commands: validation_rules.fetch(category, []),
        residual_risk: doc_sync_residual_risk(path, category, docs)
      }
    end
  end

  def required_generated_artifacts_for(path, category)
    artifacts = []
    artifacts << "docs/generated/local-tooling/codex-context/latest.machine.json" if path.start_with?("scripts/") || path.include?("codex-context")
    artifacts << "docs/generated/local-tooling/codex-context/latest.review.md" if path.start_with?("scripts/") || path.include?("codex-context")
    artifacts << "docs/tooling/codex-local-audits.yml" if path.start_with?("scripts/") || path == "Makefile"
    artifacts << "docs/generated/local-tooling/manifest-path-resolution.json" if category == "docs" || path.start_with?(".agents/")
    artifacts.uniq
  end

  def doc_sync_residual_risk(path, category, docs)
    risks = []
    risks << "No likely docs were resolved for this path." if docs.empty?
    risks << "Script or Makefile changes can invalidate generated audit registry outputs." if path.start_with?("scripts/") || path == "Makefile"
    risks << "Agent-facing logic changes may also require manifest and closeout updates." if path.start_with?(".agents/") || category == "docs"
    risks.uniq
  end

  def changeset_playbook_steps(files, manifest, preset, docs)
    resolved_manifest = preset.dig(:manifest_resolution, :manifest_path)
    closeout_command = resolved_manifest ? "make closeout-bundle manifest=#{resolved_manifest}" : "make closeout-bundle manifest=.agents/feature-manifests/<feature-id>-manifest.yaml"
    [
      {
        step: 1,
        kind: "read",
        title: "Review changeset shape",
        commands: ["make diff-summary"],
        outputs: ["docs/generated/local-tooling/diff-summary.md"],
        purpose: "See domain/category grouping before opening files."
      },
      {
        step: 2,
        kind: "decide",
        title: "Confirm manifest path",
        commands: ["make audit-manifest-decision", "make resolve-manifest-path"],
        outputs: ["docs/generated/local-tooling/manifest-decision-summary.md", "docs/generated/local-tooling/manifest-path-resolution-summary.md"],
        purpose: manifest[:manifest_required] ? "Manifest is required before final closeout." : "Manifest is optional, but the decision should still be visible.",
        decision: manifest[:decision],
        resolved_manifest: resolved_manifest
      },
      {
        step: 3,
        kind: "audit",
        title: "Run focused audits",
        commands: router_audits_for(files).first(12),
        outputs: ["docs/generated/local-tooling/audit-router-summary.md", "docs/generated/local-tooling/doc-sync-preflight-summary.md"],
        purpose: "Pick the smallest report set that matches the current files."
      },
      {
        step: 4,
        kind: "update",
        title: "Update living docs and agent artifacts",
        files: docs.flat_map { |row| row[:likely_docs] }.uniq.first(20),
        purpose: "Keep business, technical, and agent-safety docs synchronized with the changeset."
      },
      {
        step: 5,
        kind: "validate",
        title: "Run preset validation",
        commands: preset[:commands],
        outputs: preset[:supporting_reports],
        purpose: "Use one preset instead of manually assembling commands."
      },
      {
        step: 6,
        kind: "closeout",
        title: "Use resolved manifest path for final closeout",
        commands: [closeout_command],
        purpose: resolved_manifest ? "Resolved manifest path can be reused directly in closeout commands." : "Resolver could not pick one manifest deterministically; replace the placeholder after review."
      }
    ]
  end

  def validation_preset_reasons(preset, files, categories, domains, manifest)
    reasons = []
    reasons << "Manifest decision is `#{manifest[:decision]}`."
    reasons << "Changed files span #{categories.size} categories." if categories.any?
    reasons << "Changed files span #{domains.size} domains." if domains.any?
    reasons << "Preset `#{preset}` keeps validation selection deterministic for #{files.size} files."
    reasons
  end

  def validation_preset_commands(preset, files, categories, manifest_resolution)
    targeted = dedupe_command_rows(
      targeted_backend_commands(files, related_tests(files)) +
      targeted_frontend_commands(files, categories) +
      targeted_docs_commands(files, categories) +
      targeted_generated_commands(files)
    ).map { |row| row[:command] }
    manifest_path = manifest_resolution[:manifest_path] || ".agents/feature-manifests/<feature-id>-manifest.yaml"
    case preset
    when "manifest-required"
      ([
        "make recommend-targeted-tests",
        "make closeout-bundle manifest=#{manifest_path}",
        "make feature-closeout-audit manifest=#{manifest_path}"
      ] + targeted + ["make audit-agent-safety"]).uniq.first(12)
    when "full-closeout"
      (["make closeout-bundle"] + targeted + ["make audit-agent-safety"]).uniq.first(12)
    when "standard-backend"
      (["make recommend-targeted-tests"] + targeted + ["cd apps/themuffinman && ./mvnw test"]).uniq.first(10)
    when "standard-frontend"
      (["make recommend-targeted-tests"] + targeted + ["npm --prefix apps/themuffinman/frontend run type-check", "npm --prefix apps/themuffinman/frontend run build"]).uniq.first(10)
    when "fast"
      ["make fast-check", "make recommend-targeted-tests", "make audit-summary-index"]
    else
      ["make audit-router", "make recommend-targeted-tests", "make audit-summary-index"]
    end
  end

  def validation_preset_supporting_reports(preset)
    reports = ["docs/generated/local-tooling/targeted-tests-summary.md", "docs/generated/local-tooling/audit-router-summary.md"]
    reports << "docs/generated/local-tooling/doc-sync-required-surfaces-summary.md"
    reports << "docs/generated/local-tooling/closeout-bundle-summary.md" if %w[full-closeout manifest-required].include?(preset)
    reports << "docs/generated/local-tooling/fast-check-report-summary.md" if preset == "fast"
    reports
  end

  def audit_json_output_for(audit_id)
    registry_entry = AUDIT_REGISTRY.find { |target, _script, _output| target == audit_id }
    raise "Unknown audit target: #{audit_id}" unless registry_entry

    output = registry_entry[2]
    raise "Audit delta does not support templated outputs for #{audit_id}" if output.include?("<")
    return output if output.end_with?(".json")
    if output.end_with?("-summary.md")
      same_stem = output.sub(/\.md\z/, ".json")
      return same_stem if File.exist?(abs(same_stem))

      return output.sub(/-summary\.md\z/, ".json")
    end
    return output.sub(/\.md\z/, ".json") if output.end_with?(".md")

    raise "Could not infer JSON output for #{audit_id}"
  end

  def history_snapshots_for(audit_id)
    rel_glob("#{OUT}/.history/#{audit_id}/*.json").map do |path|
      {path: path, mtime: File.mtime(abs(path)).utc.iso8601}
    end.sort_by { |entry| entry[:mtime] }
  end

  def latest_history_snapshot(audit_id)
    history_snapshots_for(audit_id).last
  end

  def risk_signals_from_report(payload)
    collect_signal_entries(payload).uniq.sort
  end

  def collect_signal_entries(value, path = [])
    case value
    when Hash
      value.flat_map do |key, child|
        key_path = path + [key.to_s]
        signals = risk_key?(key.to_s) ? normalize_signal_entries(key_path.join("."), child) : []
        signals + collect_signal_entries(child, key_path)
      end
    when Array
      value.flat_map { |child| collect_signal_entries(child, path) }
    else
      []
    end
  end

  def normalize_signal_entries(label, value)
    case value
    when Array
      value.flat_map { |entry| normalize_signal_entries(label, entry) }
    when Hash
      keys = %w[id file reason command path category domain preset decision]
      summary = keys.each_with_object([]) do |key, parts|
        match = value[key] || value[key.to_sym]
        parts << match if match
      end.join(" | ")
      ["#{label}: #{summary.empty? ? JSON.generate(value)[0, 200] : summary}"]
    else
      ["#{label}: #{value}"]
    end
  end

  def risk_key?(key)
    key.match?(/risk|warning|missing|review|issue|failure|gap|stale|conflict|residual|required/i)
  end

  def count_deltas(previous, current)
    current_counts = flatten_count_fields(current || {})
    previous_counts = flatten_count_fields(previous || {})
    (current_counts.keys | previous_counts.keys).sort.map do |key|
      old_value = previous_counts[key] || 0
      new_value = current_counts[key] || 0
      next if old_value == new_value

      {field: key, previous: old_value, current: new_value, delta: new_value - old_value}
    end.compact.first(30)
  end

  def flatten_count_fields(value, path = [], acc = {})
    case value
    when Hash
      value.each do |key, child|
        key_path = path + [key.to_s]
        if child.is_a?(Integer) && (key.to_s.end_with?("_count") || key.to_s =~ /count|total|rows|findings|issues|gaps|missing|stale/i)
          acc[key_path.join(".")] = child
        else
          flatten_count_fields(child, key_path, acc)
        end
      end
    when Array
      acc[path.join(".")] = value.size if path.last.to_s.match?(/files|rows|items|groups|audits|commands|risks|warnings|missing|review/i)
      value.first(10).each_with_index { |child, index| flatten_count_fields(child, path + [index.to_s], acc) if child.is_a?(Hash) }
    end
    acc
  end

  def top_level_field_deltas(previous, current)
    old_hash = previous.is_a?(Hash) ? previous : {}
    new_hash = current.is_a?(Hash) ? current : {}
    (old_hash.keys | new_hash.keys).first(40).each_with_object([]) do |key, rows|
      old_value = summarize_delta_value(old_hash[key] || old_hash[key.to_sym])
      new_value = summarize_delta_value(new_hash[key] || new_hash[key.to_sym])
      next if old_value == new_value

      rows << {field: key, previous: old_value, current: new_value}
    end
  end

  def summarize_delta_value(value)
    case value
    when Array then "array(size=#{value.size})"
    when Hash then "object(keys=#{value.keys.size})"
    else value
    end
  end

  def audit_delta_notes(current_output, current, previous)
    notes = []
    notes << "Run the target audit at least twice to build a richer delta history." unless previous
    notes << "No current JSON output was found at `#{current_output}`." unless current
    notes << "History snapshots are created automatically when a report changes." if current
    notes
  end

  def feature_slice(kind, files, domains, categories)
    {
      id: kind,
      purpose: "Implement the smallest #{kind} behavior or contract change before widening scope.",
      files: files.first(30),
      docs: domains.flat_map { |domain| DOCS_BY_DOMAIN.fetch(domain, []) }.uniq.first(12),
      validation: categories.flat_map { |category| validation_rules.fetch(category, []) }.uniq.first(10)
    }
  end

  def targeted_backend_commands(files, direct_tests)
    backend_files = files.select { |path| category_for(path).start_with?("backend_") || path.include?("/src/main/java/") }
    return [] if backend_files.empty?

    rows = []
    if direct_tests.any?
      test_names = direct_tests.map { |path| File.basename(path, ".java") }.uniq.first(8)
      rows << {
        command: "cd apps/themuffinman && ./mvnw test -Dtest=#{test_names.join(',')}",
        reason: "Runs nearest backend tests for changed Java files.",
        confidence: "high",
        covers: direct_tests.first(8),
        uncovered: backend_files.size > direct_tests.size ? ["Some backend files only matched by domain or broad category."] : []
      }
    else
      rows << {
        command: "cd apps/themuffinman && ./mvnw test",
        reason: "Backend files changed but no nearby direct tests were found.",
        confidence: "medium",
        covers: backend_files.first(12),
        uncovered: ["No direct test class name or domain match was found."]
      }
    end
    rows
  end

  def targeted_frontend_commands(files, categories)
    return [] unless categories.any? { |category| category.start_with?("frontend_") }

    frontend_files = files.select { |path| path.include?("/frontend/") }.first(12)
    [
      {
        command: "npm --prefix apps/themuffinman/frontend run type-check",
        reason: "Frontend TypeScript or Vue files changed.",
        confidence: "high",
        covers: frontend_files,
        uncovered: []
      },
      {
        command: "npm --prefix apps/themuffinman/frontend run build",
        reason: "Covers Vite/build-time imports, generated contract usage, and production bundling.",
        confidence: "medium",
        covers: frontend_files,
        uncovered: ["Does not replace browser-level interaction checks for visual or workflow changes."]
      }
    ]
  end

  def targeted_docs_commands(files, categories)
    return [] unless categories.include?("docs") || files.any? { |path| path.start_with?(".agents/") }

    [
      {
        command: "make audit-documentation",
        reason: "Docs, plans, or agent artifacts changed.",
        confidence: "high",
        covers: files.select { |path| path.start_with?("docs/") || path.start_with?(".agents/") }.first(12),
        uncovered: []
      },
      {
        command: "make audit-doc-canonical-phrases",
        reason: "Protected documentation wording may be affected by docs or agent-safety edits.",
        confidence: "medium",
        covers: files.select { |path| path.start_with?("docs/") }.first(12),
        uncovered: ["Does not validate Java-side agent operating model tests."]
      }
    ]
  end

  def targeted_generated_commands(files)
    return [] unless files.any? { |path| LocalToolingCommon.generated_path?(path) || path.start_with?("scripts/") || path == "Makefile" }

    [
      {
        command: "make audit-generated-artifact-freshness",
        reason: "Generated artifacts, generation scripts, or Make targets changed.",
        confidence: "high",
        covers: files.select { |path| LocalToolingCommon.generated_path?(path) || path.start_with?("scripts/") || path == "Makefile" }.first(12),
        uncovered: []
      },
      {
        command: "make audit-generated-commit-scope",
        reason: "Classifies changed generated artifacts before closeout.",
        confidence: "medium",
        covers: files.select { |path| LocalToolingCommon.generated_path?(path) }.first(12),
        uncovered: ["Advisory only; reviewer still chooses which generated files belong in the changeset."]
      }
    ]
  end

  def targeted_scenario_commands(domains)
    scenarios = regression_scenarios.select { |scenario| domains.include?(scenario["domain"]) }
    scenarios.first(6).map do |scenario|
      {
        command: Array(scenario["commands"]).first,
        reason: "Regression scenario `#{scenario['id']}` covers #{scenario['scenario']}",
        confidence: scenario["risk"] == "high" ? "high" : "medium",
        covers: Array(scenario["test_files"]),
        uncovered: []
      }
    end
  end

  def regression_scenarios
    data = YAML.load_file(abs("docs/regression-scenario-catalog.yaml"))
    data.fetch("regression_scenarios", [])
  rescue StandardError
    []
  end

  def dedupe_command_rows(rows)
    seen = Set.new
    rows.compact.select do |row|
      command = row[:command].to_s
      next false if command.empty? || seen.include?(command)

      seen << command
      true
    end
  end

  def targeted_residual_risk(files, domains, categories, commands)
    risks = []
    risks << "No changed files were available; recommendations may be incomplete." if files.empty?
    risks << "Multiple domains changed; targeted commands do not prove cross-domain integration." if domains.size > 1
    risks << "Backend behavior changed; full `cd apps/themuffinman && ./mvnw test` may still be required before closeout." if categories.any? { |category| category.start_with?("backend_") }
    risks << "Frontend behavior changed; targeted type/build checks do not verify manual UX behavior." if categories.any? { |category| category.start_with?("frontend_") }
    risks << "No targeted command could be selected; fall back to audit-router and full validation." if commands.empty?
    risks.uniq
  end

  def context_budget(value)
    case value.to_s
    when "medium" then {id: "medium", file_limit: 50}
    when "large" then {id: "large", file_limit: 100}
    else {id: "small", file_limit: 20}
    end
  end

  def budget_read_next(topic, files, omitted_sections)
    next_steps = []
    next_steps << "Rerun with `budget=medium` or `budget=large` if omitted sections matter." if omitted_sections.any?
    next_steps << "Rerun with `include_generated=true` when generated artifacts are the primary review target."
    next_steps << "Rerun with `include_agents=true` when temporary plan or manifest files are the primary review target."
    next_steps << "Use `make context-pack topic=#{topic} budget=large` for a broader handoff." if files.size >= 20
    next_steps.uniq
  end

  def filter_files(files, options)
    LocalToolingCommon.filter_file_list(
      files,
      include_generated: LocalToolingCommon.truthy?(options["include_generated"]),
      include_agents: LocalToolingCommon.truthy?(options["include_agents"])
    )
  end

  def diagnostic_command(command_name)
    case command_name
    when "backend_test" then ["./mvnw", "test"]
    when "frontend_type_check" then ["npm", "run", "type-check"]
    when "frontend_build" then ["npm", "run", "build"]
    else raise "unknown diagnostic command: #{command_name}"
    end
  end

  def command_workdir(command_name)
    command_name.start_with?("frontend") ? File.join(REPO_ROOT, "apps/themuffinman/frontend") : File.join(REPO_ROOT, "apps/themuffinman")
  end
end
