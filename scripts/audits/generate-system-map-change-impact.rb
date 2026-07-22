#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require_relative "../audit_support"

module SystemMapChangeImpact
  extend self

  REGISTRIES_BY_DOMAIN = {
    "identity" => %w[docs/endpoint-capability-traceability-registry.yaml docs/entity-relation-retention-registry.yaml docs/permission-visibility-matrix.yaml docs/capability-evidence-registry.yaml],
    "workmarket" => %w[docs/endpoint-capability-traceability-registry.yaml docs/entity-relation-retention-registry.yaml docs/permission-visibility-matrix.yaml docs/capability-evidence-registry.yaml],
    "business" => %w[docs/endpoint-capability-traceability-registry.yaml docs/entity-relation-retention-registry.yaml docs/permission-visibility-matrix.yaml docs/capability-evidence-registry.yaml],
    "chat" => %w[docs/endpoint-capability-traceability-registry.yaml docs/entity-relation-retention-registry.yaml docs/permission-visibility-matrix.yaml docs/runtime-observability-registry.yaml],
    "social" => %w[docs/endpoint-capability-traceability-registry.yaml docs/entity-relation-retention-registry.yaml docs/permission-visibility-matrix.yaml],
    "things" => %w[docs/endpoint-capability-traceability-registry.yaml docs/entity-relation-retention-registry.yaml docs/permission-visibility-matrix.yaml],
    "rides" => %w[docs/endpoint-capability-traceability-registry.yaml docs/entity-relation-retention-registry.yaml docs/permission-visibility-matrix.yaml],
    "vision" => %w[docs/endpoint-capability-traceability-registry.yaml docs/entity-relation-retention-registry.yaml docs/permission-visibility-matrix.yaml docs/runtime-observability-registry.yaml],
    "location" => %w[docs/endpoint-capability-traceability-registry.yaml docs/permission-visibility-matrix.yaml docs/runtime-observability-registry.yaml]
  }.freeze

  BASE_REGISTRIES = %w[docs/system-truth-registry.yaml docs/system-drift-control-registry.yaml].freeze
  DELIVERY_PATHS = %r{\A(?:Makefile|apps/themuffinman/pom\.xml|apps/themuffinman/frontend/(?:package(?:-lock)?\.json|scripts/))}

  def run(argv)
    paths = argv.empty? ? AuditSupport.git_changed_files : argv
    paths = paths.uniq.select { |path| File.exist?(File.join(AuditSupport::REPO_ROOT, path)) }
    rows = paths.map { |path| row_for(path) }
    report = {
      generated_at: Time.now.utc.iso8601,
      advisory_only: true,
      changed_file_count: rows.length,
      changed_files: rows,
      summary: {
        registries: rows.flat_map { |row| row[:registries] }.uniq.sort,
        canonical_docs: rows.flat_map { |row| row[:canonical_docs] }.uniq.sort,
        runtime_sources: rows.flat_map { |row| row[:runtime_sources] }.uniq.sort
      },
      closeout_contract: {
        required_review: true,
        freshness: "generated_at must be recorded in the consuming closeout",
        status_authority: false,
        disposition_required: "Each material registry, canonical-doc, and runtime-source recommendation requires reviewed, deferred, or not-applicable disposition."
      },
      boundary: "The report recommends review relationships only. It does not determine capability status, permission, runtime proof, or work-plan completion."
    }
    AuditSupport.write_json("docs/audit-output/system-map-change-impact.json", report)
    AuditSupport.write_text("docs/audit-output/system-map-change-impact-summary.md", markdown(report))
    puts "System-map change impact report generated"
    puts "  changed files: #{rows.length}"
    puts "  registries to review: #{report[:summary][:registries].length}"
  end

  def row_for(path)
    domain = AuditSupport.domain_for_path(path)
    category = AuditSupport.path_category(path)
    registries = BASE_REGISTRIES.dup
    registries.concat(REGISTRIES_BY_DOMAIN.fetch(domain, []))
    registries << "docs/release-operations-registry.yaml" if path.match?(DELIVERY_PATHS)
    registries << "docs/runtime-observability-registry.yaml" if path.include?("/config/") || path.include?("/websocket/") || path.include?("Retention")
    registries.uniq!
    {
      path: path,
      domain: domain,
      category: category,
      registries: registries,
      canonical_docs: canonical_docs(domain),
      runtime_sources: runtime_sources(category)
    }
  end

  def canonical_docs(domain)
    docs = %w[docs/system-map.md]
    docs.concat(%w[docs/business-logic.md docs/domain-technical.md]) unless %w[unknown docs].include?(domain)
    docs
  end

  def runtime_sources(category)
    return [] unless category.start_with?("backend_") || category.start_with?("frontend_")

    %w[docs/runtime-acceptance-matrix.yaml docs/regression-scenario-catalog.yaml]
  end

  def markdown(report)
    lines = ["# System-map Change Impact", "", "- Advisory only: does not set completion, permission, or runtime status.", "- Changed files: `#{report[:changed_file_count]}`", "", "## Review map", ""]
    report[:changed_files].each do |row|
      lines << "- `#{row[:path]}` | domain=`#{row[:domain]}` category=`#{row[:category]}`"
      lines << "  - registries: #{row[:registries].join(', ')}"
      lines << "  - canonical docs: #{row[:canonical_docs].join(', ')}"
      lines << "  - runtime sources: #{row[:runtime_sources].empty? ? 'none' : row[:runtime_sources].join(', ')}"
    end
    lines.join("\n")
  end
end

SystemMapChangeImpact.run(ARGV)
