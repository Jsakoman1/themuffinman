#!/usr/bin/env ruby
# frozen_string_literal: true

require "digest"
require "fileutils"
require "json"
require "open3"
require "set"
require "time"
require_relative "../local_tooling_common"
require_relative "./local_tooling_extended_tools"

module CodexLocalContextGateway
  extend self

  REPO_ROOT = LocalToolingCommon::REPO_ROOT
  CONTEXT_ROOT = File.join(REPO_ROOT, "docs/generated/local-tooling/codex-context")
  PACK_ROOT = File.join(CONTEXT_ROOT, "packs")
  HISTORY_ROOT = File.join(CONTEXT_ROOT, ".history")
  LATEST_MACHINE_PATH = File.join(CONTEXT_ROOT, "latest.machine.json")
  LATEST_HUMAN_PATH = File.join(CONTEXT_ROOT, "latest.human.md")
  CONFIG_PATH = File.join(CONTEXT_ROOT, "config.json")
  PLAN_PATH = File.join(REPO_ROOT, ".agents/codex-local-context-gateway-master-plan.md")

  DEFAULT_CONFIG = {
    "schemaVersion" => 1,
    "defaultBudgetTokens" => 6000,
    "defaultMode" => "implementation",
    "reuseExistingAuditOutputs" => true,
    "maxFiles" => 60,
    "providerDefaults" => {
      "repo-map" => true,
      "symbol-index" => true,
      "recommend-targeted-tests" => true,
      "endpoint-contract-packs" => true,
      "audit-frontend-usage-graph" => true,
      "audit-backend-dependency-graph" => true,
      "validation-matrix" => true,
      "session-handoff" => true,
      "link-symbol-to-tests" => true,
      "dto-usage-pack" => true,
      "workflow-slice-pack" => true,
      "plan-code-map" => true,
      "rank-changeset-hotspots" => true,
      "domain-pack" => true,
      "audit-delta-report" => true,
      "codebase-capsule" => true
    }
  }.freeze

  OUTPUT_REGISTRY = {
    "repo-map" => "docs/generated/local-tooling/repo-map.json",
    "symbol-index" => "docs/generated/local-tooling/symbol-index.json",
    "recommend-targeted-tests" => "docs/generated/local-tooling/targeted-tests.json",
    "endpoint-contract-packs" => "docs/generated/local-tooling/endpoint-contract-packs/index.json",
    "audit-frontend-usage-graph" => "docs/generated/local-tooling/frontend-usage-graph.json",
    "audit-backend-dependency-graph" => "docs/generated/local-tooling/backend-dependency-graph.json",
    "validation-matrix" => "docs/generated/local-tooling/validation-matrix.json",
    "audit-delta-report" => "docs/generated/local-tooling/audit-deltas/diff-summary.json",
    "codebase-capsule" => "docs/generated/local-tooling/codebase-capsule.json"
  }.freeze

  ALWAYS_REFRESH_TARGETS = Set.new(%w[
    dto-usage-pack
    workflow-slice-pack
    link-symbol-to-tests
  ]).freeze

  PROVIDER_ORDER = %w[
    intent
    git-diff
    ast-diff
    symbol-test
    dto-endpoint-frontend
    workflow-slice
    domain-pack
    targeted-tests
    endpoint-contract
    frontend-usage
    backend-dependency
    validation
    audit-delta
    session
    repo-map
    symbol-index
    hotspots
    codebase-capsule
    plan-code-map
  ].freeze

  def run(argv = ARGV)
    command, args = split_command(argv)
    case command
    when "collect" then collect(args)
    when "explain" then explain(args)
    when "clean" then clean
    else
      raise "Unknown command `#{command}`. Use collect, explain, or clean."
    end
  end

  def collect(argv)
    ensure_output_dirs
    options = parse_options(argv)
    request = build_request(options)
    packs = []
    failures = []

    intent_pack = build_intent_pack(request)
    packs << intent_pack if intent_pack

    diff_pack = build_git_diff_pack(request)
    packs << diff_pack if diff_pack

    ast_pack = build_ast_diff_pack(request)
    packs << ast_pack if ast_pack

    existing_tool_packs(request).each do |entry|
      if entry[:error]
        failures << entry[:error]
      elsif entry[:pack]
        packs << entry[:pack]
      end
    end

    packs = rescore_packs(packs.compact, request).sort_by { |pack| provider_sort_key(pack) }
    composition = compose_packs(packs, request["budgetTokens"])
    latest_payload = build_latest_payload(request, composition, failures)
    write_outputs(latest_payload)
  end

  def explain(argv)
    options = parse_options(argv)
    latest = read_json_file(LATEST_MACHINE_PATH)
    human_path = File.exist?(LATEST_HUMAN_PATH) ? "docs/generated/local-tooling/codex-context/latest.human.md" : nil
    explanation = []
    explanation << "# CODEX Local Context Explain"
    explanation << ""
    explanation << "- Generated At: `#{latest.fetch("generatedAt", "unknown")}`"
    explanation << "- Topic: `#{latest.fetch("topic", "current-change")}`"
    explanation << "- Mode: `#{latest.fetch("mode", "implementation")}`"
    explanation << "- Budget Tokens: `#{latest.fetch("budgetTokens", 0)}`"
    explanation << "- Included Packs: `#{latest.fetch("includedPackCount", 0)}`"
    explanation << "- Human Summary: `#{human_path || 'missing'}`"
    explanation << ""
    explanation << "## Included Packs"
    explanation << ""
    latest.fetch("packs", []).each do |pack|
      explanation << "- `#{pack["id"]}` `#{pack["kind"]}` mode=`#{pack["mode"]}` tokens=`#{pack["estimatedTokens"]}` relevance=`#{pack["relevance"]}`"
    end
    unless latest.fetch("excludedPacks", []).empty?
      explanation << ""
      explanation << "## Excluded Packs"
      explanation << ""
      latest.fetch("excludedPacks", []).each do |pack|
        explanation << "- `#{pack["id"]}` reason=`#{pack["reason"]}`"
      end
    end
    unless latest.fetch("providerFailures", []).empty?
      explanation << ""
      explanation << "## Provider Failures"
      explanation << ""
      latest.fetch("providerFailures", []).each do |failure|
        explanation << "- `#{failure}`"
      end
    end
    explanation_path = File.join(CONTEXT_ROOT, "latest.explain.md")
    File.write(explanation_path, explanation.join("\n") + "\n")
    puts "CODEX Local Context Explain\n  summary: docs/generated/local-tooling/codex-context/latest.explain.md\n  human: #{human_path || 'missing'}\n  machine: docs/generated/local-tooling/codex-context/latest.machine.json"
  end

  def clean
    FileUtils.rm_f(LATEST_MACHINE_PATH)
    FileUtils.rm_f(LATEST_HUMAN_PATH)
    FileUtils.rm_f(File.join(CONTEXT_ROOT, "latest.explain.md"))
    FileUtils.rm_rf(PACK_ROOT)
    FileUtils.rm_rf(HISTORY_ROOT)
    puts "CODEX Local Context\n  cleaned: docs/generated/local-tooling/codex-context generated outputs removed"
  end

  private

  def split_command(argv)
    first = argv.first
    return ["collect", argv] if first.nil? || first.include?("=") || first.start_with?("--")

    [first, argv.drop(1)]
  end

  def ensure_output_dirs
    FileUtils.mkdir_p(CONTEXT_ROOT)
    FileUtils.mkdir_p(PACK_ROOT)
    FileUtils.mkdir_p(HISTORY_ROOT)
  end

  def load_config
    config = deep_copy(DEFAULT_CONFIG)
    return config unless File.exist?(CONFIG_PATH)

    deep_merge(config, JSON.parse(File.read(CONFIG_PATH)))
  rescue JSON::ParserError
    config
  end

  def build_request(options)
    config = load_config
    snapshot = tooling_call(
      :changeset_snapshot,
      nil,
      include_generated: truthy?(options["include_generated"]),
      include_agents: truthy?(options["include_agents"])
    )
    files = resolve_files(options, config, snapshot)
    topic = normalize_topic(options["topic"], files)
    {
      "schemaVersion" => config["schemaVersion"],
      "generatedAt" => now,
      "topic" => topic,
      "mode" => options["mode"] || config["defaultMode"],
      "budgetTokens" => resolve_budget_tokens(options["budget"], config["defaultBudgetTokens"]),
      "files" => files.first(config["maxFiles"]),
      "allChangedFiles" => snapshot[:all_changed_files],
      "intent" => present_option(options["intent"]),
      "refresh" => truthy?(options["refresh"]),
      "reuseExistingAuditOutputs" => options.key?("reuse_existing") ? truthy?(options["reuse_existing"]) : config["reuseExistingAuditOutputs"],
      "includeGenerated" => truthy?(options["include_generated"]),
      "includeAgents" => truthy?(options["include_agents"]),
      "providerDefaults" => config["providerDefaults"],
      "changesetSnapshot" => stringify_row(snapshot)
    }
  end

  def build_intent_pack(request)
    intent = request["intent"].to_s.strip
    return nil if intent.empty?

    payload = {
      "intent" => intent,
      "topic" => request["topic"],
      "mode" => request["mode"],
      "requestedFiles" => request["files"].first(20)
    }
    build_pack(
      id: "intent",
      kind: "other",
      scope: {"files" => request["files"].first(20)},
      confidence: 1.0,
      relevance: 1.0,
      payload: payload,
      provider: "gateway.intent",
      source_commands: [],
      source_files: []
    )
  end

  def build_git_diff_pack(request)
    files = request["files"]
    all_changed = request["allChangedFiles"]
    snapshot = request.fetch("changesetSnapshot", {})
    status_rows = Array(snapshot["status_entries"] || git_status_rows)
    diff_payload = {
      "changedFiles" => all_changed,
      "selectedFiles" => files,
      "status" => status_rows,
      "diffStats" => files.map { |path| diff_stat_for(path, snapshot) }.compact,
      "notes" => diff_notes(files, all_changed)
    }
    build_pack(
      id: "git-diff",
      kind: "diff",
      scope: {"files" => files},
      confidence: all_changed.empty? ? 0.65 : 0.95,
      relevance: 1.0,
      payload: diff_payload,
      provider: "gateway.git_diff",
      source_commands: ["git status --short", "git diff --numstat -- <file>"],
      source_files: files
    )
  end

  def build_ast_diff_pack(request)
    files = request["files"]
    snapshot = request.fetch("changesetSnapshot", {})
    changed_lines = files.each_with_object({}) do |path, rows|
      rows[path] = Array(snapshot.dig("changed_line_map", path) || changed_line_numbers(path))
    end
    parser_backed = parser_backed_symbols(files, changed_lines)
    parser_backed_index = parser_backed.each_with_object({}) do |entry, rows|
      rows[entry["file"]] = entry
    end
    symbols = files.flat_map do |path|
      entry = parser_backed_index[path]
      next entry["spans"] if entry && Array(entry["spans"]).any?

      extract_changed_symbols(path, changed_lines.fetch(path, []))
    end
    payload = {
      "changedLineMap" => changed_lines,
      "symbols" => symbols.first(30),
      "coverageNote" => ast_coverage_note(parser_backed, files)
    }
    build_pack(
      id: "ast-diff",
      kind: "ast",
      scope: {
        "files" => files,
        "symbols" => symbols.map { |row| row["symbol"] }.uniq.first(30)
      },
      confidence: ast_confidence(symbols, parser_backed),
      relevance: files.empty? ? 0.4 : 0.95,
      payload: payload,
      provider: "gateway.ast_diff",
      source_commands: ["git diff --unified=0 -- <file>", "node scripts/audits/codex_ast_context.mjs"],
      source_files: files
    )
  end

  def existing_tool_packs(request)
    packs = []
    packs << in_memory_pack(
      request,
      id: "targeted-tests",
      kind: "test",
      provider: "existing-tool.recommend-targeted-tests",
      confidence: 0.95,
      relevance: 0.92,
      payload: targeted_tests_report_for(request),
      scope: {"files" => request["files"]},
      source_commands: ["make recommend-targeted-tests files=<csv>"]
    )
    packs << tool_wrapper_pack(request, "endpoint-contract-packs", "endpoint-contract", "endpoint-contract", [], OUTPUT_REGISTRY.fetch("endpoint-contract-packs"), endpoint_relevance(request), 0.9)
    packs << tool_wrapper_pack(request, "audit-frontend-usage-graph", "frontend-usage", "frontend-usage", [], OUTPUT_REGISTRY.fetch("audit-frontend-usage-graph"), frontend_relevance(request), 0.85)
    packs << tool_wrapper_pack(request, "audit-backend-dependency-graph", "backend-dependency", "symbol", [], OUTPUT_REGISTRY.fetch("audit-backend-dependency-graph"), backend_relevance(request), 0.88)
    packs << tool_wrapper_pack(request, "validation-matrix", "validation", "validation", [], OUTPUT_REGISTRY.fetch("validation-matrix"), 0.72, 0.98)
    packs << tool_wrapper_pack(request, "repo-map", "repo-map", "domain", [], OUTPUT_REGISTRY.fetch("repo-map"), 0.45, 0.98)
    packs << tool_wrapper_pack(request, "symbol-index", "symbol-index", "symbol", [], OUTPUT_REGISTRY.fetch("symbol-index"), symbol_index_relevance(request), 0.9)
    packs << tool_wrapper_pack(request, "codebase-capsule", "codebase-capsule", "other", [], OUTPUT_REGISTRY.fetch("codebase-capsule"), 0.25, 0.98)
    packs << in_memory_pack(
      request,
      id: "session",
      kind: "session",
      provider: "existing-tool.session-handoff",
      confidence: 0.82,
      relevance: request["files"].empty? ? 0.25 : 0.62,
      payload: session_handoff_report_for(request),
      scope: {"files" => request["files"]},
      source_commands: ["make session-handoff topic=<topic>"]
    )
    packs << symbol_to_test_pack(request)
    packs << dto_usage_pack(request)
    packs << workflow_slice_pack(request)
    packs << domain_pack(request)
    packs << in_memory_pack(
      request,
      id: "hotspots",
      kind: "other",
      provider: "existing-tool.rank-changeset-hotspots",
      confidence: 0.8,
      relevance: 0.64,
      payload: hotspot_report_for(request),
      scope: {"files" => request["files"]},
      source_commands: ["make rank-changeset-hotspots files=<csv>"]
    )
    plan_pack = plan_code_map_pack(request)
    packs << plan_pack if plan_pack
    packs << in_memory_pack(
      request,
      id: "audit-delta",
      kind: "validation",
      provider: "existing-tool.audit-delta-report",
      confidence: 0.78,
      relevance: 0.55,
      payload: audit_delta_report_for("diff-summary"),
      scope: {"files" => request["files"]},
      source_commands: ["make audit-delta-report audit=diff-summary"],
      source_files: [OUTPUT_REGISTRY.fetch("audit-delta-report")]
    )
    packs.compact
  end

  def in_memory_pack(request, id:, kind:, provider:, confidence:, relevance:, payload:, scope:, source_commands:, source_files: [])
    return nil if payload.nil?

    {
      pack: build_pack(
        id: id,
        kind: kind,
        scope: scope,
        confidence: confidence,
        relevance: relevance,
        payload: payload,
        provider: provider,
        source_commands: source_commands,
        source_files: source_files
      )
    }
  rescue StandardError => e
    {error: "#{id}: #{e.message.lines.first.to_s.strip}"}
  end

  def tool_wrapper_pack(request, target, id, kind, args, output_path, relevance, confidence)
    return nil unless provider_enabled?(request, target)

    ensure_tool_output(target, args, output_path, request)
    payload = read_json_relative(output_path)
    pack = build_pack(
      id: id,
      kind: kind,
      scope: infer_scope_for_payload(id, payload, request),
      confidence: confidence,
      relevance: relevance,
      payload: payload,
      provider: "existing-tool.#{target}",
      source_commands: ["make #{target}"],
      source_files: [output_path]
    )
    {pack: pack}
  rescue StandardError => e
    {error: "#{target}: #{e.message.lines.first.to_s.strip}"}
  end

  def symbol_to_test_pack(request)
    return nil unless provider_enabled?(request, "link-symbol-to-tests")

    symbol = first_changed_symbol_name(request)
    return nil if symbol.nil? || symbol.empty?

    output_path = "docs/generated/local-tooling/symbol-test-links/#{symbol.downcase}.json"
    ensure_tool_output("link-symbol-to-tests", ["symbol=#{symbol}"], output_path, request)
    payload = read_json_relative(output_path)
    {
      pack: build_pack(
        id: "symbol-test",
        kind: "test",
        scope: {"files" => request["files"], "symbols" => [symbol]},
        confidence: 0.88,
        relevance: 0.9,
        payload: payload,
        provider: "existing-tool.link-symbol-to-tests",
        source_commands: ["make link-symbol-to-tests symbol=#{symbol}"],
        source_files: [output_path]
      )
    }
  rescue StandardError => e
    {error: "link-symbol-to-tests: #{e.message.lines.first.to_s.strip}"}
  end

  def dto_usage_pack(request)
    return nil unless provider_enabled?(request, "dto-usage-pack")

    dto = first_changed_dto(request["files"])
    return nil if dto.nil? || dto.empty?

    output_path = "docs/generated/local-tooling/dto-usage-packs/#{dto.downcase}.json"
    ensure_tool_output("dto-usage-pack", ["dto=#{dto}"], output_path, request)
    payload = read_json_relative(output_path)
    {
      pack: build_pack(
        id: "dto-endpoint-frontend",
        kind: "endpoint-contract",
        scope: {"files" => request["files"], "dtoTypes" => [dto]},
        confidence: 0.9,
        relevance: 0.87,
        payload: payload,
        provider: "existing-tool.dto-usage-pack",
        source_commands: ["make dto-usage-pack dto=#{dto}"],
        source_files: [output_path]
      )
    }
  rescue StandardError => e
    {error: "dto-usage-pack: #{e.message.lines.first.to_s.strip}"}
  end

  def workflow_slice_pack(request)
    return nil unless provider_enabled?(request, "workflow-slice-pack")

    workflow = inferred_workflow_id(request["files"])
    return nil if workflow.nil? || workflow.empty?

    output_path = "docs/generated/local-tooling/workflow-slices/#{workflow}.json"
    ensure_tool_output("workflow-slice-pack", ["workflow=#{workflow}"], output_path, request)
    payload = read_json_relative(output_path)
    {
      pack: build_pack(
        id: "workflow-slice",
        kind: "domain",
        scope: {"files" => request["files"]},
        confidence: 0.8,
        relevance: 0.76,
        payload: payload,
        provider: "existing-tool.workflow-slice-pack",
        source_commands: ["make workflow-slice-pack workflow=#{workflow}"],
        source_files: [output_path]
      )
    }
  rescue StandardError => e
    {error: "workflow-slice-pack: #{e.message.lines.first.to_s.strip}"}
  end

  def domain_pack(request)
    return nil unless provider_enabled?(request, "domain-pack")

    domain = primary_domain(request["files"])
    return nil if domain.nil? || domain == "shared"

    output_path = "docs/generated/local-tooling/domain-packs/#{domain}.json"
    ensure_tool_output("domain-pack", ["domain=#{domain}"], output_path, request)
    payload = read_json_relative(output_path)
    {
      pack: build_pack(
        id: "domain-pack",
        kind: "domain",
        scope: {"files" => request["files"]},
        confidence: 0.84,
        relevance: 0.73,
        payload: payload,
        provider: "existing-tool.domain-pack",
        source_commands: ["make domain-pack domain=#{domain}"],
        source_files: [output_path]
      )
    }
  rescue StandardError => e
    {error: "domain-pack: #{e.message.lines.first.to_s.strip}"}
  end

  def plan_code_map_pack(request)
    return nil unless provider_enabled?(request, "plan-code-map")

    plan_file = request["files"].find { |path| path.start_with?(".agents/") && path.end_with?("-plan.md") } ||
      ".agents/codex-local-context-gateway-master-plan.md"
    return nil unless File.exist?(File.join(REPO_ROOT, plan_file))

    payload = plan_code_map_report_for(plan_file)
    {
      pack: build_pack(
        id: "plan-code-map",
        kind: "other",
        scope: {"files" => [plan_file]},
        confidence: 0.84,
        relevance: plan_file.include?("codex-local-context-gateway") ? 0.72 : 0.45,
        payload: payload,
        provider: "existing-tool.plan-code-map",
        source_commands: ["make plan-code-map plan=#{plan_file}"],
        source_files: [plan_file]
      )
    }
  rescue StandardError => e
    {error: "plan-code-map: #{e.message.lines.first.to_s.strip}"}
  end

  def compose_packs(packs, budget_tokens)
    active = packs.map { |pack| activate_pack_variant(pack, "full") }
    excluded = []
    total = total_tokens(active)
    return {packs: strip_internal_fields(active), excluded: excluded, total_tokens: total} if total <= budget_tokens

    downgrade_candidates = active.sort_by { |pack| [required_pack?(pack) ? 1 : 0, pack["relevance"], pack["confidence"], -pack["estimatedTokens"]] }
    low_priority = active.reject { |pack| required_pack?(pack) }.sort_by { |pack| [pack["relevance"], pack["confidence"], -pack["estimatedTokens"]] }
    %w[compact indexOnly].each do |variant|
      downgrade_candidates.each do |pack|
        next unless pack["_variants"].key?(variant)
        next if pack["mode"] == variant

        activate_pack_variant(pack, variant)
        total = total_tokens(active)
        return {packs: strip_internal_fields(active), excluded: excluded, total_tokens: total} if total <= budget_tokens
      end
    end

    low_priority.each do |pack|
      next if required_pack?(pack)

      active.delete(pack)
      excluded << {"id" => pack["id"], "reason" => "budget_excluded_after_downgrade", "relevance" => pack["relevance"]}
      total = total_tokens(active)
      return {packs: strip_internal_fields(active), excluded: excluded, total_tokens: total} if total <= budget_tokens
    end

    {packs: strip_internal_fields(active), excluded: excluded, total_tokens: total_tokens(active)}
  end

  def build_latest_payload(request, composition, failures)
    {
      "schemaVersion" => request["schemaVersion"],
      "generatedAt" => request["generatedAt"],
      "topic" => request["topic"],
      "mode" => request["mode"],
      "budgetTokens" => request["budgetTokens"],
      "totalEstimatedTokens" => composition[:total_tokens],
      "includedPackCount" => composition[:packs].size,
      "excludedPacks" => composition[:excluded],
      "files" => request["files"],
      "providerFailures" => failures,
      "packs" => composition[:packs],
      "gatewayProvenance" => {
        "provider" => "codex-local-context-gateway",
        "planPath" => relative_path(PLAN_PATH),
        "reuseExistingAuditOutputs" => request["reuseExistingAuditOutputs"]
      }
    }
  end

  def targeted_tests_report_for(request)
    files = request["files"]
    domains = files.map { |path| tooling_call(:domain_for, path) }.uniq.sort
    categories = files.map { |path| tooling_call(:category_for, path) }.uniq.sort
    direct_tests = tooling_call(:related_tests, files).first(12)
    commands = tooling_call(:targeted_backend_commands, files, direct_tests)
    commands += tooling_call(:targeted_frontend_commands, files, categories)
    commands += tooling_call(:targeted_docs_commands, files, categories)
    commands += tooling_call(:targeted_generated_commands, files)
    commands += tooling_call(:targeted_scenario_commands, domains)
    commands = tooling_call(:dedupe_command_rows, commands).first(14)
    {
      "generated_at" => now,
      "files_considered" => files.first(80),
      "domains" => domains,
      "categories" => categories,
      "direct_tests" => direct_tests,
      "recommended_commands" => stringify_rows(commands),
      "residual_risk" => tooling_call(:targeted_residual_risk, files, domains, categories, commands),
      "notes" => [
        "This in-memory gateway pack mirrors recommend-targeted-tests without rewriting its standalone report.",
        "Use full backend/frontend validation for broader behavioral confidence."
      ]
    }
  end

  def session_handoff_report_for(request)
    files = request["files"]
    topic = request["topic"]
    budget = tooling_call(:context_budget, "small")
    {
      "generated_at" => now,
      "topic" => topic,
      "budget" => budget[:id],
      "omitted_sections" => [],
      "read_next" => tooling_call(:budget_read_next, topic, files, []),
      "changed_files" => files,
      "plans" => tooling_call(:rel_glob, ".agents/*#{topic}*plan.md"),
      "manifests" => tooling_call(:rel_glob, ".agents/feature-manifests/*#{topic}*.yaml"),
      "recommended_audits" => tooling_call(:router_audits_for, files),
      "recommended_commands" => files.flat_map { |path| tooling_call(:validation_rules).fetch(tooling_call(:category_for, path), []) }.uniq
    }
  end

  def hotspot_report_for(request)
    tooling_call(:rank_changeset_hotspots_for, request["files"]).merge(
      "generated_at" => now
    )
  end

  def plan_code_map_report_for(plan_file)
    plan_text = safe_read(plan_file)
    plan_id = File.basename(plan_file, ".md")
    files = tooling_call(:plan_candidate_files, plan_file, plan_text)
    categories = files.map { |path| tooling_call(:category_for, path) }.uniq.sort
    domains = files.map { |path| tooling_call(:domain_for, path) }.uniq.sort
    manifest_resolution = tooling_call(:resolve_manifest_path_for, files)
    {
      "generated_at" => now,
      "plan" => plan_file,
      "plan_id" => plan_id,
      "categories" => categories,
      "domains" => domains,
      "mapped_files" => files.map { |path| stringify_row(tooling_call(:plan_file_map_entry, path, plan_text)) },
      "likely_docs" => tooling_call(:doc_sync_rows_for, files).flat_map { |row| row[:likely_docs] }.uniq.sort,
      "related_generated_artifacts" => tooling_call(:plan_related_generated_artifacts, plan_text, files),
      "related_audits" => tooling_call(:router_audits_for, files),
      "recommended_commands" => tooling_call(:validation_preset_for, files, tooling_call(:manifest_decision_for, files), manifest_resolution)[:commands],
      "related_manifests" => stringify_rows(tooling_call(:plan_related_manifests, plan_file, plan_text, files, manifest_resolution)),
      "residual_risk" => tooling_call(:plan_map_residual_risk, files, manifest_resolution)
    }
  end

  def audit_delta_report_for(audit_id)
    current_output = tooling_call(:audit_json_output_for, audit_id)
    current = tooling_call(:read_json_if_present, current_output)
    previous_snapshot = tooling_call(:latest_history_snapshot, audit_id)
    previous = previous_snapshot && tooling_call(:read_json_if_present, previous_snapshot[:path])
    {
      "generated_at" => now,
      "audit" => audit_id,
      "current_output" => current_output,
      "current_exists" => !current.nil?,
      "previous_output" => previous_snapshot && previous_snapshot[:path],
      "previous_exists" => !previous.nil?,
      "history_entries" => tooling_call(:history_snapshots_for, audit_id).last(10).map { |entry| entry[:path] },
      "changed" => current && previous ? Digest::SHA256.hexdigest(JSON.generate(current)) != Digest::SHA256.hexdigest(JSON.generate(previous)) : nil,
      "current_generated_at" => current && (current["generated_at"] || current[:generated_at]),
      "previous_generated_at" => previous && (previous["generated_at"] || previous[:generated_at]),
      "introduced_risks" => current && previous ? (tooling_call(:risk_signals_from_report, current) - tooling_call(:risk_signals_from_report, previous)).first(30) : [],
      "fixed_risks" => current && previous ? (tooling_call(:risk_signals_from_report, previous) - tooling_call(:risk_signals_from_report, current)).first(30) : [],
      "count_deltas" => stringify_rows(tooling_call(:count_deltas, previous, current)),
      "field_deltas" => stringify_rows(tooling_call(:top_level_field_deltas, previous, current)),
      "notes" => tooling_call(:audit_delta_notes, current_output, current, previous)
    }
  end

  def write_outputs(latest_payload)
    archive_latest_machine(latest_payload)
    File.write(LATEST_MACHINE_PATH, JSON.pretty_generate(latest_payload) + "\n")
    latest_payload.fetch("packs", []).each do |pack|
      pack_path = File.join(PACK_ROOT, "#{pack.fetch("id")}.json")
      File.write(pack_path, JSON.pretty_generate(pack) + "\n")
    end
    File.write(LATEST_HUMAN_PATH, human_markdown(latest_payload))
    puts "CODEX Local Context Gateway\n  machine: docs/generated/local-tooling/codex-context/latest.machine.json\n  human: docs/generated/local-tooling/codex-context/latest.human.md\n  packs: docs/generated/local-tooling/codex-context/packs/"
  end

  def archive_latest_machine(latest_payload)
    return unless File.exist?(LATEST_MACHINE_PATH)

    current_content = File.read(LATEST_MACHINE_PATH)
    next_content = JSON.pretty_generate(latest_payload) + "\n"
    return if Digest::SHA256.hexdigest(current_content) == Digest::SHA256.hexdigest(next_content)

    timestamp = now.tr(":", "-")
    File.write(File.join(HISTORY_ROOT, "#{timestamp}.json"), current_content)
  end

  def human_markdown(latest_payload)
    lines = []
    lines << "# CODEX Local Context"
    lines << ""
    lines << "- Generated At: `#{latest_payload["generatedAt"]}`"
    lines << "- Topic: `#{latest_payload["topic"]}`"
    lines << "- Mode: `#{latest_payload["mode"]}`"
    lines << "- Budget Tokens: `#{latest_payload["budgetTokens"]}`"
    lines << "- Total Estimated Tokens: `#{latest_payload["totalEstimatedTokens"]}`"
    lines << "- Included Packs: `#{latest_payload["includedPackCount"]}`"
    lines << ""
    unless latest_payload["files"].empty?
      lines << "## Selected Files"
      lines << ""
      latest_payload["files"].first(20).each { |path| lines << "- `#{path}`" }
      lines << ""
    end
    lines << "## Included Packs"
    lines << ""
    latest_payload.fetch("packs", []).each do |pack|
      lines << "- `#{pack["id"]}` `#{pack["kind"]}` mode=`#{pack["mode"]}` tokens=`#{pack["estimatedTokens"]}`"
      pack_human_summary(pack).first(3).each do |note|
        lines << "  #{note}"
      end
    end
    unless latest_payload.fetch("excludedPacks", []).empty?
      lines << ""
      lines << "## Excluded Packs"
      lines << ""
      latest_payload["excludedPacks"].each do |pack|
        lines << "- `#{pack["id"]}` reason=`#{pack["reason"]}`"
      end
    end
    unless latest_payload.fetch("providerFailures", []).empty?
      lines << ""
      lines << "## Provider Failures"
      lines << ""
      latest_payload["providerFailures"].each do |failure|
        lines << "- `#{failure}`"
      end
    end
    lines.join("\n") + "\n"
  end

  def pack_human_summary(pack)
    payload = pack["payload"]
    case pack["id"]
    when "git-diff"
      stats = Array(payload["diffStats"]).first(3).map { |row| "`#{row["path"]}` +#{row["added"]} -#{row["removed"]}" }
      stats.empty? ? ["No changed files were detected; this pack reflects current repo state only."] : stats
    when "ast-diff"
      Array(payload["symbols"]).first(3).map { |row| "`#{row["symbol"]}` in `#{row["file"]}` lines #{row["lineStart"]}-#{row["lineEnd"]}" }
    when "targeted-tests"
      Array(payload["recommended_commands"]).first(3).map { |row| "`#{row}`" }
    else
      generic_payload_facts(payload).first(3)
    end
  end

  def build_pack(id:, kind:, scope:, confidence:, relevance:, payload:, provider:, source_commands:, source_files:)
    full = payload
    compact = compact_payload(payload)
    index_only = index_payload(payload)
    pack = {
      "id" => id,
      "kind" => kind,
      "scope" => compact_scope(scope),
      "confidence" => confidence.round(2),
      "relevance" => relevance.round(2),
      "estimatedTokens" => estimate_tokens(full),
      "mode" => "full",
      "fingerprint" => fingerprint_for(provider, full, source_files),
      "provenance" => {
        "generatedAt" => now,
        "provider" => provider,
        "sourceCommands" => source_commands,
        "sourceFiles" => source_files,
        "sourceHashes" => source_files.each_with_object({}) do |path, hashes|
          absolute = absolute_path(path)
          hashes[path] = Digest::SHA256.hexdigest(File.binread(absolute)) if File.file?(absolute)
        end
      },
      "payload" => full,
      "_variants" => {
        "full" => full,
        "compact" => compact,
        "indexOnly" => index_only
      }
    }
    pack["_required"] = %w[intent git-diff ast-diff targeted-tests].include?(id)
    pack
  end

  def activate_pack_variant(pack, variant)
    pack["mode"] = variant
    pack["payload"] = pack.fetch("_variants").fetch(variant)
    pack["estimatedTokens"] = estimate_tokens(pack["payload"])
    pack
  end

  def strip_internal_fields(packs)
    packs.map do |pack|
      pack.reject { |key, _value| key.start_with?("_") }
    end
  end

  def required_pack?(pack)
    pack["_required"]
  end

  def total_tokens(packs)
    packs.sum { |pack| pack["estimatedTokens"].to_i }
  end

  def compact_payload(payload, depth = 0)
    return payload if depth >= 2

    case payload
    when Hash
      payload.each_with_object({}) do |(key, value), result|
        result[key] = compact_payload(value, depth + 1)
      end
    when Array
      payload.first(8).map { |entry| compact_payload(entry, depth + 1) }
    when String
      compact_text(payload)
    else
      payload
    end
  end

  def index_payload(payload)
    case payload
    when Hash
      {
        "keys" => payload.keys.first(20),
        "arraySizes" => payload.each_with_object({}) do |(key, value), result|
          result[key] = value.size if value.is_a?(Array)
        end
      }
    when Array
      {"count" => payload.size, "sample" => payload.first(5).map { |entry| index_payload(entry) }}
    when String
      payload.length > 140 ? "#{payload[0, 137]}..." : payload
    else
      payload
    end
  end

  def compact_text(text)
    lines = text.to_s.lines.map(&:rstrip)
    lines.reject!(&:empty?)
    lines.reject! { |line| line.strip.start_with?("//") || line.strip.start_with?("#") }
    trimmed = lines.first(25)
    joined = trimmed.join("\n")
    joined.length > 1200 ? "#{joined[0, 1197]}..." : joined
  end

  def estimate_tokens(payload)
    [(JSON.generate(payload).length / 4.0).ceil, 1].max
  end

  def rescore_packs(packs, request)
    context = relevance_context(request, packs)
    packs.map { |pack| apply_relevance_score(pack, context) }
  end

  def relevance_context(request, packs)
    files = request["files"]
    categories = files.map { |path| tooling_call(:category_for, path) }.uniq.sort
    domains = files.map { |path| tooling_call(:domain_for, path) }.uniq.sort
    ast_pack = packs.find { |pack| pack["id"] == "ast-diff" }
    symbols = Array(ast_pack&.dig("payload", "symbols"))
    {
      files: files,
      file_count: files.size,
      categories: categories,
      domains: domains,
      symbols: symbols.map { |row| row["symbol"].to_s }.reject(&:empty?),
      has_backend: categories.any? { |category| category.start_with?("backend_") } || files.any? { |path| path.include?("/src/main/java/") },
      has_frontend: categories.any? { |category| category.start_with?("frontend_") } || files.any? { |path| path.include?("/frontend/") },
      has_docs: categories.include?("docs") || files.any? { |path| path.start_with?(".agents/") },
      has_scripts: files.any? { |path| path.start_with?("scripts/") || path == "Makefile" },
      has_dto: files.any? { |path| path.include?("/dto/") },
      has_controller: files.any? { |path| path.include?("/controller/") },
      has_plan: files.any? { |path| path.start_with?(".agents/") && path.end_with?("-plan.md") },
      direct_tests: tooling_call(:related_tests, files).first(12),
      manifest_resolution: tooling_call(:resolve_manifest_path_for, files)
    }
  end

  def apply_relevance_score(pack, context)
    base = pack["relevance"].to_f
    score = base
    notes = []
    case pack["id"]
    when "git-diff", "ast-diff", "intent"
      score = 1.0
      notes << "Always include primary diff intent context."
    when "targeted-tests", "symbol-test"
      score += 0.08 if context[:has_backend] || context[:has_frontend]
      score += 0.06 if context[:direct_tests].any?
      notes << "Boosted by direct validation relevance."
    when "endpoint-contract", "dto-endpoint-frontend"
      score += 0.16 if context[:has_controller] || context[:has_dto]
      score += 0.1 if context[:has_frontend]
      notes << "Boosted by controller/DTO/frontend contract evidence."
    when "frontend-usage"
      score += 0.18 if context[:has_frontend]
      score -= 0.1 unless context[:has_frontend]
      notes << "Aligned with touched frontend surfaces."
    when "backend-dependency"
      score += 0.16 if context[:has_backend]
      score += 0.05 if context[:symbols].any?
      notes << "Boosted by backend dependency and symbol evidence."
    when "validation", "audit-delta"
      score += 0.08 if context[:has_docs] || context[:has_scripts]
      score += 0.05 if context[:manifest_resolution][:decision] == "resolved"
      notes << "Boosted by closeout and drift evidence."
    when "repo-map", "symbol-index", "codebase-capsule"
      score -= 0.12 if context[:file_count] <= 6
      score += 0.08 if context[:domains].size > 1
      notes << "Lowered when the changeset is already narrowly isolated."
    when "session", "plan-code-map"
      score += 0.16 if context[:has_plan] || context[:has_docs]
      notes << "Boosted by active plan or closeout surfaces."
    when "domain-pack", "workflow-slice", "hotspots"
      score += 0.08 if context[:domains].size == 1
      score += 0.05 if context[:symbols].any?
      notes << "Boosted by focused domain/workflow evidence."
    end
    pack["relevance"] = [[score, 0.05].max, 1.0].min.round(2)
    pack["scoring"] = {
      "categories" => context[:categories],
      "domains" => context[:domains],
      "notes" => notes.uniq
    }
    pack
  end

  def provider_sort_key(pack)
    priority = PROVIDER_ORDER.index(pack["id"]) || 999
    [priority, -pack["relevance"], pack["estimatedTokens"]]
  end

  def ensure_tool_output(target, args, output_path, request)
    absolute = absolute_path(output_path)
    return if request["reuseExistingAuditOutputs"] &&
      !ALWAYS_REFRESH_TARGETS.include?(target) &&
      File.exist?(absolute) &&
      !request["refresh"]

    LocalToolingExtendedTools.run(target, args)
  end

  def provider_enabled?(request, name)
    request.fetch("providerDefaults", {}).fetch(name, true)
  end

  def wrapper_args_for_files(request)
    files = request["files"]
    return [] if files.empty?

    ["files=#{files.join(',')}"]
  end

  def infer_scope_for_payload(id, payload, request)
    scope = {"files" => request["files"]}
    case id
    when "endpoint-contract"
      scope["endpoints"] = Array(payload["packs"]).first(8)
    when "frontend-usage"
      scope["routes"] = Array(payload["routes"]).first(8) if payload.is_a?(Hash)
    when "backend-dependency"
      scope["symbols"] = Array(payload["nodes"]).first(8).map { |row| row["name"] || row[:name] } if payload.is_a?(Hash)
    when "symbol-index"
      scope["symbols"] = Array(payload["symbols"]).first(12).map { |row| row["symbol"] || row[:symbol] }
    end
    compact_scope(scope)
  end

  def compact_scope(scope)
    scope.each_with_object({}) do |(key, value), result|
      next if value.nil?
      next if value.respond_to?(:empty?) && value.empty?

      result[key] = value
    end
  end

  def diff_notes(files, all_changed)
    notes = []
    notes << "No changed files detected; packs rely on reusable local inventory and current repo structure." if all_changed.empty?
    notes << "Selected files were narrowed from #{all_changed.size} changed paths to #{files.size} context paths." if all_changed.size > files.size && !files.empty?
    notes
  end

  def resolve_files(options, config, snapshot)
    explicit_files = present_option(options["files"])
    if explicit_files
      requested = explicit_files.split(",").map(&:strip).reject(&:empty?)
      filter_requested_files(requested, options)
    else
      changed = Array(snapshot[:included_files] || snapshot["included_files"])
      filtered = filter_requested_files(changed, options)
      return filtered unless filtered.empty?

      inferred = tooling_call(:infer_topic_files, present_option(options["topic"]).to_s)
      return filter_requested_files(inferred, options) unless inferred.empty?

      []
    end
  end

  def filter_requested_files(files, options)
    LocalToolingCommon.filter_file_list(
      files,
      include_generated: truthy?(options["include_generated"]),
      include_agents: truthy?(options["include_agents"])
    ).fetch(:included)
  end

  def normalize_topic(topic, files)
    return tooling_call(:slug, topic) if present_option(topic)
    return "current-change" if files.empty?

    tooling_call(:slug, File.basename(files.first, File.extname(files.first)))
  end

  def resolve_budget_tokens(value, default_value)
    return default_value if value.nil? || value.to_s.empty?
    return 3000 if value == "small"
    return 6000 if value == "medium"
    return 12000 if value == "large"

    value.to_i.positive? ? value.to_i : default_value
  end

  def parse_options(argv)
    options = {}
    cursor = 0
    while cursor < argv.length
      arg = argv[cursor]
      if arg.start_with?("--")
        key, value = arg.sub(/\A--/, "").split("=", 2)
        if value.nil?
          next_arg = argv[cursor + 1]
          if next_arg && !next_arg.start_with?("--") && !next_arg.include?("=")
            value = next_arg
            cursor += 1
          else
            value = "true"
          end
        end
        options[key.tr("-", "_")] = value
      elsif arg.include?("=")
        key, value = arg.split("=", 2)
        options[key.tr("-", "_")] = value
      end
      cursor += 1
    end
    options
  end

  def truthy?(value)
    LocalToolingCommon.truthy?(value)
  end

  def read_json_relative(relative_path)
    read_json_file(absolute_path(relative_path))
  end

  def read_json_file(path)
    JSON.parse(File.read(path))
  end

  def absolute_path(path)
    return path if path.start_with?("/")

    File.join(REPO_ROOT, path)
  end

  def relative_path(path)
    path.delete_prefix("#{REPO_ROOT}/")
  end

  def fingerprint_for(provider, payload, source_files)
    Digest::SHA256.hexdigest(
      JSON.generate(
        {
          provider: provider,
          source_files: source_files.sort,
          payload: payload
        }
      )
    )
  end

  def deep_copy(value)
    JSON.parse(JSON.generate(value))
  end

  def deep_merge(left, right)
    left.merge(right) do |_key, old_value, new_value|
      if old_value.is_a?(Hash) && new_value.is_a?(Hash)
        deep_merge(old_value, new_value)
      else
        new_value
      end
    end
  end

  def git_status_rows
    tooling_call(:git_status_entries).map do |entry|
      {"status" => entry[:status], "path" => entry[:path]}
    end
  rescue StandardError
    []
  end

  def diff_stat_for(path, snapshot = nil)
    row = snapshot && snapshot.dig("diff_stats", path)
    return row if row

    LocalToolingCommon.diff_stat_for(path, request_status_map(snapshot))
  end

  def changed_line_numbers(path, snapshot = nil)
    cached = snapshot && snapshot.dig("changed_line_map", path)
    return cached if cached

    LocalToolingCommon.changed_line_numbers(path, request_status_map(snapshot))
  end

  def request_status_map(snapshot = nil)
    raw = snapshot && (snapshot["status_by_path"] || snapshot[:status_by_path])
    return raw.each_with_object({}) { |(key, value), rows| rows[key.to_s] = value } if raw

    git_status_rows.each_with_object({}) do |row, memo|
      memo[row["path"]] = row["status"]
    end
  end

  def parser_backed_symbols(files, changed_lines)
    parser_files = files.select { |path| %w[.ts .tsx .js .jsx .mjs .vue].include?(File.extname(path)) }
    return [] if parser_files.empty?

    input = {
      repoRoot: REPO_ROOT,
      files: parser_files.map { |path| {path: path, changedLines: changed_lines.fetch(path, [])} }
    }
    stdout, stderr, status = Open3.capture3("node", "scripts/audits/codex_ast_context.mjs", stdin_data: JSON.generate(input), chdir: REPO_ROOT)
    raise stderr unless status.success?

    JSON.parse(stdout).fetch("files")
  rescue StandardError
    []
  end

  def ast_coverage_note(parser_backed, files)
    parser_backed_files = parser_backed.map { |entry| entry["file"] }.uniq
    fallback_files = files - parser_backed_files
    notes = []
    notes << "Parser-backed AST extraction is active for TypeScript/JavaScript/Vue and Java files when local parsers are available." if parser_backed_files.any?
    notes << "Some files still use heuristic changed-line symbol isolation." if fallback_files.any?
    notes.join(" ")
  end

  def ast_confidence(symbols, parser_backed)
    return 0.45 if symbols.empty?
    return 0.93 if parser_backed.any? { |entry| Array(entry["spans"]).any? }

    0.78
  end

  def extract_changed_symbols(path, changed_lines)
    content = safe_read(path)
    return [] if content.empty?

    lines = content.lines
    case File.extname(path)
    when ".java"
      select_changed_spans(java_symbol_spans(lines, path), changed_lines)
    when ".ts", ".js", ".mjs"
      select_changed_spans(script_symbol_spans(lines, path), changed_lines)
    when ".vue"
      select_changed_vue_symbols(path, content, changed_lines)
    else
      []
    end
  end

  def select_changed_spans(spans, changed_lines)
    changed_lines = changed_lines.to_set
    spans.select do |span|
      (span["lineStart"]..span["lineEnd"]).any? { |line_number| changed_lines.include?(line_number) }
    end.first(12)
  end

  def java_symbol_spans(lines, path)
    spans = []
    stack = []
    lines.each_with_index do |line, index|
      line_number = index + 1
      stripped = line.strip
      if stripped.match?(/\b(class|interface|enum|record)\s+([A-Z][A-Za-z0-9_]*)/)
        name = stripped[/\b(class|interface|enum|record)\s+([A-Z][A-Za-z0-9_]*)/, 2]
        stack << {
          type: "java-type",
          symbol: name,
          file: path,
          lineStart: line_number,
          braceDepth: net_brace_change(line)
        }
      elsif stripped.match?(/(?:public|protected|private)\s+(?:static\s+)?[A-Za-z0-9_<>, ?\[\]]+\s+([a-z][A-Za-z0-9_]*)\s*\(/)
        name = stripped[/([a-z][A-Za-z0-9_]*)\s*\(/, 1]
        stack << {
          type: "java-member",
          symbol: name,
          file: path,
          lineStart: line_number,
          braceDepth: net_brace_change(line)
        }
      else
        stack.each { |entry| entry[:braceDepth] += net_brace_change(line) }
      end

      closed = stack.select { |entry| entry[:braceDepth] <= 0 }
      closed.each do |entry|
        spans << symbol_payload(entry, line_number, lines)
        stack.delete(entry)
      end
    end
    stack.each { |entry| spans << symbol_payload(entry, lines.size, lines) }
    spans
  end

  def script_symbol_spans(lines, path, offset = 0)
    spans = []
    stack = []
    lines.each_with_index do |line, index|
      absolute_line = offset + index + 1
      stripped = line.strip
      if stripped.match?(/\b(?:export\s+)?(?:async\s+)?function\s+([A-Za-z0-9_]+)/)
        name = stripped[/function\s+([A-Za-z0-9_]+)/, 1]
        stack << {type: "script-function", symbol: name, file: path, lineStart: absolute_line, braceDepth: net_brace_change(line)}
      elsif stripped.match?(/\b(?:export\s+)?(?:const|let|var|class|interface|type)\s+([A-Za-z0-9_]+)/)
        name = stripped[/\b(?:const|let|var|class|interface|type)\s+([A-Za-z0-9_]+)/, 1]
        stack << {type: "script-symbol", symbol: name, file: path, lineStart: absolute_line, braceDepth: net_brace_change(line)}
      else
        stack.each { |entry| entry[:braceDepth] += net_brace_change(line) }
      end

      closed = stack.select { |entry| entry[:braceDepth] <= 0 }
      closed.each do |entry|
        spans << symbol_payload(entry, absolute_line, lines, offset)
        stack.delete(entry)
      end
    end
    stack.each { |entry| spans << symbol_payload(entry, offset + lines.size, lines, offset) }
    spans
  end

  def select_changed_vue_symbols(path, content, changed_lines)
    template_block = content.match(/<template[^>]*>(.*?)<\/template>/m)
    script_block = content.match(/<script[^>]*>(.*?)<\/script>/m)
    symbols = []
    if template_block
      template_start = content[0...template_block.begin(1)].count("\n") + 1
      template_end = template_start + template_block[1].lines.size
      if changed_lines.any? { |line| line >= template_start && line <= template_end }
        symbols << {
          "type" => "vue-template",
          "symbol" => "#{File.basename(path, ".vue")}Template",
          "file" => path,
          "lineStart" => template_start,
          "lineEnd" => template_end,
          "snippet" => compact_text(template_block[1])
        }
      end
    end
    if script_block
      script_start = content[0...script_block.begin(1)].count("\n") + 1
      script_symbols = script_symbol_spans(script_block[1].lines, path, script_start - 1)
      symbols.concat(select_changed_spans(script_symbols, changed_lines))
    end
    symbols
  end

  def symbol_payload(entry, line_end, lines, offset = 0)
    snippet_start = [entry[:lineStart] - offset - 1, 0].max
    snippet_end = [line_end - offset - 1, lines.size - 1].min
    {
      "type" => entry[:type],
      "symbol" => entry[:symbol],
      "file" => entry[:file],
      "lineStart" => entry[:lineStart],
      "lineEnd" => line_end,
      "snippet" => compact_text(lines[snippet_start..snippet_end].join)
    }
  end

  def net_brace_change(line)
    line.count("{") - line.count("}")
  end

  def safe_read(relative_path)
    File.binread(File.join(REPO_ROOT, relative_path))
      .encode("UTF-8", invalid: :replace, undef: :replace, replace: "")
  rescue Errno::ENOENT, Errno::EISDIR
    ""
  end

  def first_changed_symbol_name(request)
    ast_payload = build_ast_diff_pack(request)
    symbols = ast_payload.dig("payload", "symbols") || []
    preferred = symbols.find do |row|
      file = row["file"].to_s
      symbol = row["symbol"].to_s
      runtime_surface = file.include?("apps/themuffinman/src/main/java/") || file.include?("apps/themuffinman/frontend/src/")
      runtime_surface && symbol.match?(/\A[A-Z][A-Za-z0-9_]+\z/)
    end
    preferred&.fetch("symbol", nil)
  end

  def first_changed_dto(files)
    dto_file = files.find { |path| path.include?("/dto/") && path.end_with?(".java") }
    dto_file ? File.basename(dto_file, ".java") : nil
  end

  def inferred_workflow_id(files)
    return "quest-application" if files.any? { |path| path.downcase.include?("questapplication") || path.downcase.include?("application") }

    nil
  end

  def primary_domain(files)
    domains = files.map { |path| tooling_call(:domain_for, path) }.reject { |domain| domain == "shared" }
    domains.group_by(&:itself).max_by { |_domain, rows| rows.size }&.first
  end

  def endpoint_relevance(request)
    files = request["files"]
    return 0.85 if files.any? { |path| path.include?("/controller/") || path.include?("/dto/") || path.include?("/frontend/src/modules/") }

    0.35
  end

  def frontend_relevance(request)
    request["files"].any? { |path| path.include?("/frontend/") } ? 0.76 : 0.18
  end

  def backend_relevance(request)
    request["files"].any? { |path| path.include?("/src/main/java/") } ? 0.78 : 0.18
  end

  def symbol_index_relevance(request)
    request["files"].empty? ? 0.35 : 0.68
  end

  def generic_payload_facts(payload)
    case payload
    when Hash
      payload.map do |key, value|
        if value.is_a?(Array)
          "`#{key}` count=#{value.size}"
        elsif value.is_a?(Hash)
          "`#{key}` keys=#{value.keys.size}"
        else
          "`#{key}`=#{value}"
        end
      end
    when Array
      ["array count=#{payload.size}"]
    else
      [payload.to_s]
    end
  end

  def now
    Time.now.utc.iso8601
  end

  def tooling_call(method_name, *args)
    LocalToolingExtendedTools.send(method_name, *args)
  end

  def present_option(value)
    stripped = value.to_s.strip
    stripped.empty? ? nil : stripped
  end

  def stringify_rows(rows)
    Array(rows).map { |row| stringify_row(row) }
  end

  def stringify_row(row)
    case row
    when Hash
      row.each_with_object({}) do |(key, value), result|
        result[key.to_s] = case value
                           when Hash then stringify_row(value)
                           when Array then stringify_rows(value)
                           else value
                           end
      end
    else
      row
    end
  end
end
