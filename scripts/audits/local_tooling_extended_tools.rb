#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "digest"
require "json"
require "open3"
require "time"
require_relative "../local_tooling_common"

module LocalToolingExtendedTools
  extend self

  REPO_ROOT = LocalToolingCommon::REPO_ROOT
  OUT = "docs/generated/local-tooling"
  AUDIT_REGISTRY = [
    ["audit-change-impact-preflight", "scripts/audits/audit-change-impact-preflight.rb", "docs/generated/local-tooling/change-impact-preflight-summary.md"],
    ["audit-router", "scripts/audits/audit-router.rb", "docs/generated/local-tooling/audit-router-summary.md"],
    ["context-pack", "scripts/audits/generate-context-pack.rb", "docs/generated/local-tooling/context-packs"],
    ["repo-map", "scripts/audits/generate-repo-map.rb", "docs/generated/local-tooling/repo-map-summary.md"],
    ["symbol-index", "scripts/audits/generate-symbol-index.rb", "docs/generated/local-tooling/symbol-index-summary.md"],
    ["endpoint-contract-packs", "scripts/audits/generate-endpoint-contract-packs.rb", "docs/generated/local-tooling/endpoint-contract-packs"],
    ["validation-matrix", "scripts/audits/generate-validation-matrix.rb", "docs/generated/local-tooling/validation-matrix-summary.md"],
    ["audit-doc-sync-preflight", "scripts/audits/audit-doc-sync-preflight.rb", "docs/generated/local-tooling/doc-sync-preflight-summary.md"],
    ["audit-migration-entity-drift", "scripts/audits/audit-migration-entity-drift.rb", "docs/generated/local-tooling/migration-entity-drift-audit-summary.md"],
    ["audit-test-gap-recommendations", "scripts/audits/audit-test-gap-recommendations.rb", "docs/generated/local-tooling/test-gap-recommendations-summary.md"],
    ["audit-frontend-usage-graph", "scripts/audits/audit-frontend-usage-graph.rb", "docs/generated/local-tooling/frontend-usage-graph-summary.md"],
    ["audit-backend-dependency-graph", "scripts/audits/audit-backend-dependency-graph.rb", "docs/generated/local-tooling/backend-dependency-graph-summary.md"],
    ["diff-summary", "scripts/audits/generate-diff-summary.rb", "docs/generated/local-tooling/diff-summary.md"],
    ["session-handoff", "scripts/audits/generate-session-handoff.rb", "docs/generated/local-tooling/session-handoffs"],
    ["plan-scaffold-discovery", "scripts/audits/generate-plan-scaffold-discovery.rb", ".agents/<topic>-plan.md"],
    ["audit-summary-index", "scripts/audits/generate-audit-summary-index.rb", "docs/generated/local-tooling/audit-summary-index.md"],
    ["generate-audit-registry-artifacts", "scripts/audits/generate-audit-registry-artifacts.rb", "docs/tooling/codex-local-audits.yml"],
    ["fast-check", "scripts/audits/generate-fast-check-report.rb", "docs/generated/local-tooling/fast-check-report-summary.md"],
    ["diagnose-backend-test", "scripts/audits/diagnose-backend-test.rb", "docs/generated/local-tooling/diagnostics/backend-test-latest-summary.md"],
    ["diagnose-frontend-type-check", "scripts/audits/diagnose-frontend-type-check.rb", "docs/generated/local-tooling/diagnostics/frontend-type-check-latest-summary.md"],
    ["diagnose-frontend-build", "scripts/audits/diagnose-frontend-build.rb", "docs/generated/local-tooling/diagnostics/frontend-build-latest-summary.md"],
    ["api-contract-snapshot", "scripts/audits/generate-api-contract-snapshot.rb", "docs/generated/local-tooling/api-contract-snapshot-summary.md"],
    ["audit-doc-canonical-phrases", "scripts/audits/audit-doc-canonical-phrases.rb", "docs/generated/local-tooling/doc-canonical-phrases-summary.md"],
    ["audit-sandbox-data-coverage-pack", "scripts/audits/audit-sandbox-data-coverage-pack.rb", "docs/generated/local-tooling/sandbox-data-coverage-pack-summary.md"],
    ["smoke-local-authenticated", "scripts/audits/smoke-local-authenticated.rb", "docs/generated/local-tooling/smoke/local-authenticated-latest.json"],
    ["smoke-local-dashboard", "scripts/audits/smoke-local-dashboard.rb", "docs/generated/local-tooling/smoke/local-dashboard-latest.json"],
    ["closeout-bundle", "scripts/audits/generate-closeout-bundle.rb", "docs/generated/local-tooling/closeout-bundle-summary.md"]
  ].freeze

  DOCS_BY_DOMAIN = {
    "workmarket" => %w[docs/business-logic.md docs/domain-technical.md docs/agent-operating-model.md docs/agent-operating-model.yaml],
    "social" => %w[docs/business-logic.md docs/domain-technical.md docs/agent-operating-model.md docs/agent-operating-model.yaml],
    "identity" => %w[docs/business-logic.md docs/domain-technical.md docs/agent-operating-model.md],
    "location" => %w[docs/business-logic.md docs/domain-technical.md docs/location-services.md docs/agent-operating-model.md docs/agent-operating-model.yaml],
    "chat" => %w[docs/business-logic.md docs/domain-technical.md docs/agent-operating-model.md docs/agent-operating-model.yaml],
    "agent" => %w[docs/agent-operating-model.md docs/agent-operating-model.yaml docs/domain-technical.md]
  }.freeze

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
    files = files.first(80)
    domains = files.map { |path| domain_for(path) }.uniq.sort
    audits = relevant_audit_summaries(files, domains)
    report = {
      generated_at: now,
      topic: topic,
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
    files = argv.empty? || argv.include?("--changed") ? changed_files : argv.reject { |arg| arg.start_with?("--") }
    files = files.uniq
    matrix = validation_rules
    categories = files.map { |path| category_for(path) }.uniq.sort
    audits = router_audits_for(files)
    commands = categories.flat_map { |category| matrix.fetch(category, []) }.uniq
    report = {
      generated_at: now,
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

  def run_doc_sync_preflight(argv)
    files = argv.empty? ? changed_files : argv
    rows = files.map do |path|
      category = category_for(path)
      domain = domain_for(path)
      docs = DOCS_BY_DOMAIN.fetch(domain, []).dup
      docs << "docs/codex-local-tooling-todo.md" if path.start_with?("scripts/")
      docs << "docs/agent-operating-model.yaml" if category.start_with?("backend_") || category == "docs"
      docs.uniq!
      {file: path, category: category, domain: domain, likely_docs: docs}
    end
    report = {generated_at: now, files: rows, unique_docs: rows.flat_map { |row| row[:likely_docs] }.uniq.sort}
    write_report("doc-sync-preflight", "Doc Sync Preflight", report)
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
    entries = git_status_entries
    report = {
      generated_at: now,
      changed_file_count: entries.size,
      groups: entries.group_by { |entry| [domain_for(entry[:path]), category_for(entry[:path])] }.map { |(domain, category), rows| {domain: domain, category: category, count: rows.size, files: rows} },
      recommended_audits: router_audits_for(entries.map { |entry| entry[:path] })
    }
    write_report("diff-summary", "Diff Summary", report, summary_path: "#{OUT}/diff-summary.md")
  end

  def run_session_handoff(argv)
    topic = slug(parse_key_values(argv)["topic"] || argv.first || "current-work")
    files = changed_files
    report = {
      generated_at: now,
      topic: topic,
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
    write_report("audit-summary-index", "Audit Summary Index", {generated_at: now, registry: registry, summaries: summaries}, summary_path: "#{OUT}/audit-summary-index.md")
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
    files = option_files(parse_key_values(argv), argv)
    files = changed_files if files.empty?
    audits = router_audits_for(files).first(10)
    commands = files.flat_map { |path| validation_rules.fetch(category_for(path), []) }.uniq.first(8)
    report = {generated_at: now, files: files, audits_to_run: audits, commands_to_run: commands, note: "Advisory fast-check plan only; run full validation for behavior changes."}
    write_report("fast-check-report", "Fast Check Report", report)
  end

  def run_api_contract_snapshot(_argv)
    report = {generated_at: now, endpoint_count: endpoint_entries.size, endpoints: endpoint_entries}
    write_report("api-contract-snapshot", "API Contract Snapshot", report)
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
      final_checks: ["make audit-summary-index", options["manifest"] ? "make feature-closeout-audit manifest=#{options['manifest']}" : nil].compact
    }
    write_report("closeout-bundle", "Closeout Bundle", report)
  end

  def run_diagnostic(argv, command_name)
    command = diagnostic_command(command_name)
    started = now
    stdout, stderr, status = Open3.capture3(*command, chdir: command_workdir(command_name))
    failures = (stdout + "\n" + stderr).lines.select { |line| line.match?(/FAIL|ERROR|Exception|Cannot find|TS\d+|BUILD FAILED/i) }.first(80)
    report = {generated_at: started, command: command.join(" "), success: status.success?, exit_status: status.exitstatus, failure_lines: failures, changed_files: changed_files}
    name = command_name.tr("_", "-")
    write_report("diagnostics/#{name}-latest", "Diagnostic #{name}", report)
    exit(status.exitstatus || 1) unless status.success?
  end

  private

  def write_smoke_placeholder(name, argv, note)
    payload = {generated_at: now, smoke: name, status: "not_run", args: argv, note: note}
    LocalToolingCommon.write_json("#{OUT}/smoke/#{name}-latest.json", payload)
    LocalToolingCommon.write_text("#{OUT}/smoke/#{name}-latest-summary.md", "# Smoke #{name}\n\n- Status: `not_run`\n- Note: #{note}\n")
    puts "Smoke #{name}: not_run"
  end

  def write_report(base_name, title, payload, terminal: true, summary_path: nil)
    json_path = "#{OUT}/#{base_name}.json"
    md_path = summary_path || "#{OUT}/#{base_name}-summary.md"
    LocalToolingCommon.write_json(json_path, payload)
    LocalToolingCommon.write_text(md_path, summary_markdown(title, payload))
    update_audit_cache(base_name, json_path, payload)
    puts terminal_line(title, payload, json_path, md_path) if terminal
  end

  def update_audit_cache(base_name, json_path, payload)
    cache_path = "#{OUT}/.cache/audit-inputs.json"
    cache = File.exist?(abs(cache_path)) ? JSON.parse(File.read(abs(cache_path))) : {}
    cache[base_name] = {
      "updated_at" => now,
      "output" => json_path,
      "cache_status" => "refreshed",
      "payload_checksum" => Digest::SHA256.hexdigest(JSON.generate(payload))
    }
    LocalToolingCommon.write_json(cache_path, cache)
  rescue StandardError
    nil
  end

  def summary_markdown(title, payload)
    lines = ["# #{title}", ""]
    payload.each do |key, value|
      case value
      when Array
        lines << "## `#{key}`"
        lines << ""
        value.first(25).each { |entry| lines << "- `#{entry}`".gsub("=>", ": ") }
        lines << ""
      when Hash
        lines << "- `#{key}`: `#{value.size}` entries"
      else
        lines << "- #{LocalToolingCommon.titleize_slug(key.to_s)}: `#{value}`"
      end
    end
    lines.join("\n") + "\n"
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

  def router_audits_for(files)
    categories = files.map { |path| category_for(path) }
    audits = []
    audits += ["make audit-read-surface-inventory", "make audit-repository-fetch", "make audit-mapper-usage"] if categories.any? { |category| category.start_with?("backend_") }
    audits += ["make audit-api-contract-drift", "make audit-endpoint-callsite-linker", "make endpoint-contract-packs"] if categories.include?("backend_controller") || categories.include?("frontend_api")
    audits += ["make audit-frontend-route-surfaces", "make audit-async-mutation-flow", "make audit-frontend-usage-graph"] if categories.any? { |category| category.start_with?("frontend_") }
    audits += ["make audit-doc-sync-preflight", "make audit-documentation", "make audit-doc-canonical-phrases"] if categories.include?("docs")
    audits += ["make audit-migration-entity-drift"] if categories.include?("backend_model")
    audits += ["make audit-test-gap-recommendations", "make audit-summary-index"]
    audits.uniq
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
    LocalToolingCommon.run_command("git", "status", "--short").lines.each_with_object([]) do |line, entries|
      next if line.strip.empty?
      entries << {status: line[0, 2].strip, path: line[3..].to_s.strip.split(" -> ").last}
    end
  rescue StandardError
    []
  end

  def changed_files
    git_status_entries.map { |entry| entry[:path] }.select { |path| File.exist?(abs(path)) }.uniq
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

  def read(path)
    normalize_text(File.binread(abs(path)))
  rescue Errno::ENOENT
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
    files.map(&:strip).reject(&:empty?).select { |path| File.exist?(abs(path)) }.uniq
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
