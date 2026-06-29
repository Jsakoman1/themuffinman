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
    lines << "- Generated at: `#{report[:generated_at]}`"
    lines << "- Artifacts checked: `#{report[:artifact_count]}`"
    lines << "- Stale artifacts: `#{report[:stale_count]}`"
    lines << ""
    report[:artifacts].each do |entry|
      source = entry[:newest_source] ? entry[:newest_source] : "none"
      lines << "- `#{entry[:label]}`: `#{entry[:status]}` (source: `#{source}`)"
    end
    lines << ""
    lines.join("\n")
  end

  def terminal_summary(report)
    lines = []
    lines << "Generated artifact freshness audit"
    lines << "  artifacts checked: #{report[:artifact_count]}"
    lines << "  stale: #{report[:stale_count]}"
    report[:artifacts].each do |entry|
      lines << "  - #{entry[:label]}: #{entry[:status]} (newest source: #{entry[:newest_source] || 'none'})"
    end
    lines.join("\n")
  end
end

GeneratedArtifactFreshnessAudit.run
