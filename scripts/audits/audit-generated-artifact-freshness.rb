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
    }
  ].freeze

  def run
    artifact_reports = ARTIFACTS.map { |entry| freshness_for(entry) }
    report = {
      generated_at: Time.now.utc.iso8601,
      artifact_count: artifact_reports.size,
      stale_count: artifact_reports.count { |entry| entry[:status] == "stale" },
      artifacts: artifact_reports
    }
    LocalToolingCommon.write_json("docs/generated/local-tooling/generated-artifact-freshness.json", report)
    LocalToolingCommon.write_text("docs/generated/local-tooling/generated-artifact-freshness-summary.md", markdown_summary(report))
    puts terminal_summary(report)
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
    lines << "# Generated Artifact Freshness Audit"
    lines << ""
    lines << "- Decision: `#{report[:stale_count].to_i.zero? ? "fresh" : "stale"}`"
    lines << "- Why: stale artifacts=#{report[:stale_count]}"
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
    lines << "Generated artifact freshness audit"
    lines << "  artifacts checked: #{report[:artifact_count]}"
    lines << "  stale: #{report[:stale_count]}"
    report[:artifacts].first(5).each do |entry|
      lines << "  - #{entry[:label]}: #{entry[:status]} (newest source: #{entry[:newest_source] || 'none'})"
    end
    lines.join("\n")
  end
end

GeneratedArtifactFreshnessAudit.run
