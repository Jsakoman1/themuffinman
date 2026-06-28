#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "set"
require_relative "../local_tooling_common"

module FrontendStaleSurfaceAudit
  extend self

  ROOT = "apps/themuffinman/frontend/src"
  ROUTE_INVENTORY_PATH = File.join(LocalToolingCommon::REPO_ROOT, "docs/generated/local-tooling/frontend-route-surface-inventory.json")
  ENDPOINT_LINKER_PATH = File.join(LocalToolingCommon::REPO_ROOT, "docs/generated/local-tooling/endpoint-callsite-linker.json")

  ALWAYS_KEEP_FILES = Set.new([
    "#{ROOT}/main.ts",
    "#{ROOT}/App.vue",
    "#{ROOT}/router.ts",
    "#{ROOT}/style.css",
    "#{ROOT}/auth.ts",
    "#{ROOT}/env.d.ts",
    "#{ROOT}/contracts/index.ts",
    "#{ROOT}/contracts/generated/themuffinmanContract.ts"
  ]).freeze

  def run
    files = LocalToolingCommon.repo_glob("#{ROOT}/**/*.ts", "#{ROOT}/**/*.vue", "#{ROOT}/**/*.css")
    importers_by_file = build_reverse_import_graph(files)
    route_inventory = JSON.parse(File.read(ROUTE_INVENTORY_PATH))
    endpoint_linker = JSON.parse(File.read(ENDPOINT_LINKER_PATH))

    route_surface_files = Set.new(route_inventory.fetch("routes", []).map { |route| route["surface_file"] }.compact)
    route_support_files = Set.new(route_inventory.fetch("routes", []).flat_map { |route| route["primary_composables"] || [] })
    callsite_files = Set.new(
      endpoint_linker.fetch("endpoints", []).flat_map do |endpoint|
        endpoint.fetch("frontend_matches", []).flat_map do |match|
          [match["client_file"]] + match.fetch("callsites", []).map { |callsite| callsite["file"] }
        end
      end.compact
    )

    entries = files.map do |path|
      build_entry(
        LocalToolingCommon.relative_path(path),
        importers_by_file,
        route_surface_files,
        route_support_files,
        callsite_files
      )
    end

    report = {
      generated_at: Time.now.utc.iso8601,
      file_count: entries.size,
      likely_unused_count: entries.count { |entry| entry[:status] == "likely_unused" },
      route_detached_count: entries.count { |entry| entry[:status] == "route_detached" },
      callsite_detached_count: entries.count { |entry| entry[:status] == "callsite_detached" },
      review_needed_count: entries.count { |entry| entry[:status] == "review_needed" },
      files: entries.sort_by { |entry| [status_rank(entry[:status]), entry[:path]] }
    }

    LocalToolingCommon.write_json("docs/generated/local-tooling/frontend-stale-surface-audit.json", report)
    LocalToolingCommon.write_text("docs/generated/local-tooling/frontend-stale-surface-audit-summary.md", markdown_summary(report))
    puts terminal_summary(report)
  end

  def build_reverse_import_graph(files)
    graph = Hash.new { |hash, key| hash[key] = Set.new }
    files.each do |path|
      relative_path = LocalToolingCommon.relative_path(path)
      extract_relative_imports(path).each do |target|
        graph[target] << relative_path
      end
    end
    graph
  end

  def extract_relative_imports(path)
    content = LocalToolingCommon.read(path)
    source_dir = File.dirname(path)
    static_imports = content.scan(/from\s+"([^"]+)"|from\s+'([^']+)'/).flatten.compact
    dynamic_imports = content.scan(/import\("([^"]+)"\)|import\('([^']+)'\)/).flatten.compact
    (static_imports + dynamic_imports).map do |import_path|
      next unless import_path.start_with?(".")

      resolve_import(source_dir, import_path)
    end.compact
  end

  def resolve_import(source_dir, import_path)
    base = File.expand_path(import_path, source_dir)
    candidates = [
      base,
      "#{base}.ts",
      "#{base}.vue",
      "#{base}.css",
      "#{base}/index.ts",
      "#{base}/index.vue"
    ]
    match = candidates.find { |candidate| File.file?(candidate) }
    match && LocalToolingCommon.relative_path(match)
  end

  def build_entry(relative_path, importers_by_file, route_surface_files, route_support_files, callsite_files)
    importers = importers_by_file.fetch(relative_path, Set.new).to_a.sort
    category = LocalToolingCommon.path_category(relative_path)
    route_surface = route_surface_files.include?(relative_path)
    route_support = route_support_files.include?(relative_path)
    endpoint_callsite = callsite_files.include?(relative_path)
    status = classify(relative_path, category, importers, route_surface, route_support, endpoint_callsite)

    {
      path: relative_path,
      category: category,
      importer_count: importers.size,
      importers: importers.first(12),
      route_surface: route_surface,
      route_support: route_support,
      endpoint_callsite: endpoint_callsite,
      status: status
    }
  end

  def classify(relative_path, category, importers, route_surface, route_support, endpoint_callsite)
    return "active_entry" if ALWAYS_KEEP_FILES.include?(relative_path)
    return "active_entry" if route_surface
    return "active_entry" if endpoint_callsite
    return "active_entry" if route_support
    return "active_entry" if importers.any? && !isolated_ui_surface?(relative_path, category)

    return "route_detached" if ui_surface_file?(relative_path) && importers.empty?
    return "callsite_detached" if api_or_composable_file?(relative_path) && importers.empty?
    return "review_needed" if stylesheet_file?(relative_path) && importers.empty?
    return "likely_unused" if importers.empty?

    "review_needed"
  end

  def isolated_ui_surface?(relative_path, category)
    ui_surface_file?(relative_path) || category == "frontend_view"
  end

  def ui_surface_file?(relative_path)
    relative_path.include?("/views/") || relative_path.include?("/pages/")
  end

  def api_or_composable_file?(relative_path)
    relative_path.include?("/api/") || relative_path.include?("/composables/")
  end

  def stylesheet_file?(relative_path)
    relative_path.end_with?(".css")
  end

  def status_rank(status)
    {
      "likely_unused" => 0,
      "route_detached" => 1,
      "callsite_detached" => 2,
      "review_needed" => 3,
      "active_entry" => 4
    }.fetch(status, 5)
  end

  def markdown_summary(report)
    lines = []
    lines << "# Frontend Stale Surface Audit"
    lines << ""
    lines << "- Generated at: `#{report[:generated_at]}`"
    lines << "- Files scanned: `#{report[:file_count]}`"
    lines << "- Likely unused: `#{report[:likely_unused_count]}`"
    lines << "- Route detached: `#{report[:route_detached_count]}`"
    lines << "- Callsite detached: `#{report[:callsite_detached_count]}`"
    lines << "- Review needed: `#{report[:review_needed_count]}`"
    lines << ""
    %w[likely_unused route_detached callsite_detached review_needed].each do |status|
      entries = report[:files].select { |entry| entry[:status] == status }
      next if entries.empty?

      lines << "## `#{status}`"
      lines << ""
      entries.first(40).each do |entry|
        lines << "- `#{entry[:path]}` | category=`#{entry[:category]}` | importers=`#{entry[:importer_count]}` | route_surface=`#{entry[:route_surface]}` | route_support=`#{entry[:route_support]}` | endpoint_callsite=`#{entry[:endpoint_callsite]}`"
      end
      lines << ""
    end
    lines.join("\n")
  end

  def terminal_summary(report)
    lines = []
    lines << "Frontend stale surface audit"
    lines << "  files scanned: #{report[:file_count]}"
    lines << "  likely unused: #{report[:likely_unused_count]}"
    lines << "  route detached: #{report[:route_detached_count]}"
    lines << "  callsite detached: #{report[:callsite_detached_count]}"
    lines << "  review needed: #{report[:review_needed_count]}"
    report[:files].first(10).each do |entry|
      lines << "  - #{entry[:path]} status=#{entry[:status]} importers=#{entry[:importer_count]}"
    end
    lines.join("\n")
  end
end

FrontendStaleSurfaceAudit.run
