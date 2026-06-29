#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "shellwords"
require "set"
require "time"
require_relative "../local_tooling_common"

module LocalToolingBatchAudits
  extend self

  REPO_ROOT = LocalToolingCommon::REPO_ROOT

  def run(mode)
    report = public_send("build_#{mode}")
    write_report(mode, report)
    puts report.fetch(:terminal_summary)
  end

  def build_frontend_dead_code
    files = rel_glob("apps/themuffinman/frontend/src/**/*.{ts,vue,css}")
    app_files = files.reject { |path| path.include?("/api/") || path.include?("/contracts/") || path.end_with?(".d.ts") }
    entries = app_files.map do |path|
      basename = File.basename(path, File.extname(path))
      import_refs = count_refs(basename, "apps/themuffinman/frontend/src")
      route_refs = count_refs(path, "apps/themuffinman/frontend/src/router")
      status =
        if import_refs <= 1 && !path.include?("/views/") && !path.include?("/pages/")
          "likely_unused"
        elsif import_refs.zero?
          "review_needed"
        else
          "active"
        end
      {file: path, basename: basename, refs: import_refs, route_refs: route_refs, status: status}
    end
    likely_unused = entries.select { |entry| entry[:status] == "likely_unused" }.sort_by { |entry| [entry[:refs], entry[:file]] }
    deps = unused_frontend_dependencies
    report(
      title: "Frontend Dead Code Audit",
      output_dir: "docs/generated/dead-code-audit",
      base_name: "frontend-unused",
      payload: {
        generated_at: now,
        files_scanned: entries.size,
        likely_unused_files: likely_unused,
        unused_dependencies: deps
      },
      terminal: [
        "Frontend dead code audit",
        "  files scanned: #{entries.size}",
        "  likely unused files: #{likely_unused.size}",
        "  unused dependencies: #{deps.size}"
      ].join("\n")
    )
  end

  def build_backend_dead_code
    java_files = rel_glob("apps/themuffinman/src/main/java/**/*.java")
    class_entries = java_files.map do |path|
      class_name = File.basename(path, ".java")
      refs = count_refs(class_name, "apps/themuffinman/src")
      next if framework_heavy_class?(path)
      confidence = refs <= 1 ? "high_confidence" : (refs == 2 ? "review_needed" : "active")
      {file: path, class_name: class_name, refs: refs, confidence: confidence}
    end.compact
    private_methods = java_files.flat_map do |path|
      content = read(path)
      content.scan(/private\s+[A-Za-z0-9_<>, ?]+\s+([a-zA-Z0-9_]+)\s*\(/).flatten.map do |method_name|
        refs = content.scan(/\b#{Regexp.escape(method_name)}\b/).size
        next unless refs == 1
        {file: path, method: method_name, confidence: "review_needed"}
      end.compact
    end
    high = class_entries.select { |entry| entry[:confidence] == "high_confidence" }
    review = class_entries.select { |entry| entry[:confidence] == "review_needed" } + private_methods
    report(
      title: "Backend Dead Code Audit",
      output_dir: "docs/generated/dead-code-audit",
      base_name: "backend-unused",
      payload: {
        generated_at: now,
        java_files_scanned: java_files.size,
        high_confidence: high,
        review_needed: review
      },
      terminal: [
        "Backend dead code audit",
        "  java files scanned: #{java_files.size}",
        "  high confidence: #{high.size}",
        "  review needed: #{review.size}"
      ].join("\n")
    )
  end

  def build_architecture_drift
    thresholds = {
      service_lines: 420,
      controller_lines: 220,
      vue_view_lines: 360,
      doc_section_lines: 90,
      public_methods: 12,
      responsibility_markers: 3
    }

    backend_files = rel_glob(
      "apps/themuffinman/src/main/java/com/themuffinman/app/**/*Service.java",
      "apps/themuffinman/src/main/java/com/themuffinman/app/**/*Controller.java"
    )
    frontend_views = rel_glob(
      "apps/themuffinman/frontend/src/modules/**/*View.vue",
      "apps/themuffinman/frontend/src/modules/**/*Page.vue",
      "apps/themuffinman/frontend/src/views/**/*View.vue"
    )
    doc_files = %w[
      docs/business-logic.md
      docs/domain-technical.md
      docs/agent-operating-model.md
      docs/documentation-sync-policy.md
      docs/change-completion-checklist.md
    ].select { |path| File.exist?(File.join(REPO_ROOT, path)) }

    backend_entries = backend_files.map do |path|
      content = read(path)
      lines = content.lines.size
      public_methods = content.scan(/^\s+public\s+[A-Za-z0-9_<>, ?]+\s+[a-zA-Z0-9_]+\s*\(/).size
      markers = responsibility_markers(content)
      kind = path.end_with?("Controller.java") ? "controller" : "service"
      threshold = kind == "controller" ? thresholds[:controller_lines] : thresholds[:service_lines]
      flags = []
      flags << "oversized_#{kind}" if lines > threshold
      flags << "many_public_methods" if public_methods > thresholds[:public_methods]
      flags << "mixed_responsibilities" if markers.size >= thresholds[:responsibility_markers]
      next if flags.empty?

      {
        file: path,
        kind: kind,
        lines: lines,
        public_methods: public_methods,
        responsibility_markers: markers,
        flags: flags
      }
    end.compact

    frontend_entries = frontend_views.map do |path|
      content = read(path)
      lines = content.lines.size
      product_logic_hits = content.scan(/\b(status|permission|allowedAction|can[A-Z]|isOwner|isAdmin|transition|workflow|mutation|approve|decline|delete)\b/).size
      flags = []
      flags << "oversized_vue_view" if lines > thresholds[:vue_view_lines]
      flags << "product_logic_in_view" if product_logic_hits >= 18
      next if flags.empty?

      {file: path, kind: "vue_view", lines: lines, product_logic_hits: product_logic_hits, flags: flags}
    end.compact

    doc_entries = doc_files.flat_map do |path|
      parse_doc_sections(path).map do |section|
        line_count = section[:content].lines.size
        next unless line_count > thresholds[:doc_section_lines]

        {
          file: path,
          section: section[:title],
          level: section[:level],
          lines: line_count,
          flags: ["oversized_doc_section"]
        }
      end.compact
    end

    all_entries = backend_entries + frontend_entries + doc_entries
    report(
      title: "Architecture Drift Audit",
      output_dir: "docs/generated/local-tooling",
      base_name: "architecture-drift",
      payload: {
        generated_at: now,
        mode: "report_first",
        thresholds: thresholds,
        backend_entries: backend_entries.sort_by { |entry| [-entry[:lines], entry[:file]] },
        frontend_entries: frontend_entries.sort_by { |entry| [-entry[:lines], entry[:file]] },
        doc_entries: doc_entries.sort_by { |entry| [-entry[:lines], entry[:file], entry[:section]] },
        total_findings: all_entries.size
      },
      terminal: [
        "Architecture drift audit",
        "  mode: report_first",
        "  backend findings: #{backend_entries.size}",
        "  frontend findings: #{frontend_entries.size}",
        "  doc section findings: #{doc_entries.size}",
        "  total findings: #{all_entries.size}"
      ].join("\n")
    )
  end

  def build_aggregate_dead_code
    frontend = read_json("docs/generated/dead-code-audit/frontend-unused.json")
    backend = read_json("docs/generated/dead-code-audit/backend-unused.json")
    shortlist = []
    shortlist.concat(Array(frontend["likely_unused_files"]).first(15).map { |entry| {kind: "frontend", file: entry["file"]} })
    shortlist.concat(Array(backend["high_confidence"]).first(15).map { |entry| {kind: "backend", file: entry["file"]} })
    report(
      title: "Aggregate Dead Code Audit",
      output_dir: "docs/generated/dead-code-audit",
      base_name: "dead-code-summary",
      payload: {
        generated_at: now,
        frontend_likely_unused_count: Array(frontend["likely_unused_files"]).size,
        backend_high_confidence_count: Array(backend["high_confidence"]).size,
        shortlist: shortlist
      },
      terminal: [
        "Aggregate dead code audit",
        "  frontend likely unused: #{Array(frontend['likely_unused_files']).size}",
        "  backend high confidence: #{Array(backend['high_confidence']).size}",
        "  shortlist: #{shortlist.size}"
      ].join("\n")
    )
  end

  def build_state_transition_coverage
    service_files = rel_glob("apps/themuffinman/src/main/java/com/themuffinman/app/**/*Service.java")
    transitions = service_files.flat_map do |path|
      content = read(path)
      content.scan(/setStatus\(([^)]+)\)/).flatten.map do |target|
        action = infer_action_from_target(target)
        tests = find_test_refs(action)
        {service_file: path, action: action, target: target.strip, scenario_test: tests[:scenario], contract_test: tests[:contract], unit_refs: tests[:other]}
      end
    end.uniq
    missing = transitions.select { |entry| !entry[:scenario_test] || !entry[:contract_test] }
    report(
      title: "State Transition Coverage Audit",
      output_dir: "docs/generated/local-tooling",
      base_name: "state-transition-coverage-audit",
      payload: {
        generated_at: now,
        transitions: transitions,
        missing_or_partial: missing
      },
      terminal: [
        "State transition coverage audit",
        "  transitions: #{transitions.size}",
        "  missing or partial: #{missing.size}"
      ].join("\n")
    )
  end

  def build_docs_to_code_drift
    endpoint_inventory = endpoint_ids
    doc_paths = rel_glob("docs/**/*.md", "docs/**/*.yaml")
    doc_endpoint_mentions = doc_paths.sum { |path| read(path).scan(%r{/(auth|admin|app_users|dashboard|quests|applications|users|circles)[A-Za-z0-9/_:\-]*}).size }
    symbol_counts = Hash.new(0)
    read_all("apps/themuffinman/src/main/java").scan(/\b[A-Z][A-Z_]{2,}\b/).each { |symbol| symbol_counts[symbol] += 1 }
    status_symbols = symbol_counts.select { |key, _| key.include?("_") || %w[OPEN ASSIGNED PENDING APPROVED DECLINED BLOCKED].include?(key) }
    undocumented_endpoints = endpoint_inventory.reject { |endpoint| docs_contain?(endpoint) }
    report(
      title: "Docs To Code Drift Audit",
      output_dir: "docs/generated/local-tooling",
      base_name: "docs-to-code-drift-audit",
      payload: {
        generated_at: now,
        endpoint_count: endpoint_inventory.size,
        doc_endpoint_mentions: doc_endpoint_mentions,
        undocumented_endpoints: undocumented_endpoints,
        code_status_symbols: status_symbols.keys.sort
      },
      terminal: [
        "Docs to code drift audit",
        "  endpoints: #{endpoint_inventory.size}",
        "  undocumented endpoints: #{undocumented_endpoints.size}",
        "  code status symbols: #{status_symbols.size}"
      ].join("\n")
    )
  end

  def build_doc_staleness_scoring
    doc_paths = [
      "docs/business-logic.md",
      "docs/domain-technical.md",
      "docs/agent-operating-model.md",
      "docs/documentation-sync-policy.md",
      "docs/change-completion-checklist.md"
    ]
    changed_files = current_changed_files.select do |path|
      path.start_with?("apps/", "services/", "scripts/") ||
        path.start_with?("docs/generated/agent-", "docs/generated/automation-", "docs/generated/source-of-truth")
    end
    newest_endpoint_signal = newest_existing_mtime(["docs/generated/agent-endpoint-inventory.json"])
    dto_files = rel_glob("apps/themuffinman/src/main/java/com/themuffinman/app/**/*DTO.java")
    newest_dto_signal = newest_existing_mtime(dto_files)
    workflow_files = rel_glob("apps/themuffinman/src/main/java/com/themuffinman/app/**/*.{java}")
      .select { |path| read(path).match?(/setStatus|Status|Workflow|transition/i) }
    newest_workflow_signal = newest_existing_mtime(workflow_files)
    changed_tokens = changed_files.flat_map { |path| stale_signal_tokens(path) }.uniq

    sections = doc_paths.flat_map do |doc_path|
      parse_doc_sections(doc_path).map do |section|
        section_tokens = stale_signal_tokens("#{doc_path} #{section[:title]} #{section[:content]}")
        score = 0
        reasons = []
        doc_mtime = File.mtime(File.join(REPO_ROOT, doc_path))
        if newest_endpoint_signal && doc_mtime < newest_endpoint_signal && (section_tokens & %w[endpoint api controller contract]).any?
          score += 30
          reasons << "endpoint inventory is newer than this documentation section"
        end
        if newest_dto_signal && doc_mtime < newest_dto_signal && (section_tokens & %w[dto contract field mapper]).any?
          score += 25
          reasons << "DTO source files are newer than this documentation section"
        end
        if newest_workflow_signal && doc_mtime < newest_workflow_signal && (section_tokens & %w[workflow state transition status permission validation]).any?
          score += 25
          reasons << "workflow or state-transition source files are newer than this documentation section"
        end
        matching_changed_tokens = section_tokens & changed_tokens
        if matching_changed_tokens.any?
          score += [20, matching_changed_tokens.size * 5].min
          reasons << "current changed files share tokens: #{matching_changed_tokens.sort.first(6).join(', ')}"
        end
        score += 10 if section[:content].length < 400 && reasons.any?

        {
          doc_path: doc_path,
          section: section[:title],
          score: score,
          reasons: reasons,
          doc_mtime: doc_mtime.utc.iso8601
        }
      end
    end

    candidates = sections.select { |section| section[:score].positive? }
      .sort_by { |section| [-section[:score], section[:doc_path], section[:section]] }

    report(
      title: "Doc Staleness Scoring Audit",
      output_dir: "docs/generated/local-tooling",
      base_name: "doc-staleness-scoring",
      payload: {
        generated_at: now,
        mode: "report_first",
        changed_files_considered: changed_files.size,
        sections_scored: sections.size,
        candidate_count: candidates.size,
        top_candidates: candidates.first(20),
        signal_mtimes: {
          endpoint_inventory: newest_endpoint_signal&.utc&.iso8601,
          dto_sources: newest_dto_signal&.utc&.iso8601,
          workflow_sources: newest_workflow_signal&.utc&.iso8601
        }
      },
      terminal: [
        "Doc staleness scoring audit",
        "  mode: report_first",
        "  sections scored: #{sections.size}",
        "  candidates: #{candidates.size}",
        "  changed files considered: #{changed_files.size}"
      ].join("\n")
    )
  end

  def build_doc_coverage_gap
    surface_files = rel_glob("apps/themuffinman/src/main/java/com/themuffinman/app/**/*Controller.java",
                             "apps/themuffinman/frontend/src/modules/**/*.{ts,vue}")
    docs = {
      business: read("docs/business-logic.md"),
      technical: read("docs/domain-technical.md"),
      agent: read("docs/agent-operating-model.md")
    }
    gaps = surface_files.map do |path|
      feature = feature_name_for(path)
      next if feature.nil?
      missing = docs.keys.reject { |key| docs[key].downcase.include?(feature.downcase) }
      next if missing.empty?
      {file: path, feature: feature, missing_docs: missing}
    end.compact
    report(
      title: "Doc Coverage Gap Audit",
      output_dir: "docs/generated/local-tooling",
      base_name: "doc-coverage-gap-audit",
      payload: {generated_at: now, surfaces_scanned: surface_files.size, gaps: gaps},
      terminal: [
        "Doc coverage gap audit",
        "  surfaces scanned: #{surface_files.size}",
        "  gaps: #{gaps.size}"
      ].join("\n")
    )
  end

  def build_automation_readiness_gap
    features = inferred_features
    yaml = read("docs/agent-operating-model.yaml")
    report_rows = features.map do |feature|
      {
        feature: feature,
        endpoint_mapping: yaml.include?(feature),
        read_model: read("docs/domain-technical.md").include?(feature),
        intent_safety: read("docs/agent-operating-model.md").include?(feature),
        scenario_verification: find_test_refs(feature)[:scenario]
      }
    end
    gaps = report_rows.select { |row| row.values_at(:endpoint_mapping, :read_model, :intent_safety, :scenario_verification).any? { |v| v == false || v.nil? } }
    report(
      title: "Automation Readiness Gap Audit",
      output_dir: "docs/generated/local-tooling",
      base_name: "automation-readiness-gap-audit",
      payload: {generated_at: now, features: report_rows, gaps: gaps},
      terminal: [
        "Automation readiness gap audit",
        "  features: #{report_rows.size}",
        "  gaps: #{gaps.size}"
      ].join("\n")
    )
  end

  def build_agent_model_feature_coverage
    features = inferred_features
    model_text = read("docs/agent-operating-model.md") + read("docs/agent-operating-model.yaml")
    rows = features.map do |feature|
      {feature: feature, in_agent_model: model_text.downcase.include?(feature.downcase), in_simulation: model_text.downcase.include?("simulate") && model_text.downcase.include?(feature.downcase)}
    end
    missing = rows.select { |row| !row[:in_agent_model] || !row[:in_simulation] }
    report(
      title: "Agent Model Feature Coverage Audit",
      output_dir: "docs/generated/local-tooling",
      base_name: "agent-model-feature-coverage-audit",
      payload: {generated_at: now, features: rows, missing: missing},
      terminal: [
        "Agent model feature coverage audit",
        "  features: #{rows.size}",
        "  missing: #{missing.size}"
      ].join("\n")
    )
  end

  def build_sandbox_generation_coverage
    entities = rel_glob("apps/themuffinman/src/main/java/com/themuffinman/app/**/*DTO.java").map { |path| File.basename(path, ".java").sub(/DTO$/, "") }.uniq.sort
    sandbox_text = read_all(".agents") + read_all("docs")
    rows = entities.map do |entity|
      {entity: entity, sandbox_or_synthetic_mentioned: sandbox_text.downcase.include?(entity.downcase)}
    end
    missing = rows.select { |row| !row[:sandbox_or_synthetic_mentioned] }
    report(
      title: "Sandbox Generation Coverage Audit",
      output_dir: "docs/generated/local-tooling",
      base_name: "sandbox-generation-coverage-audit",
      payload: {generated_at: now, entities: rows, missing: missing},
      terminal: [
        "Sandbox generation coverage audit",
        "  entities: #{rows.size}",
        "  missing: #{missing.size}"
      ].join("\n")
    )
  end

  def build_domain_ownership_inventory
    files = rel_glob("apps/themuffinman/src/main/java/com/themuffinman/app/**/*.java",
                     "apps/themuffinman/frontend/src/modules/**/*.{ts,vue}")
    rows = files.map do |path|
      {file: path, domain: domain_for(path), owner: owner_for(path), layer: layer_for(path)}
    end
    grouped = rows.group_by { |row| [row[:domain], row[:owner], row[:layer]] }.map do |(domain, owner, layer), entries|
      {domain: domain, owner: owner, layer: layer, file_count: entries.size}
    end.sort_by { |row| [row[:domain], row[:owner], row[:layer]] }
    report(
      title: "Domain Ownership Inventory",
      output_dir: "docs/generated/local-tooling",
      base_name: "domain-ownership-inventory",
      payload: {generated_at: now, file_count: rows.size, groups: grouped},
      terminal: [
        "Domain ownership inventory",
        "  files: #{rows.size}",
        "  groups: #{grouped.size}"
      ].join("\n")
    )
  end

  def build_config_sprawl
    java_files = rel_glob("apps/themuffinman/src/main/java/**/*.java")
    at_value = java_files.flat_map do |path|
      read(path).lines.each_with_index.map { |line, idx| line.include?("@Value") ? {file: path, line: idx + 1, snippet: line.strip} : nil }.compact
    end
    config_props = java_files.select { |path| read(path).include?("@ConfigurationProperties") }
    report(
      title: "Config Sprawl Audit",
      output_dir: "docs/generated/local-tooling",
      base_name: "config-sprawl-audit",
      payload: {generated_at: now, at_value_usages: at_value, configuration_properties_classes: config_props},
      terminal: [
        "Config sprawl audit",
        "  @Value usages: #{at_value.size}",
        "  @ConfigurationProperties classes: #{config_props.size}"
      ].join("\n")
    )
  end

  def build_naming_consistency
    targets = {
      "applicant_vs_worker" => %w[applicant worker assignee],
      "quest_detail_vs_record" => ["quest detail", "quest record"],
      "circle_connection_wording" => ["circle connection", "invite", "request"]
    }
    corpus = {
      backend: read_all("apps/themuffinman/src/main/java").downcase,
      frontend: read_all("apps/themuffinman/frontend/src").downcase,
      docs: read_all("docs").downcase
    }
    rows = targets.map do |key, terms|
      counts = corpus.transform_values { |text| terms.to_h { |term| [term, text.scan(term.downcase).size] } }
      {target: key, counts: counts}
    end
    report(
      title: "Naming Consistency Audit",
      output_dir: "docs/generated/local-tooling",
      base_name: "naming-consistency-audit",
      payload: {generated_at: now, targets: rows},
      terminal: [
        "Naming consistency audit",
        "  targets: #{rows.size}"
      ].join("\n")
    )
  end

  def build_dormant_code
    files = rel_glob("apps/themuffinman/**/*.{java,ts,vue,md}")
    patterns = [/placeholder/i, /stub/i, /not yet/i, /todo/i, /unsupported/i, /coming soon/i]
    hits = files.flat_map do |path|
      read(path).lines.each_with_index.map do |line, idx|
        next unless patterns.any? { |pattern| line.match?(pattern) }
        {file: path, line: idx + 1, snippet: sanitize_generated_snippet(line.strip)}
      end.compact
    end
    report(
      title: "Dormant Code Audit",
      output_dir: "docs/generated/local-tooling",
      base_name: "dormant-code-audit",
      payload: {generated_at: now, hits: hits},
      terminal: [
        "Dormant code audit",
        "  hits: #{hits.size}"
      ].join("\n")
    )
  end

  def build_manual_cleanup_candidate_report
    stale = read_json("docs/generated/local-tooling/frontend-stale-surface-audit.json")
    frontend_dead = read_json("docs/generated/dead-code-audit/frontend-unused.json")
    backend_dead = read_json("docs/generated/dead-code-audit/backend-unused.json")
    shortlist = []
    shortlist.concat(Array(stale["likely_unused"]).first(10).map { |entry| {source: "frontend-stale", file: entry["file"]} })
    shortlist.concat(Array(frontend_dead["likely_unused_files"]).first(10).map { |entry| {source: "frontend-dead", file: entry["file"]} })
    shortlist.concat(Array(backend_dead["high_confidence"]).first(10).map { |entry| {source: "backend-dead", file: entry["file"]} })
    report(
      title: "Manual Cleanup Candidate Report",
      output_dir: "docs/generated/local-tooling",
      base_name: "manual-cleanup-candidate-report",
      payload: {generated_at: now, shortlist: shortlist},
      terminal: [
        "Manual cleanup candidate report",
        "  shortlist: #{shortlist.size}"
      ].join("\n")
    )
  end

  def build_file_relation_graph
    edges = []
    rel_glob("apps/themuffinman/src/main/java/**/*.java").each do |path|
      content = read(path)
      file = File.basename(path, ".java")
      content.scan(/\b([A-Z][A-Za-z0-9]+Service|[A-Z][A-Za-z0-9]+Repository|[A-Z][A-Za-z0-9]+Mgr|[A-Z][A-Za-z0-9]+DTO)\b/).flatten.uniq.each do |target|
        edges << {from: file, to: target, file: path}
      end
    end
    rel_glob("apps/themuffinman/frontend/src/modules/**/*.{ts,vue}").each do |path|
      content = read(path)
      file = File.basename(path)
      content.scan(/from\s+"([^"]+)"/).flatten.each { |target| edges << {from: file, to: target, file: path} }
    end
    report(
      title: "File Relation Graph",
      output_dir: "docs/generated/local-tooling",
      base_name: "file-relation-graph",
      payload: {generated_at: now, edge_count: edges.size, edges: edges.first(500)},
      terminal: [
        "File relation graph",
        "  edges: #{edges.size}"
      ].join("\n")
    )
  end

  def build_test_surface_inventory
    services = rel_glob("apps/themuffinman/src/main/java/com/themuffinman/app/**/*Service.java").map { |path| File.basename(path, ".java") }
    tests = rel_glob("apps/themuffinman/src/test/java/**/*.java")
    rows = services.map do |service|
      refs = tests.select { |path| read(path).include?(service) || File.basename(path).include?(service.sub(/Service$/, "")) }
      {service: service, test_files: refs, has_scenario: refs.any? { |path| path.include?("Scenario") }, has_contract: refs.any? { |path| path.include?("Contract") }}
    end
    gaps = rows.select { |row| !row[:has_scenario] || !row[:has_contract] }
    report(
      title: "Test Surface Inventory",
      output_dir: "docs/generated/local-tooling",
      base_name: "test-surface-inventory",
      payload: {generated_at: now, services: rows, gaps: gaps},
      terminal: [
        "Test surface inventory",
        "  services: #{rows.size}",
        "  gaps: #{gaps.size}"
      ].join("\n")
    )
  end

  def build_error_pattern
    files = rel_glob("apps/themuffinman/src/main/java/**/*.java")
    patterns = {
      "response_status_exception" => /ResponseStatusException/,
      "lazy_init_risk" => /LazyInitializationException|fetch = FetchType\.LAZY/,
      "forbidden_message" => /forbidden|unauthorized/i
    }
    hits = patterns.flat_map do |name, pattern|
      files.flat_map do |path|
        read(path).lines.each_with_index.map do |line, idx|
          line.match?(pattern) ? {pattern: name, file: path, line: idx + 1, snippet: line.strip} : nil
        end.compact
      end
    end
    report(
      title: "Error Pattern Audit",
      output_dir: "docs/generated/local-tooling",
      base_name: "error-pattern-audit",
      payload: {generated_at: now, hits: hits},
      terminal: [
        "Error pattern audit",
        "  hits: #{hits.size}"
      ].join("\n")
    )
  end

  def build_rich_text_safety
    backend = rel_glob("apps/themuffinman/src/main/java/**/*.java").flat_map do |path|
      read(path).lines.each_with_index.map do |line, idx|
        next unless line.match?(/description|comment|bio/i)
        {file: path, line: idx + 1, snippet: line.strip}
      end.compact
    end
    frontend = rel_glob("apps/themuffinman/frontend/src/**/*.{ts,vue}").flat_map do |path|
      read(path).lines.each_with_index.map do |line, idx|
        next unless line.match?(/RichTextEditor|ProfileBio|richText/i)
        {file: path, line: idx + 1, snippet: line.strip}
      end.compact
    end
    report(
      title: "Rich Text Safety Audit",
      output_dir: "docs/generated/local-tooling",
      base_name: "rich-text-safety-audit",
      payload: {generated_at: now, backend_fields: backend, frontend_renderers: frontend},
      terminal: [
        "Rich text safety audit",
        "  backend fields: #{backend.size}",
        "  frontend renderers: #{frontend.size}"
      ].join("\n")
    )
  end

  def build_async_mutation_flow
    files = rel_glob("apps/themuffinman/frontend/src/**/*.{ts,vue}")
    rows = files.map do |path|
      content = read(path)
      next unless content.match?(/createFeedbackMutationRunner|runWithFeedback|afterSuccess|load[A-Z]|fetch[A-Z]/)
      {
        file: path,
        uses_feedback_runner: content.include?("createFeedbackMutationRunner"),
        uses_run_with_feedback: content.include?("runWithFeedback"),
        has_after_success: content.include?("afterSuccess"),
        refresh_calls: content.scan(/\b(load[A-Z][A-Za-z0-9_]*|fetch[A-Z][A-Za-z0-9_]*)\b/).flatten.uniq.sort
      }
    end.compact
    report(
      title: "Async Mutation Flow Audit",
      output_dir: "docs/generated/local-tooling",
      base_name: "async-mutation-flow-audit",
      payload: {generated_at: now, files: rows},
      terminal: [
        "Async mutation flow audit",
        "  files: #{rows.size}"
      ].join("\n")
    )
  end

  def build_style_token_usage
    files = rel_glob("apps/themuffinman/frontend/src/**/*.{vue,css}")
    rows = files.map do |path|
      content = read(path)
      token_refs = content.scan(/var\(--[A-Za-z0-9_-]+\)|class="[^"]*ui-[^"]*"/).size
      inline_style = content.scan(/\bstyle=|:style=/).size
      next if token_refs.zero? && inline_style.zero?
      {file: path, token_refs: token_refs, inline_style_refs: inline_style}
    end.compact
    report(
      title: "Style Token Usage Audit",
      output_dir: "docs/generated/local-tooling",
      base_name: "style-token-usage-audit",
      payload: {generated_at: now, files: rows},
      terminal: [
        "Style token usage audit",
        "  files: #{rows.size}"
      ].join("\n")
    )
  end

  def build_feature_intro_check
    docs = %w[
      docs/agent-operating-model.md
      docs/agent-operating-model.yaml
      docs/domain-technical.md
      docs/business-logic.md
    ]
    artifacts = %w[
      docs/generated/agent-endpoint-inventory.json
      docs/generated/backend-audit-inventory.json
      apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts
    ]
    rows = {
      docs: docs.map { |path| {path: path, exists: File.exist?(File.join(REPO_ROOT, path))} },
      artifacts: artifacts.map { |path| {path: path, exists: File.exist?(File.join(REPO_ROOT, path))} }
    }
    report(
      title: "Feature Intro Check",
      output_dir: "docs/generated/local-tooling",
      base_name: "feature-intro-check",
      payload: {generated_at: now, checks: rows},
      terminal: [
        "Feature intro check",
        "  docs checks: #{rows[:docs].size}",
        "  artifact checks: #{rows[:artifacts].size}"
      ].join("\n")
    )
  end

  def build_make_target_index
    makefile = read("Makefile")
    excluded = %w[dev backend-dev backend-test backend-package backend-bootstrap-example]
    targets = makefile.scan(/^([a-z0-9-]+):$/).flatten.reject { |name| excluded.include?(name) }.sort
    report(
      title: "Make Target Index",
      output_dir: "docs/generated/local-tooling",
      base_name: "make-target-index",
      payload: {generated_at: now, targets: targets, audit_targets: targets.select { |name| name.start_with?("audit-") }},
      terminal: [
        "Make target index",
        "  targets: #{targets.size}"
      ].join("\n")
    )
  end

  def build_audit_documentation
    make_targets = read_json("docs/generated/local-tooling/make-target-index.json")
    doc_lines = []
    doc_lines << "# Codex Local Audits"
    doc_lines << ""
    doc_lines << "Use local audits before broad repo discovery when the question can be answered by a generated inventory or summary."
    doc_lines << ""
    doc_lines << "## Recommended Order"
    doc_lines << ""
    doc_lines << "1. `make audit-change-impact-preflight`"
    doc_lines << "2. `make audit-endpoint-callsite-linker` or `make audit-frontend-route-surfaces`"
    doc_lines << "3. choose a focused audit from the target index"
    doc_lines << ""
    doc_lines << "`audit-change-impact-preflight` includes report-only `scope_guardrails` that warn when one changeset mixes multiple product domains, runtime code with tooling or infrastructure, broad generated-report churn, or generated reports that were not predicted by the changed source files."
    doc_lines << ""
    doc_lines << "## Context-First Session Start"
    doc_lines << ""
    doc_lines << "Before broad repository searches, read the compact local context in this order:"
    doc_lines << ""
    doc_lines << "1. `docs/generated/local-tooling/diff-summary.md` for the current changed-file shape."
    doc_lines << "2. `docs/generated/local-tooling/audit-summary-index.md` to choose the smallest relevant generated report."
    doc_lines << "3. `make context-pack topic=<topic>` when the task has a clear feature, domain, or changed-file focus."
    doc_lines << "4. `docs/generated/local-tooling/repo-map-summary.md` or `symbol-index-summary.md` only when the first three sources do not identify the needed files."
    doc_lines << ""
    doc_lines << "## Available Targets"
    doc_lines << ""
    Array(make_targets["targets"] || make_targets["audit_targets"]).each do |target|
      doc_lines << "- `#{target}`"
    end
    LocalToolingCommon.write_text("docs/tooling/codex-local-audits.md", doc_lines.join("\n") + "\n")
    report(
      title: "Audit Documentation",
      output_dir: "docs/generated/local-tooling",
      base_name: "audit-documentation",
      payload: {generated_at: now, documentation_path: "docs/tooling/codex-local-audits.md", target_count: Array(make_targets["targets"] || make_targets["audit_targets"]).size},
      terminal: [
        "Audit documentation",
        "  target count: #{Array(make_targets['targets'] || make_targets['audit_targets']).size}"
      ].join("\n")
    )
  end

  private

  def report(title:, output_dir:, base_name:, payload:, terminal:)
    payload[:terminal_summary] = terminal
    payload[:title] = title
    payload[:output_dir] = output_dir
    payload[:base_name] = base_name
    payload
  end

  def write_report(mode, report)
    json_path, summary_path =
      case report[:output_dir]
      when "docs/generated/dead-code-audit"
        ["#{report[:output_dir]}/#{report[:base_name]}.json", "#{report[:output_dir]}/#{report[:base_name]}-summary.md"]
      else
        ["#{report[:output_dir]}/#{report[:base_name]}.json", "#{report[:output_dir]}/#{report[:base_name]}-summary.md"]
      end
    LocalToolingCommon.write_json(json_path, report.reject { |key, _| %i[terminal_summary title output_dir base_name].include?(key) })
    LocalToolingCommon.write_text(summary_path, summary_markdown(report))
  end

  def summary_markdown(report)
    body = report.reject { |key, _| %i[terminal_summary title output_dir base_name].include?(key) }
    lines = ["# #{report[:title]}", ""]
    body.each do |key, value|
      if value.is_a?(Array)
        lines << "## `#{key}`"
        lines << ""
        value.first(20).each { |entry| lines << "- `#{entry}`".gsub("=>", ": ") }
        lines << ""
      elsif value.is_a?(Hash)
        lines << "- `#{key}`: `#{value.size}` entries"
      else
        lines << "- #{LocalToolingCommon.titleize_slug(key.to_s)}: `#{value}`"
      end
    end
    lines.join("\n") + "\n"
  end

  def rel_glob(*patterns)
    LocalToolingCommon.repo_glob(*patterns).map { |path| LocalToolingCommon.relative_path(path) }
  end

  def read(path)
    LocalToolingCommon.read(File.join(REPO_ROOT, path))
  rescue Errno::ENOENT
    ""
  end

  def read_all(relative_root)
    Dir.glob(File.join(REPO_ROOT, relative_root, "**", "*")).select { |path| File.file?(path) }.sort.map { |path| File.read(path) }.join("\n")
  rescue Errno::ENOENT
    ""
  end

  def read_json(path)
    JSON.parse(read(path))
  rescue JSON::ParserError
    {}
  end

  def now
    Time.now.utc.iso8601
  end

  def count_refs(token, root)
    read_all(root).scan(/\b#{Regexp.escape(token)}\b/).size
  end

  def framework_heavy_class?(path)
    read(path).match?(/@Entity|@RestController|@Controller|@Configuration|@Repository/)
  end

  def unused_frontend_dependencies
    package_path = File.join(REPO_ROOT, "apps/themuffinman/frontend/package.json")
    return [] unless File.exist?(package_path)
    pkg = JSON.parse(File.read(package_path))
    deps = (pkg["dependencies"] || {}).keys.sort
    src = read_all("apps/themuffinman/frontend/src")
    deps.reject { |dep| src.include?(dep.sub(/^@[^\/]+\//, "")) || src.include?(dep) }
  end

  def infer_action_from_target(target)
    return "approveApplication" if target.include?("APPROVED")
    return "declineApplication" if target.include?("DECLINED")
    return "withdrawMyApplication" if target.include?("WITHDRAWN")
    return "startQuest" if target.include?("IN_PROGRESS")
    return "completeQuest" if target.include?("COMPLETED")
    return "reopenQuest" if target.include?("OPEN")

    target
  end

  def find_test_refs(token)
    tests = rel_glob("apps/themuffinman/src/test/java/**/*.java").select { |path| read(path).include?(token.to_s) }
    {
      scenario: tests.any? { |path| path.include?("Scenario") },
      contract: tests.any? { |path| path.include?("Contract") },
      other: tests
    }
  end

  def endpoint_ids
    rel_glob("apps/themuffinman/src/main/java/**/*Controller.java").flat_map do |path|
      content = read(path)
      controller_root = content[/@RequestMapping\("([^"]+)"\)/, 1]
      content.scan(/@(GetMapping|PostMapping|PutMapping|PatchMapping|DeleteMapping)\("([^"]*)"\)/).map do |verb, mapped|
        "#{verb.sub('Mapping', '').upcase} #{[controller_root, mapped].compact.join}"
      end
    end.uniq.sort
  end

  def current_changed_files
    output = `git -C #{Shellwords.escape(REPO_ROOT)} status --short 2>/dev/null`
    output.lines.map { |line| line[3..]&.strip }.compact.reject(&:empty?)
  end

  def newest_existing_mtime(paths)
    times = paths.map { |path| File.join(REPO_ROOT, path) }
      .select { |path| File.exist?(path) }
      .map { |path| File.mtime(path) }
    times.max
  end

  def parse_doc_sections(path)
    content = read(path)
    sections = []
    current = {title: "Document", content: +"", level: 1}
    content.each_line do |line|
      if (match = line.match(/^(##+)\s+(.+?)\s*$/))
        sections << current unless current[:content].strip.empty?
        current = {title: match[2].strip, content: +"", level: match[1].length}
      else
        current[:content] << line
      end
    end
    sections << current unless current[:content].strip.empty?
    sections
  end

  def stale_signal_tokens(text)
    stopwords = %w[
      agents agent docs documentation generated local tooling summary changed updated
      validation evidence feature plan template section status source target report
      business domain technical model policy checklist change changes current file
      files themuffinman app apps java script scripts frontend backend
    ]
    text.downcase.scan(/[a-z][a-z0-9]+/).map do |token|
      token.sub(/(controller|service|repository|mapper|dto|view|page|component|composable|test|contract)\z/, "")
    end.select { |token| token.length >= 4 && !stopwords.include?(token) }.uniq
  end

  def responsibility_markers(content)
    markers = []
    markers << "query" if content.match?(/\b(find|get|list|search|read)\w*\s*\(/)
    markers << "mutation" if content.match?(/\b(create|update|delete|save|approve|decline|withdraw|start|complete)\w*\s*\(/)
    markers << "policy" if content.match?(/\b(can|allow|authorize|permission|forbidden|owner|admin)\b/i)
    markers << "mapping" if content.match?(/\b(toDto|fromDto|Mapper|Mgr|ResponseDTO|RequestDTO)\b/)
    markers << "notification" if content.match?(/\b(notify|publish|event|news)\b/i)
    markers.uniq
  end

  def docs_contain?(text)
    read_all("docs").include?(text)
  end

  def feature_name_for(path)
    parts = path.split("/")
    parts.reverse.find { |part| part.match?(/[A-Z]/) }&.sub(/\.(java|ts|vue)$/, "")&.gsub(/(Controller|Service|View|Page|DTO|Mgr)\z/, "")
  end

  def inferred_features
    rel_glob("apps/themuffinman/src/main/java/com/themuffinman/app/**/*Controller.java").map do |path|
      File.basename(path, ".java").sub(/Controller$/, "")
    end.uniq.sort
  end

  def domain_for(path)
    return "workmarket" if path.include?("/workmarket/")
    return "social" if path.include?("/social/")
    return "identity" if path.include?("/identity/")
    return "chat" if path.include?("/chat/")
    return "location" if path.include?("/location/")

    "shared"
  end

  def owner_for(path)
    return "backend" if path.include?("/src/main/java/")
    return "frontend" if path.include?("/frontend/src/")
    return "docs" if path.start_with?("docs/")

    "tooling"
  end

  def layer_for(path)
    return "controller" if path.include?("/controller/")
    return "service" if path.include?("/service/")
    return "repository" if path.include?("/repository/")
    return "mapper" if path.include?("/mapper/")
    return "dto" if path.include?("/dto/")
    return "model" if path.include?("/model/")
    return "view" if path.match?(%r{/(views|pages)/})
    return "composable" if path.include?("/composables/")
    return "component" if path.include?("/components/")

    "other"
  end

  def sanitize_generated_snippet(snippet)
    snippet.gsub(/\bTODO\b/, "TODO_").gsub(/\bFIXME\b/, "FIXME_")
  end
end
