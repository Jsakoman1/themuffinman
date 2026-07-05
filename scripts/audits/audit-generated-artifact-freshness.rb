#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../local_tooling_common"

module GeneratedArtifactFreshnessAudit
  extend self

  ARTIFACTS = [
    {
      artifact: "docs/generated/agent-endpoint-inventory.json",
      label: "agent_endpoint_inventory",
      regenerate: "ruby scripts/generate-agent-endpoint-inventory.rb",
      source_patterns: [
        "apps/themuffinman/src/main/java/com/themuffinman/app/*/controller/*.java",
        "docs/agent-operating-model/sections/api.yaml",
        "docs/agent-operating-model/sections/intents.yaml",
        "scripts/generate-agent-endpoint-inventory.rb"
      ]
    },
    {
      artifact: "docs/generated/automation-read-model-inventory.json",
      label: "automation_read_model_inventory",
      regenerate: "ruby scripts/generate-automation-read-model-inventory.rb",
      source_patterns: [
        "apps/themuffinman/src/main/java/com/themuffinman/app/**/*DTO.java",
        "docs/agent-operating-model/sections/automation_read_models.yaml",
        "scripts/generate-automation-read-model-inventory.rb"
      ]
    },
    {
      artifact: "docs/generated/source-of-truth-audit.json",
      label: "source_of_truth_audit",
      regenerate: "ruby scripts/generate-source-of-truth-audit.rb",
      source_patterns: [
        "apps/themuffinman/src/main/java/com/themuffinman/app/**/*.java",
        "apps/themuffinman/src/test/java/com/themuffinman/app/**/*.java",
        "docs/source-of-truth-inventory.md",
        "docs/agent-operating-model/sections/source_of_truth.yaml",
        "scripts/generate-source-of-truth-audit.rb"
      ]
    },
    {
      artifact: "docs/generated/backend-audit-inventory.json",
      label: "backend_audit_inventory",
      regenerate: "ruby scripts/generate-backend-audit-inventory.rb",
      source_patterns: [
        "apps/themuffinman/src/main/java/com/themuffinman/app/**/*.java",
        "docs/agent-operating-model/sections/backend_audit_coverage.yaml",
        "scripts/generate-backend-audit-inventory.rb"
      ]
    },
    {
      artifact: "apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts",
      label: "frontend_generated_contract",
      regenerate: "node apps/themuffinman/frontend/scripts/generate-workmarket-contracts.mjs",
      source_patterns: [
        "apps/themuffinman/src/main/java/com/themuffinman/app/**/*DTO.java",
        "apps/themuffinman/src/main/java/com/themuffinman/app/**/*.java",
        "apps/themuffinman/frontend/scripts/generate-workmarket-contracts.mjs"
      ]
    },
    {
      artifact: "docs/generated/local-tooling/codex-context/latest.execution.json",
      label: "codex_context_execution_manifest",
      regenerate: "make codex-context topic=<topic> intent='<intent>'",
      source_patterns: [
        "scripts/audits/codex-context.rb",
        "scripts/audits/codex_local_context_gateway.rb",
        "scripts/audits/local_tooling_extended_tools.rb",
        "scripts/local_tooling_common.rb",
        "scripts/audits/CodexJavaAstContext.java",
        "docs/codex-fast-path.md",
        "docs/feature-delivery-workflow.md",
        "docs/documentation-sync-policy.md",
        "docs/change-completion-checklist.md",
        "docs/agent-operating-model.md",
        "docs/agent-operating-model.yaml",
        "docs/codex-context-execution-manifest.schema.json",
        "docs/codex-local-tooling-todo.md"
      ]
    },
    {
      artifact: "docs/generated/local-tooling/control-start-summary.md",
      label: "control_start_summary",
      regenerate: "make control-start",
      source_patterns: [
        "scripts/audits/generate-control-start.rb",
        "scripts/audits/generate-plan-index.rb",
        "scripts/audits/generate-audit-summary-index.rb",
        "scripts/audits/local_tooling_extended_tools.rb",
        "docs/generated/local-tooling/plan-index.json",
        "docs/generated/local-tooling/audit-summary-index.json",
        "docs/generated/local-tooling/codex-context/latest.review.md"
      ]
    },
    {
      artifact: "docs/generated/local-tooling/plan-index.json",
      label: "plan_index",
      regenerate: "make plan-index",
      source_patterns: [
        ".agents/*.md",
        ".agents/god-plans/*.yaml",
        ".agents/god-plans/*.md",
        "scripts/audits/generate-plan-index.rb",
        "scripts/audits/local_tooling_extended_tools.rb",
        "docs/program-planning-model.md",
        "docs/codex-fast-path.md",
        "docs/tooling/codex-local-audits.md"
      ]
    }
  ].freeze

  def run
    options = parse_options(ARGV)
    scope_files = scope_files_for(options)
    scoped = scope_files.any?
    scoped_artifacts = scoped ? ARTIFACTS.select { |entry| relevant_to_scope?(entry, scope_files) } : ARTIFACTS
    artifact_reports = scoped_artifacts.map { |entry| freshness_for(entry) }
    ignored_reports = scoped ? (ARTIFACTS - scoped_artifacts).map { |entry| freshness_for(entry) } : []
    report = {
      generated_at: Time.now.utc.iso8601,
      scope_files: scope_files,
      scoped: scoped,
      artifact_count: artifact_reports.size,
      stale_count: artifact_reports.count { |entry| entry[:status] == "stale" },
      ignored_stale_count: ignored_reports.count { |entry| entry[:status] == "stale" },
      artifacts: artifact_reports
    }
    json_path, summary_path = output_paths(scoped)
    LocalToolingCommon.write_json(json_path, report)
    LocalToolingCommon.write_text(summary_path, markdown_summary(report))
    puts terminal_summary(report)
  end

  def parse_options(argv)
    argv.each_with_object({}) do |arg, options|
      key, value = arg.split("=", 2)
      options[key] = value if value
    end
  end

  def scope_files_for(options)
    raw = options["files"].to_s.strip
    return [] if raw.empty?

    raw.split(",").map(&:strip).reject(&:empty?).select { |path| File.exist?(File.join(LocalToolingCommon::REPO_ROOT, path)) }.uniq
  end

  def relevant_to_scope?(entry, scope_files)
    source_paths = LocalToolingCommon.repo_glob(*entry[:source_patterns])
    !(source_paths.map { |path| LocalToolingCommon.relative_path(path) } & scope_files).empty?
  end

  def output_paths(scoped)
    if scoped
      [
        "docs/generated/local-tooling/generated-artifact-hygiene.json",
        "docs/generated/local-tooling/generated-artifact-hygiene-summary.md"
      ]
    else
      [
        "docs/generated/local-tooling/generated-artifact-freshness.json",
        "docs/generated/local-tooling/generated-artifact-freshness-summary.md"
      ]
    end
  end

  def freshness_for(entry)
    artifact_path = File.join(LocalToolingCommon::REPO_ROOT, entry[:artifact])
    source_paths = LocalToolingCommon.repo_glob(*entry[:source_patterns])
    newest_source = source_paths.max_by { |path| File.mtime(path) }
    artifact_exists = File.exist?(artifact_path)
    stale = !artifact_exists || (newest_source && File.mtime(newest_source) > File.mtime(artifact_path))
    {
      label: entry[:label],
      artifact: entry[:artifact],
      status: stale ? "stale" : "fresh",
      artifact_exists: artifact_exists,
      artifact_mtime: artifact_exists ? LocalToolingCommon.iso_mtime(artifact_path) : nil,
      newest_source: newest_source ? LocalToolingCommon.relative_path(newest_source) : nil,
      newest_source_mtime: newest_source ? LocalToolingCommon.iso_mtime(newest_source) : nil,
      source_count: source_paths.size,
      regenerate_command: entry[:regenerate]
    }
  end

  def markdown_summary(report)
    lines = []
    lines << "# Generated Artifact #{report[:scoped] ? "Hygiene" : "Freshness"} Audit"
    lines << ""
    lines << "- Decision: `#{report[:stale_count].to_i.zero? ? "fresh" : "stale"}`"
    lines << "- Why: stale artifacts=#{report[:stale_count]}"
    lines << "- Scope files: `#{Array(report[:scope_files]).join(", ")}`" if report[:scoped]
    lines << "- Ignored stale artifacts outside scope: #{report[:ignored_stale_count].to_i}" if report[:scoped]
    lines << "- Next action: `#{report[:artifacts].select { |entry| entry[:status] == "stale" }.first(3).map { |entry| entry[:regenerate_command] }.join("`, `")}`" if report[:stale_count].to_i.positive?
    lines << "- Evidence: artifacts=#{report[:artifact_count]}"
    lines << ""
    report[:artifacts].first(5).each do |entry|
      source = entry[:newest_source] ? entry[:newest_source] : "none"
      lines << "- `#{entry[:label]}` `#{entry[:status]}` source=`#{source}`"
    end
    lines << ""
    lines.join("\n")
  end

  def terminal_summary(report)
    lines = []
    lines << "Generated artifact #{report[:scoped] ? "hygiene" : "freshness"} audit"
    lines << "  artifacts checked: #{report[:artifact_count]}"
    lines << "  stale: #{report[:stale_count]}"
    lines << "  ignored stale outside scope: #{report[:ignored_stale_count]}" if report[:scoped]
    report[:artifacts].first(5).each do |entry|
      lines << "  - #{entry[:label]}: #{entry[:status]} (newest source: #{entry[:newest_source] || 'none'})"
    end
    lines.join("\n")
  end
end

GeneratedArtifactFreshnessAudit.run
